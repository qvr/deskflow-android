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

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import arrow.core.raise.catch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.tfv.deskflow.Application
import org.tfv.deskflow.R
import org.tfv.deskflow.client.events.KeyboardEvent
import org.tfv.deskflow.client.util.Keyboard
import org.tfv.deskflow.client.util.Keyboard.findModifierKey
import org.tfv.deskflow.client.util.Keyboard.findSpecialKey
import org.tfv.deskflow.client.util.logging.KLoggingManager
import org.tfv.deskflow.components.GlobalKeyboardManager.Companion.loadKeyboardActions
import org.tfv.deskflow.services.keyboard.KeyboardEditHistory
import org.tfv.deskflow.services.keyboard.actions.VirtualKeyboardAction
import org.tfv.deskflow.types.EditorKeyboardAction
import org.tfv.deskflow.types.ModifierKeys
import org.tfv.deskflow.types.ShortcutKey
import org.tfv.deskflow.ui.components.VirtualKeyboardView
import org.tfv.deskflow.ui.components.rememberAppState

@OptIn(ExperimentalLayoutApi::class)
class VirtualKeyboardService : InputMethodService() {
  companion object {
    private val log =
      KLoggingManager.logger(VirtualKeyboardService::class.java.simpleName)

    private class KeyboardViewLifecycleOwner :
      LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

      fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
      }

      fun onResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
      }

      fun onPause() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
      }

      fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
      }

      fun attachToView(view: View?) {
        log.debug { "attachToView(view=$view)" }
        if (view == null) return

        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
      }

      /**
       * Compose uses the Window's decor view to locate the
       * Lifecycle/ViewModel/SavedStateRegistry owners. Therefore, we need to
       * set this class as the "owner" for the decor view.
       */
      fun attachToDecorView(decorView: View?) {
        log.debug { "attachToDecorView(decorView=$decorView)" }
        attachToView(decorView)
      }

      // LifecycleOwner methods
      private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
      override val lifecycle: Lifecycle
        get() = lifecycleRegistry

      // ViewModelStore methods
      private val store = ViewModelStore()
      override val viewModelStore: ViewModelStore
        get() = store

      // SavedStateRegistry methods
      private val savedStateRegistryController =
        SavedStateRegistryController.create(this)
      override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
    }
  }

  private var keyboardViewLifecycleOwner = KeyboardViewLifecycleOwner()

  private val editHistoryMap = mutableMapOf<String, KeyboardEditHistory>()

  private fun getCurrentPackage(info: EditorInfo? = editorInfo): String? =
    info?.packageName

  private fun getEditHistory(
    info: EditorInfo? = editorInfo
  ): KeyboardEditHistory? {
    val pkg = getCurrentPackage(info) ?: return null
    return editHistoryMap.getOrPut(pkg) { KeyboardEditHistory() }
  }

  internal fun saveEditHistory(
    info: EditorInfo?,
    extractedText: ExtractedText?,
  ) {
    if (extractedText == null) return
    getEditHistory(info)?.save(extractedText.text.toString(), maxSize = 25)
  }

  internal val editorInfo: EditorInfo?
    get() = currentInputEditorInfo

  private val clipboard by lazy {
    getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
  }

  private val imeManager by lazy {
    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  }

  /** Client responsible for communicating with the connection service. */
  private lateinit var serviceClient: ConnectionServiceClient

  private lateinit var keyboardActions:
    Map<VirtualKeyboardAction, EditorKeyboardAction>

  private val app
    get() = application as Application

  private val serviceScope =
    CoroutineScope(Dispatchers.Main + SupervisorJob())

  private var connectionStateMonitorJob: Job? = null

  override fun onUpdateExtractedText(token: Int, text: ExtractedText?) {
    super.onUpdateExtractedText(token, text)
    logCurrentImeState("onUpdateExtractedText")
  }

  override fun onUpdateSelection(
    oldSelStart: Int,
    oldSelEnd: Int,
    newSelStart: Int,
    newSelEnd: Int,
    candidatesStart: Int,
    candidatesEnd: Int,
  ) {
    super.onUpdateSelection(
      oldSelStart,
      oldSelEnd,
      newSelStart,
      newSelEnd,
      candidatesStart,
      candidatesEnd,
    )

    logCurrentImeState(
      "onUpdateSelection(oldSelStart=$oldSelStart, oldSelEnd=$oldSelEnd, newSelStart=$newSelStart, newSelEnd=$newSelEnd, candidatesStart=$candidatesStart, candidatesEnd=$candidatesEnd)"
    )
  }

  override fun onUpdateEditorToolType(toolType: Int) {
    super.onUpdateEditorToolType(toolType)

    logCurrentImeState("onUpdateEditorToolType($toolType)")
  }

  @Deprecated("Deprecated in Java")
  override fun onUpdateCursor(newCursor: Rect?) {
    @Suppress("DEPRECATION") super.onUpdateCursor(newCursor)

    logCurrentImeState("onUpdateCursor")
  }

  override fun onUpdateCursorAnchorInfo(cursorAnchorInfo: CursorAnchorInfo?) {
    super.onUpdateCursorAnchorInfo(cursorAnchorInfo)

    logCurrentImeState("onUpdateCursorAnchorInfo")
  }

  override fun onCreate() {
    super.onCreate()
    log.debug { "onCreate:${this::class.java.simpleName}" }
    keyboardActions =
      loadKeyboardActions<VirtualKeyboardAction, EditorKeyboardAction>(
        this,
        R.raw.editor_actions_defaults,
      )

    serviceClient =
      ConnectionServiceClient(this) { event ->
        when (event) {
          is KeyboardEvent -> {
            onKeyboardEvent(event)
          }

          else -> {
            log.trace {
              "Unhandled event type: ${event::class.java.simpleName}"
            }
          }
        }
      }

    serviceClient.bind()
    keyboardViewLifecycleOwner.onCreate()

    // Monitor connection state and handle IME switching
    startConnectionStateMonitoring()
  }

  /**
   * Start monitoring connection state to show IME picker when disconnected.
   */
  private fun startConnectionStateMonitoring() {
    connectionStateMonitorJob = serviceScope.launch {
      serviceClient.stateFlow.collect { state ->
        log.debug {
          "Connection state changed: isConnected=${state.isConnected}, isEnabled=${state.isEnabled}"
        }

        // If we're not connected or not enabled, show IME picker so user can switch keyboards
        if (!state.isConnected || !state.isEnabled) {
          log.info { "Connection lost or disabled, showing IME picker" }
          showIMEPicker()
        }
      }
    }
  }

  /**
   * Show the system IME picker dialog to let the user choose a keyboard.
   */
  private fun showIMEPicker() {
    try {
      imeManager.showInputMethodPicker()
      log.debug { "IME picker shown" }
    } catch (err: Exception) {
      log.error(err) { "Error showing IME picker" }
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    return super.onStartCommand(intent, flags, startId)
  }

  override fun onDestroy() {
    log.debug { "onDestroy:${this::class.java.simpleName}" }

    // Cancel connection state monitoring
    connectionStateMonitorJob?.cancel()
    connectionStateMonitorJob = null

    // Cancel service scope
    serviceScope.cancel()

    serviceClient.unbind()
    super.onDestroy()
    catch({ keyboardViewLifecycleOwner.onDestroy() }) { err: Throwable ->
      log.error(err) {
        "Error during keyboard view lifecycle owner destruction"
      }
    }

    keyboardViewLifecycleOwner = KeyboardViewLifecycleOwner()
  }

  override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
    super.onStartInputView(info, restarting)
    keyboardViewLifecycleOwner.onResume()
  }

  override fun onFinishInputView(finishingInput: Boolean) {
    keyboardViewLifecycleOwner.onPause()
  }

  override fun onCreateInputView(): View {
    log.debug { "onCreateInputView" }
    val win = window.window ?: return super.onCreateInputView()
    WindowCompat.setDecorFitsSystemWindows(win, false)
    keyboardViewLifecycleOwner.attachToDecorView(win.decorView)
    WindowCompat.setDecorFitsSystemWindows(win, false)
    val view = ComposeView(this)

    view.setViewTreeLifecycleOwner(keyboardViewLifecycleOwner)
    view.setViewTreeViewModelStoreOwner(keyboardViewLifecycleOwner)
    view.setViewTreeSavedStateRegistryOwner(keyboardViewLifecycleOwner)

    // win.setBackgroundDrawable(android.graphics.Color.TRANSPARENT.toDrawable())
    // win.decorView.background  =
    // android.graphics.Color.TRANSPARENT.toDrawable()
    // view.background  = android.graphics.Color.TRANSPARENT.toDrawable()
    view.setContent {
      val state = rememberAppState(serviceClient)
      val remainingInsets = remember { MutableWindowInsets() }
      val safeContent = WindowInsets.safeContent
      Box(
        Modifier
          // .background(Color.Transparent)
          .navigationBarsPadding()
          .onConsumedWindowInsetsChanged { consumedWindowInsets ->
            remainingInsets.insets = safeContent.exclude(consumedWindowInsets)
          }
      ) { // padding can be used without recomposition when insets change.
        val padding = remainingInsets.asPaddingValues()
        Box(
          modifier =
            Modifier
              // .background(Color.Transparent)
              .padding(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
                start = 0.dp,
                end = 0.dp,
              )
        ) {
          VirtualKeyboardView(state)
        }
      }
    }

    return view
  }

  fun currentExtractedTest(): ExtractedText? {
    return currentInputConnection.getExtractedText(ExtractedTextRequest(), 0)
  }

  private fun applyCommand(
    command: String?,
    ic: InputConnection = currentInputConnection,
    extractedText: ExtractedText? =
      ic.getExtractedText(ExtractedTextRequest(), 0),
  ) {
    command?.let {
      ic.commitText(it, 1)
      saveEditHistory(currentInputEditorInfo, currentExtractedTest())
    }
  }

  internal fun logCurrentImeState(calledBy: String = "KeyboardEvent") {
    val ic = currentInputConnection
    val extracted = ic?.getExtractedText(ExtractedTextRequest(), 0)
    log.debug {
      "$calledBy >> Extracted text >> \"${extracted?.text}\" — selection: [${extracted?.selectionStart}, ${extracted?.selectionEnd}]"
    }

    val selected = ic?.getSelectedText(0)
    log.debug { "$calledBy >> Selected text >> \"$selected\"" }
  }

  /**
   * Gets the current text from the clipboard.
   *
   * @return The text from the clipboard, or null if the clipboard is empty.
   */
  internal fun getClipboardText(): CharSequence? {
    return clipboard.primaryClip?.getItemAt(0)?.text
  }

  internal fun setClipboardText(
    text: CharSequence,
    label: String = getString(R.string.clipboard_item_label),
  ) {
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
  }

  /**
   * Handles keyboard events from the client.
   *
   * @param event The keyboard event to handle.
   */
  private fun onKeyboardEvent(event: KeyboardEvent) {
    log.debug { "onKeyboardEvent: $event" }

    // 1. Check if we have a valid input connection.
    val ic = currentInputConnection
    if (ic == null) {
      log.warn { "Current input connection is null, ignoring event: $event" }
      return
    }

    // 2. Check if the keyboard view lifecycle is started.
    if (
      keyboardViewLifecycleOwner.lifecycle.currentState <
        Lifecycle.State.RESUMED
    ) {
      log.warn { "Lifecycle is not started, ignoring event: $event" }
      return
    }

    // 3. Only handle Down and Repeat events - ignore Up to avoid duplicates.
    if (event.type == KeyboardEvent.Type.Up) {
      return
    }

    // 4. Get the key ID and check if it's a modifier or special key.
    val id = event.id.toInt()

    val modifierKey = findModifierKey(id)
    if (modifierKey != null) {
      return
    }
    val specialKey = findSpecialKey(id)

    // 5. If all the previous checks passed, grab the current `ExtractedText` of
    // the `InputConnection`.
    // Note: Some apps (like terminal emulators) return null for getExtractedText,
    // but commitText still works for basic input.
    val et = ic.getExtractedText(ExtractedTextRequest(), 0)
    val mods = event.getModifiers()
    val editHistory = getEditHistory()

    // Build modifier keys for editor shortcut matching
    var modKeys = ModifierKeys()
    val setModKeys = { isPressed: Boolean, modKey: Keyboard.ModifierKey ->
      if (isPressed) modKeys = modKeys.updateModifierKeys(true, modKey)
    }
    setModKeys(mods.isMeta, Keyboard.ModifierKey.Meta)
    setModKeys(mods.isSuper, Keyboard.ModifierKey.Super)
    setModKeys(mods.isControl, Keyboard.ModifierKey.Control)
    setModKeys(mods.isAlt, Keyboard.ModifierKey.Alt)
    setModKeys(mods.isShift, Keyboard.ModifierKey.Shift)
    setModKeys(mods.isAltGr, Keyboard.ModifierKey.AltRight)
    setModKeys(mods.isCapsLock, Keyboard.ModifierKey.CapsLock)

    val shortcut = ShortcutKey(event.id.toInt(), modKeys)
    val shortcutActionEntry = keyboardActions.entries.find {
      it.value.defaultShortcutKeys.contains(shortcut)
    }

    when {
      // Editor shortcut actions
      shortcutActionEntry != null && et != null -> {
        log.debug {
          "Matched shortcut to action(type=${shortcutActionEntry.key},value=${shortcutActionEntry.value})"
        }
        shortcutActionEntry.key.action(
          ic,
          et,
          specialKey,
          mods,
          event,
          editHistory,
          this,
        )
      }
      // Special key actions (e.g., BackSpace, Delete, etc.)
      specialKey != null -> {
        val action =
          keyboardActions.entries.find { it.value.specialKey == specialKey }
        if (action == null) {
          // No action registered for this special key
          // Only apply fallback if the special key has imeText defined
          if (specialKey.imeText != null) {
            log.debug { "Special key detected with imeText: $specialKey" }
            applyCommand(specialKey.imeText, ic, et)
          } else {
            log.warn { "No action registered for special key without imeText: $specialKey - ignoring" }
          }
          return
        }

        log.debug { "Invoking action: ${action.key}" }
        action.key.action(ic, et, specialKey, mods, event, editHistory, this)
      }
      // Regular character input (including terminal apps where ExtractedText is null)
      else -> {
        log.debug { "Received $event" }
        val keyChar = id.toChar()
        val keyStr = keyChar.toString()

        // Terminal apps: Handle Ctrl+A-Z as ASCII control characters
        if (et == null && mods.isControl && !mods.isAlt && !mods.isMeta && !mods.isSuper) {
          val upperChar = keyChar.uppercaseChar()
          if (upperChar in 'A'..'Z') {
            val controlChar = (upperChar.code - 'A'.code + 1).toChar()
            log.debug { "Terminal: Ctrl+$upperChar -> control char ${controlChar.code}" }
            applyCommand(controlChar.toString(), ic, et)
            return
          }
        }

        // Apply regular character input
        log.debug { "Applying command: $keyStr" }
        applyCommand(keyStr, ic, et)
      }
    }
  }
}
