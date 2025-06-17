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

import org.tfv.deskflow.client.util.logging.KLoggingManager

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/*public interface Message <T> {
	
	private Message <T>;
	public Message<T> create (MessageHeader header); 
	
}*/
/**
 * Writing:
 * Write to a ByteArrayOutputStream using a DataOutputStream
 * Create the header
 */
abstract class Message(
    val type: MessageType,
    val header: MessageHeader =  MessageHeader(type)
) {

    /**
     * Create a message with a message header
     */
    constructor(header: MessageHeader) : this(header.type, header)


    /**
     * Writes the message data to the byte array stream
     *
     */
    abstract fun writeData(outStream: DataOutputStream)

    abstract fun readData(inStream: DataInputStream, dataSize: Int)


    override fun toString(): String {
        return "${javaClass.simpleName}(type=$type)"
    }

    fun toBytes(): ByteArray {
        val outByteStream = ByteArrayOutputStream()
        val outStream = DataOutputStream(outByteStream)

        writeMessage(outStream, this)

        return outByteStream.toByteArray()
    }

    companion object {
        private val log = KLoggingManager.logger(Message::class.java.name)

        fun writeMessage(
            outStream: DataOutputStream,
            message: Message
        ) {
            // Write the message data to the byte array.
            //  Subclasses MUST override this function
            val msgByteStream = ByteArrayOutputStream()
            val msgStream = DataOutputStream(msgByteStream)
            message.writeData(msgStream)

            val msgBytes = msgByteStream.toByteArray()
            // Set the message header size based on how much data
            //  has been written to the byte array stream
            message.header.dataSize = msgBytes.size

            // Write out the header and the message data
            message.header.writeData(outStream)
            msgByteStream.writeTo(outStream)
            outStream.flush()

        }

    }
}
