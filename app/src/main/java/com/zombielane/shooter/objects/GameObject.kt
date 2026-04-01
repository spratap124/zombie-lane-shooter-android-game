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

    /**
     * @param playfieldLeft min x for entities that should stay in the firing lane (e.g. [Enemy]).
     * @param playfieldRight right edge of that lane (same convention as [android.graphics.RectF.right]); when null, use full [screenWidth].
     */
    abstract fun update(
        screenWidth: Int,
        screenHeight: Int,
        playfieldLeft: Float = 0f,
        playfieldRight: Float? = null
    )
    abstract fun draw(canvas: Canvas)

    fun collidesWith(other: GameObject): Boolean {
        if (!active || !other.active) return false
        val x1 = x + width
        val ox1 = other.x + other.width
        if (x1 <= other.x || x >= ox1) return false
        val y1 = y + height
        val oy1 = other.y + other.height
        return y1 > other.y && y < oy1
    }
}
