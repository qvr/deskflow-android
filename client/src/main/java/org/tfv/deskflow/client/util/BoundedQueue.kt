package org.tfv.deskflow.client.util

class BoundedDeque<T>(val maxSize: Int) {
    private val buffer = ArrayDeque<T>(maxSize)

    /**
     * Adds `element` to the end of the deque. If the buffer is already at capacity,
     * it first removes the oldest element (at the front).
     */
    fun add(element: T) {
        if (buffer.size >= maxSize) {
            buffer.removeFirst()
        }
        buffer.addLast(element)
    }

    /** Returns the elements in insertion order (oldest first). */
    fun toList(): List<T> = buffer.toList()

    /** Other read‐only helpers, if you like: */
    val size: Int get() = buffer.size
    fun isEmpty() = buffer.isEmpty()
    fun peekFirst(): T? = buffer.firstOrNull()
    fun peekLast(): T? = buffer.lastOrNull()
}
