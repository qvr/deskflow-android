package org.tfv.deskflow.client.models

const val SERVER_DEFAULT_SCREEN_NAME = "Deskflow-Android"
const val SERVER_DEFAULT_ADDRESS = "0.0.0.0"
const val SERVER_DEFAULT_PORT = 24800

data class ServerTarget(
    val screenName: String = SERVER_DEFAULT_SCREEN_NAME,
    val address: String= SERVER_DEFAULT_ADDRESS,
    val port: Int = SERVER_DEFAULT_PORT,
    val useTls: Boolean = false,
    val width: Int = 0,
    val height: Int = 0
) {
}

