package org.tfv.deskflow.client.events

open class EventEmitter<Type, Payload> {
    private val listeners = mutableMapOf<Type, MutableList<(Payload?) -> Unit>>()

    /** Register a listener for [event]. */
    fun on(event: Type, listener: (payload: Payload?) -> Unit) {
        listeners.getOrPut(event) { mutableListOf() }.add(listener)
    }

    /** Register a one-time listener for [event]. */
    fun once(event: Type, listener: (payload: Payload?) -> Unit) {
        val wrapper = createOnceWrapper(event, listener)
        on(event, wrapper)
    }

    private fun createOnceWrapper(event: Type, listener: (payload: Payload?) -> Unit): (payload: Payload?) -> Unit {
        var wrapper: ((payload: Payload?) -> Unit)? = null
        wrapper = { payload ->
            listener(payload)
            off(event, wrapper!!)
        }

        return wrapper
    }

    /** Remove a specific [listener] for [event]. */
    fun off(event: Type, listener: (payload: Payload?) -> Unit) {
        listeners[event]?.run {
            remove(listener)
            if (isEmpty()) listeners.remove(event)
        }
    }

    /** Emit [event], passing along an optional [payload]. */
    fun emit(event: Type, payload: Payload? = null) {
        listeners[event]?.toList()?.forEach { it(payload) }
    }

    /** Remove all listeners for [event], or all events if [event] is null. */
    fun clear(event: Type? = null) {
        if (event != null) listeners.remove(event)
        else listeners.clear()
    }
}
