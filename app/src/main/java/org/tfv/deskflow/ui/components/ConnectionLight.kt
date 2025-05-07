package org.tfv.deskflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionLight(
  isConnected: Boolean,
  modifier: Modifier = Modifier,
  size: Dp = 16.dp
) {
  val color = when {
    isConnected -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.error
  }

  Box(
    modifier = modifier
      .size(size)
      .shadow(4.dp, CircleShape)
      .background(color)
  )
}