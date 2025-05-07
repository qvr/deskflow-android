package org.tfv.deskflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.tfv.deskflow.R
import org.tfv.deskflow.ext.drawTopShadow
import org.tfv.deskflow.ui.annotations.PreviewAll
import org.tfv.deskflow.ui.components.preview.PreviewAppState
import org.tfv.deskflow.ui.components.preview.PreviewDeskflowThemedRoot
import org.tfv.deskflow.ui.graphics.beveledTopEdgeShape
import org.tfv.deskflow.ui.theme.DeskflowTheme
import org.tfv.deskflow.ui.theme.LocalDeskflowExtendedColorScheme

//state: VirtualKeyboardViewState
@Composable
fun VirtualKeyboardView(
    appState: IAppState
) {
    val connState by appState.connectionStateFlow.collectAsStateWithLifecycle()
    val (isPortrait, isXL, isLarge) = currentDeviceConfig()
    DeskflowTheme {
        val extColorScheme = LocalDeskflowExtendedColorScheme.current
        Surface(
            modifier = Modifier.height(60.dp)
                .fillMaxWidth(),
            tonalElevation = 6.dp,
            color = extColorScheme.keyboardBackground,
            shadowElevation = 24.dp,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides extColorScheme.onKeyboardBackground
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                        .drawTopShadow(shadowHeight = 16.dp)
                        .padding(start = 32.dp,end = 32.dp, top = 8.dp, bottom = 8.dp)

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.deskflow_icon_fit),
                        contentDescription = stringResource(R.string.app_toolbar_logo_desc),
                        modifier = Modifier.height(24.dp),
                    )
                    val serverHostAndPort =
                        connState.screen.run {
                            val suffix = if (server.useTls) " TLS" else ""
                            val host = "${server.address}:${server.port}${suffix}"

                            "$name\n$host"
                        }
                    Text(
                        textAlign = TextAlign.Start,
                        text = serverHostAndPort,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    ConnectionStatusLabel(connState)
                }
            }
        }
    }
}

@PreviewAll
@Composable
fun VirtualKeyboardViewPreview() {
    PreviewDeskflowThemedRoot { appState -> VirtualKeyboardView(appState) }

}
