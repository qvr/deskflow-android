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

package org.tfv.deskflow.ui.components

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.tfv.deskflow.R

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
  error("No SnackbarHostState provided")
}

fun showSnackbar(
  snackbarHostState: SnackbarHostState,
  scope: CoroutineScope,
  context: Context,
  @StringRes message: Int,
  onAction: ((SnackbarResult?) -> Unit)? = null
) {
  scope.launch {
    val result = snackbarHostState?.showSnackbar(
      message = context.resources.getString(message)
    )
    
    if (onAction != null) {
      onAction(result)
    }
  }
}

@Composable
fun showSnackbar(@StringRes message: Int, onAction: ((SnackbarResult?) -> Unit)? = null) {
  val snackbarHostState = LocalSnackbarHostState.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  
  LaunchedEffect(message) {
    showSnackbar(
      snackbarHostState = snackbarHostState,
      scope = scope,
      context = context,
      message = message,
      onAction = onAction
    )
  }
}
