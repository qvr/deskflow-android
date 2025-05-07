package org.tfv.deskflow.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import org.tfv.deskflow.services.ConnectionServiceClient

@Composable
fun rememberKeyboardViewState(
  connectionServiceClient: ConnectionServiceClient,
  scope: CoroutineScope = rememberCoroutineScope()
): VirtualKeyboardViewState {

  return remember(
    connectionServiceClient, scope
  ) {
    VirtualKeyboardViewState(
      serviceClient = connectionServiceClient, scope = scope
    )
  }
}

@Stable
class VirtualKeyboardViewState(
  val serviceClient: ConnectionServiceClient,
  val scope: CoroutineScope,
) {
  val connectionStateFlow = serviceClient.stateFlow

}