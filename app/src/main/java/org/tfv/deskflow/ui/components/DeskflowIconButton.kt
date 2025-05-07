package org.tfv.deskflow.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.tfv.deskflow.ui.components.preview.PreviewDeskflowThemedRoot
import org.tfv.deskflow.ui.theme.DimensionDefaults

//@Composable
//fun DeskflowIconButton(
//  @DrawableRes iconId: Int,
//  onClick: () -> Unit,
//  contentDescription: String = "",
//  tintColor: Color = Color.Unspecified,
//) {
//  IconButton(
//    onClick = onClick,
//    colors =
//      IconButtonDefaults.iconButtonColors(
//        containerColor = Color.Transparent,
//        contentColor = tintColor,
//      ),
//  ) {
//    Icon(
//      painter = painterResource(id = iconId),
//      contentDescription = contentDescription,
//    )
//  }
//}

@Composable
fun DeskflowToggleIconButton(
  checked: Boolean,
  icon: @Composable (Boolean) -> Unit,
  enabled: Boolean = true,
  onCheckChange: (Boolean) -> Unit,
  shape: Shape = RoundedCornerShape(4.dp),
  interactionSource: MutableInteractionSource? = null,
  colors: IconToggleButtonColors = IconButtonDefaults.filledTonalIconToggleButtonColors(),
  modifier: Modifier = Modifier,
) {
  FilledTonalIconToggleButton(
    checked = checked,
    enabled = enabled,
    shape = shape,
    onCheckedChange = onCheckChange,
    colors = colors,
    interactionSource = interactionSource,
    modifier = modifier
      .height(DimensionDefaults.toolbarButtonHeight)
      .border(0.dp, Color.Transparent, shape)
  ) {
    icon(checked)
  }
}

@Composable
fun DeskflowIconButton(
  icon: @Composable () -> Unit,
  onClick: () -> Unit,
  enabled: Boolean = true,
//  shape: Shape = RoundedCornerShape(4.dp),
  interactionSource: MutableInteractionSource? = null,
  colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
  modifier: Modifier = Modifier,
) {

  IconButton(
    enabled = enabled,
    onClick = onClick,
    colors = colors,
    interactionSource = interactionSource,
    modifier = modifier,
  ) {
    icon()
  }
}


@Composable
@Preview
fun DeskflowToggleIconButtonPreview() {
  PreviewDeskflowThemedRoot {
    val checked = remember { mutableStateOf(false) }

    DeskflowToggleIconButton(
      checked = checked.value,
      icon = { isChecked ->
        Icon(
          if (isChecked) Icons.Outlined.KeyboardArrowDown
          else Icons.Filled.KeyboardArrowDown,
          contentDescription = "Settings Icon",
        )
      },
      onCheckChange = {
        checked.value = !checked.value // Toggle for preview
      },
    )
  }
}
