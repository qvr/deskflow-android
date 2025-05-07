package org.tfv.deskflow.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.tfv.deskflow.R
import org.tfv.deskflow.ui.annotations.PreviewAll
import org.tfv.deskflow.ui.theme.DeskflowTheme

@Composable
fun PermissionsNeededDialog(
  @StringRes text: Int,
  required: Boolean = true,
  onClick: () -> Unit,
) {
  val snackbarHostState = LocalSnackbarHostState.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val openDialog = remember(text) { mutableStateOf(true) }
  if (required || openDialog.value) {
    AlertDialog(
      onDismissRequest = {
        if (required) {
          showSnackbar(
            snackbarHostState = snackbarHostState,
            scope = scope,
            context = context,
            message = R.string.permission_dialog_dismissed_message,
          )
        } else {
          openDialog.value = false
        }
      },
      title = {
        Text(
          stringResource(
            if (required) R.string.permission_dialog_title_required
            else R.string.permission_dialog_title_optional
          ),
          style = MaterialTheme.typography.headlineSmall,
        )
      },
      text = { Text(stringResource(text)) },
      dismissButton = when (required) {
        true -> null
        false -> {
          @Composable {
            TextButton(onClick = { openDialog.value = false }) {
              Text(
                stringResource(R.string.permission_dialog_buttons_dismiss)
              )
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = {
          onClick()
          openDialog.value = false
        }) {
          Text(stringResource(R.string.permission_dialog_buttons_open_settings))
        }
      },
    )
  }
}

@PreviewAll
@Composable
fun PermissionsNeededDialogPreview() {
  val snackbarHostState = remember { SnackbarHostState() }

  CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
    DeskflowTheme {
      PermissionsNeededDialog(
        text = R.string.permission_dialog_overlay_message,
        onClick = {},
      )
    }
  }
}

@PreviewAll
@Composable
fun PermissionsNeededDialogNonRequiredPreview() {
  val snackbarHostState = remember { SnackbarHostState() }

  CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
    DeskflowTheme {
      PermissionsNeededDialog(
        required = false,
        text = R.string.permission_dialog_overlay_message,
        onClick = {},
      )
    }
  }
}
