package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.zombielane.shooter.data.Shooter
import com.zombielane.shooter.data.ShooterManager
import com.zombielane.shooter.data.UpgradeManager

class GameOverScreen {

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#DD000000")
        style = Paint.Style.FILL
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F44336")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(8f, 3f, 3f, Color.BLACK)
    }

    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val highScorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val statLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78909C")
        textAlign = Paint.Align.CENTER
    }

    private val statValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
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

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2A2A4A")
        style = Paint.Style.FILL
    }

    private val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78909C")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }

    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val costPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val playBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val menuBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#37474F")
        style = Paint.Style.FILL
    }

    private val actionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val cardNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val cardStatusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }
    private val cardIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    var upgradeBtnRects = mutableListOf<RectF>()
    var shooterBtnRects = mutableListOf<RectF>()
    var playAgainBtnRect = RectF()
    var menuBtnRect = RectF()

    fun draw(
        canvas: Canvas,
        safeArea: RectF,
        score: Int,
        sessionCoins: Int,
        totalCoins: Int,
        maxCombo: Int,
        enemiesKilled: Int,
        timeSurvivedMs: Long,
        upgradeManager: UpgradeManager,
        shooterManager: ShooterManager
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val availableH = safeArea.height()

        // Scale factor: reference width is 1080px (standard 1080p phone)
        val s = w / 1080f

        canvas.drawRect(0f, 0f, w, h, overlayPaint)

        val sp = (availableH * 0.013f).coerceIn(8f * s, 18f * s)
        val btnHeight = (availableH * 0.058f).coerceIn(56f * s, 84f * s)
        val btnGap = (availableH * 0.008f).coerceIn(6f * s, 14f * s)
        val statBoxH = (availableH * 0.065f).coerceIn(50f * s, 75f * s)

        // Apply scale to all paint text sizes
        titlePaint.textSize = 72f * s
        scorePaint.textSize = 56f * s
        highScorePaint.textSize = 34f * s
        statLabelPaint.textSize = 26f * s
        statValuePaint.textSize = 38f * s
        sectionPaint.textSize = 26f * s
        btnTextPaint.textSize = 30f * s
        costPaint.textSize = 24f * s
        actionTextPaint.textSize = 38f * s
        cardNamePaint.textSize = 20f * s
        cardStatusPaint.textSize = 17f * s
        cardBorderPaint.strokeWidth = 3f * s

        var yPos = safeArea.top + sp

        // Title
        canvas.drawText("GAME OVER", cx, yPos + 56f * s, titlePaint)
        yPos += 56f * s + sp * 2f

        // Score
        yPos += 44f * s
        canvas.drawText("$score", cx, yPos, scorePaint)

        yPos += sp + 22f * s
        if (score >= upgradeManager.highScore) {
            highScorePaint.color = Color.parseColor("#FFD600")
            canvas.drawText("NEW HIGH SCORE!", cx, yPos, highScorePaint)
        } else {
            highScorePaint.color = Color.parseColor("#78909C")
            canvas.drawText("Best: ${upgradeManager.highScore}", cx, yPos, highScorePaint)
        }

        // Stats row
        yPos += sp * 2f + 8f * s
        val statWidth = safeArea.width() / 3f
        val stats = listOf("KILLS" to "$enemiesKilled", "COMBO" to "x$maxCombo", "TIME" to formatTime(timeSurvivedMs))
        val statsTop = yPos
        canvas.drawRoundRect(safeArea.left, statsTop, safeArea.right, statsTop + statBoxH, 12f * s, 12f * s, dividerPaint)
        for (i in stats.indices) {
            val sx = safeArea.left + statWidth * i + statWidth / 2f
            canvas.drawText(stats[i].first, sx, statsTop + statBoxH * 0.36f, statLabelPaint)
            canvas.drawText(stats[i].second, sx, statsTop + statBoxH * 0.78f, statValuePaint)
        }

        // Coins
        yPos = statsTop + statBoxH + sp + 10f * s
        coinPaint.textSize = 32f * s
        canvas.drawText("+$sessionCoins earned", cx, yPos, coinPaint)
        yPos += sp + 22f * s
        canvas.drawCircle(cx - 60f * s, yPos - 10f * s, 13f * s, coinIconPaint)
        coinPaint.textSize = 36f * s
        canvas.drawText("$totalCoins", cx, yPos, coinPaint)

        // ── WEAPONS SECTION ──
        yPos += sp * 2f + 6f * s
        canvas.drawText("WEAPONS", safeArea.left, yPos, sectionPaint)
        yPos += 8f * s
        val cardH = (availableH * 0.09f).coerceIn(70f * s, 100f * s)
        drawShooterPanel(canvas, safeArea, yPos, shooterManager, s, cardH)
        yPos += cardH + sp

        // ── UPGRADES SECTION ──
        canvas.drawText("UPGRADES", safeArea.left, yPos, sectionPaint)
        yPos += 8f * s + sp * 0.5f
        val btnWidth = (safeArea.width() * 0.92f).coerceAtMost(w * 0.82f)

        upgradeBtnRects.clear()
        val types = UpgradeManager.UpgradeType.entries

        for (type in types) {
            val btnX = cx - btnWidth / 2f
            val rect = RectF(btnX, yPos, btnX + btnWidth, yPos + btnHeight)
            upgradeBtnRects.add(rect)

            val canAfford = upgradeManager.canAfford(type)
            btnPaint.color = if (canAfford) Color.parseColor("#37474F") else Color.parseColor("#263238")
            canvas.drawRoundRect(rect, 12f * s, 12f * s, btnPaint)

            val label = when (type) {
                UpgradeManager.UpgradeType.DAMAGE -> "DAMAGE"
                UpgradeManager.UpgradeType.FIRE_RATE -> "FIRE RATE"
                UpgradeManager.UpgradeType.HEALTH -> "HEALTH"
            }

            btnTextPaint.color = if (canAfford) Color.WHITE else Color.parseColor("#607D8B")
            canvas.drawText("$label  ${upgradeManager.statSummary(type)}", cx, yPos + btnHeight * 0.42f, btnTextPaint)

            costPaint.color = if (canAfford) Color.parseColor("#FFD600") else Color.parseColor("#607D8B")
            canvas.drawText("${upgradeManager.upgradeCost(type)} coins", cx, yPos + btnHeight * 0.8f, costPaint)

            yPos += btnHeight + btnGap
        }

        // Action buttons
        yPos += sp * 0.5f
        val halfW = (safeArea.width() * 0.42f).coerceAtMost(w * 0.38f)
        val actionH = (availableH * 0.055f).coerceIn(52f * s, 72f * s)

        playAgainBtnRect = RectF(cx + 8f * s, yPos, cx + 8f * s + halfW, yPos + actionH)
        canvas.drawRoundRect(playAgainBtnRect, 16f * s, 16f * s, playBtnPaint)
        canvas.drawText("PLAY", playAgainBtnRect.centerX(), yPos + actionH * 0.66f, actionTextPaint)

        menuBtnRect = RectF(cx - 8f * s - halfW, yPos, cx - 8f * s, yPos + actionH)
        canvas.drawRoundRect(menuBtnRect, 16f * s, 16f * s, menuBtnPaint)
        canvas.drawText("MENU", menuBtnRect.centerX(), yPos + actionH * 0.66f, actionTextPaint)
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

            cardPaint.color = if (isAvailable) Color.parseColor("#2A2A4A") else Color.parseColor("#1A1A30")
            canvas.drawRoundRect(rect, 10f * s, 10f * s, cardPaint)

            if (isEquipped) {
                cardBorderPaint.color = shooter.bulletColor
                canvas.drawRoundRect(rect, 10f * s, 10f * s, cardBorderPaint)
            }

            val ccx = rect.centerX()

            val iconY = topY + cardH * 0.24f
            cardIconPaint.color = if (isAvailable) shooter.bulletColor else Color.parseColor("#455A64")
            canvas.drawCircle(ccx, iconY, 10f * s, cardIconPaint)

            cardNamePaint.color = if (isAvailable) Color.WHITE else Color.parseColor("#607D8B")
            canvas.drawText(shooter.name, ccx, topY + cardH * 0.55f, cardNamePaint)

            when {
                isEquipped -> {
                    cardStatusPaint.color = shooter.bulletColor
                    canvas.drawText("EQUIPPED", ccx, topY + cardH * 0.82f, cardStatusPaint)
                }
                isTemp -> {
                    val secs = (shooterManager.getRemainingTempMs(st) / 1000).toInt()
                    val min = secs / 60; val sec = secs % 60
                    cardStatusPaint.color = Color.parseColor("#FF9800")
                    canvas.drawText("${min}:${sec.toString().padStart(2, '0')}", ccx, topY + cardH * 0.82f, cardStatusPaint)
                }
                isUnlocked -> {
                    cardStatusPaint.color = Color.parseColor("#4CAF50")
                    canvas.drawText("SELECT", ccx, topY + cardH * 0.82f, cardStatusPaint)
                }
                else -> {
                    cardStatusPaint.color = Color.parseColor("#FFD600")
                    canvas.drawText("${shooter.unlockCost}", ccx, topY + cardH * 0.82f, cardStatusPaint)
                }
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).toInt()
        val min = totalSec / 60
        val sec = totalSec % 60
        return if (min > 0) "${min}m ${sec}s" else "${sec}s"
    }
}
