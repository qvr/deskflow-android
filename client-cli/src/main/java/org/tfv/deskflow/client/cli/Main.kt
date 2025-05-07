package org.tfv.deskflow.client.cli

import org.tfv.deskflow.client.util.logging.KLoggingManager

import org.tfv.deskflow.client.Client
import org.tfv.deskflow.client.models.ServerTarget

import java.lang.Thread.sleep
import java.net.InetAddress

private val log = KLoggingManager.logger("org.tfv.deskflow.client.cli.Main")

fun main() {
    val serverTarget = ServerTarget(
        "Deskflow-KM-CLI",
        InetAddress.getLocalHost().hostName,
        24800,
        1920,
        1080
    )
    log.info { "Starting client: $serverTarget}" }

    val client = Client()
    client.setTarget(serverTarget)
    sleep(100)
    client.waitForSocket()
}