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

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.tfv.deskflow.android.iconics.fontawesomepro.FontAwesomeProLight
import org.tfv.deskflow.android.iconics.fontawesomepro.FontAwesomeProRegular
import org.tfv.deskflow.android.iconics.fontawesomepro.FontAwesomeProSolid

/**
 * A composable that lays out and draws a given [IconicsIcon]. This will attempt to
 * size the composable according to the [IconicsIcon]'s given width and height.
 * However, an optional [Modifier] parameter can be provided to adjust sizing or
 * draw additional content (ex. background). Any unspecified dimension will
 * leverage the default 24dp size as a minimum constraint.
 *
 * @param asset The [IconicsIcon] to draw.
 * @param modifier Modifier used to adjust the layout algorithm or draw
 *   decoration content (ex. background)
 * @param alignment Optional alignment parameter used to place the [ImageAsset]
 *   in the given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect
 *   ratio scaling to be used if the bounds are a different size from the
 *   intrinsic size of the [ImageAsset].
 * @param alpha Optional opacity to be applied to the [ImageAsset] when it is
 *   rendered onscreen
 * @param colorFilter Optional ColorFilter to apply for the [ImageAsset] when it
 *   is rendered onscreen
 */
@Suppress("NOTHING_TO_INLINE")
@Composable
@SuppressLint("ModifierParameter")
inline fun IconicsImage(
  asset: IconicsIcon,
  contentDescription: String? = asset.name,
  modifier: Modifier = Modifier.size(24.dp),
  iconicsConfig: IconicsConfig =
    IconicsConfig(iconBrush = SolidColor(LocalContentColor.current)),
  alignment: Alignment = Alignment.Center,
  contentScale: ContentScale = ContentScale.Fit,
  alpha: Float = DefaultAlpha,
  colorFilter: ColorFilter? = null,
) {
  val ctx = LocalContext.current
  val imagePainter =
    remember(asset, iconicsConfig) { IconicsPainter(ctx, asset, iconicsConfig) }
  Image(
    painter = imagePainter,
    contentDescription = contentDescription,
    modifier = modifier,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
  )
}

@Preview(showBackground = true)
@Composable
private fun IconicsImagePreview() {
  CompositionLocalProvider(LocalContentColor provides Color.Green) {
    Column {
    IconicsImage(
      asset = FontAwesomeProRegular.Icon.far_stop,
      modifier = Modifier.size(48.dp),
    )
      IconicsImage(
        asset = FontAwesomeProLight.Icon.fal_stop,
        modifier = Modifier.size(48.dp),
      )

      IconicsImage(
        asset = FontAwesomeProSolid.Icon.fas_stop,
        modifier = Modifier.size(48.dp),
      )
    }
  }
}
