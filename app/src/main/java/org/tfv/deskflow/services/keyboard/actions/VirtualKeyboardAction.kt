package org.tfv.deskflow.services.keyboard.actions

import android.view.inputmethod.ExtractedText
import android.view.inputmethod.InputConnection
import kotlin.math.max
import kotlin.math.min
import org.tfv.deskflow.BuildConfig
import org.tfv.deskflow.client.events.KeyboardEvent
import org.tfv.deskflow.client.models.keys.KeyModifierMask
import org.tfv.deskflow.client.util.Keyboard
import org.tfv.deskflow.client.util.logging.KLoggingManager
import org.tfv.deskflow.services.VirtualKeyboardService
import org.tfv.deskflow.services.keyboard.KeyboardEditHistory

typealias VirtualKeyboardActionCallable =
  (
    ic: InputConnection,
    et: ExtractedText,
    specialKey: Keyboard.SpecialKey?,
    mods: KeyModifierMask,
    KeyboardEvent,
    KeyboardEditHistory?,
    VirtualKeyboardService,
  ) -> Unit

private val log = KLoggingManager.logger("VirtualKeyboardAction")

val editActionSelectAll: VirtualKeyboardActionCallable =
  { ic, et, specialKey, mods, event, editHistory, service ->
    ic.setSelection(0, et.text.length)
  }

val editActionCopy: VirtualKeyboardActionCallable =
  { ic, et, specialKey, mods, event, editHistory, service ->
    if (et.selectionStart != et.selectionEnd) {
      val selectedText = et.text.subSequence(et.selectionStart, et.selectionEnd)
      log.debug { "Copying selected text: $selectedText" }
      service.setClipboardText(selectedText)
    } else {
      log.warn { "No text selected to copy" }
    }
  }

val editActionCut: VirtualKeyboardActionCallable =
  { ic, et, specialKey, mods, event, editHistory, service ->
    if (et.selectionStart != et.selectionEnd) {
      val selectedText = et.text.subSequence(et.selectionStart, et.selectionEnd)
      log.debug { "Cutting selected text: $selectedText" }
      service.setClipboardText(selectedText)
      ic.deleteSurroundingText(et.selectionEnd - et.selectionStart, 0)
      service.saveEditHistory(
        service.editorInfo,
        service.currentExtractedTest(),
      )
    } else {
      log.warn { "No text selected to cut" }
    }
  }

val editActionPaste: VirtualKeyboardActionCallable =
  { ic, et, specialKey, mods, event, editHistory, service ->
    val cbText = service.getClipboardText()
    if (cbText != null) {
      log.debug { "Pasting clipboard text: $cbText" }
      ic.commitText(cbText, 1)
      service.saveEditHistory(
        service.editorInfo,
        service.currentExtractedTest(),
      )
    } else {
      log.warn { "Clipboard is empty, nothing to paste" }
    }
  }

val editActionLeft: VirtualKeyboardActionCallable =
  { ic, et, specialKey, mods, event, editHistory, service ->
    if (mods.isShift) {
      log.debug { "Extend selection left" }
      val selStart =
        when {
          mods.isMeta || mods.isSuper -> 0
          else -> max(0, et.selectionStart - 1)
        }
      val selEnd = et.selectionEnd
      log.debug {
        "Selection(start=$selStart,end=$selEnd),Previous(start=${et.selectionStart},end=${et.selectionEnd})"
      }
      ic.setSelection(selStart, selEnd)
    } else if (mods.isMeta || mods.isSuper) {
      log.debug { "Move cursor to start of line" }
      ic.setSelection(0, 0)
    }
  }

val editActionRight: VirtualKeyboardActionCallable =
  { ic, et, specialKey, mods, event, editHistory, service ->
    if (mods.isShift) {
      log.debug { "Extend selection right" }
      val text = et.text
      val selStart = et.selectionStart
      val selEnd =
        when {
          mods.isMeta || mods.isSuper -> text.length
          else -> min(text.length, et.selectionEnd + 1)
        }
      log.debug {
        "Selection(start=$selStart,end=$selEnd),Previous(start=${et.selectionStart},end=${et.selectionEnd})"
      }
      ic.setSelection(selStart, selEnd)
    } else if (mods.isMeta || mods.isSuper) {
      log.debug { "Move cursor to end of line" }
      val text = et.text
      ic.setSelection(text.length, text.length)
    }
  }

val editActionBackSpace: VirtualKeyboardActionCallable =
  { ic, et, specialKey, mods, event, editHistory, service ->
    log.debug { "BackSpace key detected" }

    if (BuildConfig.DEBUG) service.logCurrentImeState()

    ic.deleteSurroundingText(1, 0)
    service.saveEditHistory(service.editorInfo, service.currentExtractedTest())
  }

val editActionDelete: VirtualKeyboardActionCallable =
  { ic, et, specialKey, mods, event, editHistory, service ->
    log.debug { "Delete detected" }

    ic.deleteSurroundingText(0, 1)
    service.saveEditHistory(service.editorInfo, service.currentExtractedTest())
  }

val editActionUndo: VirtualKeyboardActionCallable =
  { ic, et, specialKey, mods, event, editHistory, service ->
    val undoValue = editHistory?.undo() // et.text.toString()
    log.debug { "Undo value=$undoValue" }
    if (undoValue != null) {
      ic.beginBatchEdit()
      ic.setSelection(0, et.text.length)
      ic.deleteSurroundingText(et.text.length, 0)
      ic.commitText(undoValue, undoValue.length)
      ic.endBatchEdit()
    }
  }

val editActionRedo: VirtualKeyboardActionCallable =
  { ic, et, specialKey, mods, event, editHistory, service ->
    val redoValue = editHistory?.redo()
    log.debug { "Redo value=$redoValue" }
    if (redoValue != null) {
      ic.beginBatchEdit()
      ic.setSelection(0, et.text.length)
      ic.deleteSurroundingText(et.text.length, 0)
      ic.commitText(redoValue, redoValue.length)
      ic.endBatchEdit()
    }
  }

val editActionUnknown: VirtualKeyboardActionCallable =
  { ic, et, specialKey, mods, event, editHistory, service ->
    throw Error("Unknown action called")
  }

enum class VirtualKeyboardAction(val action: VirtualKeyboardActionCallable) {
  Unknown(editActionUnknown),
  SelectAll(editActionSelectAll),
  Copy(editActionCopy),
  Cut(editActionCut),
  Paste(editActionPaste),
  Left(editActionLeft),
  Right(editActionRight),
  BackSpace(editActionBackSpace),
  Delete(editActionDelete),
  Undo(editActionUndo),
  Redo(editActionRedo);

  val actionId: Int = ordinal

  companion object {
    fun fromActionId(actionId: Int): VirtualKeyboardAction? {
      return entries.find { it.actionId == actionId }
    }

    fun fromName(name: String): VirtualKeyboardAction? {
      return entries.find { it.name.equals(name, ignoreCase = true) }
    }
  }
}
