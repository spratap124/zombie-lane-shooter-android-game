package com.zombielane.shooter.objects

import android.graphics.Canvas
import android.graphics.RectF

abstract class GameObject(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float
) {
    var active = true

    val bounds: RectF
        get() = RectF(x, y, x + width, y + height)

    abstract fun update(screenWidth: Int, screenHeight: Int)
    abstract fun draw(canvas: Canvas)

    fun collidesWith(other: GameObject): Boolean {
        return active && other.active && RectF.intersects(bounds, other.bounds)
    }
}
