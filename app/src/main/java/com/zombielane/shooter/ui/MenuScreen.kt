package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import kotlin.math.sin

class MenuScreen {

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 64f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(8f, 3f, 3f, Color.BLACK)
    }

    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        textSize = 36f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val playBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val settingsBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#37474F")
        style = Paint.Style.FILL
    }

    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 42f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val settingsTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 34f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#607D8B")
        textSize = 28f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }

    private val highScorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        textSize = 32f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    private val coinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        textSize = 30f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val coinIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        style = Paint.Style.FILL
    }

    private val zombiePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7B1FA2")
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

    var playBtnRect = RectF()
    var settingsBtnRect = RectF()

    private var frameCount = 0L

    fun draw(canvas: Canvas, safeArea: RectF, highScore: Int, totalCoins: Int) {
        frameCount++
        val w = canvas.width.toFloat()
        val cx = w / 2f

        var yPos = safeArea.top + safeArea.height() * 0.12f

        // Animated zombie mascot
        val zombieY = yPos + 10f + sin(frameCount * 0.05) .toFloat() * 12f
        drawZombieMascot(canvas, cx, zombieY)

        yPos += 130f

        // Title
        canvas.drawText("ZOMBIE LANE", cx, yPos, titlePaint)
        yPos += 52f
        canvas.drawText("SHOOTER", cx, yPos, subtitlePaint)

        // High score
        yPos += 70f
        if (highScore > 0) {
            canvas.drawText("BEST: $highScore", cx, yPos, highScorePaint)
            yPos += 40f
        }

        // Coins
        canvas.drawCircle(cx - 50f, yPos - 10f, 12f, coinIconPaint)
        canvas.drawText("$totalCoins", cx + 10f, yPos, coinPaint)

        // Play button
        yPos += 80f
        val playW = safeArea.width() * 0.65f
        val playH = 80f
        playBtnRect = RectF(cx - playW / 2f, yPos, cx + playW / 2f, yPos + playH)

        val playPulse = 1f + sin(frameCount * 0.06).toFloat() * 0.02f
        val pRect = RectF(
            cx - playW / 2f * playPulse, yPos - (playH * playPulse - playH) / 2f,
            cx + playW / 2f * playPulse, yPos + playH + (playH * playPulse - playH) / 2f
        )
        canvas.drawRoundRect(pRect, 20f, 20f, playBtnPaint)
        canvas.drawText("PLAY", cx, yPos + 54f, btnTextPaint)

        // Settings button
        yPos += playH + 30f
        val settingsW = safeArea.width() * 0.50f
        val settingsH = 64f
        settingsBtnRect = RectF(cx - settingsW / 2f, yPos, cx + settingsW / 2f, yPos + settingsH)
        canvas.drawRoundRect(settingsBtnRect, 16f, 16f, settingsBtnPaint)
        canvas.drawText("SETTINGS", cx, yPos + 43f, settingsTextPaint)

        // Footer
        val footerY = safeArea.bottom - 16f
        canvas.drawText("v1.0", cx, footerY, infoPaint)
    }

    private fun drawZombieMascot(canvas: Canvas, cx: Float, y: Float) {
        val s = 80f
        val left = cx - s / 2f

        canvas.drawRoundRect(left, y, left + s, y + s, 12f, 12f, zombiePaint)

        canvas.drawCircle(cx - 16f, y + s * 0.38f, 14f, eyePaint)
        canvas.drawCircle(cx + 16f, y + s * 0.38f, 14f, eyePaint)
        canvas.drawCircle(cx - 16f, y + s * 0.40f, 7f, pupilPaint)
        canvas.drawCircle(cx + 16f, y + s * 0.40f, 7f, pupilPaint)
    }
}
