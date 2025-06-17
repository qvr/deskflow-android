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
import kotlin.collections.ArrayList

class SetOptionsMessage(
    var options: ArrayList<Pair<UInt,UInt>> = ArrayList()
) : Message(MessageType.DSETOPTIONS) {



    override fun readData(inStream: DataInputStream, dataSize: Int) {
        val options = ArrayList<Pair<UInt,UInt>>()
        var dataLeft = dataSize
        while (dataLeft >= kOptSize) {
            val key = inStream.readInt().toUInt()
            val value = inStream.readInt().toUInt()
            options.add(Pair(key, value))

            dataLeft -= kOptSize
        }

        this.options = options
    }

    override fun writeData(outStream: DataOutputStream) {
        for ((key,value) in options) {
            try {
                outStream.writeInt(key.toInt())
                outStream.writeInt(value.toInt())
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    override fun toString(): String {
        return "SetOptionsMessage:"
    }

    companion object {
        val MESSAGE_TYPE: MessageType = MessageType.DSETOPTIONS
        val kOptSize: Int = Int.SIZE_BYTES * 2
    }
}
