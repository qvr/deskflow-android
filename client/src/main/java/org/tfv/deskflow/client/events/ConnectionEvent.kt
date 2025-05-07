package org.tfv.deskflow.client.events

sealed class ConnectionEvent : ClientEvent() {
    data object Connected : ConnectionEvent()
    data object Disconnected : ConnectionEvent()

}