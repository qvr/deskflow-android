

package org.tfv.deskflow.logging

import android.util.Log

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KLoggingEventBuilder
import io.github.oshai.kotlinlogging.Level
import io.github.oshai.kotlinlogging.Marker
import org.tfv.deskflow.BuildConfig
import org.tfv.deskflow.client.ClientEventBus
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class AndroidForwardingLogger(override val name: String) : KLogger {
    companion object {
        private val forwardingEnabledStorage = AtomicBoolean(true)

        var forwardingEnabled: Boolean
            get() = forwardingEnabledStorage.load()
            set(value)= forwardingEnabledStorage.store(value)

        fun setForwardingEnabled(enabled: Boolean): Boolean {
            return forwardingEnabledStorage.exchange(enabled)
        }

        private val forwardingLevelStorage = AtomicReference(if (BuildConfig.DEBUG) Level.TRACE else Level.INFO)
        var forwardingLevel: Level
            get() = forwardingLevelStorage.load()
            set(value) = forwardingLevelStorage.store(value)
    }

    internal fun forwardLogEvent(level: Level, tag: String,  message: String, cause: Throwable? = null, timestamp: Long = System.currentTimeMillis()) {
        if (!forwardingEnabled || level < forwardingLevel) {
            return
        }

        ClientEventBus.emit(LogRecordEvent(
            level = level,
            tag = tag,
            message = message,
            cause = cause,
            timestamp = timestamp
        ))

    }

    override fun at(level: Level, marker: Marker?, block: KLoggingEventBuilder.() -> Unit) {
        if (isLoggingEnabledFor(level, marker)) {
            KLoggingEventBuilder().apply(block).run {
                when (level) {
                    Level.TRACE -> Log.v(name, this.message, this.cause)
                    Level.DEBUG -> Log.d(name, this.message, this.cause)
                    Level.INFO -> Log.i(name, this.message, this.cause)
                    Level.WARN -> Log.w(name, this.message, this.cause)
                    Level.ERROR -> Log.e(name, this.message, this.cause)
                    Level.OFF -> Unit
                }

                forwardLogEvent(level, name, this.message ?: "<NULL>", this.cause)
            }
        }
    }

    override fun isLoggingEnabledFor(level: Level, marker: Marker?): Boolean {
        return when (level) {
            Level.TRACE -> Log.isLoggable(name, Log.VERBOSE)
            Level.DEBUG -> Log.isLoggable(name, Log.DEBUG)
            Level.INFO -> Log.isLoggable(name, Log.INFO)
            Level.WARN -> Log.isLoggable(name, Log.WARN)
            Level.ERROR -> Log.isLoggable(name, Log.ERROR)
            Level.OFF -> false
        }
    }
}
