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

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.tfv.deskflow.R
import org.tfv.deskflow.client.net.FingerprintManager
import org.tfv.deskflow.client.net.FingerprintVerificationResult

/**
 * Dialog to display and verify TLS certificate fingerprints
 */
@Composable
fun FingerprintVerificationDialog(
  result: FingerprintVerificationResult,
  clientCertificateFingerprint: String? = null,
  onAccept: () -> Unit,
  onReject: () -> Unit,
) {
  Dialog(
    onDismissRequest = onReject,
    properties = DialogProperties(
      dismissOnBackPress = true,
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false,
    ),
  ) {
    Card(
      modifier = Modifier
        .wrapContentWidth()
        .widthIn(max = 600.dp)
        .fillMaxHeight(0.9f),
      shape = MaterialTheme.shapes.large,
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Title
        when (result) {
          is FingerprintVerificationResult.Unknown -> {
            Text(
              text = stringResource(R.string.fingerprint_dialog_unknown_title),
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = stringResource(R.string.fingerprint_dialog_unknown_description),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          is FingerprintVerificationResult.Mismatch -> {
            Text(
              text = stringResource(R.string.fingerprint_dialog_mismatch_title),
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.error,
            )
            Text(
              text = stringResource(R.string.fingerprint_dialog_mismatch_description),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          else -> {}
        }

        val fingerprint = when (result) {
          is FingerprintVerificationResult.Unknown -> result.fingerprint
          is FingerprintVerificationResult.Mismatch -> result.fingerprint
          else -> ""
        }

        // Show both server and client fingerprints side by side
        Row(
          modifier = Modifier.wrapContentWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Server Fingerprint
          Card(
            modifier = Modifier
              .widthIn(max = 200.dp)
              .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = stringResource(R.string.fingerprint_dialog_server_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )

              // ASCII art representation
              SelectionContainer {
                Text(
                  text = FingerprintManager.generateFingerprintAsciiArt(fingerprint),
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                  ),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier
                    .widthIn(min = 180.dp)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(
                      color = MaterialTheme.colorScheme.surface,
                      shape = MaterialTheme.shapes.small,
                    )
                    .padding(8.dp),
                  softWrap = false,
                )
              }

              // Full fingerprint (selectable)
              SelectionContainer {
                Text(
                  text = fingerprint,
                  style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(
                      color = MaterialTheme.colorScheme.surface,
                      shape = MaterialTheme.shapes.small,
                    )
                    .padding(8.dp),
                  maxLines = 6,
                )
              }
            }
          }

          // Client Certificate Fingerprint (if available and client auth was requested)
          if (clientCertificateFingerprint != null &&
              ((result is FingerprintVerificationResult.Unknown && result.clientAuthRequested) ||
               (result is FingerprintVerificationResult.Mismatch && result.clientAuthRequested))) {
            Card(
              modifier = Modifier
                .widthIn(max = 200.dp)
                .padding(vertical = 8.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
              ),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = stringResource(R.string.fingerprint_dialog_client_title),
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                // ASCII art representation
                SelectionContainer {
                  Text(
                    text = FingerprintManager.generateFingerprintAsciiArt(clientCertificateFingerprint),
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                      .widthIn(min = 180.dp)
                      .horizontalScroll(rememberScrollState())
                      .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small,
                      )
                      .padding(8.dp),
                    softWrap = false,
                  )
                }

                // Full fingerprint (selectable)
                SelectionContainer {
                  Text(
                    text = clientCertificateFingerprint,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small,
                      )
                      .padding(8.dp),
                    maxLines = 6,
                  )
                }
              }
            }
          }
        }

        // Note about verifying both
        if (clientCertificateFingerprint != null &&
            ((result is FingerprintVerificationResult.Unknown && result.clientAuthRequested) ||
             (result is FingerprintVerificationResult.Mismatch && result.clientAuthRequested))) {
          Text(
            text = stringResource(R.string.fingerprint_dialog_verify_both),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        // Additional info
        if (result is FingerprintVerificationResult.Mismatch) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = stringResource(R.string.fingerprint_dialog_expected_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
              )
              SelectionContainer {
                Text(
                  text = result.expectedFingerprint,
                  style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                  color = MaterialTheme.colorScheme.onErrorContainer,
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(
                      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                      shape = MaterialTheme.shapes.small,
                    )
                    .padding(8.dp),
                  maxLines = 3,
                )
              }
            }
          }
        }

        // Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
          Button(
            onClick = onReject,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.errorContainer,
              contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
            shape = MaterialTheme.shapes.small,
          ) {
            Text(stringResource(R.string.fingerprint_dialog_button_reject))
          }
          Button(
            onClick = onAccept,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = MaterialTheme.shapes.small,
          ) {
            Text(stringResource(R.string.fingerprint_dialog_button_accept))
          }
        }
      }
    }
  }
}
