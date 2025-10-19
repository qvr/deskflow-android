/*
 * MIT License
 *
 * Copyright (c) 2025 Jonathan Glanz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.tfv.deskflow.services

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT
import android.view.accessibility.AccessibilityWindowInfo
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import arrow.core.raise.catch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.tfv.deskflow.R
import org.tfv.deskflow.client.events.KeyboardEvent
import org.tfv.deskflow.client.events.MouseEvent
import org.tfv.deskflow.client.events.ScreenEvent
import org.tfv.deskflow.client.models.ClipboardData
import org.tfv.deskflow.client.util.Keyboard
import org.tfv.deskflow.client.util.logging.KLoggingManager
import org.tfv.deskflow.components.GlobalKeyboardManager
import org.tfv.deskflow.ext.canDrawOverlays
import org.tfv.deskflow.ext.getScreenSize
import org.tfv.deskflow.ext.sendServiceConnectionEvent

@OptIn(kotlin.concurrent.atomics.ExperimentalAtomicApi::class)
@SuppressLint("ServiceCast", "NewApi")
class GlobalInputService : AccessibilityService() {

  private var pickerShownForPackage: String? = null

  /** Client responsible for communicating with the connection service. */
  private lateinit var serviceClient: ConnectionServiceClient

  /**
   * Keyboard manager for handling keyboard events, calculating state and
   * triggering actions.
   */
  private lateinit var keyboardManager: GlobalKeyboardManager

  private val clipboard by lazy {
    getSystemService(ClipboardManager::class.java)
  }

  private val imeManager by lazy {
    getSystemService(InputMethodManager::class.java)
  }

  private val notificationManager by lazy {
    getSystemService(NotificationManager::class.java)
  }

  /**
   * Flow to observe if the home screen is currently active. This is used to
   * determine if the current screen is the home screen.
   * > Example: Used for checking if the user is on the home screen in the
   * > global input service.
   */
  private val isHomeScreenActiveFlow = MutableStateFlow(false)

  /** Check if the home screen is currently active. */
  private val isHomeScreenActive: Boolean
    get() = isHomeScreenActiveFlow.value

  /**
   * Set of known home packages. Populated when the service is connected via
   * `fetchHomePackages`.
   */
  private val knownHomePackages = mutableSetOf<String>()

  private val activePackageName: String?
    get() = rootInActiveWindow?.packageName?.toString()

  private val keyboardWindowInfo: AccessibilityWindowInfo?
    get() =
      windows.find { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }

  private val isKeyboardOpened: Boolean
    get() = keyboardWindowInfo != null

  /**
   * Flag to indicate if a global input action is currently pending. This is
   * used to prevent multiple gestures from being dispatched at the same time.
   */
  @Volatile private var globalInputPending = false

  /**
   * Handler for posting global input actions to the main thread. This is used
   * to ensure that gestures are dispatched on the main thread.
   */
  private val globalInputHandler = Handler(Looper.getMainLooper())

  /**
   * Callback for gesture results. This is used to handle the completion or
   * cancellation of gestures.
   */
  private val gestureResultCallback =
    object : GestureResultCallback() {
      override fun onCompleted(gestureDescription: GestureDescription) {
        super.onCompleted(gestureDescription)
        globalInputPending = false
      }

      override fun onCancelled(gestureDescription: GestureDescription) {
        super.onCancelled(gestureDescription)
        globalInputPending = false
      }
    }

  /**
   * Layout parameters for the mouse pointer view. This is used to position the
   * mouse pointer on the screen.
   */
  private lateinit var mousePointerLayout: WindowManager.LayoutParams

  /**
   * View for the mouse pointer. This is the view that will be displayed on the
   * screen to represent the mouse pointer.
   */
  private lateinit var mousePointerView: View

  /**
   * Flag to indicate if the mouse pointer is currently visible. This is used to
   * control the visibility of the mouse pointer view.
   */
  @Volatile private var mousePointerVisible = false

  private val keyboardWasOpen = AtomicBoolean(false)

  /** Store the IME that was active before switching to Deskflow */
  private var previousImeId: String? = null

  /** WindowManager instance for managing the mouse pointer view. */
  private lateinit var windowManager: WindowManager

  private val serviceScope =
    CoroutineScope(SupervisorJob() + Dispatchers.Default)

  private fun createNotificationChannel() {

    val name =
      resources.getText(R.string.global_input_service_notification_channel_name)
    val desc =
      resources.getText(
        R.string.global_input_service_notification_channel_description
      )
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val chan =
      NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = desc.toString()
      }
    notificationManager.createNotificationChannel(chan)
  }

  private fun sendStatusNotification(
    text: String,
    customizer: (NotificationCompat.Builder.() -> Unit)? = null,
  ) {
    if (
      ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS,
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      log.warn {
        "POST_NOTIFICATIONS permission not granted, cannot send notification."
      }
      return
    }
    val notificationBuilder =
      NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.deskflow_icon_fit) // your icon
        .setContentTitle(
          resources.getText(
            R.string.global_input_service_notification_channel_name
          )
        )
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    if (customizer != null) {
      customizer(notificationBuilder)
    }

    val notification = notificationBuilder.build()
    // TODO: Add action to open keyboard settings
    NotificationManagerCompat.from(this)
      .notify(NOTIF_IME_NOT_SETUP_ID, notification)
  }

  private fun isDeskflowKeyboardActive(
    imeInfo: InputMethodInfo? = imeManager.currentInputMethodInfo
  ): Boolean {
    return imeInfo?.let { current -> deskflowImeInfo?.id == current.id }
      ?: false
  }

  @SuppressLint("NewApi")
  private fun checkIMESetup() {
    val kbOpen = isKeyboardOpened
    val kbWasOpen = keyboardWasOpen.load()
    if (!kbOpen) {
      keyboardWasOpen.store(false)
      return
    }

    if (kbWasOpen) return

    if (!serviceClient.state.isConnected) return

    keyboardWasOpen.store(true)

    val imeInfo = imeManager.currentInputMethodInfo
    if (imeInfo != null) {
      log.debug { "Current IME: ${imeInfo.packageName}" }
      when {
        isDeskflowKeyboardActive(imeInfo) -> {
          log.debug { "Current IME is deskflow, no action needed." }
        }
        !isDeskflowImeEnabled -> {
          log.warn { "IME is not enabled, showing notification." }
          sendStatusNotification(
            resources.getString(
              R.string.global_input_service_notification_ime_not_setup
            )
          ) {
            setContentIntent(
              PendingIntent.getActivity(
                this@GlobalInputService,
                0,
                Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS),
                PendingIntent.FLAG_IMMUTABLE,
              )
            )
          }
        }
        with(activePackageName) {
          this == null || pickerShownForPackage == this
        } -> {
          log.debug {
            "IME picker was already shown for package $pickerShownForPackage"
          }
        }
        else -> {
          log.debug {
            "IME is enabled, but not active. Previous picker was shown for package $pickerShownForPackage"
          }

          // Store the current IME before switching
          if (!imeInfo.packageName.contains("deskflow", ignoreCase = true)) {
            previousImeId = imeInfo.id
            log.debug { "Stored previous IME before switching: $previousImeId" }
          }

          pickerShownForPackage = activePackageName
          val imeId = deskflowImeInfo?.id
          if (
            imeId != null && softKeyboardController.switchToInputMethod(imeId)
          ) {
            log.debug { "softKeyboardController set IME to $imeId" }
            return
          }

          imeManager.showInputMethodPicker()
        }
      }
    } else {
      log.warn { "No current IME found." }
    }
  }

  @SuppressLint("RtlHardcoded")
  override fun onCreate() {
    super.onCreate()
    log.debug { "onCreate:${GlobalInputService::class.simpleName}" }

    createNotificationChannel()

    keyboardManager = GlobalKeyboardManager(this)
    serviceScope.launch {
      keyboardManager.actionFlow.collect { action ->
        log.debug { "Triggered Action: ${action.label}(${action.actionId})" }
        when (action.actionId) {
          GLOBAL_ACTION_DPAD_CENTER -> {
            clickFocused()
          }

          else -> {
            performGlobalAction(action.actionId)
          }
        }
      }
    }

    serviceClient =
      ConnectionServiceClient(this) { event ->
        globalInputHandler.post {
          try {
            when (event) {
              is MouseEvent -> {
                onMouseEvent(event)
              }

              is KeyboardEvent -> {
                onKeyboardEvent(event)
              }

              is ScreenEvent.SetClipboard -> {
                val data = event.data
                log.debug { "SetClipboard(variantCount=${data.variants.size})" }
                val knownFormats =
                  arrayOf(
                    ClipboardData.Format.Text // ClipboardData.Format.Bitmap
                  )

                for (format in knownFormats) {
                  val variant = data.variants[format] ?: continue

                  // TODO: Implement `Converter` concept
                  val clipData =
                    when (format) {
                      ClipboardData.Format.Text -> {
                        log.debug { "SetClipboard: Text(${variant.data.size})" }
                        val text = String(variant.data, Charsets.UTF_8)
                        ClipData.newPlainText("deskflow_clipboard", text)
                      }

                      else -> {
                        log.warn {
                          "SetClipboard: No converter for format $format"
                        }
                        continue
                      }
                    }

                  log.debug { "Clipboard variant ready: $variant" }
                  clipboard.setPrimaryClip(clipData)
                  break
                }
              }

              else -> {}
            }
          } catch (err: Exception) {
            log.error(err) {
              "Error running on globalInputHandler: ${err.message}"
            }
          }
        }
      }

    mousePointerView = View.inflate(baseContext, R.layout.mouse_pointer, null)
    mousePointerLayout =
      WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
          WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
          WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
      )

    mousePointerLayout.layoutInDisplayCutoutMode =
      LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
    mousePointerLayout.gravity = Gravity.TOP or Gravity.LEFT
    mousePointerLayout.x = 200
    mousePointerLayout.y = 200

    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

    startService(Intent(applicationContext, ConnectionService::class.java))
    serviceClient.bind()

    // Monitor connection state to reset IME state when disconnected
    monitorConnectionState()
  }

  /**
   * Monitor connection state and reset IME-related state when disconnected.
   * Also manages mouse pointer visibility based on screen active state.
   */
  private fun monitorConnectionState() {
    serviceScope.launch {
      serviceClient.stateFlow.collect { state ->
        log.debug {
          "Connection state changed in GlobalInputService: isConnected=${state.isConnected}, isEnabled=${state.isEnabled}, isActive=${state.screen.isActive}"
        }

        // Show/hide mouse pointer based on connection state AND screen active state
        // The pointer should only be visible when:
        // 1. Connected to server (isConnected)
        // 2. Service is enabled (isEnabled)
        // 3. Cursor is on THIS client's screen (screen.isActive)
        val shouldShowPointer = state.isConnected && state.isEnabled && state.screen.isActive

        if (shouldShowPointer) {
          log.info { "Cursor entered this client, showing mouse pointer" }
          withContext(Dispatchers.Main) {
            showMousePointer()
          }
        } else {
          if (state.isConnected && state.isEnabled && !state.screen.isActive) {
            log.info { "Cursor left this client, hiding mouse pointer" }
          } else {
            log.info { "Connection inactive, hiding mouse pointer" }
          }
          withContext(Dispatchers.Main) {
            hideMousePointer()
          }
        }

        // When disconnected or disabled, reset IME tracking state
        if (!state.isConnected || !state.isEnabled) {
          log.info { "Connection lost or disabled, resetting IME state" }

          // Reset IME picker tracking
          pickerShownForPackage = null
          keyboardWasOpen.store(false)
        }
      }
    }
  }

  /**
   * Handle keyboard events from the client. This is where we process keyboard
   * events and pass them to the keyboard manager.
   * > Example: Used for handling key presses in the global input service.
   */
  private fun onKeyboardEvent(event: KeyboardEvent) {
    log.debug { "onKeyboardEvent: $event" }

    // always process modifier key events (both Down and Up) to keep keyboardManager state synchronized
    val modKey = Keyboard.findModifierKey(event.id.toInt())
    if (modKey != null) {
      log.debug { "Modifier key event: $modKey, type=${event.type}" }
      keyboardManager.process(event)
      return
    }

    // Only handle Down and Repeat events for non-modifier keys - ignore Up to avoid duplicates
    if (event.type == KeyboardEvent.Type.Up) {
      return
    }

    keyboardManager.process(event)
  }

  /**
   * Called when the service is destroyed. This is where we clean up resources
   * and remove the mouse pointer view.
   */
  override fun onDestroy() {
    serviceScope.cancel()
    serviceClient.unbind()
    hideMousePointer()  // Use the safe hide method instead of direct removeView
    super.onDestroy()
  }

  /**
   * Perform a click on the closest node to the mouse pointer. This is used to
   * simulate a mouse click on the screen.
   * > Example: Used for clicking on buttons or input fields in the global input
   * > service.
   */
  @Suppress("unused")
  private fun clickClosestNodeToPointer() {
    val nodeInfo = rootInActiveWindow ?: return
    val nearestNodeToMouse: AccessibilityNodeInfo? =
      findSmallestNodeAtPoint(
        nodeInfo,
        mousePointerLayout.x,
        mousePointerLayout.y,
      )
    if (nearestNodeToMouse != null) {
      logNodeHierarchy(nearestNodeToMouse, 0)
      nearestNodeToMouse.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
  }

  /**
   * Perform a click gesture at the current mouse pointer position. This is used
   * to simulate a mouse click on the screen.
   * > Example: Used for clicking on buttons or input fields in the global input
   * > service.
   */
  private fun clickGesture(
    x: Float = mousePointerLayout.x.toFloat(),
    y: Float = mousePointerLayout.y.toFloat(),
  ) {

    log.debug { "Click gesture at [$x, $y]" }

    val path = Path().apply { moveTo(x, y) }
    val gesture =
      GestureDescription.Builder()
        .addStroke(StrokeDescription(path, 0, 100))
        .build()
    dispatchGesture(gesture, gestureResultCallback, globalInputHandler)
  }

  /**
   * Move the mouse pointer to the specified coordinates. This is used to update
   * the position of the mouse pointer on the screen.
   * > Example: Used for mouse movement in the global input service.
   */
  private fun moveMousePointer(x: Int, y: Int) {
    if (!mousePointerVisible) return

    val screenSize = getScreenSize()
    log.debug {
      "Cursor move to [${x}, ${y}] with size(${screenSize.px.width},${screenSize.px.height})"
    }
    mousePointerLayout.x = x
    mousePointerLayout.y = y

    windowManager.updateViewLayout(mousePointerView, mousePointerLayout)
  }

  /**
   * Move the cursor to the specified coordinates. This is used to move the
   * mouse pointer to a specific location on the screen.
   * > Example: Used for mouse movement in the global input service.
   */
  private fun onMouseEvent(event: MouseEvent) {
    when (event.type) {
      MouseEvent.Type.Move -> {
        moveMousePointer(event.x, event.y)
      }

      MouseEvent.Type.MoveRelative -> {
        moveMousePointer(
          mousePointerLayout.x + event.x,
          mousePointerLayout.y + event.y,
        )
      }

      MouseEvent.Type.Down -> {
        log.debug { "Down [$event]" }
      }

      MouseEvent.Type.Up -> {
        log.debug { "Up [$event]" }
        clickGesture()
      }

      MouseEvent.Type.Wheel -> {
        // Single mouse wheel "click" seems to be 120 for me
        if (abs(event.y) < 30) return

        val scrollUp = event.y > 0
        log.debug { "Wheel [x=${event.x}, y=${event.y}, scrollUp=$scrollUp]" }

        // Use swipe gesture at pointer location for consistent scrolling
        scrollSwipe(up = scrollUp)
      }
    }
  }

  /** Set up the pointer view and add it to the window manager. */
  private fun setupMousePointer() {
    when (canDrawOverlays()) {
      true -> {
        if (!mousePointerVisible) {
          windowManager.addView(mousePointerView, mousePointerLayout)
          mousePointerVisible = true
          log.debug { "Mouse pointer shown" }
        }
      }

      false -> {
        log.warn { "Overlay permissions not granted yet" }
      }
    }
  }

  /**
   * Perform a scroll swipe gesture at the mouse pointer location.
   * This creates a small vertical swipe to simulate scrolling.
   *
   * @param up true to scroll up (swipe down gesture), false to scroll down (swipe up gesture)
   */
  private fun scrollSwipe(up: Boolean = false) {
    if (globalInputPending) {
      log.debug { "Scroll ignored - gesture already pending" }
      return
    }

    globalInputPending = true

    val screenSize = getScreenSize()
    val screenHeight = screenSize.px.height.toFloat()

    // Use pointer position as center of swipe
    val swipeX = mousePointerLayout.x.toFloat()
    val currentY = mousePointerLayout.y.toFloat()

    // Calculate scroll distance - use 20% of screen height for balanced scrolling
    val scrollDistance = screenHeight * 0.20f

    val (startY, endY) = when {
      // On home screen, wheel UP should pull up app drawer from bottom
      up && isHomeScreenActive -> {
        Pair(
          max(screenHeight * 0.6f - scrollDistance * 2, 0f),  // endY becomes start (top position)
          screenHeight * 0.6f  // startY becomes end (bottom position)
        )
      }
      // Normal scroll up in apps
      up -> {
        Pair(currentY + scrollDistance / 2, currentY - scrollDistance / 2)
      }
      // On home screen, wheel DOWN should pull down notification shade from top
      !up && isHomeScreenActive -> {
        Pair(
          min(screenHeight * 0.3f + scrollDistance * 2, screenHeight),  // endY becomes start (bottom position)
          screenHeight * 0.3f  // startY becomes end (top position)
        )
      }
      // Normal scroll down in apps
      else -> {
        Pair(currentY - scrollDistance / 2, currentY + scrollDistance / 2)
      }
    }

    // Ensure swipe stays within screen bounds
    val clampedStartY = startY.coerceIn(0f, screenHeight)
    val clampedEndY = endY.coerceIn(0f, screenHeight)

    val path = Path().apply {
      moveTo(swipeX, clampedEndY)
      lineTo(swipeX, clampedStartY)
    }

    // Swipe for 150ms for more responsive scrolling
    val stroke = StrokeDescription(path, 0, 150)
    val gesture = GestureDescription.Builder().addStroke(stroke).build()

    log.info {
      "Scroll swipe: up=$up, homeScreen=$isHomeScreenActive, x=$swipeX, startY=$clampedStartY, endY=$clampedEndY, distance=${abs(clampedEndY - clampedStartY)}"
    }

    dispatchGesture(gesture, gestureResultCallback, globalInputHandler)
  }

  /**
   * Show the mouse pointer overlay.
   * Safe to call multiple times - will only add view if not already visible.
   */
  private fun showMousePointer() {
    if (mousePointerVisible) {
      log.debug { "Mouse pointer already visible, skipping" }
      return
    }

    if (!canDrawOverlays()) {
      log.warn { "Cannot show mouse pointer - overlay permission not granted" }
      return
    }

    try {
      windowManager.addView(mousePointerView, mousePointerLayout)
      mousePointerVisible = true
      log.info { "Mouse pointer shown" }
    } catch (err: Exception) {
      log.error(err) { "Error showing mouse pointer" }
    }
  }

  /**
   * Hide the mouse pointer overlay.
   * Safe to call multiple times - will only remove view if currently visible.
   */
  private fun hideMousePointer() {
    if (!mousePointerVisible) {
      log.debug { "Mouse pointer already hidden, skipping" }
      return
    }

    try {
      windowManager.removeView(mousePointerView)
      mousePointerVisible = false
      log.info { "Mouse pointer hidden" }
    } catch (err: Exception) {
      log.error(err) { "Error hiding mouse pointer" }
    }
  }

  /**
   * Called when the service is started. This is where we set the service to be
   * sticky and return START_STICKY.
   */
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    return START_STICKY
  }

  val deskflowImeInfo: InputMethodInfo?
    get() =
      imeManager.inputMethodList.find {
        it.packageName.lowercase().contains("deskflow")
      }

  val isDeskflowImeEnabled: Boolean
    get() =
      imeManager.enabledInputMethodList.any { it.id == deskflowImeInfo?.id }

  private val onClipboardChanged =
    object : ClipboardManager.OnPrimaryClipChangedListener {
      override fun onPrimaryClipChanged() {
        catch({
          val clip = clipboard.primaryClip

          log.debug { "onPrimaryClipChanged(clip=$clip)" }
          if (clip == null) return@catch
          val clipDesc = clip.description
          val itemIdx =
            0.rangeUntil(clipDesc.mimeTypeCount).find { idx ->
              listOf(
                  ClipDescription.MIMETYPE_TEXT_PLAIN,
                  ClipDescription.MIMETYPE_TEXT_HTML,
                )
                .contains(clipDesc.getMimeType(idx))
            }

          if (itemIdx == null) {
            log.warn { "No compatible mimetypes in primary clip" }
            return@catch
          }
          val item = clip.getItemAt(itemIdx)
          val text = item.coerceToText(this@GlobalInputService).toString()
          if (text.isBlank()) {
            log.warn { "ignoring empty clipdata \"${text}\"" }
            return@catch
          }
          val clipboardData =
            ClipboardData(
              0,
              0,
              mapOf(
                ClipboardData.Format.Text to
                  ClipboardData.Variant(
                    ClipboardData.Format.Text,
                    text.toByteArray(),
                  )
              ),
            )
          serviceClient.setClipboardData(
            Bundle().apply { putSerializable("clipboardData", clipboardData) }
          )
        }) { err: Throwable ->
          log.error(err) { "unable to update clipboard" }
        }
      }
    }

  /**
   * Called when the service is connected to the system. This is where we set up
   * the cursor and fetch the home packages.
   */
  override fun onServiceConnected() {
    super.onServiceConnected()

    val imeId = deskflowImeInfo
    Log.i(TAG, "imeId=$imeId,imeEnabled=$isDeskflowImeEnabled")
    clipboard.addPrimaryClipChangedListener(this.onClipboardChanged)

    setupMousePointer()

    sendServiceConnectionEvent<GlobalInputService>()

    fetchHomePackages()
    val pkgName = rootInActiveWindow?.packageName
    isHomeScreenActiveFlow.value =
      pkgName == null || knownHomePackages.contains(pkgName)
  }

  /**
   * All accessibility events are received here; we pay little attention to
   * them, but we do check for window state changes to determine if the home
   * screen is active.
   */
  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    log.debug { "onAccessibilityEvent: ${event?.eventType}" }
    //
    when (event?.eventType) {
      AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
        checkIMESetup()
      }
      AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
        val pkgName = rootInActiveWindow?.packageName
        isHomeScreenActiveFlow.value =
          pkgName == null || knownHomePackages.contains(pkgName)
      }
      else -> {
        log.debug { "Ignored event: ${event?.eventType}" }
      }
    }
  }

  override fun onInterrupt() {
    // Required override.
  }

  /**
   * Fetch the list of known home packages. This is used to determine if the
   * current screen is the home screen.
   */
  private fun fetchHomePackages() {
    val intent =
      Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
    val resolveInfoList =
      packageManager.queryIntentActivities(
        intent,
        PackageManager.MATCH_DEFAULT_ONLY,
      )
    knownHomePackages.clear()
    for (info in resolveInfoList) {
      knownHomePackages.add(info.activityInfo.packageName)
    }
  }

  /**
   * Click the currently focused node. This is useful for clicking on input
   * fields or buttons that are currently focused.
   * > Example: It's used for the DPAD_CENTER action in the system actions.
   */
  private fun clickFocused() {
    val focusedNode = findFocus(FOCUS_INPUT)
    // val focusedNode =
    // rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
    if (focusedNode != null) {
      logNodeHierarchy(focusedNode, 0)
      focusedNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    } else {
      log.warn { "No focused node found to click" }
    }
  }

  /// **
  // * Example: continuously move pointer in a circle.
  // * Call this from onServiceConnected or via a button in your overlay.
  // */
  // fun spinPointer(centerX: Int, centerY: Int, radius: Int, steps: Int = 36,
  // intervalMs: Long = 100) {
  //    for (i in 0 until steps) {
  //        uiHandler.postDelayed({
  //            val theta = 2 * Math.PI * i / steps
  //            val x = centerX + (radius * Math.cos(theta)).toInt()
  //            val y = centerY + (radius * Math.sin(theta)).toInt()
  //            movePointer(centerX, centerY, x - centerX, y - centerY)
  //        }, i * intervalMs)
  //    }
  // }

  companion object {
    private const val CHANNEL_ID = "deskflow_service_channel"
    private const val NOTIF_IME_NOT_SETUP_ID = 1

    private val TAG = GlobalInputService::class.java.simpleName
    private val log =
      KLoggingManager.logger(GlobalInputService::class.java.simpleName)

    private fun logNodeHierarchy(nodeInfo: AccessibilityNodeInfo, depth: Int) {
      val bounds = Rect()
      nodeInfo.getBoundsInScreen(bounds)

      val sb = StringBuilder()
      if (depth > 0) {
        (0..<depth).forEach { _ -> sb.append("  ") }
        sb.append("\u2514 ")
      }
      sb.append(nodeInfo.className)
      sb.append(" (" + nodeInfo.childCount + ")")
      sb.append(" $bounds")
      if (nodeInfo.getText() != null) {
        sb.append(" - \"" + nodeInfo.getText() + "\"")
      }
      log.trace { sb.toString() }

      for (i in 0..<nodeInfo.childCount) {
        val childNode = nodeInfo.getChild(i)
        if (childNode != null) {
          logNodeHierarchy(childNode, depth + 1)
        }
      }
    }

    private fun findSmallestNodeAtPoint(
      sourceNode: AccessibilityNodeInfo,
      x: Int,
      y: Int,
    ): AccessibilityNodeInfo? {
      val bounds = Rect()
      sourceNode.getBoundsInScreen(bounds)

      if (!bounds.contains(x, y)) {
        return null
      }

      for (i in 0..<sourceNode.childCount) {
        val nearestSmaller =
          findSmallestNodeAtPoint(sourceNode.getChild(i), x, y)
        if (nearestSmaller != null) {
          return nearestSmaller
        }
      }
      return sourceNode
    }
  }
}
