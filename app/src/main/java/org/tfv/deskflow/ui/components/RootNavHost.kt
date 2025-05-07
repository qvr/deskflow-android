package org.tfv.deskflow.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import org.tfv.deskflow.ui.screens.settingsScreen

@Composable
fun RootNavHost(
  appState: IAppState,
//  onShowSnackbar: suspend (String, String?) -> Boolean,
  modifier: Modifier = Modifier,
) {
  val navController = appState.navController
  NavHost(
    navController = navController,
    startDestination = HomeScreenRoute,
    modifier = modifier,
  ) {
    homeScreen(appState = appState)
    settingsScreen(appState = appState)
  }
}