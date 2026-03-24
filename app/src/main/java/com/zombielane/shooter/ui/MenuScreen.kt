package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.zombielane.shooter.data.Shooter
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

    private val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78909C")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(2f, 1f, 1f, Color.BLACK)
    }

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val cardIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val cardNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val cardTagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val cardStatusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }

    var playBtnRect = RectF()
    var settingsBtnRect = RectF()
    var shooterBtnRects = mutableListOf<RectF>()

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
        sectionPaint.textSize = 28f * s
        btnTextPaint.textSize = 44f * s
        settingsTextPaint.textSize = 38f * s
        infoPaint.textSize = 28f * s
        cardNamePaint.textSize = 22f * s
        cardTagPaint.textSize = 17f * s
        cardStatusPaint.textSize = 18f * s
        cardBorderPaint.strokeWidth = 3f * s

        var yPos = safeArea.top + safeArea.height() * 0.08f

        val mascotSize = 90f * s
        val zombieY = yPos + 10f * s + sin(frameCount * 0.05).toFloat() * 12f * s
        drawZombieMascot(canvas, cx, zombieY, mascotSize)

        yPos += mascotSize + 40f * s

        canvas.drawText("ZOMBIE LANE", cx, yPos, titlePaint)
        yPos += 52f * s
        canvas.drawText("SHOOTER", cx, yPos, subtitlePaint)

        yPos += 58f * s
        if (highScore > 0) {
            canvas.drawText("BEST: $highScore", cx, yPos, highScorePaint)
            yPos += 40f * s
        }
        canvas.drawCircle(cx - 55f * s, yPos - 10f * s, 14f * s, coinIconPaint)
        canvas.drawText("$totalCoins", cx + 12f * s, yPos, coinPaint)

        yPos += 52f * s
        canvas.drawText("SELECT WEAPON", cx, yPos, sectionPaint)
        yPos += 18f * s
        val cardH = 100f * s
        drawShooterPanel(canvas, safeArea, yPos, shooterManager, s, cardH)
        yPos += cardH

        yPos += 28f * s
        val playW = safeArea.width() * 0.65f
        val playH = 80f * s
        playBtnRect = RectF(cx - playW / 2f, yPos, cx + playW / 2f, yPos + playH)

        val playPulse = 1f + sin(frameCount * 0.06).toFloat() * 0.02f
        val pRect = RectF(
            cx - playW / 2f * playPulse, yPos - (playH * playPulse - playH) / 2f,
            cx + playW / 2f * playPulse, yPos + playH + (playH * playPulse - playH) / 2f
        )
        canvas.drawRoundRect(pRect, 20f * s, 20f * s, playBtnPaint)
        canvas.drawText("PLAY", cx, yPos + playH * 0.66f, btnTextPaint)

        yPos += playH + 26f * s
        val settingsW = safeArea.width() * 0.50f
        val settingsH = 66f * s
        settingsBtnRect = RectF(cx - settingsW / 2f, yPos, cx + settingsW / 2f, yPos + settingsH)
        canvas.drawRoundRect(settingsBtnRect, 16f * s, 16f * s, settingsBtnPaint)
        canvas.drawText("SETTINGS", cx, yPos + settingsH * 0.65f, settingsTextPaint)

        val footerY = safeArea.bottom - 16f * s
        canvas.drawText("v1.0", cx, footerY, infoPaint)
    }

    private fun drawShooterPanel(canvas: Canvas, safeArea: RectF, topY: Float, shooterManager: ShooterManager, s: Float, cardH: Float) {
        val allShooters = Shooter.ALL
        val gap = 8f * s
        val totalGap = gap * (allShooters.size - 1)
        val cardW = (safeArea.width() - totalGap) / allShooters.size

        shooterBtnRects.clear()

        for (i in allShooters.indices) {
            val shooter = allShooters[i]
            val st = shooter.type
            val x = safeArea.left + i * (cardW + gap)
            val rect = RectF(x, topY, x + cardW, topY + cardH)
            shooterBtnRects.add(rect)

            val isEquipped = shooterManager.equipped == st
            val isUnlocked = shooterManager.isUnlocked(st)
            val isTemp = shooterManager.isTemporaryActive(st)
            val isAvailable = isUnlocked || isTemp

            cardPaint.color = if (isEquipped) Color.parseColor("#33FFFFFF")
                else if (isAvailable) Color.parseColor("#2A2A4A")
                else Color.parseColor("#1A1A30")
            canvas.drawRoundRect(rect, 10f * s, 10f * s, cardPaint)

            if (isEquipped) {
                cardBorderPaint.color = shooter.bulletColor
                canvas.drawRoundRect(rect, 10f * s, 10f * s, cardBorderPaint)
            }

            val ccx = rect.centerX()

            cardIconPaint.color = if (isAvailable) shooter.bulletColor else Color.parseColor("#455A64")
            canvas.drawCircle(ccx, topY + cardH * 0.2f, 10f * s, cardIconPaint)

            cardNamePaint.color = if (isAvailable) Color.WHITE else Color.parseColor("#607D8B")
            canvas.drawText(shooter.name, ccx, topY + cardH * 0.46f, cardNamePaint)

            cardTagPaint.color = if (isAvailable) Color.parseColor("#90A4AE") else Color.parseColor("#455A64")
            canvas.drawText(shooter.tagline, ccx, topY + cardH * 0.62f, cardTagPaint)

            when {
                isEquipped -> {
                    cardStatusPaint.color = shooter.bulletColor
                    canvas.drawText("EQUIPPED", ccx, topY + cardH * 0.86f, cardStatusPaint)
                }
                isTemp -> {
                    val secs = (shooterManager.getRemainingTempMs(st) / 1000).toInt()
                    val min = secs / 60; val sec = secs % 60
                    cardStatusPaint.color = Color.parseColor("#FF9800")
                    canvas.drawText("${min}:${sec.toString().padStart(2, '0')}", ccx, topY + cardH * 0.86f, cardStatusPaint)
                }
                isUnlocked -> {
                    cardStatusPaint.color = Color.parseColor("#4CAF50")
                    canvas.drawText("SELECT", ccx, topY + cardH * 0.86f, cardStatusPaint)
                }
                else -> {
                    cardStatusPaint.color = Color.parseColor("#FFD600")
                    canvas.drawText("${shooter.unlockCost}", ccx, topY + cardH * 0.86f, cardStatusPaint)
                }
            }
        }
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
