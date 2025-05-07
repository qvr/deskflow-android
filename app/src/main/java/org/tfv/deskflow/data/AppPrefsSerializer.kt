package org.tfv.deskflow.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.github.oshai.kotlinlogging.Level
import org.tfv.deskflow.client.models.SERVER_DEFAULT_ADDRESS
import org.tfv.deskflow.client.models.SERVER_DEFAULT_PORT
import org.tfv.deskflow.client.models.SERVER_DEFAULT_SCREEN_NAME
import org.tfv.deskflow.data.models.AppPrefs
import org.tfv.deskflow.data.models.AppPrefsKt.ScreenConfigKt.serverConfig
import org.tfv.deskflow.data.models.AppPrefsKt.loggingConfig
import org.tfv.deskflow.data.models.AppPrefsKt.screenConfig
import org.tfv.deskflow.data.models.copy
import java.io.InputStream
import java.io.OutputStream

object AppPrefsSerializer : Serializer<AppPrefs> {
    override val defaultValue: AppPrefs = AppPrefs.getDefaultInstance(
    ).copy {
        logging = loggingConfig {
            forwardingEnabled = true
            forwardingLevel = Level.INFO.name
        }

        screen = screenConfig {
            name = SERVER_DEFAULT_SCREEN_NAME
            server = serverConfig {
                address = SERVER_DEFAULT_ADDRESS
                port = SERVER_DEFAULT_PORT
                useTls = false
            }
        }
    }

    override suspend fun readFrom(input: InputStream): AppPrefs {
        try {
            return AppPrefs.parseFrom(input)
        } catch (e: Exception) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: AppPrefs, output: OutputStream) = t.writeTo(output)
}