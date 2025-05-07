package org.tfv.deskflow.client.models

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.Serializable
import org.tfv.deskflow.client.util.logging.KLoggingManager

data class ClipboardData(
  val id: Int,
  val sequenceNumber: Int = 0,
  val variants: Map<Format, Variant> = emptyMap(),
) : Serializable {
  companion object {
    const val VARIANT_BASE_SIZE = 4 /* format code */ + 4 /* data size */

    private val log = KLoggingManager.logger<ClipboardData>()
  }

  enum class Format(val code: Int) {
    Text(0),
    Html(1),
    Bitmap(2),
  }

  data class Variant(val format: Format, val data: ByteArray = byteArrayOf()) :
    Serializable {
    val size: Int
      get() = data.size

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as Variant

      if (format != other.format) return false
      if (!data.contentEquals(other.data)) return false

      return true
    }

    override fun hashCode(): Int {
      var result = format.hashCode()
      result = 31 * result + data.contentHashCode()
      return result
    }

    fun toByteArray(): ByteArray {
      val byteStream = ByteArrayOutputStream(data.size + VARIANT_BASE_SIZE)
      val dataStream = DataOutputStream(byteStream)
      dataStream.writeInt(format.code)
      dataStream.writeInt(data.size)
      dataStream.write(data)
      return byteStream.toByteArray()
    }
  }

  fun toByteArray(): ByteArray {
    val variantData =
      variants.values.flatMap { it.toByteArray().toList() }.toByteArray()
    val dataSize = 4 /* format count */ + variantData.size
    val dataByteStream = ByteArrayOutputStream(dataSize)
    val dataStream = DataOutputStream(dataByteStream)
    dataStream.writeInt(variants.size)
    dataStream.write(variantData)

    return dataByteStream.toByteArray()
  }
}
