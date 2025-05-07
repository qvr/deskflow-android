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

import org.tfv.deskflow.client.io.readString
import org.tfv.deskflow.client.io.writeString
import java.io.DataInputStream
import java.io.DataOutputStream

class ClipboardMessage(
    var id: Byte = 0,
    var sequenceNumber: Int = 0
) : Message(MESSAGE_TYPE) {


    override fun writeData(outStream: DataOutputStream) {
        outStream.writeByte(id.toInt())
        outStream.writeInt(sequenceNumber)
    }

    override fun readData(inStream: DataInputStream, dataSize: Int) {
        id = inStream.readByte()
        sequenceNumber = inStream.readInt()
    }


    override fun toString(): String {
        return "ClipboardMessage: TODO"
    }

    companion object {
        val MESSAGE_TYPE: MessageType = MessageType.CCLIPBOARD
    }
}
