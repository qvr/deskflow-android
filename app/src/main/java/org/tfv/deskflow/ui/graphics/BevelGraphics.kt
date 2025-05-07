package org.tfv.deskflow.ui.graphics

import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

fun beveledTopEdgeShape(radius: Float): Shape = object : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = 0f,
                    top = 0f,
                    right = radius * 2,
                    bottom = radius * 2
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Top-right corner arc
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = size.width - radius * 2,
                    top = 0f,
                    right = size.width,
                    bottom = radius * 2
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
//            moveTo(0f, cutSize)

//            lineTo(cutSize, 0f)
//            lineTo(size.width - cutSize, 0f)
//            lineTo(size.width, cutSize)
//            lineTo(size.width, size.height)
//            lineTo(0f, size.height)
//            close()
        }
        return Outline.Generic(path)
    }
}