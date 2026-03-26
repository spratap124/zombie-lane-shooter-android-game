package com.zombielane.shooter.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class EnemyBullet(
    x: Float,
    y: Float,
    private val vx: Float = 0f,
    private val vy: Float = SPEED,
    bulletColor: Int = COLOR_NORMAL
) : GameObject(x - WIDTH / 2f, y, WIDTH, HEIGHT) {

    companion object {
        const val WIDTH = 8f
        const val HEIGHT = 14f
        const val SPEED = 3f
        val COLOR_NORMAL = Color.parseColor("#FF5252")
        val COLOR_BOSS = Color.parseColor("#FF9100")
        val COLOR_ZIGZAG = Color.parseColor("#E040FB")
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bulletColor
        style = Paint.Style.FILL
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bulletColor
        alpha = 80
        style = Paint.Style.FILL
    }

    override fun update(screenWidth: Int, screenHeight: Int) {
        x += vx
        y += vy
        if (y > screenHeight || y + height < 0 || x + width < 0 || x > screenWidth) active = false
    }

    override fun draw(canvas: Canvas) {
        canvas.drawOval(x - 3f, y - 2f, x + width + 3f, y + height + 2f, glowPaint)
        canvas.drawOval(x, y, x + width, y + height, paint)
    }
}
