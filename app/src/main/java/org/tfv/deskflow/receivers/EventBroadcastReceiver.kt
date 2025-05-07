package org.tfv.deskflow.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.tfv.deskflow.client.ClientEventBus
import org.tfv.deskflow.client.events.KeyboardEvent
import org.tfv.deskflow.client.events.MouseEvent
import org.tfv.deskflow.client.util.logging.KLoggingManager

class EventBroadcastReceiver() : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_KEYBOARD -> {
                ClientEventBus.emit(
                    intent.getSerializableExtra("payload", KeyboardEvent::class.java)!!
                )
            }
            ACTION_MOUSE -> {
                ClientEventBus.emit(
                    intent.getSerializableExtra("payload", MouseEvent::class.java)!!
                )
            }
            else -> {
                log.warn { "Unsupported action ${intent.action}" }
            }
        }


    }

    companion object {
        const val ACTION_KEYBOARD = "org.tfv.deskflow.KEYBOARD_ACTION"
        const val ACTION_MOUSE = "org.tfv.deskflow.MOUSE_ACTION"


        private val log = KLoggingManager.logger(EventBroadcastReceiver::class.java.simpleName)

    }
}