package com.zombielane.shooter.ui

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
        textSize = 56f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(6f, 3f, 3f, Color.BLACK)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
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

    private val backBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#37474F")
        style = Paint.Style.FILL
    }

    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 34f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val warnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EF9A9A")
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }

    data class ToggleRow(val label: String, val rect: RectF, val enabled: Boolean)

    var toggleRects = mutableListOf<RectF>()
    var resetBtnRect = RectF()
    var backBtnRect = RectF()
    var confirmResetActive = false

    fun draw(canvas: Canvas, safeArea: RectF, settings: SettingsManager) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f

        canvas.drawRect(0f, 0f, w, h, overlayPaint)

        var yPos = safeArea.top + 60f
        canvas.drawText("SETTINGS", cx, yPos, titlePaint)

        yPos += 80f
        toggleRects.clear()

        val toggles = listOf(
            ToggleRow("Sound", RectF(), settings.soundEnabled),
            ToggleRow("Music", RectF(), settings.musicEnabled),
            ToggleRow("Vibration", RectF(), settings.vibrationEnabled),
            ToggleRow("Show FPS", RectF(), settings.showFps)
        )

        val toggleW = 80f
        val toggleH = 40f
        val rowH = 70f
        val leftPad = safeArea.left + 20f
        val rightPad = safeArea.right - 20f

        for (toggle in toggles) {
            canvas.drawText(toggle.label, leftPad, yPos + 28f, labelPaint)

            val tRect = RectF(rightPad - toggleW, yPos + 4f, rightPad, yPos + 4f + toggleH)
            toggleRects.add(tRect)

            val bgPaint = if (toggle.enabled) toggleOnPaint else toggleOffPaint
            canvas.drawRoundRect(tRect, toggleH / 2f, toggleH / 2f, bgPaint)

            val knobX = if (toggle.enabled) tRect.right - toggleH / 2f - 4f else tRect.left + toggleH / 2f + 4f
            canvas.drawCircle(knobX, tRect.centerY(), toggleH / 2f - 6f, toggleKnobPaint)

            yPos += rowH
        }

        // Reset progress
        yPos += 30f
        val resetW = safeArea.width() * 0.55f
        val resetH = 62f
        resetBtnRect = RectF(cx - resetW / 2f, yPos, cx + resetW / 2f, yPos + resetH)
        canvas.drawRoundRect(resetBtnRect, 16f, 16f, resetBtnPaint)

        val resetLabel = if (confirmResetActive) "TAP AGAIN TO CONFIRM" else "RESET PROGRESS"
        btnTextPaint.textSize = if (confirmResetActive) 26f else 30f
        canvas.drawText(resetLabel, cx, yPos + 40f, btnTextPaint)
        btnTextPaint.textSize = 34f

        if (confirmResetActive) {
            yPos += resetH + 8f
            canvas.drawText("This will erase all coins & upgrades!", cx, yPos, warnPaint)
        }

        // Back button
        yPos += resetH + 40f
        val backW = safeArea.width() * 0.45f
        val backH = 62f
        backBtnRect = RectF(cx - backW / 2f, yPos, cx + backW / 2f, yPos + backH)
        canvas.drawRoundRect(backBtnRect, 16f, 16f, backBtnPaint)
        canvas.drawText("BACK", cx, yPos + 42f, btnTextPaint)
    }
}
