package com.zombielane.shooter.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.sin

enum class PowerUpType {
    RAPID_FIRE,
    SHIELD,
    BOMB
}

class PowerUp(
    x: Float,
    y: Float,
    val type: PowerUpType
) : GameObject(x - SIZE / 2f, y, SIZE, SIZE) {

    companion object {
        const val SIZE = 40f
        const val FALL_SPEED = 1.8f
    }

    private var frameAge = 0

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = when (type) {
            PowerUpType.RAPID_FIRE -> Color.parseColor("#FF9800")
            PowerUpType.SHIELD -> Color.parseColor("#2196F3")
            PowerUpType.BOMB -> Color.parseColor("#F44336")
        }
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun update(screenWidth: Int, screenHeight: Int, playfieldLeft: Float, playfieldRight: Float?) {
        frameAge++
        y += FALL_SPEED
        if (y > screenHeight) active = false
    }

    override fun draw(canvas: Canvas) {
        val cx = x + width / 2f
        val cy = y + height / 2f
        val pulse = 1f + sin(frameAge * 0.15).toFloat() * 0.15f
        val r = SIZE / 2f * pulse

        glowPaint.color = bgPaint.color
        glowPaint.alpha = 50
        canvas.drawCircle(cx, cy, r + 6f, glowPaint)

        canvas.drawCircle(cx, cy, r, bgPaint)

        when (type) {
            PowerUpType.RAPID_FIRE -> {
                val boltPath = Path().apply {
                    moveTo(cx - 4f, cy - 10f)
                    lineTo(cx + 6f, cy - 10f)
                    lineTo(cx + 1f, cy - 1f)
                    lineTo(cx + 8f, cy - 1f)
                    lineTo(cx - 4f, cy + 12f)
                    lineTo(cx - 1f, cy + 2f)
                    lineTo(cx - 8f, cy + 2f)
                    close()
                }
                canvas.drawPath(boltPath, iconPaint)
            }
            PowerUpType.SHIELD -> {
                val shieldPath = Path().apply {
                    moveTo(cx, cy - 11f)
                    lineTo(cx + 10f, cy - 5f)
                    lineTo(cx + 8f, cy + 6f)
                    lineTo(cx, cy + 12f)
                    lineTo(cx - 8f, cy + 6f)
                    lineTo(cx - 10f, cy - 5f)
                    close()
                }
                canvas.drawPath(shieldPath, iconPaint)
            }
            PowerUpType.BOMB -> {
                canvas.drawCircle(cx, cy + 2f, 8f, iconPaint)
                canvas.drawRect(cx - 2f, cy - 10f, cx + 2f, cy - 4f, iconPaint)
            }
        }
    }
}
