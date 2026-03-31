package com.zombielane.shooter.objects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.sin

class Player(
    screenWidth: Int,
    screenHeight: Int,
    val maxHealth: Int = 3,
    safeArea: RectF = RectF(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat())
) : GameObject(
    x = screenWidth / 2f - PLAYER_WIDTH / 2f,
    y = safeArea.bottom - PLAYER_HEIGHT - BOTTOM_MARGIN,
    width = PLAYER_WIDTH,
    height = PLAYER_HEIGHT
) {

    companion object {
        const val PLAYER_WIDTH = 80f
        const val PLAYER_HEIGHT = 90f
        const val BOTTOM_MARGIN = 16f
        const val SPEED = 14f
    }

    var targetX: Float = x + width / 2f
    var health: Int = maxHealth
    var invincibleFrames: Int = 0
    var shielded = false
    var rapidFireUntilMs = 0L

    /** While [System.currentTimeMillis] < this, player takes no damage (rewarded continue shield). */
    var continueShieldUntilMs: Long = 0L

    var currentBitmap: Bitmap? = null

    private var frameAge = 0

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val healthBarBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#44FFFFFF")
        style = Paint.Style.FILL
    }

    private val healthBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val shieldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#442196F3")
        style = Paint.Style.FILL
    }

    private val shieldStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2196F3")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val flamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    val isInvincible: Boolean get() = invincibleFrames > 0
    val isDead: Boolean get() = health <= 0
    val isNearDeath: Boolean get() = health == 1 && maxHealth > 1

    fun takeDamage() {
        val now = System.currentTimeMillis()
        if (now < continueShieldUntilMs) return
        if (isInvincible) return
        if (shielded) {
            shielded = false
            invincibleFrames = 30
            return
        }
        health--
        invincibleFrames = 90
    }

    fun update(screenWidth: Int, screenHeight: Int, safeArea: RectF) {
        frameAge++
        val nowMs = System.currentTimeMillis()
        if (continueShieldUntilMs > 0L && nowMs >= continueShieldUntilMs) {
            continueShieldUntilMs = 0L
        }
        val centerX = x + width / 2f
        val dx = targetX - centerX

        if (abs(dx) > SPEED) {
            x += if (dx > 0) SPEED else -SPEED
        } else {
            x += dx
        }

        x = x.coerceIn(safeArea.left, safeArea.right - width)

        if (invincibleFrames > 0) invincibleFrames--
    }

    override fun update(screenWidth: Int, screenHeight: Int, playfieldLeft: Float, playfieldRight: Float?) {
        val right = playfieldRight ?: screenWidth.toFloat()
        update(screenWidth, screenHeight, RectF(playfieldLeft, 0f, right, screenHeight.toFloat()))
    }

    override fun draw(canvas: Canvas) {
        if (isInvincible && !shielded && (invincibleFrames / 4) % 2 == 0) return

        val cx = x + width / 2f

        drawFlame(canvas, cx)
        drawJet(canvas)
        drawShield(canvas, cx)
        drawHealthBar(canvas)
    }

    private fun drawFlame(canvas: Canvas, cx: Float) {
        val flicker = sin(frameAge * 0.5).toFloat() * 4f
        val flameTop = y + height - 4f
        val flameH = 14f + flicker

        flamePaint.color = Color.parseColor("#FFEB3B")
        canvas.drawOval(cx - 10f, flameTop, cx + 10f, flameTop + flameH, flamePaint)
        flamePaint.color = Color.parseColor("#FF9800")
        canvas.drawOval(cx - 6f, flameTop + 2f, cx + 6f, flameTop + flameH + 4f, flamePaint)
    }

    private fun drawJet(canvas: Canvas) {
        val bmp = currentBitmap
        if (bmp != null) {
            val dst = RectF(x, y, x + width, y + height)
            canvas.drawBitmap(bmp, null, dst, bitmapPaint)
        } else {
            drawFallbackShape(canvas)
        }
    }

    private fun drawFallbackShape(canvas: Canvas) {
        val fallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4CAF50")
            style = Paint.Style.FILL
        }
        val cx = x + width / 2f
        val path = android.graphics.Path().apply {
            moveTo(cx, y)
            lineTo(x + width, y + height * 0.7f)
            lineTo(x + width - 10f, y + height)
            lineTo(x + 10f, y + height)
            lineTo(x, y + height * 0.7f)
            close()
        }
        canvas.drawPath(path, fallbackPaint)
    }

    private fun drawShield(canvas: Canvas, cx: Float) {
        val now = System.currentTimeMillis()
        if (!shielded && now >= continueShieldUntilMs) return
        val pulse = 1f + sin(frameAge * 0.12).toFloat() * 0.05f
        val shieldR = width * 0.7f * pulse
        canvas.drawCircle(cx, y + height * 0.4f, shieldR, shieldPaint)
        canvas.drawCircle(cx, y + height * 0.4f, shieldR, shieldStrokePaint)
    }

    private fun drawHealthBar(canvas: Canvas) {
        val barWidth = width + 20f
        val barHeight = 8f
        val barX = x - 10f
        val barY = y + height + 8f
        val fraction = health.toFloat() / maxHealth

        canvas.drawRoundRect(barX, barY, barX + barWidth, barY + barHeight, 4f, 4f, healthBarBgPaint)

        healthBarPaint.color = when {
            fraction > 0.5f -> Color.parseColor("#4CAF50")
            fraction > 0.25f -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#F44336")
        }
        canvas.drawRoundRect(barX, barY, barX + barWidth * fraction, barY + barHeight, 4f, 4f, healthBarPaint)
    }

    val gunTipX: Float get() = x + width / 2f
    val gunTipY: Float get() = y - 15f
}
