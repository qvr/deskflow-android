package org.tfv.deskflow.client.models

data class Point(val x: Int, val y: Int)
data class Size(val width: Int, val height: Int)
data class SizeF(val width: Float, val height: Float)
data class Rect(val point: Point, val size: Size)