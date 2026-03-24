package com.zombielane.shooter.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class CoinParticle(
    private var x: Float,
    private var y: Float,
    private val targetX: Float,
    private val targetY: Float
) {
    var active = true
    private var progress = 0f

    private val startX = x
    private val startY = y

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        style = Paint.Style.FILL
    }

    fun update() {
        progress += 0.06f
        if (progress >= 1f) {
            active = false
            return
        }

        val t = easeOutCubic(progress)
        x = startX + (targetX - startX) * t
        y = startY + (targetY - startY) * t - (1f - (2f * t - 1f) * (2f * t - 1f)) * 80f
    }

    private fun easeOutCubic(t: Float): Float {
        val t1 = t - 1f
        return t1 * t1 * t1 + 1f
    }

    fun draw(canvas: Canvas) {
        val radius = 8f * (1f - progress * 0.3f)
        paint.alpha = ((1f - progress * 0.5f) * 255).toInt().coerceIn(0, 255)
        canvas.drawCircle(x, y, radius, paint)
    }
}
