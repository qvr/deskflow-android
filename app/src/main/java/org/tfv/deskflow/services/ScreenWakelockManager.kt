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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY IS AN KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.tfv.deskflow.services

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import org.tfv.deskflow.client.util.logging.KLoggingManager

/**
 * Manages screen wakelock acquisition with automatic release to keep the device
 * screen on during mouse movement. The wakelock is acquired on mouse movement
 * and automatically released 5 seconds after acquisition by Android.
 *
 * The wakelock uses the following flags:
 * - FULL_WAKE_LOCK: Keeps both CPU and screen on
 * - ACQUIRE_CAUSES_WAKEUP: Immediately wakes up the device
 * - ON_AFTER_RELEASE: Pokes Android's user activity timer so the screen stays on
 *
 * @param context The application context used to get the PowerManager service
 * @param autoReleaseTimeoutMs The timeout after which Android automatically releases the wakelock (default: 5000ms)
 */
@SuppressLint("WakelockTimeout")
class ScreenWakelockManager(
  private val context: Context,
  private val autoReleaseTimeoutMs: Long = 5000L,
) {

  private val powerManager = context.getSystemService(PowerManager::class.java)
  private var wakelock: PowerManager.WakeLock? = null

  private val log = KLoggingManager.logger(ScreenWakelockManager::class.java.simpleName)

  /**
   * Called when mouse movement is detected. This will acquire the wakelock, which will be
   * automatically released by Android after [autoReleaseTimeoutMs] milliseconds.
   */
  fun onMouseMovement() {
    try {
      // Create wakelock if not already created
      if (wakelock == null) {
        val lockLevel = PowerManager.FULL_WAKE_LOCK or
          PowerManager.ACQUIRE_CAUSES_WAKEUP or
          PowerManager.ON_AFTER_RELEASE
        wakelock = powerManager.newWakeLock(lockLevel, "deskflow:mouse_activity")
      }

      // Only acquire if not already held
      if (wakelock?.isHeld != true) {
        wakelock?.acquire(autoReleaseTimeoutMs)
        log.debug { "Wakelock acquired with ${autoReleaseTimeoutMs}ms timeout" }
      }
    } catch (e: Exception) {
      log.error(e) { "Failed to acquire wakelock: ${e.message}" }
    }
  }

  /**
   * Clean up resources. Should be called when the service is being destroyed.
   */
  fun cleanup() {
    try {
      if (wakelock?.isHeld == true) {
        wakelock?.release()
        log.debug { "Wakelock released" }
      }
    } catch (e: Exception) {
      log.error(e) { "Error during cleanup: ${e.message}" }
    }
  }

  /**
   * Returns true if the wakelock is currently held.
   */
  fun isWakelockHeld(): Boolean = wakelock?.isHeld == true
}
