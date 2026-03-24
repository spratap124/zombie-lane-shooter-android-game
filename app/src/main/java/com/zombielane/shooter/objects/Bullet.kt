package com.zombielane.shooter.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Bullet(
    x: Float,
    y: Float,
    val damage: Int = 1
) : GameObject(
    x = x - WIDTH / 2f,
    y = y,
    width = WIDTH,
    height = HEIGHT
) {

    companion object {
        const val WIDTH = 6f
        const val HEIGHT = 18f
        const val SPEED = 18f
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (damage >= 3) Color.parseColor("#FF5722")
        else Color.parseColor("#FFEB3B")
        style = Paint.Style.FILL
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (damage >= 3) Color.parseColor("#80FF5722")
        else Color.parseColor("#80FFEB3B")
        style = Paint.Style.FILL
    }

    override fun update(screenWidth: Int, screenHeight: Int) {
        y -= SPEED
        if (y + height < 0) active = false
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
