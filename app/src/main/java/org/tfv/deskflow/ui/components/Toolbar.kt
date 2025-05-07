package org.tfv.deskflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.tfv.deskflow.ext.drawBottomShadow
import org.tfv.deskflow.ui.theme.DimensionDefaults
import org.tfv.deskflow.ui.theme.LocalDeskflowExtendedColorScheme

@Composable
fun Toolbar(content: @Composable RowScope.() -> Unit) {
  val extColorScheme = LocalDeskflowExtendedColorScheme.current
  val colorScheme = MaterialTheme.colorScheme
  CompositionLocalProvider(
    LocalContentColor provides colorScheme.onSurfaceVariant
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
        .height(DimensionDefaults.toolbarHeight)
        .background(colorScheme.surfaceVariant)
        .drawBottomShadow()
    ) {
      Row(
        horizontalArrangement =
          Arrangement.spacedBy(
            DimensionDefaults.toolbarSpacing,
            alignment = Alignment.Start,
          ),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
          Modifier.fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
      ) {
        content()
      }
    }
  }
}
