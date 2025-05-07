package org.tfv.deskflow.ext

import android.content.Context
import org.tfv.deskflow.client.models.Size
import org.tfv.deskflow.client.models.SizeF

data class ScreenSize(val px: Size, val dp: SizeF, val scale: Float)

fun Context.getScreenSize(): ScreenSize {
    val dm = resources.displayMetrics
    val widthPx = dm.widthPixels
    val heightPx = dm.heightPixels
    val widthDp = widthPx / dm.density
    val heightDp = heightPx / dm.density
    return ScreenSize(Size(widthPx, heightPx), SizeF(widthDp, heightDp), widthPx.toFloat() / widthDp)
}