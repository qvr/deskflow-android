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

import java.security.cert.X509Certificate

/**
 * Certificate fingerprint data extracted from a TLS connection.
 * The socket layer extracts this data and passes it to the app layer for verification.
 * The app layer is responsible for checking against the trust store and making decisions.
 */
data class CertificateFingerprint(
  val certificate: X509Certificate,
  val fingerprint: String,
)

/**
 * Verification result types for UI display.
 * These are created by the app layer after checking the TrustStore.
 */
sealed class FingerprintVerificationResult {
  /**
   * Fingerprint is unknown - first time seeing this server
   */
  data class Unknown(
    val certificate: X509Certificate,
    val fingerprint: String,
  ) : FingerprintVerificationResult()

  /**
   * Fingerprint mismatch - server certificate changed
   */
  data class Mismatch(
    val certificate: X509Certificate,
    val fingerprint: String,
    val expectedFingerprint: String,
  ) : FingerprintVerificationResult()
}

/**
 * Callback interface for certificate fingerprint verification.
 * The socket layer calls this to ask the app layer whether to accept the certificate.
 */
interface FingerprintVerificationCallback {
  /**
   * Called when a TLS connection is established and the certificate needs verification.
   * The implementation should check the fingerprint against a trust store and
   * optionally prompt the user.
   *
   * @param certificateFingerprint The certificate and its computed SHA-256 fingerprint
   * @param callback Function to invoke with the verification decision (true = accept, false = reject)
   */
  suspend fun onFingerprintChallenge(
    certificateFingerprint: CertificateFingerprint,
    callback: suspend (Boolean) -> Unit,
  )
}
