package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.zombielane.shooter.data.ShooterManager
import kotlin.math.sin

class MenuScreen {

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(8f, 3f, 3f, Color.BLACK)
    }

    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val playBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val shopBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF9800")
        style = Paint.Style.FILL
    }

    private val settingsBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#37474F")
        style = Paint.Style.FILL
    }

    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val settingsTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#607D8B")
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }

    private val highScorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    private val coinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
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

    private val equippedLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78909C")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val equippedNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    var playBtnRect = RectF()
    var shopBtnRect = RectF()
    var settingsBtnRect = RectF()

    private var frameCount = 0L

    fun draw(canvas: Canvas, safeArea: RectF, highScore: Int, totalCoins: Int, shooterManager: ShooterManager) {
        frameCount++
        val w = canvas.width.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        titlePaint.textSize = 68f * s
        subtitlePaint.textSize = 40f * s
        highScorePaint.textSize = 36f * s
        coinPaint.textSize = 34f * s
        equippedLabelPaint.textSize = 26f * s
        equippedNamePaint.textSize = 34f * s
        btnTextPaint.textSize = 44f * s
        settingsTextPaint.textSize = 38f * s
        infoPaint.textSize = 28f * s

        var yPos = safeArea.top + safeArea.height() * 0.08f

        val mascotSize = 100f * s
        val zombieY = yPos + 10f * s + sin(frameCount * 0.05).toFloat() * 12f * s
        drawZombieMascot(canvas, cx, zombieY, mascotSize)

        yPos += mascotSize + 48f * s

        canvas.drawText("ZOMBIE LANE", cx, yPos, titlePaint)
        yPos += 56f * s
        canvas.drawText("SHOOTER", cx, yPos, subtitlePaint)

        yPos += 68f * s
        if (highScore > 0) {
            canvas.drawText("BEST: $highScore", cx, yPos, highScorePaint)
            yPos += 44f * s
        }
        canvas.drawCircle(cx - 55f * s, yPos - 10f * s, 14f * s, coinIconPaint)
        canvas.drawText("$totalCoins", cx + 12f * s, yPos, coinPaint)

        yPos += 64f * s
        val equipped = shooterManager.getEquipped()
        canvas.drawText("EQUIPPED", cx, yPos, equippedLabelPaint)
        yPos += 36f * s
        equippedNamePaint.color = equipped.bulletColor
        canvas.drawText(equipped.name, cx, yPos, equippedNamePaint)

        yPos += 64f * s
        val playW = safeArea.width() * 0.65f
        val playH = 84f * s
        playBtnRect = RectF(cx - playW / 2f, yPos, cx + playW / 2f, yPos + playH)

        val playPulse = 1f + sin(frameCount * 0.06).toFloat() * 0.02f
        val pRect = RectF(
            cx - playW / 2f * playPulse, yPos - (playH * playPulse - playH) / 2f,
            cx + playW / 2f * playPulse, yPos + playH + (playH * playPulse - playH) / 2f
        )
        canvas.drawRoundRect(pRect, 20f * s, 20f * s, playBtnPaint)
        canvas.drawText("PLAY", cx, yPos + playH * 0.66f, btnTextPaint)

        yPos += playH + 24f * s
        val shopW = safeArea.width() * 0.55f
        val shopH = 72f * s
        shopBtnRect = RectF(cx - shopW / 2f, yPos, cx + shopW / 2f, yPos + shopH)
        canvas.drawRoundRect(shopBtnRect, 18f * s, 18f * s, shopBtnPaint)
        btnTextPaint.textSize = 40f * s
        canvas.drawText("WEAPONS", cx, yPos + shopH * 0.65f, btnTextPaint)
        btnTextPaint.textSize = 44f * s

        yPos += shopH + 20f * s
        val settingsW = safeArea.width() * 0.50f
        val settingsH = 66f * s
        settingsBtnRect = RectF(cx - settingsW / 2f, yPos, cx + settingsW / 2f, yPos + settingsH)
        canvas.drawRoundRect(settingsBtnRect, 16f * s, 16f * s, settingsBtnPaint)
        canvas.drawText("SETTINGS", cx, yPos + settingsH * 0.65f, settingsTextPaint)

        val footerY = safeArea.bottom - 16f * s
        canvas.drawText("v1.0", cx, footerY, infoPaint)
    }

    private fun drawZombieMascot(canvas: Canvas, cx: Float, y: Float, size: Float) {
        val left = cx - size / 2f
        canvas.drawRoundRect(left, y, left + size, y + size, size * 0.15f, size * 0.15f, zombiePaint)
        canvas.drawCircle(cx - size * 0.2f, y + size * 0.38f, size * 0.175f, eyePaint)
        canvas.drawCircle(cx + size * 0.2f, y + size * 0.38f, size * 0.175f, eyePaint)
        canvas.drawCircle(cx - size * 0.2f, y + size * 0.40f, size * 0.088f, pupilPaint)
        canvas.drawCircle(cx + size * 0.2f, y + size * 0.40f, size * 0.088f, pupilPaint)
    }
}
