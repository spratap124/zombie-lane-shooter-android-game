package com.zombielane.shooter.objects

import android.graphics.Canvas
import android.graphics.Paint

class Particle(
    x: Float,
    y: Float,
    private val vx: Float,
    private val vy: Float,
    private val color: Int,
    private var life: Int = 20
) : GameObject(x, y, 6f, 6f) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = this@Particle.color
        style = Paint.Style.FILL
    }

    override fun update(screenWidth: Int, screenHeight: Int, playfieldLeft: Float, playfieldRight: Float?) {
        x += vx
        y += vy
        life--
        if (life <= 0) active = false
        paint.alpha = ((life / 20f) * 255).toInt().coerceIn(0, 255)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(x + width / 2f, y + height / 2f, width / 2f, paint)
    }
}
