package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.zombielane.shooter.engine.ComboTracker
import com.zombielane.shooter.engine.GameEventManager

class HUD {

    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val coinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val coinIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        style = Paint.Style.FILL
    }

    private val comboPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFEB3B")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(5f, 2f, 2f, Color.BLACK)
    }

    private val eventBannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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
        typeface = Typeface.MONOSPACE
    }

    private val shooterBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val shooterNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    private val tempTimerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF9800")
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    private val stageLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90A4AE")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    private val stageNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    private val progressBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33FFFFFF")
        style = Paint.Style.FILL
    }

    private val progressFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
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
        fps: Int,
        shooterName: String = "BASIC",
        shooterColor: Int = Color.parseColor("#FFEB3B"),
        tempRemainingMs: Long = -1L,
        stageNumber: Int = 1,
        stageName: String = "",
        stageProgress: Float = 0f,
        endlessRun: Boolean = false
    ) {
        val left = safeArea.left
        val top = safeArea.top
        val right = safeArea.right
        val w = canvas.width.toFloat()
        val s = w / 1080f

        scorePaint.textSize = 48f * s
        coinPaint.textSize = 44f * s
        comboPaint.textSize = 56f * s
        eventBannerPaint.textSize = 34f * s
        fpsPaint.textSize = 24f * s
        shooterNamePaint.textSize = 24f * s
        tempTimerPaint.textSize = 20f * s

        canvas.drawText("SCORE: $score", left, top + 48f * s, scorePaint)

        val coinY = top + 100f * s
        canvas.drawCircle(left + 18f * s, coinY - 12f * s, 15f * s, coinIconPaint)
        canvas.drawText("$sessionCoins", left + 44f * s, coinY, coinPaint)

        // Pause button
        val pauseSize = 52f * s
        val pauseX = right - pauseSize
        val pauseY = top + 74f * s
        pauseBtnRect = RectF(pauseX - 10f * s, pauseY - 10f * s, pauseX + pauseSize + 10f * s, pauseY + pauseSize + 10f * s)
        canvas.drawRoundRect(pauseBtnRect, 12f * s, 12f * s, pauseBtnPaint)
        val barW = 11f * s
        val barH = 30f * s
        val barGap = 7f * s
        val bx = pauseX + pauseSize / 2f
        val by = pauseY + pauseSize / 2f
        canvas.drawRoundRect(bx - barGap - barW, by - barH / 2f, bx - barGap, by + barH / 2f, 3f * s, 3f * s, pauseIconPaint)
        canvas.drawRoundRect(bx + barGap, by - barH / 2f, bx + barGap + barW, by + barH / 2f, 3f * s, 3f * s, pauseIconPaint)

        // Hearts
        val heartSize = 30f * s
        val heartSpacing = 38f * s
        val heartsStartX = right - (playerMaxHealth * heartSpacing)
        for (i in 0 until playerMaxHealth) {
            val hx = heartsStartX + i * heartSpacing
            val hy = top + 32f * s
            drawHeart(canvas, hx, hy, heartSize, i < playerHealth)
        }

        // Shooter badge (top-left, below coins)
        val badgeW = 110f * s
        val badgeH = if (tempRemainingMs > 0) 50f * s else 34f * s
        val badgeX = left
        val badgeY = top + 130f * s
        shooterBadgePaint.color = Color.parseColor("#33FFFFFF")
        canvas.drawRoundRect(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, 8f * s, 8f * s, shooterBadgePaint)

        val dotX = badgeX + 16f * s
        val dotY = badgeY + 17f * s
        shooterBadgePaint.color = shooterColor
        canvas.drawCircle(dotX, dotY, 7f * s, shooterBadgePaint)

        shooterNamePaint.color = shooterColor
        canvas.drawText(shooterName, badgeX + badgeW / 2f + 6f * s, badgeY + 24f * s, shooterNamePaint)

        if (tempRemainingMs > 0) {
            val secs = (tempRemainingMs / 1000).toInt()
            val min = secs / 60
            val sec = secs % 60
            canvas.drawText("${min}:${sec.toString().padStart(2, '0')}", badgeX + badgeW / 2f + 6f * s, badgeY + 44f * s, tempTimerPaint)
        }

        // Combo
        if (comboTracker.isVisible) {
            comboPaint.color = when {
                comboTracker.displayCombo >= 10 -> Color.parseColor("#FF5722")
                comboTracker.displayCombo >= 5 -> Color.parseColor("#FFD600")
                else -> Color.parseColor("#4CAF50")
            }
            canvas.drawText("x${comboTracker.displayCombo} COMBO!", w / 2f, top + 56f * s, comboPaint)
        }

        // Event banner
        if (eventManager.isActive) {
            val bannerY = top + 128f * s
            val bannerH = 48f * s
            eventBgPaint.color = Color.parseColor("#44000000")
            canvas.drawRoundRect(w * 0.15f, bannerY, w * 0.85f, bannerY + bannerH, 12f * s, 12f * s, eventBgPaint)
            eventBannerPaint.color = Color.parseColor("#FFEB3B")
            canvas.drawText(eventManager.bannerText ?: "", w / 2f, bannerY + 34f * s, eventBannerPaint)
        }

        // Stage indicator (top-center, below combo/event area)
        stageLabelPaint.textSize = 20f * s
        stageNamePaint.textSize = 18f * s
        val stageY = top + 100f * s
        canvas.drawText(
            if (endlessRun) "STAGE $stageNumber · ENDLESS" else "STAGE $stageNumber",
            w / 2f, stageY, stageLabelPaint
        )
        if (stageName.isNotEmpty()) {
            canvas.drawText(stageName, w / 2f, stageY + 18f * s, stageNamePaint)
        }
        val barW2 = 140f * s
        val barH2 = 6f * s
        val barX2 = w / 2f - barW2 / 2f
        val barY2 = stageY + 24f * s
        canvas.drawRoundRect(barX2, barY2, barX2 + barW2, barY2 + barH2, 3f * s, 3f * s, progressBgPaint)
        progressFillPaint.color = if (stageProgress >= 1f) Color.parseColor("#FFD600") else Color.parseColor("#4CAF50")
        canvas.drawRoundRect(barX2, barY2, barX2 + barW2 * stageProgress, barY2 + barH2, 3f * s, 3f * s, progressFillPaint)

        if (showFps) {
            canvas.drawText("${fps} FPS", left, safeArea.bottom - 50f * s, fpsPaint)
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
