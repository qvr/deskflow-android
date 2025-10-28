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

package org.tfv.deskflow.ui.models

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.tfv.deskflow.client.net.FingerprintVerificationResult

/**
 * Manages UI state for fingerprint verification dialogs
 */
class FingerprintVerificationState {
  private val _pendingVerification = MutableStateFlow<FingerprintVerificationResult?>(null)
  val pendingVerification: StateFlow<FingerprintVerificationResult?> = _pendingVerification

  private var _currentCallback: (suspend (Boolean) -> Unit)? = null

  /**
   * Post a fingerprint verification challenge to the UI
   * @param result The verification result to display
   * @param callback The callback to invoke with user's decision
   */
  suspend fun postChallenge(
    result: FingerprintVerificationResult,
    callback: suspend (Boolean) -> Unit,
  ) {
    _pendingVerification.emit(result)
    _currentCallback = callback
  }

  /**
   * Handle user accepting the fingerprint
   */
  suspend fun acceptFingerprint() {
    _currentCallback?.invoke(true)
    _pendingVerification.emit(null)
    _currentCallback = null
  }

  /**
   * Handle user rejecting the fingerprint
   * This will disable the connection to prevent auto-reconnect loops
   */
  suspend fun rejectFingerprint() {
    _currentCallback?.invoke(false)
    _pendingVerification.emit(null)
    _currentCallback = null
    // Signal that connection should be disabled
    connectionDisabledDueToRejection = true
  }

  /**
   * Clear any pending verification and reset rejection flag
   */
  fun clear() {
    _pendingVerification.tryEmit(null)
    _currentCallback = null
    connectionDisabledDueToRejection = false
  }

  /**
   * Check if connection was disabled due to fingerprint rejection
   */
  fun isConnectionDisabledDueToRejection(): Boolean = connectionDisabledDueToRejection

  private var connectionDisabledDueToRejection = false

  companion object {
    private val instance = FingerprintVerificationState()

    fun getInstance(): FingerprintVerificationState = instance
  }
}
