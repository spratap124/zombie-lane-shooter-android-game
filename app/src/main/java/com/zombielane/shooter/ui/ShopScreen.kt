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

    var shooterCardRects = mutableListOf<RectF>()
    var backBtnRect = RectF()

    fun draw(
        canvas: Canvas,
        safeArea: RectF,
        totalCoins: Int,
        shooterManager: ShooterManager,
        playerAssets: PlayerAssets?,
        coinIcon: Bitmap,
        backButton: Bitmap
    ) {
        val w = canvas.width.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        canvas.drawRect(0f, 0f, w, canvas.height.toFloat(), bgPaint)

        titlePaint.textSize = 64f * s
        coinPaint.textSize = 44f * s
        namePaint.textSize = 50f * s
        tagPaint.textSize = 34f * s
        statusPaint.textSize = 36f * s
        detailPaint.textSize = 30f * s
        cardBorderPaint.strokeWidth = 4f * s

        val backSize = 68f * s
        backBtnRect.set(
            safeArea.left + 8f * s,
            safeArea.top + 8f * s,
            safeArea.left + 8f * s + backSize,
            safeArea.top + 8f * s + backSize
        )
        MenuUiAssets.drawBackButton(canvas, backBtnRect, backButton)

        var yPos = safeArea.top + 8f * s + backSize + 20f * s

        val titleBaseline = yPos + 58f * s
        canvas.drawText("WEAPON SHOP", cx, titleBaseline, titlePaint)
        val titleFm = titlePaint.fontMetrics
        // Fixed 24*s after baseline was shorter than the title's painted height — coin row overlapped "WEAPON SHOP".
        yPos = titleBaseline + titleFm.descent + 36f * s

        val headerCoinD = 52f * s
        drawCoin(canvas, coinIcon, cx - 58f * s, yPos - 12f * s, headerCoinD)
        canvas.drawText("$totalCoins", cx + 18f * s, yPos, coinPaint)
        yPos += 52f * s

        val allShooters = Shooter.ALL
        val cardH = 188f * s
        val cardGap = 28f * s
        val cardMargin = 14f * s
        val cardLeft = safeArea.left + cardMargin
        val cardRight = safeArea.right - cardMargin
        val iconSize = 120f * s

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
            canvas.drawRoundRect(rect, 24f * s, 24f * s, cardPaint)

            if (isEquipped) {
                cardBorderPaint.color = shooter.bulletColor
                canvas.drawRoundRect(rect, 24f * s, 24f * s, cardBorderPaint)
            }

            val iconLeft = cardLeft + 20f * s
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
                canvas.drawCircle(iconLeft + iconSize / 2f, yPos + cardH / 2f, 32f * s, dotPaint)
            }

            val textLeft = iconLeft + iconSize + 26f * s
            val nameY = yPos + cardH * 0.33f
            namePaint.color = if (isAvailable) Color.WHITE else Color.parseColor("#607D8B")
            canvas.drawText(shooter.name, textLeft, nameY, namePaint)

            tagPaint.color = if (isAvailable) Color.parseColor("#90A4AE") else Color.parseColor("#455A64")
            canvas.drawText(shooter.tagline, textLeft, nameY + 42f * s, tagPaint)

            val fireLabel = "Fire: ${shooter.baseFireRateMs}ms"
            val dmgLabel = "Dmg: x${shooter.damageMultiplier}"
            detailPaint.color = if (isAvailable) Color.parseColor("#607D8B") else Color.parseColor("#37474F")
            canvas.drawText("$fireLabel  |  $dmgLabel", textLeft, nameY + 80f * s, detailPaint)

            val statusX = cardRight - 26f * s
            val statusY = yPos + cardH / 2f + 12f * s
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
                    val costBaseline = statusY - 18f * s
                    val costTw = statusPaint.measureText(costStr)
                    val rowCoinD = 44f * s
                    val coinGap = 10f * s
                    val coinCx = statusX - costTw - coinGap - rowCoinD / 2f
                    val coinCy = costBaseline - 11f * s
                    drawCoin(canvas, coinIcon, coinCx, coinCy, rowCoinD)
                    canvas.drawText(costStr, statusX, costBaseline, statusPaint)
                    statusPaint.color = Color.parseColor("#78909C")
                    statusPaint.textSize = 28f * s
                    canvas.drawText("or Watch Ad", statusX, statusY + 18f * s, statusPaint)
                    statusPaint.textSize = 36f * s
                }
            }

            yPos += cardH + cardGap
        }
    }

    private fun drawCoin(canvas: Canvas, bmp: Bitmap, centerX: Float, centerY: Float, diameter: Float) {
        val r = diameter / 2f
        val dst = RectF(centerX - r, centerY - r, centerX + r, centerY + r)
        canvas.drawBitmap(bmp, null, dst, bitmapPaint)
    }

    private fun formatCost(cost: Int): String = when {
        cost >= 1_000_000 -> "${cost / 1_000_000}M"
        cost >= 1_000 -> "${cost / 1_000}K"
        else -> "$cost"
    }
}
