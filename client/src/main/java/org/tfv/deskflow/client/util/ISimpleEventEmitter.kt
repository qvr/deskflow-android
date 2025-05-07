package org.tfv.deskflow.client.util

interface ISimpleEventEmitter<Payload> {
    /** Register a listener for [event]. */
    fun on(listener: (payload: Payload) -> Unit)

    /** Register a one-time listener for [event]. */
    fun once(listener: (payload: Payload) -> Unit)

    /** Remove a specific [listener] for [event]. */
    fun off(listener: (payload: Payload) -> Unit)

    /** Emit passing along an optional [payload]. */
    fun emit(payload: Payload)

    /** Remove all listeners for [event], or all events if [event] is null. */
    fun clear()
}