package org.tfv.deskflow.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import io.github.oshai.kotlinlogging.Level
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.tfv.deskflow.data.aidl.ConnectionState
import org.tfv.deskflow.logging.LogRecordEvent
import org.tfv.deskflow.services.ConnectionServiceClient
import org.tfv.deskflow.ui.screens.navigateToSettingsScreen

val LocalAppState =
  staticCompositionLocalOf<IAppState> { error("No AppState provided") }

/** Interface exposing the public API of AppState. */
interface IAppState {

  val editorPlaygroundFocused: StateFlow<Boolean>

  /**
   * Sets the focus state of the editor playground.
   *
   * Used to determine the layout of the main screen, whether to show the editor
   * playground
   *
   * @param focused `true` if the editor playground is focused, `false`
   *   otherwise.
   */
  fun setEditorPlaygroundFocused(focused: Boolean)

  /** Mirrors `serviceClient.stateFlow` */
  val connectionStateFlow: StateFlow<ConnectionState>

  /** Mirrors `serviceClient.logRecordsFlow` */
  val logRecordsFlow: StateFlow<List<LogRecordEvent>>

  val isEnabled: Boolean
    get() = connectionStateFlow.value.isEnabled

  fun setEnabled(enabled: Boolean)

  fun clearLogRecords()

  fun setLogRecordForwardingLevel(level: Level)

  /** Composable getter for the current NavDestination (may be null) */
  @get:Composable val currentDestination: NavDestination?

  /** Exactly what AppState.topNavOptions returns */
  val topNavOptions: NavOptions

  val navController: NavHostController

  /** Exactly what AppState.navigateToSettings() does */
  fun navigateToSettings()

  /** Exactly what AppState.navigateToHome() does */
  fun navigateToHome()

  /** Mirrors the internal `_permissionCanDrawOverlays` StateFlow */
  val permissionCanDrawOverlays: StateFlow<Boolean>

  /** Mirrors the internal `_permissionAccessibilityEnabled` StateFlow */
  val permissionAccessibilityEnabled: StateFlow<Boolean>

  val permissionIMEEnabled: StateFlow<Boolean>
  /**
   * Updates both overlay‐permission and accessibility‐permission flags. Returns
   * `true` only if both booleans are true.
   */
  fun updatePermissions(
    canDrawOverlays: Boolean,
    accessibilityEnabled: Boolean,
    imeEnabled: Boolean
  ): Boolean
}

@Composable
fun rememberAppState(
  connectionServiceClient: ConnectionServiceClient,
  scope: CoroutineScope = rememberCoroutineScope(),
  navController: NavHostController = rememberNavController(),
): IAppState {

  return remember(connectionServiceClient, scope, navController) {
    AppState(
      serviceClient = connectionServiceClient,
      navController = navController,
      scope = scope,
    )
  }
}

@Stable
class AppState(
  val serviceClient: ConnectionServiceClient,
  override val navController: NavHostController,
  val scope: CoroutineScope,
) : IAppState {
  private val previousDestination = mutableStateOf<NavDestination?>(null)

  override val connectionStateFlow = serviceClient.stateFlow
  override val logRecordsFlow = serviceClient.logRecordsFlow

  override val editorPlaygroundFocused = MutableStateFlow(false)
  override fun setEditorPlaygroundFocused(focused: Boolean) {
    editorPlaygroundFocused.value = focused
  }

  override fun setEnabled(enabled: Boolean) {
    if (enabled != isEnabled) {
      serviceClient.setEnabled(enabled)
    }
  }

  override fun clearLogRecords() {
    serviceClient.clearLogRecords()
  }

  override fun setLogRecordForwardingLevel(level: Level) {
    serviceClient.setLogForwardingLevel(level.name)
  }

  override val currentDestination: NavDestination?
    @Composable
    get() {
      // Collect the currentBackStackEntryFlow as a state
      val currentEntry =
        navController.currentBackStackEntryFlow.collectAsState(initial = null)

      // Fallback to previousDestination if currentEntry is null
      return currentEntry.value?.destination.also { destination ->
        if (destination != null) {
          previousDestination.value = destination
        }
      } ?: previousDestination.value
    }

  override val topNavOptions: NavOptions
    get() = navOptions {
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
      }
      launchSingleTop = true
      restoreState = true
    }

  override fun navigateToSettings() {
    navController.navigateToSettingsScreen(topNavOptions)
  }

  override fun navigateToHome() {
    navController.navigateToHomeScreen(topNavOptions)
  }

  private val permissionIMEEnabledEditable = MutableStateFlow(false)
  private val permissionCanDrawOverlaysEditable = MutableStateFlow(false)
  private val permissionAccessibilityEnabledEditable = MutableStateFlow(false)

  override val permissionIMEEnabled: StateFlow<Boolean>
    get() = permissionIMEEnabledEditable.asStateFlow()
  override val permissionCanDrawOverlays =
    permissionCanDrawOverlaysEditable.asStateFlow()
  override val permissionAccessibilityEnabled =
    permissionAccessibilityEnabledEditable.asStateFlow()

  override fun updatePermissions(
    canDrawOverlays: Boolean,
    accessibilityEnabled: Boolean,
    imeEnabled: Boolean
  ): Boolean {
    permissionCanDrawOverlaysEditable.value = canDrawOverlays
    permissionAccessibilityEnabledEditable.value = accessibilityEnabled
    permissionIMEEnabledEditable.value = imeEnabled
    return canDrawOverlays && accessibilityEnabled && imeEnabled
  }
}
