package org.tfv.deskflow.logging

import io.github.oshai.kotlinlogging.Level
import org.tfv.deskflow.client.events.ClientEvent
import java.io.Serializable

data class LogRecordEvent(
    val level: Level,
    val tag: String,
    val message: String,
    val cause: Throwable? = null,
    val timestamp: Long = System.currentTimeMillis(),
) : ClientEvent(), Serializable
