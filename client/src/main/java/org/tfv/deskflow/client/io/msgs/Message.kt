/*
 * deskflow -- mouse and keyboard sharing utility
 * Copyright (C) 2010 Shaun Patterson
 * Copyright (C) 2010 The Deskflow Project
 * Copyright (C) 2009 The Deskflow+ Project
 * Copyright (C) 2002 Chris Schoeneman
 * 
 * This package is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * found in the file COPYING that should have accompanied this file.
 * 
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
