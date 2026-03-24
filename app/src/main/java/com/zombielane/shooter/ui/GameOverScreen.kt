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
        textSize = 68f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(8f, 3f, 3f, Color.BLACK)
    }

    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 52f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val highScorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        textSize = 30f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val statLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78909C")
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    private val statValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val coinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        textSize = 38f
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

    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val btnDetailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5")
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }

    private val costPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        textSize = 22f
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
        textSize = 36f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    var upgradeBtnRects = mutableListOf<RectF>()
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

        canvas.drawRect(0f, 0f, w, h, overlayPaint)

        // Scale spacing proportionally to available height so nothing overlaps
        val spacing = (availableH * 0.018f).coerceIn(10f, 22f)
        val btnHeight = (availableH * 0.065f).coerceIn(58f, 80f)
        val btnGap = (availableH * 0.01f).coerceIn(8f, 16f)
        val statBoxH = (availableH * 0.07f).coerceIn(50f, 70f)

        var yPos = safeArea.top + spacing * 2f

        // Title
        canvas.drawText("GAME OVER", cx, yPos, titlePaint)

        // Score
        yPos += spacing * 2.5f + 40f
        canvas.drawText("$score", cx, yPos, scorePaint)

        yPos += spacing + 20f
        if (score >= upgradeManager.highScore) {
            canvas.drawText("NEW HIGH SCORE!", cx, yPos, highScorePaint)
        } else {
            highScorePaint.color = Color.parseColor("#78909C")
            canvas.drawText("Best: ${upgradeManager.highScore}", cx, yPos, highScorePaint)
            highScorePaint.color = Color.parseColor("#FFD600")
        }

        // Stats row
        yPos += spacing * 2f + 10f
        val statWidth = safeArea.width() / 3f
        val stats = listOf(
            "KILLS" to "$enemiesKilled",
            "COMBO" to "x$maxCombo",
            "TIME" to formatTime(timeSurvivedMs)
        )

        val statsTop = yPos
        canvas.drawRoundRect(safeArea.left, statsTop, safeArea.right, statsTop + statBoxH, 12f, 12f, dividerPaint)

        for (i in stats.indices) {
            val sx = safeArea.left + statWidth * i + statWidth / 2f
            canvas.drawText(stats[i].first, sx, statsTop + statBoxH * 0.35f, statLabelPaint)
            canvas.drawText(stats[i].second, sx, statsTop + statBoxH * 0.78f, statValuePaint)
        }

        // Coins earned — two separate lines
        yPos = statsTop + statBoxH + spacing * 2f + 10f
        coinPaint.textSize = 30f
        canvas.drawText("+$sessionCoins earned", cx, yPos, coinPaint)
        yPos += spacing + 20f
        canvas.drawCircle(cx - 60f, yPos - 10f, 12f, coinIconPaint)
        coinPaint.textSize = 34f
        canvas.drawText("$totalCoins", cx, yPos, coinPaint)

        // Upgrades
        yPos += spacing * 2f + 10f
        val btnWidth = (safeArea.width() * 0.85f).coerceAtMost(w * 0.75f)

        upgradeBtnRects.clear()
        val types = UpgradeManager.UpgradeType.entries

        for (type in types) {
            val btnX = cx - btnWidth / 2f
            val rect = RectF(btnX, yPos, btnX + btnWidth, yPos + btnHeight)
            upgradeBtnRects.add(rect)

            val canAfford = upgradeManager.canAfford(type)
            btnPaint.color = if (canAfford) Color.parseColor("#37474F") else Color.parseColor("#263238")
            canvas.drawRoundRect(rect, 12f, 12f, btnPaint)

            val label = when (type) {
                UpgradeManager.UpgradeType.DAMAGE -> "DAMAGE"
                UpgradeManager.UpgradeType.FIRE_RATE -> "FIRE RATE"
                UpgradeManager.UpgradeType.HEALTH -> "HEALTH"
            }

            btnTextPaint.color = if (canAfford) Color.WHITE else Color.parseColor("#607D8B")
            canvas.drawText("$label   ${upgradeManager.statSummary(type)}", cx, yPos + btnHeight * 0.4f, btnTextPaint)

            costPaint.color = if (canAfford) Color.parseColor("#FFD600") else Color.parseColor("#607D8B")
            canvas.drawText("${upgradeManager.upgradeCost(type)} coins", cx, yPos + btnHeight * 0.78f, costPaint)

            yPos += btnHeight + btnGap
        }

        // Action buttons row
        yPos += spacing
        val halfW = (safeArea.width() * 0.42f).coerceAtMost(w * 0.35f)
        val actionH = (availableH * 0.06f).coerceIn(54f, 70f)

        playAgainBtnRect = RectF(cx + 8f, yPos, cx + 8f + halfW, yPos + actionH)
        canvas.drawRoundRect(playAgainBtnRect, 16f, 16f, playBtnPaint)
        canvas.drawText("PLAY", playAgainBtnRect.centerX(), yPos + actionH * 0.65f, actionTextPaint)

        menuBtnRect = RectF(cx - 8f - halfW, yPos, cx - 8f, yPos + actionH)
        canvas.drawRoundRect(menuBtnRect, 16f, 16f, menuBtnPaint)
        canvas.drawText("MENU", menuBtnRect.centerX(), yPos + actionH * 0.65f, actionTextPaint)
    }

    private fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).toInt()
        val min = totalSec / 60
        val sec = totalSec % 60
        return if (min > 0) "${min}m ${sec}s" else "${sec}s"
    }
}
