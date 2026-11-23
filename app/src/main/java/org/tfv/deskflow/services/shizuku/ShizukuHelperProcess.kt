package org.tfv.deskflow.services.shizuku

import android.content.Context
import android.content.pm.PackageManager
import java.io.DataOutputStream
import java.lang.reflect.Method
import org.tfv.deskflow.client.util.logging.KLoggingManager
import org.tfv.deskflow.uhid.UhidMain
import rikka.shizuku.Shizuku

/**
 * Runs the shell-user helper process ([UhidMain]) through Shizuku and feeds
 * it messages on stdin: virtual keyboard reports for [ShizukuKeyboardInjector]
 * and mouse events for [ShizukuInputBridge].
 *
 * One process serves both. Each feature [acquire]s it while it needs it and
 * [release]s it afterwards; the process stops when nobody holds it.
 *
 * The helper is started with Shizuku.newProcess rather than a Shizuku
 * UserService: Shizuku 13.6's UserService starter calls
 * LoadedApk.makeApplication(), which crashes on MediaTek framework builds
 * (RikkaApps/Shizuku#1198). A bare app_process main() never gets there.
 *
 * newProcess is private in Shizuku API 13 (deprecated in favour of
 * UserService), so it's called reflectively.
 */
object ShizukuHelperProcess {
  private val log = KLoggingManager.logger("ShizukuHelperProcess")

  enum class User {
    Keyboard,
    Mouse,
  }

  /** Name of the keyboard input device the helper creates. */
  const val KEYBOARD_DEVICE_NAME = UhidMain.DEVICE_NAME

  private val newProcess: Method? by lazy {
    runCatching {
        Shizuku::class
          .java
          .getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java,
          )
          .apply { isAccessible = true }
      }
      .onFailure { log.error(it) { "Shizuku.newProcess unavailable" } }
      .getOrNull()
  }

  private var process: Process? = null
  private var stdin: DataOutputStream? = null

  /** Who holds the process, each with the callback for an unexpected exit. */
  private val users = mutableMapOf<User, (Int?) -> Unit>()

  /**
   * False once the running helper reported that it can't inject pointer
   * events; [onPointerUnavailable] is called when that happens.
   */
  @Volatile
  var pointerAvailable: Boolean = false
    private set

  /** Called on a background thread when the running helper can't inject pointer events. */
  @Volatile var onPointerUnavailable: (() -> Unit)? = null

  /**
   * True while the helper process runs. Tracked here rather than via
   * Process.exitValue(): on a Shizuku remote process that's a binder call
   * whose "still running" exception doesn't reliably come back as one.
   */
  val isRunning: Boolean
    @Synchronized get() = process != null

  /**
   * Hold the helper for [user], starting it if needed. The keyboard device is
   * not created until [setKeyboardAttached]. [onExit] runs on a background
   * thread if the helper exits on its own (not after the last [release]);
   * [user] then no longer holds it.
   */
  @Synchronized
  fun acquire(context: Context, user: User, onExit: (Int?) -> Unit = {}): Boolean {
    if (process == null && !start(context)) return false
    users[user] = onExit
    return true
  }

  /** Stop holding the helper; it exits once no user holds it. */
  @Synchronized
  fun release(user: User) {
    if (users.remove(user) == null) return
    if (users.isEmpty()) stop()
  }

  /** Send one 8-byte boot keyboard report: [modifiers, 0, key1..key6]. */
  fun sendKeyboardReport(report: ByteArray): Boolean {
    require(report.size == UhidMain.KEYBOARD_REPORT_SIZE)
    return send(UhidMain.MSG_KEYBOARD_REPORT) { it.write(report) }
  }

  /**
   * Create ([attached]) or destroy the keyboard device without stopping the
   * helper. Android hides on-screen keyboards while it exists.
   */
  fun setKeyboardAttached(attached: Boolean): Boolean =
    send(if (attached) UhidMain.MSG_ATTACH else UhidMain.MSG_DETACH, null)

  /** Have the helper inject [event]. False if it isn't running or can't. */
  fun sendPointer(event: PointerEvent): Boolean {
    if (!pointerAvailable) return false
    return send(UhidMain.MSG_POINTER) { out ->
      out.writeInt(event.action)
      out.writeInt(event.actionButton)
      out.writeInt(event.buttonState)
      out.writeInt(event.metaState)
      out.writeInt(event.displayId)
      out.writeFloat(event.x)
      out.writeFloat(event.y)
      out.writeFloat(event.hScroll)
      out.writeFloat(event.vScroll)
      out.writeLong(event.downTime)
      out.writeLong(event.eventTime)
    }
  }

  private val payloadBuffer = java.io.ByteArrayOutputStream(64)
  private val payloadWriter = DataOutputStream(payloadBuffer)

  /** Writes one `[type][len][payload]` message in a single write() on the pipe. */
  @Synchronized
  private fun send(type: Int, payload: ((DataOutputStream) -> Unit)?): Boolean {
    val out = stdin ?: return false
    return runCatching {
        payloadBuffer.reset()
        payloadWriter.writeByte(type)
        payloadWriter.writeByte(0) // length, patched below
        payload?.invoke(payloadWriter)
        payloadWriter.flush()
        val bytes = payloadBuffer.toByteArray()
        val len = bytes.size - 2
        check(len <= 255) { "payload too long: $len" }
        bytes[1] = len.toByte()
        out.write(bytes)
        out.flush()
        true
      }
      .onFailure {
        log.error(it) { "Helper write failed" }
        // The helper is gone; ending the process lets the exit watcher tell
        // the users, which restart it if they still need it.
        runCatching { process?.destroy() }
      }
      .getOrDefault(false)
  }

  /** Called with the lock held. */
  private fun start(context: Context): Boolean {
    if (!Shizuku.pingBinder()) {
      log.warn { "Shizuku not running" }
      return false
    }
    if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
      log.warn { "Shizuku permission not granted" }
      return false
    }
    val method = newProcess ?: return false
    val apk = context.applicationInfo.sourceDir
    val cmd =
      "CLASSPATH='$apk' exec /system/bin/app_process / ${UhidMain::class.java.name} --detached"
    return runCatching {
        val p = method.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
        process = p
        stdin = DataOutputStream(p.outputStream)
        pointerAvailable = true
        Thread({ watch(p) }, "shizuku-helper-log").apply { isDaemon = true }.start()
        log.info { "Helper started" }
        true
      }
      .onFailure { log.error(it) { "Failed to start helper" } }
      .getOrDefault(false)
  }

  private fun watch(p: Process) {
    runCatching {
      p.errorStream.bufferedReader().forEachLine { line ->
        log.info { "helper: $line" }
        if (UhidMain.POINTER_UNAVAILABLE in line && isCurrent(p)) {
          log.warn { "Helper can't inject pointer events" }
          pointerAvailable = false
          onPointerUnavailable?.invoke()
        }
      }
    }
    val code = runCatching { p.waitFor() }.getOrNull()
    log.info { "Helper exited: $code" }
    // Only report unexpected exits of the current helper, not one that
    // stop() ended or a newer start() replaced.
    val listeners =
      synchronized(this) {
        if (process !== p) return
        process = null
        stdin = null
        pointerAvailable = false
        users.values.toList().also { users.clear() }
      }
    listeners.forEach { it(code) }
  }

  @Synchronized private fun isCurrent(p: Process): Boolean = process === p

  /** Closing stdin makes the helper destroy the keyboard device and exit. */
  private fun stop() {
    runCatching { stdin?.close() }
    runCatching { process?.destroy() }
    stdin = null
    process = null
    pointerAvailable = false
    log.info { "Helper stopped" }
  }
}

/** One mouse MotionEvent, in the fields the helper needs to rebuild it. */
data class PointerEvent(
  val action: Int,
  val actionButton: Int,
  val buttonState: Int,
  val metaState: Int,
  val displayId: Int,
  val x: Float,
  val y: Float,
  val hScroll: Float,
  val vScroll: Float,
  val downTime: Long,
  val eventTime: Long,
)
