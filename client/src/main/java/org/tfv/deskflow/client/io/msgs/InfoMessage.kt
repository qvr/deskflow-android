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
package org.tfv.deskflow.client.io.msgs

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class InfoMessage(
    var screenX: Short= 0,
    var screenY: Short= 0,
    var screenWidth: Short= 0,
    var screenHeight: Short= 0,
    var unknown: Short = 0, // TODO: I haven't figured out what this is used for yet
    var cursorX: Short = 0,
    var cursorY: Short = 0,
) : Message(MESSAGE_TYPE) {

    override fun readData(inStream: DataInputStream, dataSize: Int) {

        screenX = inStream.readShort()
        screenY = inStream.readShort()
        screenWidth = inStream.readShort()
        screenHeight = inStream.readShort()
        unknown = inStream.readShort()
        cursorX = inStream.readShort()
        cursorY = inStream.readShort()

    }
    override fun writeData(outStream: DataOutputStream) {
        outStream.writeShort(screenX.toInt())
        outStream.writeShort(screenY.toInt())
        outStream.writeShort(screenWidth.toInt())
        outStream.writeShort(screenHeight.toInt())
        outStream.writeShort(unknown.toInt())
        outStream.writeShort(cursorX.toInt())
        outStream.writeShort(cursorY.toInt())
    }

    override fun toString(): String {
        return "InfoMessage:$screenX:$screenY:$screenWidth:$screenHeight:$unknown:$cursorX:$cursorY"
    }

    companion object {
        private val MESSAGE_TYPE = MessageType.DINFO
    }
}
