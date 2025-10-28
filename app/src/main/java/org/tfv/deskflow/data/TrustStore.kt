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

package org.tfv.deskflow.data

import android.content.Context
import kotlinx.coroutines.flow.first
import org.tfv.deskflow.data.models.AppPrefsKt
import org.tfv.deskflow.data.models.copy
import org.tfv.deskflow.client.util.logging.KLoggingManager

private val log = KLoggingManager.logger("TrustStore")

/**
 * Manages trusted TLS certificate fingerprints
 */
class TrustStore(private val context: Context) {

  /**
   * Get the host key for a server (used for fingerprint storage)
   * @param host Server hostname
   * @param port Server port
   * @return "host:port" format
   */
  private fun getHostKey(host: String, port: Int): String = "$host:$port"

  /**
   * Get the trusted fingerprint for a server
   * @param host Server hostname
   * @param port Server port
   * @return The trusted fingerprint, or null if not found
   */
  suspend fun getTrustedFingerprint(host: String, port: Int): String? {
    val hostKey = getHostKey(host, port)
    val prefs = context.appPrefsStore.data.first()
    return prefs.trust.trustedFingerprintsMap[hostKey]?.fingerprintSha256
  }

  /**
   * Store a fingerprint as trusted
   * @param host Server hostname
   * @param port Server port
   * @param fingerprint The fingerprint to trust
   */
  suspend fun trustFingerprint(host: String, port: Int, fingerprint: String) {
    val hostKey = getHostKey(host, port)
    context.appPrefsStore.updateData { prefs ->
      prefs
        .toBuilder()
        .apply {
          trustBuilder.putTrustedFingerprints(
            hostKey,
            AppPrefsKt.TrustConfigKt.trustedFingerprint {
              this.host = hostKey
              this.fingerprintSha256 = fingerprint
              this.lastVerifiedTime = System.currentTimeMillis()
            },
          )
        }
        .build()
    }
    log.info { "Trusted fingerprint for $hostKey" }
  }

  /**
   * Revoke trust for a fingerprint
   * @param host Server hostname
   * @param port Server port
   */
  suspend fun revokeFingerprint(host: String, port: Int) {
    val hostKey = getHostKey(host, port)
    context.appPrefsStore.updateData { prefs ->
      prefs
        .toBuilder()
        .apply {
          trustBuilder.removeTrustedFingerprints(hostKey)
        }
        .build()
    }
    log.info { "Revoked fingerprint trust for $hostKey" }
  }

  /**
   * Clear all trusted fingerprints
   */
  suspend fun clearAllFingerprints() {
    context.appPrefsStore.updateData { prefs ->
      prefs.toBuilder().apply { trustBuilder.clearTrustedFingerprints() }.build()
    }
    log.info { "Cleared all trusted fingerprints" }
  }
}
