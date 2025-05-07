package org.tfv.deskflow.services.keyboard

data class KeyboardEditHistory(
  val undoStack: ArrayDeque<String> = ArrayDeque(),
  val redoStack: ArrayDeque<String> = ArrayDeque()
) {
  fun save(text: String, maxSize: Int = 25) {
    if (undoStack.lastOrNull() != text) {
      undoStack.addLast(text)
      if (undoStack.size > maxSize) undoStack.removeFirst()
      redoStack.clear()
    }
  }

  fun undo(): String? {
    if (undoStack.size < 2) return null
    val undoValue = undoStack.removeLast()
    redoStack.addLast(undoValue)
    return undoStack.lastOrNull()
  }

  fun redo(): String? {
    if (redoStack.isEmpty()) return null
    val next = redoStack.removeLast()
    undoStack.addLast(next)
    return next
  }
}