package org.tfv.deskflow.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ColumnScope.DeskflowFillSpacer(
    modifier: Modifier = Modifier.weight(1f)
) {
    Spacer(
        modifier = modifier
    )
}

@Composable
fun RowScope.DeskflowFillSpacer(
    modifier: Modifier = Modifier.weight(1f)
) {
    Spacer(
        modifier = modifier
    )
}