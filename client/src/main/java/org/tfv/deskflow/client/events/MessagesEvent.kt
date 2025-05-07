package org.tfv.deskflow.client.events

import org.tfv.deskflow.client.io.msgs.Message

data class MessagesEvent(val messages: List<Message>) : ClientEvent() {

//    inline fun <reified T : Message> asType(): T? {
//        if (message !is T) {
//            MessagesEventLogger.error { "Message is not of type ${T::class.java.simpleName}" }
//            return null
//        }
//        return message
//    }

//    companion object {
//        val MessagesEventLogger = KLoggingManager.logger(MessagesEvent::class.java.simpleName)
//    }
}