package com.zombielane.shooter.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Bullet(
    x: Float,
    y: Float,
    val damage: Int = 1,
    private val vx: Float = 0f,
    private val vy: Float = -SPEED,
    bulletColor: Int = Color.parseColor("#FFEB3B"),
    glowColor: Int = Color.parseColor("#80FFEB3B"),
    bulletWidth: Float = WIDTH,
    bulletHeight: Float = HEIGHT
) : GameObject(
    x = x - bulletWidth / 2f,
    y = y,
    width = bulletWidth,
    height = bulletHeight
) {

    companion object {
        const val WIDTH = 6f
        const val HEIGHT = 18f
        const val SPEED = 18f
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bulletColor
        style = Paint.Style.FILL
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = glowColor
        style = Paint.Style.FILL
    }

    override fun update(screenWidth: Int, screenHeight: Int, playfieldLeft: Float, playfieldRight: Float?) {
        x += vx
        y += vy
        if (y + height < 0 || y > screenHeight || x + width < 0 || x > screenWidth) active = false
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(
            x - 2f, y - 2f, x + width + 2f, y + height + 2f,
            4f, 4f, glowPaint
        )
        canvas.drawRoundRect(
            x, y, x + width, y + height,
            3f, 3f, paint
        )
    }
}
