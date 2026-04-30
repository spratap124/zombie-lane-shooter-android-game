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

    companion object {
        /** Extra space below [safeArea.top] so score/stage/hearts clear the bezel and notch. */
        private const val EXTRA_TOP_PAD_FACTOR = 26f // multiplied by [s] (1080-ref scale)
    }

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
        color = Color.parseColor("#FFB74D")
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 1f, 2f, Color.BLACK)
    }

    private val stageLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#ECEFF1")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 1f, 2f, Color.BLACK)
    }

    private val stageNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 1f, 2f, Color.BLACK)
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
        val t = top + EXTRA_TOP_PAD_FACTOR * s

        scorePaint.textSize = 48f * s
        coinPaint.textSize = 44f * s
        comboPaint.textSize = 56f * s
        eventBannerPaint.textSize = 34f * s
        fpsPaint.textSize = 24f * s
        shooterNamePaint.textSize = 28f * s
        tempTimerPaint.textSize = 32f * s

        canvas.drawText("SCORE: $score", left, t + 48f * s, scorePaint)

        // Below enlarged top-center stage block
        val coinY = t + 128f * s
        canvas.drawCircle(left + 18f * s, coinY - 12f * s, 15f * s, coinIconPaint)
        canvas.drawText("$sessionCoins", left + 44f * s, coinY, coinPaint)

        // Pause button
        val pauseSize = 52f * s
        val pauseX = right - pauseSize
        val pauseY = t + 74f * s
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
            val hy = t + 32f * s
            drawHeart(canvas, hx, hy, heartSize, i < playerHealth)
        }

        // Shooter badge (top-left, below coins) — two clear rows when temp timer is active
        val badgeX = left
        val badgeY = t + 158f * s
        val badgeW = 152f * s
        val badgePadV = 10f * s
        val dotR = 7f * s
        val dotX = badgeX + 14f * s

        shooterNamePaint.textSize = 28f * s
        tempTimerPaint.textSize = 30f * s

        val badgeH: Float
        val nameBaseline: Float
        val dotY: Float

        if (tempRemainingMs > 0) {
            val fmName = shooterNamePaint.fontMetrics
            val fmTime = tempTimerPaint.fontMetrics
            val rowGap = 8f * s
            nameBaseline = badgeY + badgePadV - fmName.ascent
            val timerBaseline = nameBaseline + fmName.descent + rowGap - fmTime.ascent
            badgeH = (timerBaseline + fmTime.descent + badgePadV - badgeY).coerceAtLeast(64f * s)
            dotY = nameBaseline + (fmName.ascent + fmName.descent) / 2f

            shooterBadgePaint.color = Color.parseColor("#33FFFFFF")
            canvas.drawRoundRect(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, 10f * s, 10f * s, shooterBadgePaint)

            shooterBadgePaint.color = shooterColor
            canvas.drawCircle(dotX, dotY, dotR, shooterBadgePaint)

            shooterNamePaint.textAlign = Paint.Align.LEFT
            shooterNamePaint.color = shooterColor
            val nameStartX = badgeX + 28f * s
            canvas.drawText(shooterName, nameStartX, nameBaseline, shooterNamePaint)
            shooterNamePaint.textAlign = Paint.Align.CENTER

            val secs = (tempRemainingMs / 1000).toInt()
            val min = secs / 60
            val sec = secs % 60
            tempTimerPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                "${min}:${sec.toString().padStart(2, '0')}",
                badgeX + badgeW / 2f,
                timerBaseline,
                tempTimerPaint
            )
        } else {
            badgeH = 40f * s
            nameBaseline = badgeY + 26f * s
            dotY = badgeY + 20f * s

            shooterBadgePaint.color = Color.parseColor("#33FFFFFF")
            canvas.drawRoundRect(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, 10f * s, 10f * s, shooterBadgePaint)

            shooterBadgePaint.color = shooterColor
            canvas.drawCircle(dotX, dotY, dotR, shooterBadgePaint)

            shooterNamePaint.color = shooterColor
            canvas.drawText(shooterName, badgeX + badgeW / 2f + 6f * s, nameBaseline, shooterNamePaint)
        }

        // Combo
        if (comboTracker.isVisible) {
            comboPaint.color = when {
                comboTracker.displayCombo >= 10 -> Color.parseColor("#FF5722")
                comboTracker.displayCombo >= 5 -> Color.parseColor("#FFD600")
                else -> Color.parseColor("#4CAF50")
            }
            canvas.drawText("x${comboTracker.displayCombo} COMBO!", w / 2f, t + 56f * s, comboPaint)
        }

        // Event banner
        if (eventManager.isActive) {
            val bannerY = t + 148f * s
            val bannerH = 48f * s
            eventBgPaint.color = Color.parseColor("#44000000")
            canvas.drawRoundRect(w * 0.15f, bannerY, w * 0.85f, bannerY + bannerH, 12f * s, 12f * s, eventBgPaint)
            eventBannerPaint.color = Color.parseColor("#FFEB3B")
            canvas.drawText(eventManager.bannerText ?: "", w / 2f, bannerY + 34f * s, eventBannerPaint)
        }

        // Stage indicator (top-center; spaced below combo banner)
        stageLabelPaint.textSize = 34f * s
        stageNamePaint.textSize = 28f * s
        val stageTitleBaseline = t + 94f * s
        canvas.drawText(
            if (endlessRun) "STAGE $stageNumber · ENDLESS" else "STAGE $stageNumber",
            w / 2f, stageTitleBaseline, stageLabelPaint
        )
        val afterTitleY = if (stageName.isNotEmpty()) {
            val nameBaseline = stageTitleBaseline + 32f * s
            canvas.drawText(stageName, w / 2f, nameBaseline, stageNamePaint)
            nameBaseline
        } else {
            stageTitleBaseline
        }
        val barW2 = 220f * s
        val barH2 = 12f * s
        val barTop = afterTitleY + (if (stageName.isNotEmpty()) 14f * s else 10f * s)
        val barX2 = w / 2f - barW2 / 2f
        val barY2 = barTop
        val barR = 4f * s
        canvas.drawRoundRect(barX2, barY2, barX2 + barW2, barY2 + barH2, barR, barR, progressBgPaint)
        progressFillPaint.color = if (stageProgress >= 1f) Color.parseColor("#FFD600") else Color.parseColor("#4CAF50")
        canvas.drawRoundRect(barX2, barY2, barX2 + barW2 * stageProgress, barY2 + barH2, barR, barR, progressFillPaint)

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
