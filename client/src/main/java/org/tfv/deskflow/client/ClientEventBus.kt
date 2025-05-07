package org.tfv.deskflow.client

import org.tfv.deskflow.client.events.ClientEvent
import org.tfv.deskflow.client.util.SimpleEventEmitter

object ClientEventBus : SimpleEventEmitter<ClientEvent>() {
}