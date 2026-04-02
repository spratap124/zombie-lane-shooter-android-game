package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

/**
 * "Continue?" dialog after death when a rewarded continue is still available this run.
 */
class ContinueOfferScreen {

    private val dimPaint = Paint().apply {
        color = Color.parseColor("#CC000000")
        style = Paint.Style.FILL
    }

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E1E2E")
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00BCD4")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5")
        textAlign = Paint.Align.CENTER
    }

    private val watchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val watchDisabledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#37474F")
        style = Paint.Style.FILL
    }

    private val noPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#455A64")
        style = Paint.Style.FILL
    }

    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val backCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val backStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val backArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    var backBtnRect = RectF()
    var watchAdBtnRect = RectF()
    var noBtnRect = RectF()

    fun draw(canvas: Canvas, safeArea: RectF, adReady: Boolean) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        canvas.drawRect(0f, 0f, w, h, dimPaint)

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

        val cardW = (safeArea.width() * 0.82f).coerceAtMost(520f * s)
        val btnH = 52f * s
        val btnGap = 16f * s
        val innerPad = 22f * s
        val titleSubGap = 10f * s
        val subButtonGap = 22f * s

        titlePaint.textSize = 44f * s
        subPaint.textSize = 24f * s
        val titleFm = titlePaint.fontMetrics
        val subFm = subPaint.fontMetrics
        val titleBlock = titleFm.descent - titleFm.ascent
        val subBlock = subFm.descent - subFm.ascent

        val cardH = innerPad + titleBlock + titleSubGap + subBlock + subButtonGap + btnH + btnGap + btnH + innerPad
        val cardTop = safeArea.top + safeArea.height() * 0.30f
        val card = RectF(cx - cardW / 2f, cardTop, cx + cardW / 2f, cardTop + cardH)
        canvas.drawRoundRect(card, 20f * s, 20f * s, cardPaint)
        canvas.drawRoundRect(card, 20f * s, 20f * s, strokePaint)

        var baseline = card.top + innerPad - titleFm.ascent
        canvas.drawText("Continue?", cx, baseline, titlePaint)

        baseline = baseline + titleFm.descent + titleSubGap - subFm.ascent
        canvas.drawText("Watch a short ad to keep your run", cx, baseline, subPaint)

        val watchTop = baseline + subFm.descent + subButtonGap
        val btnW = cardW - 40f * s

        watchAdBtnRect.set(card.centerX() - btnW / 2f, watchTop, card.centerX() + btnW / 2f, watchTop + btnH)
        canvas.drawRoundRect(watchAdBtnRect, 14f * s, 14f * s, if (adReady) watchPaint else watchDisabledPaint)
        btnTextPaint.textSize = 28f * s
        btnTextPaint.color = if (adReady) Color.WHITE else Color.parseColor("#78909C")
        canvas.drawText(if (adReady) "Watch ad" else "Ad loading…", watchAdBtnRect.centerX(), watchAdBtnRect.centerY() + 10f * s, btnTextPaint)
        btnTextPaint.color = Color.WHITE

        val noTop = watchTop + btnH + btnGap
        noBtnRect.set(card.centerX() - btnW / 2f, noTop, card.centerX() + btnW / 2f, noTop + btnH)
        canvas.drawRoundRect(noBtnRect, 14f * s, 14f * s, noPaint)
        canvas.drawText("No", noBtnRect.centerX(), noBtnRect.centerY() + 10f * s, btnTextPaint)
    }
}
