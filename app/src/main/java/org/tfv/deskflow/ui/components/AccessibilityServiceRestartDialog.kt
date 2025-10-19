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

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.tfv.deskflow.R

/**
 * Dialog shown when the accessibility service has crashed or disconnected
 * but is still enabled in system settings. Prompts the user to restart the service.
 */
@Composable
fun AccessibilityServiceRestartDialog(
  onOpenSettings: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        stringResource(R.string.accessibility_restart_dialog_title),
        style = MaterialTheme.typography.headlineSmall,
      )
    },
    text = {
      Text(stringResource(R.string.accessibility_restart_dialog_message))
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.accessibility_restart_dialog_dismiss))
      }
    },
    confirmButton = {
      TextButton(onClick = {
        onOpenSettings()
        onDismiss()
      }) {
        Text(stringResource(R.string.accessibility_restart_dialog_restart))
      }
    },
  )
}
