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

package org.tfv.deskflow.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.tfv.deskflow.ui.components.preview.PreviewAppState
import org.tfv.deskflow.ui.components.preview.PreviewDeskflowThemedRoot

@Composable
fun ScrollableVerticalBox(
  scrollState: ScrollableState,
  modifier: Modifier = Modifier,
  showIcons: Boolean = false,
  showGradients: Boolean = true,
  content: @Composable () -> Unit,
) {
  val coroutineScope = rememberCoroutineScope()

  BoxWithConstraints (
    modifier = modifier
  ) { // 3) If not scrolled all the way to the top, show “scroll-up” fade/arrow

    content()
    if (showGradients) {
      val scrollAvailableColor = Color.Black.copy(alpha =0.35f)
        //LocalContentColor.current.copy(alpha = 0.5f)
        //MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
      if (scrollState.canScrollBackward) {
        Box(
          Modifier.fillMaxWidth()
            .align(Alignment.TopCenter)
            .height(24.dp)
            .background(
              Brush.verticalGradient(
                0f to scrollAvailableColor,
                1f to Color.Transparent,
              )
            )
        )
      }

      // 4) If not yet at the bottom, show “scroll-down” fade/arrow
      if (scrollState.canScrollForward) {
        Box(
          Modifier.align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(24.dp)
            .background(
              Brush.verticalGradient(
                0f to Color.Transparent,
                1f to scrollAvailableColor,
              )
            )
        )
      }
    }

    if (showIcons) {
      val scrollDelta = with(LocalDensity.current) { maxHeight.toPx() }
      // scroll-up arrow
      if (scrollState.canScrollBackward) {
        Icon(
          imageVector = Icons.Default.KeyboardArrowUp,
          contentDescription = "Scroll up",
          modifier = Modifier.align(Alignment.TopCenter).padding(8.dp).clickable {
            coroutineScope.launch {
              scrollState.animateScrollBy(-scrollDelta)
            }
          },
        )
      }

      if (scrollState.canScrollForward) {
        Icon(
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = "Scroll down",
          modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)
            .clickable {
              coroutineScope.launch {
                scrollState.animateScrollBy(scrollDelta)
              }
            },
        )
      }
    }
  }
}

@Preview(showBackground = true, device = Devices.PHONE, uiMode = UI_MODE_NIGHT_YES)
@Preview(showBackground = true, device = Devices.PHONE, uiMode = UI_MODE_NIGHT_NO)
@Composable
private fun ScrollableVerticalBoxPreview() {
  val scrollState = rememberScrollState()
  PreviewDeskflowThemedRoot(darkTheme = isSystemInDarkTheme()) { appState ->
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {


        ScrollableVerticalBox(
          scrollState = scrollState,
          showGradients = true,
          showIcons = true,
          modifier = Modifier.fillMaxSize()
        ) {
          Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
          ) {
            repeat(200) {
              Text("Item $it", Modifier.padding(8.dp))
            }
          }
        }
      }
    }

}

