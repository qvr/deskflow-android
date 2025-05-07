package org.tfv.deskflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState

import org.tfv.deskflow.R
import org.tfv.deskflow.ui.screens.SettingsScreenRoute

@Composable
fun StatusBar(
  appState: IAppState = LocalAppState.current
) {
  val connectionState by appState.connectionStateFlow.collectAsStateWithLifecycle()
  val isConnected = connectionState.isConnected

  val isSettingsRoot = isCurrentDestination(route = SettingsScreenRoute::class)

  BottomAppBar(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.primary,
    modifier = if (isSettingsRoot)
      Modifier.fillMaxHeight(0f)
    else
      Modifier

  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      ConnectionLight(isConnected)
      Text(
//                  modifier = Modifier
//                    .fillMaxWidth(),
        textAlign = TextAlign.Center,
        text = when (isConnected) {
          true -> stringResource(R.string.bottom_bar_connected)
          false -> stringResource(R.string.bottom_bar_disconnected)
        },
      )
      IconButton(onClick = {
        appState.navigateToSettings()
      }) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
        )
//        Text("Settings")
      }
    }
  }
}