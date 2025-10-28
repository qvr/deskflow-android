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

package org.tfv.deskflow.client.net

import java.security.MessageDigest
import java.security.cert.X509Certificate

/**
 * Utility for computing and managing TLS certificate fingerprints
 */
object FingerprintManager {

  /**
   * Compute SHA-256 fingerprint of a certificate
   * @param certificate The X509 certificate
   * @return Hex string of SHA-256 digest
   */
  fun computeFingerprint(certificate: X509Certificate): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val fingerprint = digest.digest(certificate.encoded)
    return fingerprint.joinToString(":") { "%02X".format(it) }
  }

  /**
   * Generate ASCII art representation of fingerprint for visual verification
   * using the "randomart" algorithm compatible with Deskflow server.
   *
   * @param fingerprint SHA-256 fingerprint string (colon-separated hex)
   * @return ASCII art string representation
   */
  fun generateFingerprintAsciiArt(fingerprint: String): String {
    // Convert colon-separated hex string to byte array
    val rawDigest = fingerprint.split(":").map { it.toInt(16).toByte() }.toByteArray()

    // Field dimensions
    val baseSize = 8
    val rows = baseSize + 1  // 9 rows
    val columns = baseSize * 2 + 1  // 17 columns
    val characterPool = " .o+=*BOX@%&#/^SE"
    val len = characterPool.length - 1

    // Initialize field
    val field = Array(columns) { IntArray(rows) { 0 } }

    // Start position (center)
    var x = columns / 2
    var y = rows / 2

    // Process each byte of the digest
    for (byte in rawDigest) {
      // Each byte conveys four 2-bit move commands
      var b = byte.toInt() and 0xFF
      for (i in 0 until 4) {
        // Evaluate 2 bits for movement
        x += if ((b and 0x1) != 0) 1 else -1
        y += if ((b and 0x2) != 0) 1 else -1

        // Keep within bounds
        x = x.coerceIn(0, columns - 1)
        y = y.coerceIn(0, rows - 1)

        // Augment the field (avoid overflow)
        if (field[x][y] < len - 2) {
          field[x][y]++
        }

        // Shift to next 2 bits
        b = b shr 2
      }
    }

    // Mark starting point (S) and ending point (E)
    field[columns / 2][rows / 2] = len - 1  // S
    field[x][y] = len  // E

    // Build the output string
    val result = StringBuilder()
    result.append("+")
    result.append("-".repeat(columns))
    result.append("+\n")

    // Output field contents
    for (row in 0 until rows) {
      result.append("|")
      for (col in 0 until columns) {
        val charIndex = field[col][row].coerceAtMost(len)
        val char = characterPool[charIndex]
        // Replace regular spaces with non-breaking spaces to avoid android monospace issues
        result.append(if (char == ' ') '\u00A0' else char)
      }
      result.append("|\n")
    }

    // Footer
    result.append("+")
    result.append("-".repeat(columns))
    result.append("+")
    return result.toString()
  }
}
