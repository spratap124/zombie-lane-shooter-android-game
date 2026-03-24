package com.zombielane.shooter.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import kotlin.math.sin

enum class EnemyType {
    NORMAL,
    ZIGZAG,
    FAST,
    SPLITTER,
    BOSS
}

class Enemy(
    x: Float,
    y: Float,
    private val speed: Float,
    val scoreValue: Int,
    val coinValue: Int,
    private val bodyColor: Int,
    var health: Int,
    val maxHealth: Int = health,
    val type: EnemyType = EnemyType.NORMAL
) : GameObject(x, y, if (type == EnemyType.BOSS) BOSS_SIZE else SIZE, if (type == EnemyType.BOSS) BOSS_SIZE else SIZE) {

    companion object {
        const val SIZE = 64f
        const val BOSS_SIZE = 128f
    }

    private var frameAge = 0
    private val spawnX = x

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bodyColor
        style = Paint.Style.FILL
    }

    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val pupilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B71C1C")
        style = Paint.Style.FILL
    }

    private val healthTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = if (type == EnemyType.BOSS) 40f else 28f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    private val healthBarBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#44FFFFFF")
        style = Paint.Style.FILL
    }

    private val healthBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    fun takeDamage(amount: Int) {
        health -= amount
        if (health <= 0) {
            health = 0
            active = false
        }
    }

    val isDead: Boolean get() = health <= 0

    override fun update(screenWidth: Int, screenHeight: Int) {
        frameAge++

        when (type) {
            EnemyType.ZIGZAG -> {
                y += speed
                x = spawnX + sin(frameAge * 0.08) .toFloat() * 80f
                x = x.coerceIn(0f, screenWidth - width)
            }
            EnemyType.FAST -> {
                y += speed
            }
            EnemyType.BOSS -> {
                y += speed
                x = spawnX + sin(frameAge * 0.03).toFloat() * 100f
                x = x.coerceIn(0f, screenWidth - width)
            }
            else -> {
                y += speed
            }
        }

        if (y > screenHeight) active = false
    }

    override fun draw(canvas: Canvas) {
        val cx = x + width / 2f
        val cy = y + height / 2f
        val scale = width / SIZE

        val bodyPath = Path().apply {
            moveTo(x + 8f * scale, y)
            lineTo(x + width - 8f * scale, y)
            quadTo(x + width, y, x + width, y + 8f * scale)
            lineTo(x + width, y + height - 12f * scale)
            lineTo(x + width - 8f * scale, y + height)
            lineTo(x + width - 18f * scale, y + height - 8f * scale)
            lineTo(x + width - 28f * scale, y + height)
            lineTo(cx, y + height - 6f * scale)
            lineTo(x + 18f * scale, y + height)
            lineTo(x + 8f * scale, y + height - 8f * scale)
            lineTo(x, y + height - 12f * scale)
            lineTo(x, y + 8f * scale)
            quadTo(x, y, x + 8f * scale, y)
            close()
        }
        canvas.drawPath(bodyPath, bodyPaint)

        val eyeR = 9f * scale
        val pupilR = 5f * scale
        val eyeSpacing = 12f * scale
        canvas.drawCircle(cx - eyeSpacing, cy - 6f * scale, eyeR, eyePaint)
        canvas.drawCircle(cx + eyeSpacing, cy - 6f * scale, eyeR, eyePaint)
        canvas.drawCircle(cx - eyeSpacing, cy - 5f * scale, pupilR, pupilPaint)
        canvas.drawCircle(cx + eyeSpacing, cy - 5f * scale, pupilR, pupilPaint)

        if (type == EnemyType.BOSS) {
            // Boss horns
            val hornPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#B71C1C")
                style = Paint.Style.FILL
            }
            val hornPath = Path().apply {
                moveTo(x + 20f, y + 8f)
                lineTo(x + 10f, y - 20f)
                lineTo(x + 40f, y + 4f)
                close()
            }
            canvas.drawPath(hornPath, hornPaint)
            val hornPath2 = Path().apply {
                moveTo(x + width - 20f, y + 8f)
                lineTo(x + width - 10f, y - 20f)
                lineTo(x + width - 40f, y + 4f)
                close()
            }
            canvas.drawPath(hornPath2, hornPaint)
        }

        canvas.drawText(health.toString(), cx, cy + 22f * scale, healthTextPaint)

        if (maxHealth > 1) {
            val barWidth = width + 8f
            val barHeight = if (type == EnemyType.BOSS) 10f else 6f
            val barX = x - 4f
            val barY = y - 14f
            val healthFraction = health.toFloat() / maxHealth

            canvas.drawRoundRect(barX, barY, barX + barWidth, barY + barHeight, 3f, 3f, healthBarBgPaint)

            healthBarPaint.color = when {
                healthFraction > 0.5f -> Color.parseColor("#4CAF50")
                healthFraction > 0.25f -> Color.parseColor("#FF9800")
                else -> Color.parseColor("#F44336")
            }
            canvas.drawRoundRect(barX, barY, barX + barWidth * healthFraction, barY + barHeight, 3f, 3f, healthBarPaint)
        }
    }
}
