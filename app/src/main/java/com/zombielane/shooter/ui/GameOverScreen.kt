package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
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

    private val shopBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF9800")
        style = Paint.Style.FILL
    }

    private val actionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    var upgradeBtnRects = mutableListOf<RectF>()
    var shopBtnRect = RectF()
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
        upgradeManager: UpgradeManager
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val availableH = safeArea.height()
        val s = w / 1080f

        canvas.drawRect(0f, 0f, w, h, overlayPaint)

        val sp = (availableH * 0.018f).coerceIn(12f * s, 24f * s)
        val btnHeight = (availableH * 0.06f).coerceIn(60f * s, 88f * s)
        val btnGap = (availableH * 0.012f).coerceIn(10f * s, 18f * s)
        val statBoxH = (availableH * 0.07f).coerceIn(56f * s, 80f * s)

        titlePaint.textSize = 72f * s
        scorePaint.textSize = 56f * s
        highScorePaint.textSize = 34f * s
        statLabelPaint.textSize = 26f * s
        statValuePaint.textSize = 38f * s
        sectionPaint.textSize = 26f * s
        btnTextPaint.textSize = 30f * s
        costPaint.textSize = 24f * s
        actionTextPaint.textSize = 38f * s

        var yPos = safeArea.top + sp * 1.5f

        canvas.drawText("GAME OVER", cx, yPos + 56f * s, titlePaint)
        yPos += 56f * s + sp * 2.5f

        yPos += 44f * s
        canvas.drawText("$score", cx, yPos, scorePaint)

        yPos += sp * 1.5f + 22f * s
        if (score >= upgradeManager.highScore) {
            highScorePaint.color = Color.parseColor("#FFD600")
            canvas.drawText("NEW HIGH SCORE!", cx, yPos, highScorePaint)
        } else {
            highScorePaint.color = Color.parseColor("#78909C")
            canvas.drawText("Best: ${upgradeManager.highScore}", cx, yPos, highScorePaint)
        }

        yPos += sp * 2.5f + 8f * s
        val statWidth = safeArea.width() / 3f
        val stats = listOf("KILLS" to "$enemiesKilled", "COMBO" to "x$maxCombo", "TIME" to formatTime(timeSurvivedMs))
        val statsTop = yPos
        canvas.drawRoundRect(safeArea.left, statsTop, safeArea.right, statsTop + statBoxH, 12f * s, 12f * s, dividerPaint)
        for (i in stats.indices) {
            val sx = safeArea.left + statWidth * i + statWidth / 2f
            canvas.drawText(stats[i].first, sx, statsTop + statBoxH * 0.36f, statLabelPaint)
            canvas.drawText(stats[i].second, sx, statsTop + statBoxH * 0.78f, statValuePaint)
        }

        yPos = statsTop + statBoxH + sp * 1.8f + 10f * s
        coinPaint.textSize = 32f * s
        canvas.drawText("+$sessionCoins earned", cx, yPos, coinPaint)
        yPos += sp * 1.5f + 22f * s
        canvas.drawCircle(cx - 60f * s, yPos - 10f * s, 13f * s, coinIconPaint)
        coinPaint.textSize = 36f * s
        canvas.drawText("$totalCoins", cx, yPos, coinPaint)

        // Upgrades section
        yPos += sp * 2.5f + 6f * s
        canvas.drawText("UPGRADES", safeArea.left, yPos, sectionPaint)
        yPos += 12f * s + sp
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

        // Weapons shop button
        yPos += sp
        val shopW = (safeArea.width() * 0.60f).coerceAtMost(w * 0.55f)
        val shopH = (availableH * 0.05f).coerceIn(48f * s, 64f * s)
        shopBtnRect = RectF(cx - shopW / 2f, yPos, cx + shopW / 2f, yPos + shopH)
        canvas.drawRoundRect(shopBtnRect, 16f * s, 16f * s, shopBtnPaint)
        actionTextPaint.textSize = 34f * s
        canvas.drawText("WEAPONS", cx, yPos + shopH * 0.66f, actionTextPaint)
        actionTextPaint.textSize = 38f * s

        // Action buttons
        yPos += shopH + sp * 1.5f
        val halfW = (safeArea.width() * 0.42f).coerceAtMost(w * 0.38f)
        val actionH = (availableH * 0.055f).coerceIn(52f * s, 72f * s)

        playAgainBtnRect = RectF(cx + 8f * s, yPos, cx + 8f * s + halfW, yPos + actionH)
        canvas.drawRoundRect(playAgainBtnRect, 16f * s, 16f * s, playBtnPaint)
        canvas.drawText("PLAY", playAgainBtnRect.centerX(), yPos + actionH * 0.66f, actionTextPaint)

        menuBtnRect = RectF(cx - 8f * s - halfW, yPos, cx - 8f * s, yPos + actionH)
        canvas.drawRoundRect(menuBtnRect, 16f * s, 16f * s, menuBtnPaint)
        canvas.drawText("MENU", menuBtnRect.centerX(), yPos + actionH * 0.66f, actionTextPaint)
    }

    private fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).toInt()
        val min = totalSec / 60
        val sec = totalSec % 60
        return if (min > 0) "${min}m ${sec}s" else "${sec}s"
    }
}
