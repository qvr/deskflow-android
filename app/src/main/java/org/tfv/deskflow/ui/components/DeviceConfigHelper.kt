package org.tfv.deskflow.ui.components

import android.content.res.Configuration
import android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
import android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun currentDeviceConfig(): Triple<Boolean, Boolean, Boolean> {
    val conf = LocalConfiguration.current

    val isPortrait = conf.orientation == Configuration.ORIENTATION_PORTRAIT
    val isXL = conf.isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_XLARGE)
    val isLarge = conf.isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE)

    return Triple(isPortrait, isXL, isLarge)

}