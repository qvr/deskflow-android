package org.tfv.deskflow.ext

import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor

/**
 * Returns a new [Color] lightened or darkened by [amount].
 *
 * @param amount  A value in [-1f, 1f]:
 *   - Positive → lighter
 *   - Negative → darker
 */
fun Color.adjustBrightness(amount: Float): Color {
    val hsv = FloatArray(3)
    // Convert RGB to HSV
    AndroidColor.RGBToHSV(
        (red * 255).toInt().coerceIn(0, 255),
        (green * 255).toInt().coerceIn(0, 255),
        (blue * 255).toInt().coerceIn(0, 255),
        hsv
    )
    // Adjust value/brightness channel
    hsv[2] = (hsv[2] + amount).coerceIn(0f, 1f)
    // Recompose color, preserving alpha
    val argb = AndroidColor.HSVToColor((alpha * 255).toInt().coerceIn(0, 255), hsv)
    return Color(argb)
}