/*
 * MIT License
 *
 * Copyright (c) 2025 Jonathan Glanz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.tfv.deskflow.client.cli

import org.tfv.deskflow.client.util.logging.KLoggingManager

import org.tfv.deskflow.client.Client
import org.tfv.deskflow.client.models.SERVER_DEFAULT_SCREEN_NAME
import org.tfv.deskflow.client.models.ServerTarget

import java.lang.Thread.sleep
import java.net.InetAddress

private val log = KLoggingManager.logger("org.tfv.deskflow.client.cli.Main")

fun main() {
    val serverTarget = ServerTarget(
        SERVER_DEFAULT_SCREEN_NAME,
        InetAddress.getLocalHost().hostName,
        24800,
        false,
        1920,
        1080
    )
    log.info { "Starting client: $serverTarget}" }

    val client = Client()
    client.setTarget(serverTarget)
    sleep(100)
    client.waitForSocket()
}