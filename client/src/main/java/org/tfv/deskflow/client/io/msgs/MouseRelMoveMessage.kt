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

import java.io.DataInputStream
import java.io.DataOutputStream

class MouseRelMoveMessage(
    var x: Short = 0,
    var y: Short = 0
) : Message(MessageType.DMOUSERELMOVE) {

    override fun readData(inStream: DataInputStream, dataSize: Int) {
        x = inStream.readShort()
        y = inStream.readShort()

    }

    override fun writeData(outStream: DataOutputStream) {
        outStream.writeShort(x.toInt())
        outStream.writeShort(y.toInt())
    }


    override fun toString(): String {
        return "MouseRelMoveMessage:($x,$y)"
    }

    companion object {
        val MESSAGE_TYPE: MessageType = MessageType.DMOUSERELMOVE
    }
}
