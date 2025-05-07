package org.tfv.deskflow.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DeskflowText(
    @StringRes id: Int,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    modifier: Modifier = deskflowTextTitleModifierDefaults()
) {
    Text(
        stringResource(id),
        color = color,
        style = style,
        textAlign = textAlign,
        modifier = modifier
    )
}


fun deskflowTextTitleModifierDefaults(): Modifier = Modifier
