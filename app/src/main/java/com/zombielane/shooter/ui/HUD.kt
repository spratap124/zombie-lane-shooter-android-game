package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.zombielane.shooter.data.UpgradeManager
import com.zombielane.shooter.engine.ComboTracker
import com.zombielane.shooter.engine.GameEventManager

class HUD {

    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val coinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        textSize = 44f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val coinIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        style = Paint.Style.FILL
    }

    private val comboPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFEB3B")
        textSize = 56f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(5f, 2f, 2f, Color.BLACK)
    }

    private val eventBannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val eventBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val pauseBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#55FFFFFF")
        style = Paint.Style.FILL
    }

    private val pauseIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val fpsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#66FFFFFF")
        textSize = 22f
        typeface = Typeface.MONOSPACE
    }

    var pauseBtnRect = RectF()

    fun drawGameHud(
        canvas: Canvas,
        score: Int,
        sessionCoins: Int,
        playerHealth: Int,
        playerMaxHealth: Int,
        safeArea: RectF,
        comboTracker: ComboTracker,
        eventManager: GameEventManager,
        showFps: Boolean,
        fps: Int
    ) {
        val left = safeArea.left
        val top = safeArea.top
        val right = safeArea.right
        val w = canvas.width.toFloat()

        canvas.drawText("SCORE: $score", left, top + 44f, scorePaint)

        val coinY = top + 96f
        canvas.drawCircle(left + 16f, coinY - 12f, 14f, coinIconPaint)
        canvas.drawText("$sessionCoins", left + 42f, coinY, coinPaint)

        // Pause button (top-right area, below hearts)
        val pauseSize = 48f
        val pauseX = right - pauseSize
        val pauseY = top + 70f
        pauseBtnRect = RectF(pauseX - 8f, pauseY - 8f, pauseX + pauseSize + 8f, pauseY + pauseSize + 8f)
        canvas.drawRoundRect(pauseBtnRect, 12f, 12f, pauseBtnPaint)
        val barW = 10f
        val barH = 28f
        val barGap = 6f
        val bx = pauseX + pauseSize / 2f
        val by = pauseY + pauseSize / 2f
        canvas.drawRoundRect(bx - barGap - barW, by - barH / 2f, bx - barGap, by + barH / 2f, 3f, 3f, pauseIconPaint)
        canvas.drawRoundRect(bx + barGap, by - barH / 2f, bx + barGap + barW, by + barH / 2f, 3f, 3f, pauseIconPaint)

        // Hearts
        val heartSize = 28f
        val heartSpacing = 36f
        val heartsStartX = right - (playerMaxHealth * heartSpacing)
        for (i in 0 until playerMaxHealth) {
            val hx = heartsStartX + i * heartSpacing
            val hy = top + 30f
            drawHeart(canvas, hx, hy, heartSize, i < playerHealth)
        }

        // Combo
        if (comboTracker.isVisible) {
            comboPaint.color = when {
                comboTracker.displayCombo >= 10 -> Color.parseColor("#FF5722")
                comboTracker.displayCombo >= 5 -> Color.parseColor("#FFD600")
                else -> Color.parseColor("#4CAF50")
            }
            canvas.drawText("x${comboTracker.displayCombo} COMBO!", w / 2f, top + 52f, comboPaint)
        }

        // Event banner
        if (eventManager.isActive) {
            val bannerY = top + 120f
            val bannerH = 44f
            eventBgPaint.color = Color.parseColor("#44000000")
            canvas.drawRoundRect(w * 0.15f, bannerY, w * 0.85f, bannerY + bannerH, 12f, 12f, eventBgPaint)
            eventBannerPaint.color = Color.parseColor("#FFEB3B")
            canvas.drawText(eventManager.bannerText ?: "", w / 2f, bannerY + 32f, eventBannerPaint)
        }

        if (showFps) {
            canvas.drawText("${fps} FPS", left, safeArea.bottom - 8f, fpsPaint)
        }
    }

    private fun drawHeart(canvas: Canvas, x: Float, y: Float, size: Float, filled: Boolean) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (filled) Color.parseColor("#F44336") else Color.parseColor("#44F44336")
            style = Paint.Style.FILL
        }
        val r = size / 4f
        canvas.drawCircle(x + r, y, r, paint)
        canvas.drawCircle(x + 3 * r, y, r, paint)

        val trianglePath = Path().apply {
            moveTo(x, y)
            lineTo(x + size / 2f, y + size * 0.6f)
            lineTo(x + size, y)
            close()
        }
        canvas.drawPath(trianglePath, paint)
    }
}
