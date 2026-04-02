package com.zombielane.shooter.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.zombielane.shooter.data.SettingsManager

class SettingsScreen {

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#EE1B1B2F")
        style = Paint.Style.FILL
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(6f, 3f, 3f, Color.BLACK)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }

    private val toggleOnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val toggleOffPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#455A64")
        style = Paint.Style.FILL
    }

    private val toggleKnobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val resetBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D32F2F")
        style = Paint.Style.FILL
    }

    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val warnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EF9A9A")
        textAlign = Paint.Align.CENTER
    }

    var toggleRects = mutableListOf<RectF>()
    var resetBtnRect = RectF()
    var backBtnRect = RectF()
    var confirmResetActive = false

    fun draw(canvas: Canvas, safeArea: RectF, settings: SettingsManager, backButton: Bitmap) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        titlePaint.textSize = 60f * s
        labelPaint.textSize = 36f * s
        btnTextPaint.textSize = 36f * s
        warnPaint.textSize = 26f * s

        canvas.drawRect(0f, 0f, w, h, overlayPaint)

        val backSize = 68f * s
        backBtnRect.set(
            safeArea.left + 8f * s,
            safeArea.top + 8f * s,
            safeArea.left + 8f * s + backSize,
            safeArea.top + 8f * s + backSize
        )
        MenuUiAssets.drawBackButton(canvas, backBtnRect, backButton)

        var yPos = safeArea.top + 8f * s + backSize + 28f * s
        canvas.drawText("SETTINGS", cx, yPos, titlePaint)

        yPos += 90f * s
        toggleRects.clear()

        val toggles = listOf(
            "Sound" to settings.soundEnabled,
            "Music" to settings.musicEnabled,
            "Vibration" to settings.vibrationEnabled,
            "Show FPS" to settings.showFps
        )

        val toggleW = 88f * s
        val toggleH = 44f * s
        val rowH = 78f * s
        val leftPad = safeArea.left + 24f * s
        val rightPad = safeArea.right - 24f * s

        for ((label, enabled) in toggles) {
            canvas.drawText(label, leftPad, yPos + 32f * s, labelPaint)

            val tRect = RectF(rightPad - toggleW, yPos + 6f * s, rightPad, yPos + 6f * s + toggleH)
            toggleRects.add(tRect)

            val bgPaint = if (enabled) toggleOnPaint else toggleOffPaint
            canvas.drawRoundRect(tRect, toggleH / 2f, toggleH / 2f, bgPaint)

            val knobX = if (enabled) tRect.right - toggleH / 2f - 4f * s else tRect.left + toggleH / 2f + 4f * s
            canvas.drawCircle(knobX, tRect.centerY(), toggleH / 2f - 6f * s, toggleKnobPaint)

            yPos += rowH
        }

        yPos += 36f * s
        val resetW = safeArea.width() * 0.55f
        val resetH = 68f * s
        resetBtnRect = RectF(cx - resetW / 2f, yPos, cx + resetW / 2f, yPos + resetH)
        canvas.drawRoundRect(resetBtnRect, 16f * s, 16f * s, resetBtnPaint)

        val resetLabel = if (confirmResetActive) "TAP AGAIN TO CONFIRM" else "RESET PROGRESS"
        btnTextPaint.textSize = if (confirmResetActive) 28f * s else 34f * s
        canvas.drawText(resetLabel, cx, yPos + resetH * 0.62f, btnTextPaint)
        btnTextPaint.textSize = 36f * s

        if (confirmResetActive) {
            yPos += resetH + 10f * s
            canvas.drawText("This will erase all coins & upgrades!", cx, yPos, warnPaint)
        }
    }
}
