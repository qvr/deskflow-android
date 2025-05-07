package org.tfv.deskflow.ext

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import org.tfv.deskflow.ui.theme.DimensionDefaults

fun Modifier.drawBottomShadow(
  shadowHeight: Dp = DimensionDefaults.shadowEdgeHeight,
  shadowColor: Color = Color.Black.copy(alpha = 0.25f),
): Modifier {
  return drawBehind {
    val shadowHeightPx = shadowHeight.toPx()
    val startY = size.height - shadowHeightPx
    drawRect(
      brush =
        Brush.verticalGradient(
          colors = listOf(Color.Transparent, shadowColor),
          startY = startY,
          endY = size.height,
        ),
      topLeft = Offset(0f, startY),
      size = Size(size.width, shadowHeightPx),
    )
  }
}

fun Modifier.drawTopShadow(
  shadowHeight: Dp = DimensionDefaults.shadowEdgeHeight,
  shadowColor: Color = Color.Black.copy(alpha = 0.25f),
): Modifier {
  return drawBehind {
    val shadowHeightPx = shadowHeight.toPx()
    val endY = shadowHeightPx
    drawRect(
      brush =
        Brush.verticalGradient(
          colors = listOf(shadowColor,Color.Transparent),
          startY = 0f,
          endY = endY,
        ),
      topLeft = Offset(0f, 0f),
      size = Size(size.width, shadowHeightPx),
    )
  }
}
