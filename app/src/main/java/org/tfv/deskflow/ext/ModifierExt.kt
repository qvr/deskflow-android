/*
 * MIT License
 *
 * Copyright (c) 2025 Jonathan Glanz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
