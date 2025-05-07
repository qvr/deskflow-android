package org.tfv.deskflow.components

import android.content.Context
import androidx.annotation.RawRes
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import org.tfv.deskflow.client.util.logging.KLoggingManager
import java.io.File

class FileManager(
  val context: Context
) {
  companion object {
    private val log = KLoggingManager.logger(this::class)
  }

  val ioScope = CoroutineScope(Dispatchers.IO)

  fun toAbsolutePath(filename: String): String {
    val file = File(filename)
    if (file.isAbsolute || file.exists())
      return file.absolutePath

    return File(context.filesDir, filename).absolutePath
  }

  fun toAbsoluteFile(filename: String): File {
    val path = toAbsolutePath(filename)
    return File(path)
  }

  inline fun <reified K, reified V> writeJson(filename: String, map: Map<K,V>): File {
    val json = Json.encodeToString(map) // This only works for Map<String, String> or similar
    val file = toAbsoluteFile(filename)
    file.writeText(json)
    return file
  }

  fun writeText(filename: String, content: String): File {
    try {
      val file = toAbsoluteFile(filename)
      file.writeText(content)
      log.debug {"File written successfully: ${file.absolutePath}"}
      return file
    } catch (err: Exception) {
      log.error(err) {"Error writing file: ${err.message}"}
      throw err
    }
  }
  inline fun <reified K, reified V> writeJsonJob(filename: String, map: Map<K,V>): Job {
    return ioScope.launch {
      writeJson(filename, map)
    }
  }
  fun writeTextJob(filename: String, content: String): Job {
    return ioScope.launch {
      writeText(filename, content)
    }
  }

  inline fun <reified T: JsonElement> readJsonFromRawResource(@RawRes resId: Int): T {
    val text = context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
    return Json.parseToJsonElement(text) as T
  }

}