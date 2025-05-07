@file:Suppress("FunctionName")

package org.tfv.deskflow.ext

import android.content.Context
import org.tfv.deskflow.client.models.SERVER_DEFAULT_ADDRESS
import org.tfv.deskflow.client.models.SERVER_DEFAULT_PORT
import org.tfv.deskflow.client.models.SERVER_DEFAULT_SCREEN_NAME
import org.tfv.deskflow.client.models.ServerTarget
import org.tfv.deskflow.data.aidl.ConnectionState
import org.tfv.deskflow.data.aidl.ScreenState
import org.tfv.deskflow.data.aidl.ServerState

fun ConnectionState.copy(
    isEnabled: Boolean = this.isEnabled,
    isConnected: Boolean = this.isConnected,
    ackReceived: Boolean = this.ackReceived,
    isCaptureKeyModeEnabled: Boolean = this.isCaptureKeyModeEnabled,
    screen: ScreenState = this.screen
): ConnectionState {
    return ConnectionState().apply {
        this.isEnabled = isEnabled
        this.isConnected = isConnected
        this.ackReceived = ackReceived
        this.isCaptureKeyModeEnabled = isCaptureKeyModeEnabled
        this.screen = screen
    }
}

fun ConnectionStateDefaults(context: Context): ConnectionState {
    val screenSize = context.getScreenSize()
    return ConnectionState().apply {
        isEnabled = true
        isConnected = false
        ackReceived = false
        isCaptureKeyModeEnabled = false
        screen = ScreenState().apply {
            name = SERVER_DEFAULT_SCREEN_NAME
            isActive = false
            width = screenSize.px.width
            height = screenSize.px.height
            server = ServerState().apply {
                address = SERVER_DEFAULT_ADDRESS
                port = SERVER_DEFAULT_PORT
                useTls = false
            }
        }
    }
}

fun ScreenState.copy(
    name: String = this.name,
    isActive: Boolean = this.isActive,
    server: ServerState = this.server,
    width: Int = this.width,
    height: Int = this.height
): ScreenState {
    return ScreenState().apply {
        this.name = name
        this.isActive = isActive
        this.server = server
        this.width = width
        this.height = height
    }
}

fun ScreenState.toServerTarget(): ServerTarget {
    return ServerTarget(
        name,
        server.address,
        server.port,
        server.useTls,
        width,
        height,
    )
}

fun ServerState.copy(
    address: String = this.address,
    port: Int = this.port,
    useTls: Boolean = this.useTls
): ServerState {
    return ServerState().apply {
        this.address = address
        this.port = port
        this.useTls = useTls
    }
}