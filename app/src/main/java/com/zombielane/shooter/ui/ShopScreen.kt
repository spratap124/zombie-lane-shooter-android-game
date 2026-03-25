package com.zombielane.shooter.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.zombielane.shooter.data.Shooter
import com.zombielane.shooter.data.ShooterManager
import com.zombielane.shooter.data.ShooterType
import com.zombielane.shooter.objects.PlayerAssets

class ShopScreen {

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#FF1B1B2F")
        style = Paint.Style.FILL
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(8f, 3f, 3f, Color.BLACK)
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

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }

    private val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90A4AE")
        textAlign = Paint.Align.LEFT
    }

    private val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.RIGHT
    }

    private val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78909C")
        textAlign = Paint.Align.LEFT
    }

    private val backBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#37474F")
        style = Paint.Style.FILL
    }

    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    var shooterCardRects = mutableListOf<RectF>()
    var backBtnRect = RectF()

    fun draw(
        canvas: Canvas,
        safeArea: RectF,
        totalCoins: Int,
        shooterManager: ShooterManager,
        playerAssets: PlayerAssets?
    ) {
        val w = canvas.width.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        canvas.drawRect(0f, 0f, w, canvas.height.toFloat(), bgPaint)

        titlePaint.textSize = 52f * s
        coinPaint.textSize = 34f * s
        namePaint.textSize = 36f * s
        tagPaint.textSize = 26f * s
        statusPaint.textSize = 28f * s
        detailPaint.textSize = 22f * s
        btnTextPaint.textSize = 38f * s
        cardBorderPaint.strokeWidth = 3f * s

        var yPos = safeArea.top + 24f * s

        canvas.drawText("WEAPON SHOP", cx, yPos + 48f * s, titlePaint)
        yPos += 48f * s + 32f * s

        canvas.drawCircle(cx - 50f * s, yPos - 10f * s, 14f * s, coinIconPaint)
        canvas.drawText("$totalCoins", cx + 12f * s, yPos, coinPaint)
        yPos += 40f * s

        val allShooters = Shooter.ALL
        val cardH = 110f * s
        val cardGap = 14f * s
        val cardMargin = 20f * s
        val cardLeft = safeArea.left + cardMargin
        val cardRight = safeArea.right - cardMargin
        val iconSize = 70f * s

        shooterCardRects.clear()

        for (shooter in allShooters) {
            val st = shooter.type
            val rect = RectF(cardLeft, yPos, cardRight, yPos + cardH)
            shooterCardRects.add(rect)

            val isEquipped = shooterManager.equipped == st
            val isUnlocked = shooterManager.isUnlocked(st)
            val isTemp = shooterManager.isTemporaryActive(st)
            val isAvailable = isUnlocked || isTemp

            cardPaint.color = when {
                isEquipped -> Color.parseColor("#2A3A4A")
                isAvailable -> Color.parseColor("#222240")
                else -> Color.parseColor("#181830")
            }
            canvas.drawRoundRect(rect, 16f * s, 16f * s, cardPaint)

            if (isEquipped) {
                cardBorderPaint.color = shooter.bulletColor
                canvas.drawRoundRect(rect, 16f * s, 16f * s, cardBorderPaint)
            }

            val iconLeft = cardLeft + 14f * s
            val iconTop = yPos + (cardH - iconSize) / 2f
            val bmp = playerAssets?.get(st)
            if (bmp != null) {
                val dst = RectF(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                canvas.drawBitmap(bmp, null, dst, bitmapPaint)
            } else {
                val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isAvailable) shooter.bulletColor else Color.parseColor("#455A64")
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(iconLeft + iconSize / 2f, yPos + cardH / 2f, 20f * s, dotPaint)
            }

            val textLeft = iconLeft + iconSize + 18f * s
            val nameY = yPos + cardH * 0.35f
            namePaint.color = if (isAvailable) Color.WHITE else Color.parseColor("#607D8B")
            canvas.drawText(shooter.name, textLeft, nameY, namePaint)

            tagPaint.color = if (isAvailable) Color.parseColor("#90A4AE") else Color.parseColor("#455A64")
            canvas.drawText(shooter.tagline, textLeft, nameY + 30f * s, tagPaint)

            val fireLabel = "Fire: ${shooter.baseFireRateMs}ms"
            val dmgLabel = "Dmg: x${shooter.damageMultiplier}"
            detailPaint.color = if (isAvailable) Color.parseColor("#607D8B") else Color.parseColor("#37474F")
            canvas.drawText("$fireLabel  |  $dmgLabel", textLeft, nameY + 56f * s, detailPaint)

            val statusX = cardRight - 18f * s
            val statusY = yPos + cardH / 2f + 8f * s
            when {
                isEquipped -> {
                    statusPaint.color = shooter.bulletColor
                    canvas.drawText("EQUIPPED", statusX, statusY, statusPaint)
                }
                isTemp -> {
                    val secs = (shooterManager.getRemainingTempMs(st) / 1000).toInt()
                    val min = secs / 60; val sec = secs % 60
                    statusPaint.color = Color.parseColor("#FF9800")
                    canvas.drawText("${min}:${sec.toString().padStart(2, '0')}", statusX, statusY, statusPaint)
                }
                isUnlocked -> {
                    statusPaint.color = Color.parseColor("#4CAF50")
                    canvas.drawText("SELECT", statusX, statusY, statusPaint)
                }
                else -> {
                    statusPaint.color = Color.parseColor("#FFD600")
                    val costStr = formatCost(shooter.unlockCost)
                    canvas.drawText(costStr, statusX, statusY - 14f * s, statusPaint)
                    statusPaint.color = Color.parseColor("#78909C")
                    statusPaint.textSize = 22f * s
                    canvas.drawText("or Watch Ad", statusX, statusY + 14f * s, statusPaint)
                    statusPaint.textSize = 28f * s
                }
            }

            yPos += cardH + cardGap
        }

        yPos += 20f * s
        val backW = safeArea.width() * 0.50f
        val backH = 66f * s
        backBtnRect = RectF(cx - backW / 2f, yPos, cx + backW / 2f, yPos + backH)
        canvas.drawRoundRect(backBtnRect, 16f * s, 16f * s, backBtnPaint)
        canvas.drawText("BACK", cx, yPos + backH * 0.65f, btnTextPaint)
    }

    private fun formatCost(cost: Int): String = when {
        cost >= 1_000_000 -> "${cost / 1_000_000}M"
        cost >= 1_000 -> "${cost / 1_000}K"
        else -> "$cost"
    }
}
