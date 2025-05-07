package org.tfv.deskflow.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlin.reflect.KClass


@Composable
fun isCurrentDestination(
    route: KClass<*>,
    appState: IAppState = LocalAppState.current
): Boolean {
    val navBackStackEntry by appState.navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.hasRoute(route = route) ?: false
}