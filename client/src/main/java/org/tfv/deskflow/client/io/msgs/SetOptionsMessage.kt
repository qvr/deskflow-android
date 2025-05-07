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
