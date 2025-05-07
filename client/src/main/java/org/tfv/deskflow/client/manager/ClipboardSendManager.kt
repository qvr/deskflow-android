@file:OptIn(ExperimentalAtomicApi::class)

package org.tfv.deskflow.client.manager

import org.tfv.deskflow.client.io.msgs.ClipboardDataMessage
import org.tfv.deskflow.client.io.msgs.ClipboardMessage
import org.tfv.deskflow.client.models.ClipboardData
import org.tfv.deskflow.client.models.ClipboardDataMarker
import org.tfv.deskflow.client.util.logging.KLoggingManager
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

object ClipboardSendManager {
  const val MAX_CHUNK_SIZE = 512 * 1024
  private val log = KLoggingManager.logger("ClipboardSendManager")

  private val lock = Any()
  private var pendingClipboardDataMessages: List<ClipboardDataMessage>? = null

  private val sequenceNumberCounter = AtomicInt(0)

  fun receivedSequenceNumber(seqNum: Int) {
    synchronized(lock) {
      val seqNums = listOf(seqNum, sequenceNumberCounter.load())
      val maxSeqNum = seqNums.maxOrNull() ?: 0
      sequenceNumberCounter.store(maxSeqNum + 1)
    }
  }

  /**
   * If there are available `ClipboardDataMessage(s)`
   *
   * @return pending messages to be sent exist
   */
  fun availablePendingClipboardDataMessages(): Boolean {
    synchronized(lock) {
      return pendingClipboardDataMessages != null
    }
  }

  /**
   * Clear pending clipboard data messages
   */
  fun clearPendingClipboardDataMessages() {
    synchronized(lock) {
      pendingClipboardDataMessages = null
    }
  }

  /**
   * Pop any pending clipboard data messages
   *
   * NOTE: when this function is called, `pendingClipboardDataMessages` will be null
   *
   * @return if `pendingClipboardDataMessages` is non-null, then it is returned, otherwise null
   */
  fun popPendingClipboardDataMessages(): List<ClipboardDataMessage>? {
    synchronized(lock) {
      if (pendingClipboardDataMessages == null) return null
      val msgs = pendingClipboardDataMessages
      pendingClipboardDataMessages = null

      return msgs
    }
  }

  /**
   * Get any pending clipboard data messages
   *
   * @return if `pendingClipboardDataMessages` is non-null, then it is returned, otherwise null
   */
  fun getPendingClipboardDataMessages(): List<ClipboardDataMessage>? {
    synchronized(lock) {
      if (pendingClipboardDataMessages == null) return null
      val msgs = pendingClipboardDataMessages

      return msgs
    }
  }

  /**
   * Sends clipboard data to the server.
   *
   * This function constructs and sends a series of messages to transfer clipboard content.
   * It starts with a general `ClipboardMessage`, followed by a `ClipboardDataMessage`
   * indicating the start of data transfer and the total size of the clipboard content.
   * The actual clipboard data is then sent in chunks, each prefixed with a data marker.
   * Finally, a `ClipboardDataMessage` with an end marker is sent to signify the completion
   * of the transfer.
   *
   * @param messageHandler The handler responsible for sending messages to the server.
   * @param clipboardData The clipboard data to be sent.
   */
  fun sendClipboard(messageHandler: MessageHandler, clipboardData: ClipboardData) {
    val grabSequenceNumber = sequenceNumberCounter.addAndFetch(1)
    val sequenceNumber = sequenceNumberCounter.addAndFetch(1)
    val msgs1 = mutableListOf<ClipboardDataMessage>()
    val msgs2 = mutableListOf<ClipboardDataMessage>()
    val rawData = clipboardData.toByteArray()
    val rawDataSizeStr = rawData.size.toString(10)
    val rawDataSizeStrBytes = rawDataSizeStr.toByteArray(Charsets.UTF_8)
    val startDataByteStream = ByteArrayOutputStream()
    val startDataStream = DataOutputStream(startDataByteStream)
    startDataStream.writeByte(ClipboardDataMarker.Start.code)
    startDataStream.writeInt(rawDataSizeStrBytes.size)
    startDataStream.write(rawDataSizeStrBytes)

    val startData = startDataByteStream.toByteArray()
    msgs1.add(ClipboardDataMessage(0, sequenceNumber, startData))
    msgs2.add(ClipboardDataMessage(1, sequenceNumber, startData))

    val dataSize = rawData.size
    var dataWriteCount: Int = 0
    var dataChunkSize = MAX_CHUNK_SIZE
    while(dataWriteCount < dataSize) {
      if (dataWriteCount + dataChunkSize > dataSize) {
        dataChunkSize = dataSize - dataWriteCount
      }

      val dataChunkByteStream = ByteArrayOutputStream(dataChunkSize + 5)
      val dataChunkStream = DataOutputStream(dataChunkByteStream)
      dataChunkStream.writeByte(ClipboardDataMarker.Data.code)
      dataChunkStream.writeInt(dataChunkSize)
      dataChunkStream.write(rawData, dataWriteCount, dataChunkSize)
      //dataChunkStream.writeChar(0)
      val dataChunk = dataChunkByteStream.toByteArray()

      msgs1.add(ClipboardDataMessage(0, sequenceNumber, dataChunk))
      msgs2.add(ClipboardDataMessage(1, sequenceNumber, dataChunk))
      dataWriteCount += dataChunkSize
    }

    val endDataByteStream = ByteArrayOutputStream()
    val endDataStream = DataOutputStream(endDataByteStream)
    endDataStream.writeByte(ClipboardDataMarker.End.code)
    endDataStream.writeInt(0)
    val endData = endDataByteStream.toByteArray()

    msgs1.add(ClipboardDataMessage(0, sequenceNumber, endData))
    msgs2.add(ClipboardDataMessage(1, sequenceNumber, endData))
    synchronized(lock) {
      pendingClipboardDataMessages = msgs1 + msgs2
    }

    messageHandler.sendMessage(ClipboardMessage(0,grabSequenceNumber))
    messageHandler.sendMessage(ClipboardMessage(1,grabSequenceNumber))
  }

}