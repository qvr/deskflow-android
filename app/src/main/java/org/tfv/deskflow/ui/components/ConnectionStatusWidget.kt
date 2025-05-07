package org.tfv.deskflow.ui.components

// import androidx.compose.foundation.Image
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.tfv.deskflow.R
import org.tfv.deskflow.data.aidl.ConnectionState
import org.tfv.deskflow.ext.adjustBrightness
import org.tfv.deskflow.ui.components.preview.PreviewAppState
import org.tfv.deskflow.ui.components.preview.PreviewDeskflowThemedRoot
import org.tfv.deskflow.ui.theme.LocalDeskflowExtendedColorScheme

@Composable
fun ConnectionStatusLabel(
  connState: ConnectionState
) {
  val extColorScheme = LocalDeskflowExtendedColorScheme.current
  val infiniteTransition = rememberInfiniteTransition()
  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.75f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000),
      repeatMode = RepeatMode.Reverse
    )
  )

  val (statusColor, statusShadowColor) = when {
    !connState.isEnabled ->
      MaterialTheme.colorScheme.errorContainer
    connState.isConnected ->
      extColorScheme.success
    else -> extColorScheme.warning
  }.let { color ->
    Pair(color.copy(alpha = glowAlpha), color.adjustBrightness(-0.4f).copy(alpha = glowAlpha))
  }

  Text(
    text =
      when {
        !connState.isEnabled ->
          stringResource(
            R.string.connection_status_widget_header_status_stopped
          )
        connState.isConnected ->
          stringResource(
            R.string.connection_status_widget_header_status_connected
          )
        else ->
          stringResource(
            R.string.connection_status_widget_header_status_connecting
          )
      }.uppercase(),
    style =
      MaterialTheme.typography.titleSmall.copy(
        //fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Medium,
      ),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    color =
      when {
        !connState.isEnabled -> MaterialTheme.colorScheme.onErrorContainer
        connState.isConnected ->
          extColorScheme.onSuccess
        else -> extColorScheme.onWarning
      },
    modifier =
      Modifier
        .shadow(
          elevation = 4.dp,
          shape = RoundedCornerShape(4.dp),
          ambientColor = statusShadowColor,
          spotColor = statusShadowColor
        )
        .background(
          brush =
            SolidColor(
              statusColor
            ),
          shape = RoundedCornerShape(4.dp),
          alpha = 1f,
        )
        .padding(horizontal = 8.dp, vertical = 8.dp),
    // .width(100.dp),
  )

}

@Composable
fun ConnectionStatusWidget(
  appState: IAppState = LocalAppState.current,
  style: DeskflowCardStyle = deskflowCardWidgetStyleDefaults(),
) {
  val extColorScheme = LocalDeskflowExtendedColorScheme.current
  val connState by appState.connectionStateFlow.collectAsStateWithLifecycle()

  DeskflowCardWidget(
    style =
      style.copy(
        containerModifier = style.containerModifier.wrapContentHeight()
      ),
    adjustStyleHeight = false,
    header = {
      Toolbar {
        Text(
          text = stringResource(R.string.connection_status_widget_title),
          style = MaterialTheme.typography.titleLarge,
        )
        DeskflowFillSpacer()
        ConnectionStatusLabel(connState)
      }
    },
  ) {
  }
}

@Preview
@Composable
fun ConnectionStatusWidgetPreview() {
  PreviewDeskflowThemedRoot {appState -> ConnectionStatusWidget(appState) }

}
