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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.tfv.deskflow.R
import org.tfv.deskflow.ui.theme.LocalDeskflowExtendedColorScheme

@Composable
fun AppToolbar(appState: IAppState = LocalAppState.current) {
  val connectionState by appState.connectionStateFlow.collectAsStateWithLifecycle()
  val isConnected = connectionState.isConnected
  val (isPortrait, isXL, isLarge) = currentDeviceConfig()
  val extColorScheme = LocalDeskflowExtendedColorScheme.current
  Row(
    modifier =
      Modifier.background(extColorScheme.toolbar)
        .fillMaxWidth()
        .height(72.dp)
  ) {
    CompositionLocalProvider(
      //LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer
      LocalContentColor provides extColorScheme.onToolbar
    ) {
      Row(
        horizontalArrangement =
          Arrangement.spacedBy(16.dp, alignment = Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
          Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .fillMaxHeight(),
      ) {
        Image(
          painter = when {
            isXL && !isPortrait -> painterResource(id = R.drawable.deskflow_logo)
            else -> painterResource(id = R.drawable.deskflow_icon_fit)
                         },
          contentDescription = stringResource(R.string.app_toolbar_logo_desc),
          modifier = Modifier.height(32.dp),
        )
        Spacer(modifier = Modifier.weight(1f))

        FlowRow(
          horizontalArrangement =
            Arrangement.spacedBy(
              8.dp,
              alignment = Alignment.CenterHorizontally,
            ),
          verticalArrangement = Arrangement.Center,
          itemVerticalAlignment = Alignment.CenterVertically,
          maxLines = 3,
        ) {
          val serverHostAndPort =
            connectionState.screen.run {
              val suffix = if (server.useTls) " TLS" else ""
              val host = "${server.address}:${server.port}${suffix}"

              "$name\n$host"
            }
          Text(
            textAlign = TextAlign.End,
            text = serverHostAndPort,
          )
          IconButton(
            onClick = { appState.navigateToSettings() },
            modifier = Modifier.padding(0.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
            )
          }

          val (buttonColor, onButtonColor) =when {
            appState.isEnabled -> Pair(MaterialTheme.colorScheme.errorContainer,
              MaterialTheme.colorScheme.onErrorContainer)
            else -> Pair(MaterialTheme.colorScheme.secondaryContainer,
              MaterialTheme.colorScheme.onSecondaryContainer)
          }
          FilledTonalButton(
            onClick = { appState.setEnabled(!appState.isEnabled) },
            colors = ButtonDefaults.elevatedButtonColors(
              containerColor = buttonColor,
                contentColor = onButtonColor,
            ),
            contentPadding = PaddingValues(
              horizontal = 16.dp
            ),
            modifier = Modifier.padding(0.dp),
          ) {
            Text(
              text =
                when {
                  !appState.isEnabled ->
                    stringResource(R.string.app_toolbar_button_start)
                  else -> stringResource(R.string.app_toolbar_button_stop)
                },
              modifier = Modifier.padding(horizontal = 4.dp),
            )

            //if (appState.isEnabled)
            //  ConnectionLight(isConnected)

          }
        }
      }
    }
  }
}
