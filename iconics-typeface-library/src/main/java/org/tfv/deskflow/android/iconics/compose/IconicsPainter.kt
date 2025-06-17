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

package org.tfv.deskflow.android.iconics.compose

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

@Immutable
data class IconicsConfig(
  val iconBrush: Brush,
  val iconPaint: Paint = TextPaint(Paint.ANTI_ALIAS_FLAG),
  val respectFontBounds: Boolean = true,
  val contourPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG),
  val contourBrush: Brush? = null,
  val paddingDp: Int? = null,
  val iconOffsetXPx: Int = 0,
  val iconOffsetYPx: Int = 0,
)

class IconicsPainter(
  private val context: Context,
  private val icon: IconicsIcon,
  private val config: IconicsConfig,
) : Painter() {

  private var isDirty by mutableStateOf(true)
  private val textValue = icon.character.toString()

  private val paddingBounds = Rect()
  private val pathBounds = RectF()
  private val path = android.graphics.Path()

  private val iconPaint: Paint =
    config.iconPaint.apply {
      typeface = getTypeface(context, icon.typeface)
    }

  private var alpha: Float = 1.0f
  private var colorFilter: ColorFilter? = null

  /** Update the Padding Bounds */
  private fun updatePaddingBounds(viewBounds: Rect) {
    val padding = config.paddingDp
    if (
      padding != null &&
        padding * 2 <= viewBounds.width() &&
        padding * 2 <= viewBounds.height()
    ) {
      paddingBounds.set(
        viewBounds.left + padding,
        viewBounds.top + padding,
        viewBounds.right - padding,
        viewBounds.bottom - padding,
      )
    }
  }

  /** Update the TextSize */
  private fun updateTextSize(viewBounds: Rect) {
    var textSize =
      viewBounds.height().toFloat() * if (config.respectFontBounds) 1 else 2
    iconPaint.textSize = textSize
    iconPaint.getTextPath(
      textValue,
      0,
      textValue.length,
      0f,
      viewBounds.height().toFloat(),
      path,
    )
    path.computeBounds(pathBounds, true)

    if (!config.respectFontBounds) {
      val deltaWidth = paddingBounds.width().toFloat() / pathBounds.width()
      val deltaHeight = paddingBounds.height().toFloat() / pathBounds.height()
      val delta = if (deltaWidth < deltaHeight) deltaWidth else deltaHeight
      textSize *= delta
      iconPaint.textSize = textSize
      iconPaint.getTextPath(
        textValue,
        0,
        textValue.length,
        0f,
        viewBounds.height().toFloat(),
        path,
      )
      path.computeBounds(pathBounds, true)
    }
  }

  /** Set the icon offset */
  private fun offsetIcon(viewBounds: Rect) {
    val startX = viewBounds.centerX() - pathBounds.width() / 2
    val offsetX = startX - pathBounds.left

    val startY = viewBounds.centerY() - pathBounds.height() / 2
    val offsetY = startY - pathBounds.top

    path.offset(offsetX + config.iconOffsetXPx, offsetY + config.iconOffsetYPx)
  }

  override fun DrawScope.onDraw() {
    val bounds =
      Rect(
        0,
        0,
        this@onDraw.size.width.toInt(),
        this@onDraw.size.height.toInt(),
      )
    updatePaddingBounds(bounds)
    updateTextSize(bounds)
    offsetIcon(bounds)

    if (config.contourBrush != null) {
      drawPath(path.asComposePath(), config.contourBrush)
    }

    drawPath(
      path.asComposePath(),
      config.iconBrush,
      alpha = alpha,
      colorFilter = colorFilter,
    )

    if (isDirty) {
      isDirty = false
    }
  }

  /** Icon fonts don't have a specific intrinsics size and will just scale */
  override val intrinsicSize: Size
    get() = Size.Unspecified

  override fun applyAlpha(alpha: Float): Boolean {
    this.alpha = alpha
    return true
  }

  override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
    this.colorFilter = colorFilter
    return true
  }

  companion object {
    private val typefaceCache: MutableMap<Int, Typeface> =mutableMapOf()

    private fun getTypeface(
      context: Context,
      typeface: IconicsTypeface
    ): Typeface {
      return typefaceCache.getOrPut(typeface.fontRes) {
        context.resources.getFont(typeface.fontRes)
      }
    }
  }
}
