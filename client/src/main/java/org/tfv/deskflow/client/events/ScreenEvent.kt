package org.tfv.deskflow.client.events

import org.tfv.deskflow.client.models.ClipboardData
import java.io.Serializable
import org.tfv.deskflow.client.util.logging.KLoggingManager

/** @author Shaun Patterson */
sealed class ScreenEvent : ClientEvent(), Serializable {

  data object AckReceived : ScreenEvent()

  data object Enter : ScreenEvent()

  data object Leave : ScreenEvent()

  data class SetClipboard(val data: ClipboardData) : ScreenEvent(), Serializable

  companion object {
    private val log = KLoggingManager.logger(ScreenEvent::class.java.simpleName)
  }
}
