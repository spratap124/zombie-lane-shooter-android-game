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
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78909C")
        textAlign = Paint.Align.CENTER
    }

    private val backCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val backStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val backArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    var backBtnRect = RectF()
    var resumeBtnRect = RectF()
    var settingsBtnRect = RectF()
    var quitBtnRect = RectF()

    fun draw(canvas: Canvas, safeArea: RectF, score: Int, sessionCoins: Int) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        titlePaint.textSize = 68f * s
        btnTextPaint.textSize = 40f * s
        infoPaint.textSize = 32f * s

        canvas.drawRect(0f, 0f, w, h, overlayPaint)

        val backSize = 46f * s
        backBtnRect.set(
            safeArea.left + 8f * s,
            safeArea.top + 8f * s,
            safeArea.left + 8f * s + backSize,
            safeArea.top + 8f * s + backSize
        )
        backCirclePaint.color = Color.parseColor("#1A2238")
        canvas.drawRoundRect(backBtnRect, 12f * s, 12f * s, backCirclePaint)
        backStrokePaint.color = Color.parseColor("#3D5270")
        backStrokePaint.strokeWidth = 2f * s
        canvas.drawRoundRect(backBtnRect, 12f * s, 12f * s, backStrokePaint)
        backArrowPaint.textSize = 32f * s
        backArrowPaint.color = Color.WHITE
        backArrowPaint.setShadowLayer(5f * s, 0f, 2f * s, Color.BLACK)
        canvas.drawText("←", backBtnRect.centerX(), backBtnRect.centerY() + 11f * s, backArrowPaint)
        backArrowPaint.clearShadowLayer()

        var yPos = safeArea.top + safeArea.height() * 0.2f

        canvas.drawText("PAUSED", cx, yPos, titlePaint)

        yPos += 56f * s
        canvas.drawText("Score: $score   Coins: $sessionCoins", cx, yPos, infoPaint)

        yPos += 80f * s
        val btnW = safeArea.width() * 0.6f
        val btnH = 78f * s
        val gap = 28f * s

        resumeBtnRect = RectF(cx - btnW / 2f, yPos, cx + btnW / 2f, yPos + btnH)
        canvas.drawRoundRect(resumeBtnRect, 18f * s, 18f * s, resumeBtnPaint)
        canvas.drawText("RESUME", cx, yPos + btnH * 0.64f, btnTextPaint)

        yPos += btnH + gap
        settingsBtnRect = RectF(cx - btnW / 2f, yPos, cx + btnW / 2f, yPos + btnH)
        canvas.drawRoundRect(settingsBtnRect, 18f * s, 18f * s, settingsBtnPaint)
        canvas.drawText("SETTINGS", cx, yPos + btnH * 0.64f, btnTextPaint)

        yPos += btnH + gap
        quitBtnRect = RectF(cx - btnW / 2f, yPos, cx + btnW / 2f, yPos + btnH)
        canvas.drawRoundRect(quitBtnRect, 18f * s, 18f * s, quitBtnPaint)
        canvas.drawText("QUIT TO MENU", cx, yPos + btnH * 0.64f, btnTextPaint)
    }
}
