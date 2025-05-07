package org.tfv.deskflow.ui.components.preview

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import io.github.oshai.kotlinlogging.Level
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.tfv.deskflow.client.util.logging.KLoggingManager
import org.tfv.deskflow.data.aidl.ConnectionState
import org.tfv.deskflow.data.aidl.ScreenState
import org.tfv.deskflow.data.aidl.ServerState
import org.tfv.deskflow.ext.ConnectionStateDefaults
import org.tfv.deskflow.ext.copy
import org.tfv.deskflow.logging.LogRecordEvent
import org.tfv.deskflow.ui.components.IAppState
import org.tfv.deskflow.ui.components.LocalAppState


class PreviewAppState(
  override val navController: NavHostController,
  initialPermissionsGranted: Boolean = true,
  ctx: Context
) : IAppState {

  override val editorPlaygroundFocused = MutableStateFlow(false)
  override fun setEditorPlaygroundFocused(focused: Boolean) {
    editorPlaygroundFocused.update { focused }
  }

  val previewConnectionStateFlow = MutableStateFlow(ConnectionStateDefaults(ctx))

  override val connectionStateFlow: StateFlow<ConnectionState> = previewConnectionStateFlow.asStateFlow()
  override fun setEnabled(enabled: Boolean) {
    previewConnectionStateFlow.value = previewConnectionStateFlow.value.copy(isEnabled = enabled)
    log.info { "PreviewAppState: setEnabled($enabled)" }
  }

  val previewLogRecordsFlow = MutableStateFlow(emptyList<LogRecordEvent>())
  override val logRecordsFlow: StateFlow<List<LogRecordEvent>> = previewLogRecordsFlow.asStateFlow()

  override fun clearLogRecords() {
    previewLogRecordsFlow.value = emptyList()
  }
  override fun setLogRecordForwardingLevel(level: Level) {
    log.warn { "Ignoring set log record forwarding level in preview mode" }
  }

  fun addLogRecord(logRecord: LogRecordEvent) {
    previewLogRecordsFlow.update { current ->
      when {
        current.isEmpty() -> listOf(logRecord)
        current.size > 1000 -> current.subList(1, current.size) + logRecord
        else -> current + logRecord
      }
    }
  }


  override val currentDestination: NavDestination?
    @Composable
    get() {
      return null
    }

  override val topNavOptions: NavOptions
    get() = navOptions { launchSingleTop = true }

  override fun navigateToSettings() {}

  override fun navigateToHome() {}

  val previewPermissionIMEEnabledFlow = MutableStateFlow(initialPermissionsGranted)
  override val permissionIMEEnabled =
    previewPermissionIMEEnabledFlow.asStateFlow()


  val previewPermissionCanDrawOverlaysFlow = MutableStateFlow(initialPermissionsGranted)
  override val permissionCanDrawOverlays =
    previewPermissionCanDrawOverlaysFlow.asStateFlow()

  val previewPermissionAccessibilityEnabledFlow = MutableStateFlow(initialPermissionsGranted)
  override val permissionAccessibilityEnabled: StateFlow<Boolean> =
    previewPermissionAccessibilityEnabledFlow.asStateFlow()

  override fun updatePermissions(
    canDrawOverlays: Boolean,
    accessibilityEnabled: Boolean,
    imeEnabled: Boolean
  ): Boolean {
    return canDrawOverlays && accessibilityEnabled && imeEnabled
  }

  companion object {
    private val log = KLoggingManager.logger<PreviewAppState>()
  }
}

@Composable
fun rememberPreviewAppState(
  navController: NavHostController = rememberNavController(),
  ctx: Context = LocalContext.current
): IAppState {

  return remember(navController) {
    PreviewAppState(navController, ctx = ctx)
  }
}


@Composable
fun PreviewAppState(
  content: @Composable (PreviewAppState) -> Unit
) {
  val previewAppState = rememberPreviewAppState()
  CompositionLocalProvider(LocalAppState provides previewAppState) {
    content(previewAppState as PreviewAppState)
  }
}
