package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

class PauseScreen {

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#CC000000")
        style = Paint.Style.FILL
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 64f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(6f, 3f, 3f, Color.BLACK)
    }

    private val resumeBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val settingsBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#37474F")
        style = Paint.Style.FILL
    }

    private val quitBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D32F2F")
        style = Paint.Style.FILL
    }

    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78909C")
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    var resumeBtnRect = RectF()
    var settingsBtnRect = RectF()
    var quitBtnRect = RectF()

    fun draw(canvas: Canvas, safeArea: RectF, score: Int, sessionCoins: Int) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f

        canvas.drawRect(0f, 0f, w, h, overlayPaint)

        var yPos = safeArea.top + safeArea.height() * 0.2f

        canvas.drawText("PAUSED", cx, yPos, titlePaint)

        yPos += 50f
        canvas.drawText("Score: $score   Coins: $sessionCoins", cx, yPos, infoPaint)

        // Resume
        yPos += 70f
        val btnW = safeArea.width() * 0.6f
        val btnH = 72f
        val gap = 24f

        resumeBtnRect = RectF(cx - btnW / 2f, yPos, cx + btnW / 2f, yPos + btnH)
        canvas.drawRoundRect(resumeBtnRect, 18f, 18f, resumeBtnPaint)
        canvas.drawText("RESUME", cx, yPos + 48f, btnTextPaint)

        // Settings
        yPos += btnH + gap
        settingsBtnRect = RectF(cx - btnW / 2f, yPos, cx + btnW / 2f, yPos + btnH)
        canvas.drawRoundRect(settingsBtnRect, 18f, 18f, settingsBtnPaint)
        canvas.drawText("SETTINGS", cx, yPos + 48f, btnTextPaint)

        // Quit
        yPos += btnH + gap
        quitBtnRect = RectF(cx - btnW / 2f, yPos, cx + btnW / 2f, yPos + btnH)
        canvas.drawRoundRect(quitBtnRect, 18f, 18f, quitBtnPaint)
        canvas.drawText("QUIT TO MENU", cx, yPos + 48f, btnTextPaint)
    }
}
