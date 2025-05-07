package org.tfv.deskflow.ui.components.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.tfv.deskflow.ui.components.IAppState
import org.tfv.deskflow.ui.components.LocalSnackbarHostState
import org.tfv.deskflow.ui.theme.DeskflowTheme

@Composable
fun PreviewDeskflowThemedRoot(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable ColumnScope.(IAppState) -> Unit,
) {
  val snackbarHostState = remember { SnackbarHostState() }

  DeskflowTheme(darkTheme = darkTheme) {
    CompositionLocalProvider(
      LocalSnackbarHostState provides snackbarHostState
    ) {
      PreviewAppState { appState ->
        Scaffold(
          snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .background(color = MaterialTheme.colorScheme.background)
              .windowInsetsPadding(WindowInsets.Companion.statusBars)
              .imePadding(),

          ) {
            content(appState)
          }
        }
      }
    }
  }
}
