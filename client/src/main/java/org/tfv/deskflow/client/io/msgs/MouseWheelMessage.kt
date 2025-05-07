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

class MouseWheelMessage(
    var xDelta: Short = 0,
    var yDelta: Short = 0
) : Message(MESSAGE_TYPE) {

    override fun readData(din: DataInputStream, dataSize: Int) {
        xDelta = din.readShort()
        yDelta = din.readShort()
    }

    override fun writeData(outStream: DataOutputStream) {
        outStream.writeShort(xDelta.toInt())
        outStream.writeShort(yDelta.toInt())
    }

    fun getXDelta(): Int {
        return xDelta.toInt()
    }

    fun getYDelta(): Int {
        return yDelta.toInt()
    }

    override fun toString(): String {
        return "MouseWheelMessage:($xDelta,$yDelta)"
    }

    companion object {
        val MESSAGE_TYPE: MessageType = MessageType.DMOUSEWHEEL
    }
}
