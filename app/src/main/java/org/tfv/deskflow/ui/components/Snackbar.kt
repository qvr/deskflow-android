package org.tfv.deskflow.ui.components

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.tfv.deskflow.R

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
  error("No SnackbarHostState provided")
}

fun showSnackbar(
  snackbarHostState: SnackbarHostState,
  scope: CoroutineScope,
  context: Context,
  @StringRes message: Int,
  onAction: ((SnackbarResult?) -> Unit)? = null
) {
  scope.launch {
    val result = snackbarHostState?.showSnackbar(
      message = context.resources.getString(message)
    )
    
    if (onAction != null) {
      onAction(result)
    }
  }
}

@Composable
fun showSnackbar(@StringRes message: Int, onAction: ((SnackbarResult?) -> Unit)? = null) {
  val snackbarHostState = LocalSnackbarHostState.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  
  LaunchedEffect(message) {
    showSnackbar(
      snackbarHostState = snackbarHostState,
      scope = scope,
      context = context,
      message = message,
      onAction = onAction
    )
  }
}
