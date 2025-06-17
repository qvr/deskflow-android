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