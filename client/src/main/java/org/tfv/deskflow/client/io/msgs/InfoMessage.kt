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
