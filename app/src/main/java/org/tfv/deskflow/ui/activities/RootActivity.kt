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

package org.tfv.deskflow.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.util.trace
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.tfv.deskflow.client.util.logging.KLoggingManager
import org.tfv.deskflow.ext.isSystemInDarkTheme
import org.tfv.deskflow.services.ConnectionServiceClient
import org.tfv.deskflow.ui.components.rememberAppState
import org.tfv.deskflow.ui.screens.RootScreenRoute

private val log = KLoggingManager.logger(RootActivity::class.java.simpleName)

class RootActivity : ComponentActivity() {

  private lateinit var serviceClient: ConnectionServiceClient

  override fun onStop() {
    super.onStop()
    serviceClient.unbind()
  }

  override fun onStart() {
    super.onStart()
    serviceClient.bind()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    serviceClient = ConnectionServiceClient(this)

    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        isSystemInDarkTheme().distinctUntilChanged().collect { darkTheme ->
          trace("rootEdgeToEdge") {
            // Turn off the decor fitting system windows, which allows us to
            // handle insets,
            // including IME animations, and go edge-to-edge.
            // This is the same parameters as the default enableEdgeToEdge call,
            // but we manually
            // resolve whether or not to show dark theme using uiState, since it
            // can be different
            // than the configuration's dark theme value based on the user
            // preference.
            enableEdgeToEdge(
              statusBarStyle =
                SystemBarStyle.auto(
                  lightScrim = android.graphics.Color.TRANSPARENT,
                  darkScrim = android.graphics.Color.TRANSPARENT,
                ) {
                  darkTheme
                },
              navigationBarStyle =
                SystemBarStyle.auto(
                  lightScrim = android.graphics.Color.TRANSPARENT,
                  darkScrim = android.graphics.Color.TRANSPARENT,
                ) {
                  darkTheme
                },
            )
          }
        }
      }
    }

    setContent {
      val appState = rememberAppState(serviceClient)
      RootScreenRoute(appState = appState)
    }
  }
}
