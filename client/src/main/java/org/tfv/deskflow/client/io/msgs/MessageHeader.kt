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

import java.io.DataOutputStream
import java.io.IOException

/**
 * Describes a message header
 *
 * Size =
 * length of message type +
 * message data size (see Message.write)
 */
class MessageHeader(
    val type: MessageType
) {

    val typeLength: Int
        get() = type.value.length

    var dataSize: Int = 0

    fun writeData(outStream: DataOutputStream) {
        val totalSize = typeLength + dataSize
        require(totalSize >= 4) {
            "Message header size is < 4 bytes, which is the minimum size for a message header"
        }

        outStream.writeInt(totalSize)
        outStream.write(type.value.toByteArray(charset("UTF8")))
    }

    override fun toString(): String {
        return "MessageHeader:$typeLength:$dataSize:$type"
    }

    companion object {
        private const val MESSAGE_TYPE_SIZE = 4
    }
}
