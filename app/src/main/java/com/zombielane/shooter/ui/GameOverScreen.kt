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

    private val coinSectionLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90A4AE")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val coinBigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(6f, 2f, 2f, Color.BLACK)
    }

    private val coinHintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5")
        textAlign = Paint.Align.CENTER
    }

    private val coinCardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#252538")
        style = Paint.Style.FILL
    }

    private val coinCardStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        style = Paint.Style.STROKE
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

    private val doubleRewardsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7CB342")
        style = Paint.Style.FILL
    }

    private val doubleRewardsDisabledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#37474F")
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
    var doubleRewardsBtnRect = RectF()

    fun draw(
        canvas: Canvas,
        safeArea: RectF,
        score: Int,
        coinsEarnedThisRun: Int,
        totalCoinsDisplay: Int,
        maxCombo: Int,
        enemiesKilled: Int,
        timeSurvivedMs: Long,
        upgradeManager: UpgradeManager,
        chestBanner: String?,
        chestBannerOk: Boolean,
        doubleCoinsUsed: Boolean,
        doubleAdInFlight: Boolean,
        rewardedAdReady: Boolean
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

        yPos = statsTop + statBoxH + sp * 1.6f + 8f * s

        val cardPad = 16f * s
        val cardW = (safeArea.width() * 0.92f).coerceAtMost(w * 0.86f)
        val cardLeft = cx - cardW / 2f
        val cardTop = yPos
        val doubleBtnH = if (coinsEarnedThisRun > 0) (availableH * 0.068f).coerceIn(56f * s, 76f * s) else 0f
        val doubleBtnW = cardW - cardPad * 2f

        var measureY = cardTop + cardPad + 8f * s
        measureY += 28f * s + 44f * s + 8f * s + 22f * s
        measureY += when {
            coinsEarnedThisRun <= 0 -> 28f * s
            doubleCoinsUsed -> 30f * s
            else -> 26f * s + 34f * s
        }
        measureY += if (coinsEarnedThisRun > 0) doubleBtnH + cardPad else cardPad * 0.5f
        measureY += 26f * s + 34f * s + cardPad + 16f * s
        val cardBottom = measureY

        coinCardStrokePaint.strokeWidth = (2.5f * s).coerceAtLeast(2f)
        canvas.drawRoundRect(cardLeft, cardTop, cardLeft + cardW, cardBottom, 18f * s, 18f * s, coinCardBgPaint)
        canvas.drawRoundRect(cardLeft, cardTop, cardLeft + cardW, cardBottom, 18f * s, 18f * s, coinCardStrokePaint)

        var cardInnerY = cardTop + cardPad + 8f * s
        coinSectionLabelPaint.textSize = 22f * s
        canvas.drawText("COINS THIS RUN", cx, cardInnerY, coinSectionLabelPaint)
        cardInnerY += 28f * s

        coinBigPaint.textSize = 44f * s
        val runTotalDisplay = if (doubleCoinsUsed) coinsEarnedThisRun * 2 else coinsEarnedThisRun
        canvas.drawText("+$runTotalDisplay", cx, cardInnerY, coinBigPaint)
        cardInnerY += 8f * s + 22f * s

        coinHintPaint.textSize = 22f * s
        when {
            coinsEarnedThisRun <= 0 -> {
                canvas.drawText("No coins this run — play again to earn some!", cx, cardInnerY, coinHintPaint)
                cardInnerY += 28f * s
            }
            doubleCoinsUsed -> {
                canvas.drawText("Includes +$coinsEarnedThisRun bonus from the ad ✓", cx, cardInnerY, coinHintPaint)
                cardInnerY += 30f * s
            }
            else -> {
                canvas.drawText("$coinsEarnedThisRun already added to your wallet below.", cx, cardInnerY, coinHintPaint)
                cardInnerY += 26f * s
                coinHintPaint.textSize = 20f * s
                canvas.drawText("Watch a short ad to earn the same amount again (×2 this run).", cx, cardInnerY, coinHintPaint)
                coinHintPaint.textSize = 22f * s
                cardInnerY += 34f * s
            }
        }

        if (coinsEarnedThisRun > 0) {
            doubleRewardsBtnRect.set(cx - doubleBtnW / 2f, cardInnerY, cx + doubleBtnW / 2f, cardInnerY + doubleBtnH)
            val canTap = !doubleCoinsUsed && !doubleAdInFlight && rewardedAdReady
            val btnFill = if (canTap) doubleRewardsPaint else doubleRewardsDisabledPaint
            canvas.drawRoundRect(doubleRewardsBtnRect, 14f * s, 14f * s, btnFill)
            btnTextPaint.textSize = 26f * s
            btnTextPaint.color = if (canTap) Color.WHITE else Color.parseColor("#90A4AE")
            when {
                doubleCoinsUsed -> {
                    canvas.drawText("✓ Doubled — you claimed the bonus", doubleRewardsBtnRect.centerX(), doubleRewardsBtnRect.centerY() + 4f * s, btnTextPaint)
                }
                doubleAdInFlight -> {
                    canvas.drawText("Ad playing…", doubleRewardsBtnRect.centerX(), doubleRewardsBtnRect.centerY() - 8f * s, btnTextPaint)
                    btnTextPaint.textSize = 20f * s
                    canvas.drawText("Stay until the end to get +$coinsEarnedThisRun", doubleRewardsBtnRect.centerX(), doubleRewardsBtnRect.centerY() + 14f * s, btnTextPaint)
                    btnTextPaint.textSize = 26f * s
                }
                !rewardedAdReady -> {
                    canvas.drawText("📺 DOUBLE COINS", doubleRewardsBtnRect.centerX(), doubleRewardsBtnRect.centerY() - 8f * s, btnTextPaint)
                    btnTextPaint.textSize = 20f * s
                    canvas.drawText("Ad loading — try again in a moment", doubleRewardsBtnRect.centerX(), doubleRewardsBtnRect.centerY() + 14f * s, btnTextPaint)
                    btnTextPaint.textSize = 26f * s
                }
                else -> {
                    canvas.drawText("📺 WATCH AD · DOUBLE THIS RUN", doubleRewardsBtnRect.centerX(), doubleRewardsBtnRect.centerY() - 10f * s, btnTextPaint)
                    btnTextPaint.textSize = 22f * s
                    canvas.drawText("+ $coinsEarnedThisRun coins (same as you just earned)", doubleRewardsBtnRect.centerX(), doubleRewardsBtnRect.centerY() + 16f * s, btnTextPaint)
                    btnTextPaint.textSize = 26f * s
                }
            }
            btnTextPaint.color = Color.WHITE
            cardInnerY += doubleBtnH + cardPad
        } else {
            doubleRewardsBtnRect.setEmpty()
            cardInnerY += cardPad * 0.5f
        }

        coinSectionLabelPaint.textSize = 20f * s
        canvas.drawText("YOUR WALLET (TOTAL COINS)", cx, cardInnerY, coinSectionLabelPaint)
        cardInnerY += 26f * s

        canvas.drawCircle(cx - 56f * s, cardInnerY - 8f * s, 12f * s, coinIconPaint)
        coinPaint.textSize = 34f * s
        canvas.drawText("$totalCoinsDisplay", cx, cardInnerY, coinPaint)

        yPos = cardBottom + sp * 1.4f
        if (chestBanner != null) {
            val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (chestBannerOk) Color.parseColor("#A5D6A7") else Color.parseColor("#FFAB91")
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                textSize = 24f * s
            }
            canvas.drawText(chestBanner, cx, yPos + 20f * s, bannerPaint)
            yPos += 44f * s
        } else {
            yPos += sp * 0.5f
        }

        // Upgrades section
        yPos += sp * 1.2f + 4f * s
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
