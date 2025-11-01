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
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.tfv.deskflow.client.util.logging.KLoggingManager
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.Date
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.X509KeyManager
import javax.security.auth.x500.X500Principal

private val log = KLoggingManager.logger("ClientCertificateManager")

/**
 * Manages client TLS certificates stored in the Android Keystore.
 * Generates and stores a self-signed certificate for TLS client authentication.
 */
class ClientCertificateManager(private val context: Context) {

  companion object {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "deskflow_client_cert"
    private const val CERTIFICATE_VALIDITY_YEARS = 10
    private const val KEY_SIZE = 2048
  }

  /**
   * Check if a client certificate already exists in the keystore
   */
  fun certificateExists(): Boolean {
    return try {
      val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
      keyStore.load(null)
      keyStore.containsAlias(KEY_ALIAS)
    } catch (e: Exception) {
      log.error(e) { "Error checking if certificate exists" }
      false
    }
  }

  /**
   * Generate a new self-signed client certificate and store it in the Android Keystore
   */
  fun generateCertificate() {
    try {
      log.info { "Generating new client certificate" }

      // Delete existing certificate if present
      if (certificateExists()) {
        deleteCertificate()
      }

      // Generate key pair using Android Keystore
      val startDate = Date()
      val endDate = Date(startDate.time + (CERTIFICATE_VALIDITY_YEARS * 365L * 24 * 60 * 60 * 1000))

      val keyPairGenerator = KeyPairGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_RSA,
        KEYSTORE_PROVIDER
      )

      val parameterSpec = KeyGenParameterSpec.Builder(
        KEY_ALIAS,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
      ).apply {
        setKeySize(KEY_SIZE)
        setDigests(
          KeyProperties.DIGEST_NONE,
          KeyProperties.DIGEST_SHA256,
          KeyProperties.DIGEST_SHA512
        )
        setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
        // Allow key to be used without user authentication
        setUserAuthenticationRequired(false)
        setCertificateSubject(X500Principal("CN=Deskflow Android Client"))
        setCertificateSerialNumber(BigInteger.valueOf(System.currentTimeMillis()))
        setCertificateNotBefore(startDate)
        setCertificateNotAfter(endDate)
      }.build()

      keyPairGenerator.initialize(parameterSpec)
      keyPairGenerator.generateKeyPair()

      log.info { "Client certificate generated successfully" }
    } catch (e: Exception) {
      log.error(e) { "Error generating client certificate" }
      throw e
    }
  }

  /**
   * Delete the client certificate from the keystore
   */
  fun deleteCertificate() {
    try {
      val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
      keyStore.load(null)
      keyStore.deleteEntry(KEY_ALIAS)
      log.info { "Client certificate deleted" }
    } catch (e: Exception) {
      log.error(e) { "Error deleting client certificate" }
    }
  }

  /**
   * Get a KeyManager that can be used with SSLContext for client authentication
   */
  fun getKeyManager(): KeyManager? {
    return try {
      if (!certificateExists()) {
        log.warn { "No client certificate found" }
        return null
      }

      val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
      keyStore.load(null)

      val keyManagerFactory = KeyManagerFactory.getInstance(
        KeyManagerFactory.getDefaultAlgorithm()
      )
      keyManagerFactory.init(keyStore, null)

      keyManagerFactory.keyManagers?.firstOrNull()
    } catch (e: Exception) {
      log.error(e) { "Error getting KeyManager" }
      null
    }
  }

  /**
   * Get the client certificate if it exists
   */
  fun getCertificate(): X509Certificate? {
    return try {
      if (!certificateExists()) {
        return null
      }

      val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
      keyStore.load(null)
      keyStore.getCertificate(KEY_ALIAS) as? X509Certificate
    } catch (e: Exception) {
      log.error(e) { "Error getting certificate" }
      null
    }
  }

  /**
   * Ensure a certificate exists, generating one if necessary
   */
  fun ensureCertificateExists() {
    if (!certificateExists()) {
      log.info { "No client certificate found, generating one" }
      generateCertificate()
    } else {
      log.info { "Client certificate already exists" }
    }
  }
}
