package com.zombielane.shooter.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

/**
 * "Game over" dialog after death when a rewarded continue is still available this run.
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
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
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

    var backBtnRect = RectF()
    var watchAdBtnRect = RectF()
    var noBtnRect = RectF()

    fun draw(canvas: Canvas, safeArea: RectF, adReady: Boolean, backButton: Bitmap) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        canvas.drawRect(0f, 0f, w, h, dimPaint)

        val backSize = 68f * s
        backBtnRect.set(
            safeArea.left + 8f * s,
            safeArea.top + 8f * s,
            safeArea.left + 8f * s + backSize,
            safeArea.top + 8f * s + backSize
        )
        MenuUiAssets.drawBackButton(canvas, backBtnRect, backButton)

        val cardW = (safeArea.width() * 0.82f).coerceAtMost(520f * s)
        val btnH = 58f * s
        val btnGap = 16f * s
        val innerPad = 26f * s
        val titleSubGap = 12f * s
        val subButtonGap = 24f * s

        titlePaint.textSize = 52f * s
        subPaint.textSize = 30f * s
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
        canvas.drawText("Game over", cx, baseline, titlePaint)

        baseline = baseline + titleFm.descent + titleSubGap - subFm.ascent
        canvas.drawText("Watch a short ad to keep your run", cx, baseline, subPaint)

        val watchTop = baseline + subFm.descent + subButtonGap
        val btnW = cardW - 40f * s

        watchAdBtnRect.set(card.centerX() - btnW / 2f, watchTop, card.centerX() + btnW / 2f, watchTop + btnH)
        canvas.drawRoundRect(watchAdBtnRect, 14f * s, 14f * s, if (adReady) watchPaint else watchDisabledPaint)
        btnTextPaint.textSize = 34f * s
        btnTextPaint.color = if (adReady) Color.WHITE else Color.parseColor("#78909C")
        val btnFm = btnTextPaint.fontMetrics
        val watchBaseline =
            watchAdBtnRect.centerY() - (btnFm.ascent + btnFm.descent) / 2f
        canvas.drawText(if (adReady) "continue" else "Ad loading…", watchAdBtnRect.centerX(), watchBaseline, btnTextPaint)
        btnTextPaint.color = Color.WHITE

        val noTop = watchTop + btnH + btnGap
        noBtnRect.set(card.centerX() - btnW / 2f, noTop, card.centerX() + btnW / 2f, noTop + btnH)
        canvas.drawRoundRect(noBtnRect, 14f * s, 14f * s, noPaint)
        val noBaseline = noBtnRect.centerY() - (btnFm.ascent + btnFm.descent) / 2f
        canvas.drawText("No", noBtnRect.centerX(), noBaseline, btnTextPaint)
    }
}
