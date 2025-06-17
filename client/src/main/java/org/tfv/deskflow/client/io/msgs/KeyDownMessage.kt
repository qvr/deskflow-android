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

class KeyDownMessage(
    var id: UInt = 0u,
    var mask: UInt = 0u,
    var button: UInt = 0u
) : Message(MessageType.DKEYDOWN) {


    override fun readData(inStream: DataInputStream, dataSize: Int) {
        id = inStream.readUnsignedShort().toUInt()
        mask = inStream.readUnsignedShort().toUInt()
        button = inStream.readUnsignedShort().toUInt()
    }

    override fun writeData(outStream: DataOutputStream) {
        outStream.writeShort(id.toInt())
        outStream.writeShort(mask.toInt())
        outStream.writeShort(button.toInt())
    }


    override fun toString(): String {
        return MESSAGE_TYPE.toString() + ":" + this.id + ":" + mask + ":" + button
    }

    companion object {
        val MESSAGE_TYPE: MessageType = MessageType.DKEYDOWN
    }
}
