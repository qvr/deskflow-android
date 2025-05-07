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
import org.tfv.deskflow.client.models.ClipboardDataMarker

class ClipboardDataMessage(
  var id: Byte = 0,
  var sequenceNumber: Int = 0,
  var data: ByteArray = byteArrayOf(),
) : Message(MESSAGE_TYPE) {

  val marker: ClipboardDataMarker
    get() =
      when {
        data.isEmpty() -> ClipboardDataMarker.Unknown
        else ->
          data[0].let { markerByte ->
            ClipboardDataMarker.entries.find { it.code == markerByte.toInt() }
              ?: ClipboardDataMarker.Unknown
          }
      }

  override fun writeData(outStream: DataOutputStream) {
    outStream.writeByte(id.toInt())
    outStream.writeInt(sequenceNumber)
    outStream.write(data)
  }

  override fun readData(inStream: DataInputStream, dataSize: Int) {
    id = inStream.readByte()
    sequenceNumber = inStream.readInt()
    data = ByteArray(dataSize - 5)
    inStream.readFully(data)
  }

  override fun toString(): String {
    return "ClipboardDataMessage:$id:$sequenceNumber:$data"
  }

  companion object {
    val MESSAGE_TYPE: MessageType = MessageType.DCLIPBOARD
  }
}
