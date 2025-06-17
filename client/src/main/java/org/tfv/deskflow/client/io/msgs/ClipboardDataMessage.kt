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
