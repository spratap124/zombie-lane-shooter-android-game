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
        return active && other.active && RectF.intersects(bounds, other.bounds)
    }
}
