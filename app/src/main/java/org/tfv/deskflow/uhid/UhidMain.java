package org.tfv.deskflow.uhid;

import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileDescriptor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * Helper process that owns a virtual HID keyboard on /dev/uhid and injects mouse MotionEvents.
 *
 * <p>Runs as the shell user (launched through Shizuku, or {@code adb shell}):
 *
 * <pre>CLASSPATH=&lt;base.apk&gt; app_process / org.tfv.deskflow.uhid.UhidMain</pre>
 *
 * <p>This is a plain app_process main(): no Application or LoadedApk is created, so OEM framework
 * hooks in LoadedApk.makeApplication() (which break Shizuku UserServices on MediaTek builds) are
 * never reached. Written in Java with no dependencies so it does not need the Kotlin runtime or
 * any app class.
 *
 * <p>Protocol on stdin, repeated: {@code [type:u8][len:u8][payload:len bytes]}
 *
 * <ul>
 *   <li>type 1 = keyboard input report (8 bytes: modifiers, reserved, 6 key usages)
 *   <li>type 2 = attach: create the device if it doesn't exist (no payload)
 *   <li>type 3 = detach: destroy the device but keep running (no payload)
 *   <li>type 4 = pointer event (see {@link #POINTER_PAYLOAD_SIZE}): injected as a SOURCE_MOUSE
 *       MotionEvent through InputManager.injectInputEvent
 * </ul>
 *
 * <p>Pointer events are injected here rather than through a binder call from the app: the app's
 * own process lacks INJECT_EVENTS, and relaying every event through the Shizuku server costs an
 * extra IPC hop. They are handed to a separate thread so stdin is always drained (the app never
 * blocks on a full pipe) and, while injection lags behind (system_server busy), consecutive
 * moves collapse into the newest one instead of replaying stale positions.
 *
 * <p>The device is created at startup unless {@code --detached} is given. Detaching lets Android show the on-screen keyboard again
 * (it hides soft keyboards while a hardware keyboard is attached) without restarting the helper.
 *
 * EOF on stdin destroys the device and exits, so the keyboard disappears with the app.
 *
 * <p>{@code --demo} types a short test string instead of reading stdin.
 */
public final class UhidMain {
  private static final String TAG = "DeskflowUhid";

  // linux/uhid.h
  private static final int UHID_DESTROY = 1;
  private static final int UHID_START = 2;
  private static final int UHID_STOP = 3;
  private static final int UHID_OPEN = 4;
  private static final int UHID_CLOSE = 5;
  private static final int UHID_OUTPUT = 6;
  private static final int UHID_GET_REPORT = 9;
  private static final int UHID_CREATE2 = 11;
  private static final int UHID_INPUT2 = 12;
  private static final int UHID_SET_REPORT = 13;
  private static final int BUS_VIRTUAL = 0x06;

  public static final String DEVICE_NAME = "Deskflow Keyboard";

  /**
   * pid.codes test VID/PID. A non-zero ID plus a unique id keep Android from
   * deriving the same input device descriptor as some other virtual device
   * with zero IDs, in which case it merges both into one InputDevice (seen
   * on Lenovo, where ours became part of the internal "uinput_nav").
   */
  public static final int VENDOR_ID = 0x1209;
  public static final int PRODUCT_ID = 0x0001;
  private static final String PHYS = "deskflow/uhid0";
  private static final String UNIQ = "deskflow-uhid-keyboard";
  public static final int MSG_KEYBOARD_REPORT = 1;
  public static final int MSG_ATTACH = 2;
  public static final int MSG_DETACH = 3;
  public static final int MSG_POINTER = 4;
  public static final int KEYBOARD_REPORT_SIZE = 8;

  /**
   * Big-endian: action, actionButton, buttonState, metaState, displayId (int32); x, y, hScroll,
   * vScroll (float32); downTime, eventTime (int64, SystemClock.uptimeMillis).
   */
  public static final int POINTER_PAYLOAD_SIZE = 5 * 4 + 4 * 4 + 2 * 8;

  /** Written to stderr if pointer injection can't be set up, so the app can report it. */
  public static final String POINTER_UNAVAILABLE = "pointer injection unavailable";

  /** InputManager.INJECT_INPUT_EVENT_MODE_ASYNC */
  private static final int INJECT_MODE_ASYNC = 0;

  /** Boot-protocol keyboard: modifiers, reserved, 5 LEDs (output), 6 key slots. */
  private static final int[] KEYBOARD_REPORT_DESC = {
    0x05, 0x01, // Usage Page (Generic Desktop)
    0x09, 0x06, // Usage (Keyboard)
    0xA1, 0x01, // Collection (Application)
    0x05, 0x07, //   Usage Page (Key Codes)
    0x19, 0xE0, //   Usage Minimum (224)
    0x29, 0xE7, //   Usage Maximum (231)
    0x15, 0x00, //   Logical Minimum (0)
    0x25, 0x01, //   Logical Maximum (1)
    0x75, 0x01, //   Report Size (1)
    0x95, 0x08, //   Report Count (8)
    0x81, 0x02, //   Input (Data, Variable, Absolute): modifiers
    0x75, 0x08, //   Report Size (8)
    0x95, 0x01, //   Report Count (1)
    0x81, 0x01, //   Input (Constant): reserved
    0x05, 0x08, //   Usage Page (LEDs)
    0x19, 0x01, //   Usage Minimum (1)
    0x29, 0x05, //   Usage Maximum (5)
    0x75, 0x01, //   Report Size (1)
    0x95, 0x05, //   Report Count (5)
    0x91, 0x02, //   Output (Data, Variable, Absolute): LEDs
    0x75, 0x03, //   Report Size (3)
    0x95, 0x01, //   Report Count (1)
    0x91, 0x01, //   Output (Constant): padding
    0x05, 0x07, //   Usage Page (Key Codes)
    0x19, 0x00, //   Usage Minimum (0)
    0x29, 0xFF, //   Usage Maximum (255)
    0x15, 0x00, //   Logical Minimum (0)
    0x26, 0xFF, 0x00, //   Logical Maximum (255)
    0x75, 0x08, //   Report Size (8)
    0x95, 0x06, //   Report Count (6)
    0x81, 0x00, //   Input (Data, Array): keys
    0xC0, // End Collection
  };

  private UhidMain() {}

  /** Whether the device currently exists. Only touched by the main (pump) thread. */
  private static boolean created = false;

  private static void createDevice(FileDescriptor fd) throws Exception {
    if (created) return;
    write(fd, create2(DEVICE_NAME));
    created = true;
    log("UHID keyboard created (uid=" + Os.getuid() + ")");
  }

  private static void destroyDevice(FileDescriptor fd) throws Exception {
    if (!created) return;
    created = false;
    write(fd, header(UHID_DESTROY, 0).array());
    log("UHID keyboard destroyed");
  }

  public static void main(String[] args) {
    boolean demo = false;
    boolean detached = false;
    long demoDelayMs = 1500;
    for (String arg : args) {
      if (arg.equals("--demo")) demo = true;
      else if (arg.equals("--detached")) detached = true;
      else if (arg.startsWith("--demo-delay=")) {
        demo = true;
        demoDelayMs = Long.parseLong(arg.substring("--demo-delay=".length()));
      }
    }
    FileDescriptor fd;
    try {
      fd = Os.open("/dev/uhid", OsConstants.O_RDWR | OsConstants.O_CLOEXEC, 0);
    } catch (Throwable err) {
      log("open /dev/uhid failed: " + err);
      System.exit(2);
      return;
    }
    try {
      if (demo || !detached) createDevice(fd);
      startOutputReader(fd);
      if (demo) runDemo(fd, demoDelayMs);
      else pump(fd);
    } catch (Throwable err) {
      log("fatal: " + err);
    } finally {
      try {
        destroyDevice(fd);
      } catch (Throwable ignored) {
      }
      try {
        Os.close(fd);
      } catch (Throwable ignored) {
      }
    }
    System.exit(0);
  }

  private static void pump(FileDescriptor fd) throws Exception {
    DataInputStream input = new DataInputStream(new BufferedInputStream(System.in));
    byte[] payload = new byte[255];
    // Resolve the injection methods now rather than on the first mouse event.
    Pointer.warmUp();
    while (true) {
      int type;
      try {
        type = input.readUnsignedByte();
      } catch (EOFException e) {
        log("stdin closed");
        return;
      }
      int len = input.readUnsignedByte();
      input.readFully(payload, 0, len);
      if (type == MSG_POINTER) {
        if (len == POINTER_PAYLOAD_SIZE) Pointer.enqueue(Pointer.Event.parse(payload));
        else log("bad pointer message length " + len);
      } else if (type == MSG_KEYBOARD_REPORT) {
        if (created) write(fd, input2(payload, len));
      } else if (type == MSG_ATTACH) {
        createDevice(fd);
      } else if (type == MSG_DETACH) {
        destroyDevice(fd);
      } else {
        log("unknown message type " + type);
      }
    }
  }

  /** Injects pointer events on their own thread; see the class comment. */
  private static final class Pointer {
    static final class Event {
      int action;
      int actionButton;
      int buttonState;
      int metaState;
      int displayId;
      float x;
      float y;
      float hScroll;
      float vScroll;
      long downTime;
      long eventTime;

      static Event parse(byte[] p) {
        ByteBuffer b = ByteBuffer.wrap(p, 0, POINTER_PAYLOAD_SIZE).order(ByteOrder.BIG_ENDIAN);
        Event e = new Event();
        e.action = b.getInt();
        e.actionButton = b.getInt();
        e.buttonState = b.getInt();
        e.metaState = b.getInt();
        e.displayId = b.getInt();
        e.x = b.getFloat();
        e.y = b.getFloat();
        e.hScroll = b.getFloat();
        e.vScroll = b.getFloat();
        e.downTime = b.getLong();
        e.eventTime = b.getLong();
        return e;
      }

      boolean isMove() {
        return action == MotionEvent.ACTION_HOVER_MOVE || action == MotionEvent.ACTION_MOVE;
      }

      /** Whether {@code next} may replace this queued event without changing what apps see. */
      boolean canCoalesceWith(Event next) {
        return isMove()
            && next.action == action
            && next.buttonState == buttonState
            && next.metaState == metaState
            && next.displayId == displayId
            && next.downTime == downTime;
      }
    }

    private static final ArrayDeque<Event> queue = new ArrayDeque<>();
    private static Thread thread;
    private static boolean unavailable = false;

    private static Object inputManager;
    private static Method injectInputEvent;
    private static Method setDisplayId;
    private static Method setActionButton;

    static void warmUp() {
      synchronized (queue) {
        if (thread == null) start();
      }
    }

    static void enqueue(Event e) {
      synchronized (queue) {
        if (unavailable) return;
        if (thread == null) start();
        Event last = queue.peekLast();
        if (last != null && last.canCoalesceWith(e)) queue.pollLast();
        queue.addLast(e);
        queue.notify();
      }
    }

    /** Called with the queue lock held. */
    private static void start() {
      thread =
          new Thread(
              () -> {
                if (setUp()) run();
              },
              "pointer-inject");
      thread.setDaemon(true);
      thread.start();
    }

    private static boolean setUp() {
      try {
        // Same as the app's input threads: don't let background work delay the cursor.
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
      } catch (Throwable ignored) {
      }
      try {
        Class<?> cls;
        try {
          // Android 14+: the injection entry point moved to InputManagerGlobal.
          cls = Class.forName("android.hardware.input.InputManagerGlobal");
        } catch (ClassNotFoundException e) {
          cls = Class.forName("android.hardware.input.InputManager");
        }
        inputManager = cls.getMethod("getInstance").invoke(null);
        injectInputEvent = cls.getMethod("injectInputEvent", InputEvent.class, int.class);
        setDisplayId = InputEvent.class.getMethod("setDisplayId", int.class);
        try {
          setActionButton = MotionEvent.class.getMethod("setActionButton", int.class);
        } catch (Throwable err) {
          log("MotionEvent.setActionButton unavailable: " + err);
        }
        log("pointer injection ready");
        return true;
      } catch (Throwable err) {
        log(POINTER_UNAVAILABLE + ": " + err);
        synchronized (queue) {
          unavailable = true;
          queue.clear();
        }
        return false;
      }
    }

    private static void run() {
      MotionEvent.PointerProperties[] props = {new MotionEvent.PointerProperties()};
      props[0].id = 0;
      props[0].toolType = MotionEvent.TOOL_TYPE_MOUSE;
      MotionEvent.PointerCoords[] coords = {new MotionEvent.PointerCoords()};
      while (true) {
        Event e;
        synchronized (queue) {
          while (queue.isEmpty()) {
            try {
              queue.wait();
            } catch (InterruptedException ignored) {
              return;
            }
          }
          e = queue.pollFirst();
        }
        inject(e, props, coords);
      }
    }

    private static void inject(
        Event e, MotionEvent.PointerProperties[] props, MotionEvent.PointerCoords[] coords) {
      MotionEvent.PointerCoords c = coords[0];
      c.clear();
      c.x = e.x;
      c.y = e.y;
      c.pressure = e.buttonState != 0 ? 1f : 0f;
      c.size = 1f;
      if (e.action == MotionEvent.ACTION_SCROLL) {
        c.setAxisValue(MotionEvent.AXIS_HSCROLL, e.hScroll);
        c.setAxisValue(MotionEvent.AXIS_VSCROLL, e.vScroll);
      }
      MotionEvent event =
          MotionEvent.obtain(
              e.downTime,
              e.eventTime,
              e.action,
              1,
              props,
              coords,
              e.metaState,
              e.buttonState,
              1f,
              1f,
              0, // deviceId: not tied to a real input device, as in scrcpy
              0,
              InputDevice.SOURCE_MOUSE,
              0);
      try {
        setDisplayId.invoke(event, e.displayId);
        if (e.actionButton != 0 && setActionButton != null) {
          setActionButton.invoke(event, e.actionButton);
        }
        Object ok = injectInputEvent.invoke(inputManager, event, INJECT_MODE_ASYNC);
        if (!Boolean.TRUE.equals(ok)) log("injectInputEvent rejected: " + event);
      } catch (Throwable err) {
        log("injectInputEvent failed: " + err);
      } finally {
        event.recycle();
      }
    }
  }

  /** Types "hello " + the US apostrophe and semicolon keys (ä and ö on a Finnish layout). */
  private static void runDemo(FileDescriptor fd, long delayMs) throws Exception {
    // Give InputReader time to add the device (and the user time to focus a field).
    Thread.sleep(delayMs);
    int[] usages = {0x0B, 0x08, 0x0F, 0x0F, 0x12, 0x2C, 0x34, 0x33};
    for (int usage : usages) {
      byte[] down = new byte[KEYBOARD_REPORT_SIZE];
      down[2] = (byte) usage;
      write(fd, input2(down, down.length));
      Thread.sleep(30);
      byte[] up = new byte[KEYBOARD_REPORT_SIZE];
      write(fd, input2(up, up.length));
      Thread.sleep(30);
    }
    Thread.sleep(500);
  }

  /** Drain kernel-to-userspace events so the queue never fills; logs them for debugging. */
  private static void startOutputReader(FileDescriptor fd) {
    Thread t =
        new Thread(
            () -> {
              byte[] buf = new byte[4380];
              try {
                while (true) {
                  int n = Os.read(fd, buf, 0, buf.length);
                  if (n <= 0) return;
                  int type = ByteBuffer.wrap(buf, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                  switch (type) {
                    case UHID_START:
                    case UHID_STOP:
                    case UHID_OPEN:
                    case UHID_CLOSE:
                      log("uhid event " + type);
                      break;
                    case UHID_OUTPUT:
                      break; // LED state; ignored for now
                    case UHID_GET_REPORT:
                    case UHID_SET_REPORT:
                      log("uhid report request " + type + " (unanswered)");
                      break;
                    default:
                      break;
                  }
                }
              } catch (Throwable ignored) {
              }
            },
            "uhid-reader");
    t.setDaemon(true);
    t.start();
  }

  private static ByteBuffer header(int type, int extra) {
    return ByteBuffer.allocate(4 + extra).order(ByteOrder.LITTLE_ENDIAN).putInt(type);
  }

  private static byte[] create2(String name) {
    byte[] desc = new byte[KEYBOARD_REPORT_DESC.length];
    for (int i = 0; i < desc.length; i++) desc[i] = (byte) KEYBOARD_REPORT_DESC[i];
    // name[128] phys[64] uniq[64] rd_size:u16 bus:u16 vendor product version country:u32 rd_data
    ByteBuffer buf = header(UHID_CREATE2, 256 + 2 + 2 + 16 + desc.length);
    buf.put(Arrays.copyOf(name.getBytes(StandardCharsets.UTF_8), 128)); // NUL-padded
    buf.put(Arrays.copyOf(PHYS.getBytes(StandardCharsets.UTF_8), 64));
    buf.put(Arrays.copyOf(UNIQ.getBytes(StandardCharsets.UTF_8), 64));
    buf.putShort((short) desc.length);
    buf.putShort((short) BUS_VIRTUAL);
    buf.putInt(VENDOR_ID);
    buf.putInt(PRODUCT_ID);
    buf.putInt(1); // version
    buf.putInt(0); // country
    buf.put(desc);
    return buf.array();
  }

  private static byte[] input2(byte[] data, int len) {
    return header(UHID_INPUT2, 2 + len).putShort((short) len).put(data, 0, len).array();
  }

  /** One uhid_event per write() call, as the kernel requires. */
  private static void write(FileDescriptor fd, byte[] bytes) throws Exception {
    int n = Os.write(fd, bytes, 0, bytes.length);
    if (n != bytes.length) throw new IllegalStateException("short write: " + n + "/" + bytes.length);
  }

  private static void log(String msg) {
    System.err.println(TAG + ": " + msg);
    try {
      Log.i(TAG, msg);
    } catch (Throwable ignored) {
    }
  }
}
