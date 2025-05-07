package org.tfv.deskflow.client.util.logging

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.tfv.deskflow.client.util.isAndroidRuntime
import kotlin.reflect.KClass

object KLoggingManager {

    private val loggerMap = mutableMapOf<String, KLogger>()
    private val forwardingLoggerMap = mutableMapOf<String, KLogger>()

    init {
        try {
            System.setProperty("kotlin-logging-to-android-native", "true")
        } catch (err: Throwable) {
        }
    }

    inline fun <reified T> logger() = logger(T::class)
    fun logger(clazz: KClass<*>) = logger(clazz.java.simpleName)
    fun logger(name: String): KLogger = loggerMap.getOrPut(name) { KotlinLogging.logger(name) }

    private val androidLoggerClazz: Class<*>? = if (isAndroidRuntime()) {
        try {
            Class.forName("org.tfv.deskflow.logging.AndroidForwardingLogger")
        } catch (e: ClassNotFoundException) {
            null // Android logger not available
        }
    } else null

    fun forwardingLogger(name: String): KLogger = forwardingLoggerMap.getOrPut(name) {
        when (androidLoggerClazz) {
            null -> {
                throw Error("Android logger not available, ensure you are running on Android or have the AndroidForwardingLogger class in your classpath.")
            }

            else -> androidLoggerClazz.getDeclaredConstructor(String::class.java)
                .newInstance(name) as KLogger
        }
    }

    fun forwardingLogger(clazz: KClass<*>) = forwardingLogger(clazz.java.simpleName)

    inline fun <reified T> forwardingLogger(): KLogger {
        return forwardingLogger(T::class)
    }
}

