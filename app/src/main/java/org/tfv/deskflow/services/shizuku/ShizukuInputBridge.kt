package org.tfv.deskflow.services.shizuku

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.tfv.deskflow.client.util.logging.KLoggingManager
import rikka.shizuku.Shizuku

/**
 * Shizuku state for the app, and mouse event injection through it.
 *
 * Tracks whether Shizuku is running and this app may use it (for the
 * settings UI and [ShizukuKeyboardInjector]). While Shizuku mouse input is
 * enabled and the client is connected to a server, it holds
 * [ShizukuHelperProcess], which runs as the shell user and injects the
 * events with InputManager.injectInputEvent: one pipe write per event here,
 * one binder call into system_server there.
 */
object ShizukuInputBridge {
  private val log = KLoggingManager.logger("ShizukuInputBridge")

  enum class Status {
    /** Shizuku is not installed or its server is not started (needs ADB/root after boot). */
    NotRunning,
    /** Shizuku is running but this app has not been granted access. */
    PermissionRequired,
    /** The user denied access with "don't ask again"; must be granted from the Shizuku app. */
    PermissionDenied,
    /** Permission granted, mouse injection not enabled. */
    Idle,
    /** The helper process could not be started or cannot inject events; see logs. */
    Error,
    /** Mouse events are injected while connected to a server. */
    Ready,
  }

  private const val PERMISSION_REQUEST_CODE = 0x5e1f
  private const val MAX_HELPER_RESTARTS = 3
  private const val HELPER_RESTART_DELAY_MS = 2000L

  private val statusFlow = MutableStateFlow(Status.NotRunning)
  val status: StateFlow<Status> = statusFlow.asStateFlow()

  private val main = Handler(Looper.getMainLooper())

  private var initialized = false
  private var wanted = false
  private var connected = false
  private var appContext: Context? = null

  /** This feature holds [ShizukuHelperProcess]. */
  @Volatile private var helperAcquired = false
  /** Starting the helper or pointer injection in it failed; cleared on reconnect. */
  private var helperFailed = false
  private var helperRestarts = 0

  /** True while mouse events passed to [inject] reach the system. */
  val isReady: Boolean
    get() = helperAcquired && ShizukuHelperProcess.pointerAvailable

  private val binderReceived =
    Shizuku.OnBinderReceivedListener {
      log.info { "Shizuku binder received" }
      refresh()
    }

  private val binderDead =
    Shizuku.OnBinderDeadListener {
      log.warn { "Shizuku binder died" }
      refresh()
    }

  private val permissionResult =
    Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
      if (requestCode != PERMISSION_REQUEST_CODE) return@OnRequestPermissionResultListener
      log.info {
        "Shizuku permission result: granted=${grantResult == PackageManager.PERMISSION_GRANTED}"
      }
      refresh()
    }

  /** Idempotent. Registers Shizuku listeners. */
  @Synchronized
  fun init() {
    if (initialized) return
    initialized = true
    Shizuku.addBinderReceivedListenerSticky(binderReceived)
    Shizuku.addBinderDeadListener(binderDead)
    Shizuku.addRequestPermissionResultListener(permissionResult)
    ShizukuHelperProcess.onPointerUnavailable = {
      synchronized(this) { helperFailed = true }
      main.post { refresh() }
    }
    refresh()
  }

  /** Enable (true) or disable (false) mouse injection. */
  fun setWanted(value: Boolean) {
    init()
    synchronized(this) {
      wanted = value
      if (value) {
        helperFailed = false
        helperRestarts = 0
      }
    }
    refresh()
  }

  /**
   * Run the helper process only while connected to a server, like the
   * virtual keyboard; until then nothing is injected anyway.
   */
  fun setConnected(context: Context, value: Boolean) {
    init()
    synchronized(this) {
      appContext = context.applicationContext
      if (connected == value) return
      connected = value
      helperFailed = false
      helperRestarts = 0
    }
    refresh()
  }

  /** Ask Shizuku to show its permission dialog. */
  fun requestPermission() {
    init()
    if (!Shizuku.pingBinder()) return
    runCatching { Shizuku.requestPermission(PERMISSION_REQUEST_CODE) }
      .onFailure { log.error(it) { "Shizuku permission request failed" } }
  }

  /** Have the helper process inject [pointer]; dropped if it isn't running. */
  fun inject(pointer: PointerEvent) {
    if (helperAcquired) ShizukuHelperProcess.sendPointer(pointer)
  }

  @Synchronized
  fun refresh() {
    val running = Shizuku.pingBinder()
    val granted =
      running &&
        runCatching { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }
          .getOrDefault(false)

    updateHelper(granted)

    statusFlow.value =
      when {
        !running -> Status.NotRunning
        !granted ->
          if (
            runCatching { Shizuku.shouldShowRequestPermissionRationale() }.getOrDefault(false)
          )
            Status.PermissionDenied
          else Status.PermissionRequired
        !wanted -> Status.Idle
        helperFailed -> Status.Error
        else -> Status.Ready
      }
  }

  /** Called with the lock held. */
  private fun updateHelper(granted: Boolean) {
    val ctx = appContext
    val need = wanted && granted && connected && !helperFailed && ctx != null
    if (need && !helperAcquired) {
      helperAcquired =
        ShizukuHelperProcess.acquire(ctx!!, ShizukuHelperProcess.User.Mouse) { code ->
          main.post { onHelperExit(code) }
        }
      if (helperAcquired) log.info { "Shizuku mouse injection started" }
      else {
        log.warn { "Helper process unavailable, mouse input uses touch gestures" }
        helperFailed = true
      }
    } else if (!need && helperAcquired) {
      ShizukuHelperProcess.release(ShizukuHelperProcess.User.Mouse)
      helperAcquired = false
      log.info { "Shizuku mouse injection stopped" }
    }
  }

  private fun onHelperExit(code: Int?) {
    synchronized(this) {
      // The helper dropped every user when it exited.
      helperAcquired = false
      if (!wanted || !connected) return
      if (helperRestarts >= MAX_HELPER_RESTARTS) {
        log.error { "Helper keeps exiting ($code), mouse input uses touch gestures" }
        helperFailed = true
        statusFlow.value = Status.Error
        return
      }
      helperRestarts++
      log.warn { "Helper exited ($code), restarting (attempt $helperRestarts)" }
    }
    main.postDelayed({ refresh() }, HELPER_RESTART_DELAY_MS)
  }
}
