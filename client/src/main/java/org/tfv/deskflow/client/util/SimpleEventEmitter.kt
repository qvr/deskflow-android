package org.tfv.deskflow.client.util

open class SimpleEventEmitter<Payload> : ISimpleEventEmitter<Payload> {
    private val listeners = mutableListOf<(Payload) -> Unit>()

    /** Register a listener for [event]. */
    override fun on(listener: (payload: Payload) -> Unit) {
        listeners.add(listener)
    }

    /** Register a one-time listener for [event]. */
    override fun once(listener: (payload: Payload) -> Unit) {
        val wrapper = createOnceWrapper(listener)
        on(wrapper)
    }

    private fun createOnceWrapper(listener: (payload: Payload) -> Unit): (payload: Payload) -> Unit {
        var wrapper: ((payload: Payload) -> Unit)? = null
        wrapper = { payload ->
            listener(payload)
            off(wrapper!!)
        }

        return wrapper
    }

    /** Remove a specific [listener] for [event]. */
    override fun off(listener: (payload: Payload) -> Unit) {
        listeners.run {
            remove(listener)
        }
    }

    /** Emit passing along an optional [payload]. */
    override fun emit(payload: Payload) {
        listeners.forEach { it(payload) }
    }

    /** Remove all listeners for [event], or all events if [event] is null. */
    override fun clear() {
        listeners.clear()
    }
}
