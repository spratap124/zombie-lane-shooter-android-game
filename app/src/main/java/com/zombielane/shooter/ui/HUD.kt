package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.zombielane.shooter.data.UpgradeManager
import com.zombielane.shooter.engine.ComboTracker
import com.zombielane.shooter.engine.GameEventManager

class HUD {

    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val coinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        textSize = 44f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val coinIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        style = Paint.Style.FILL
    }

    private val comboPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFEB3B")
        textSize = 56f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(5f, 2f, 2f, Color.BLACK)
    }

    private val eventBannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    private val eventBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val gameOverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 72f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(6f, 3f, 3f, Color.BLACK)
    }

    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5")
        textSize = 36f
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.CENTER
    }

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#CC000000")
        style = Paint.Style.FILL
    }

    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 30f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val btnDetailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5")
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }

    private val costPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val restartBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val restartTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 38f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val highScorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        textSize = 30f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    var upgradeBtnRects = mutableListOf<RectF>()
    var restartBtnRect = RectF()

    fun draw(
        canvas: Canvas,
        score: Int,
        sessionCoins: Int,
        totalCoins: Int,
        playerHealth: Int,
        playerMaxHealth: Int,
        gameOver: Boolean,
        upgradeManager: UpgradeManager?,
        safeArea: RectF,
        comboTracker: ComboTracker,
        eventManager: GameEventManager
    ) {
        val left = safeArea.left
        val top = safeArea.top
        val right = safeArea.right
        val w = canvas.width.toFloat()

        canvas.drawText("SCORE: $score", left, top + 44f, scorePaint)

        val coinY = top + 96f
        canvas.drawCircle(left + 16f, coinY - 12f, 14f, coinIconPaint)
        canvas.drawText("$sessionCoins", left + 42f, coinY, coinPaint)

        // Hearts
        val heartSize = 28f
        val heartSpacing = 36f
        val heartsStartX = right - (playerMaxHealth * heartSpacing)
        for (i in 0 until playerMaxHealth) {
            val hx = heartsStartX + i * heartSpacing
            val hy = top + 30f
            drawHeart(canvas, hx, hy, heartSize, i < playerHealth)
        }

        // Combo display
        if (comboTracker.isVisible) {
            comboPaint.color = when {
                comboTracker.displayCombo >= 10 -> Color.parseColor("#FF5722")
                comboTracker.displayCombo >= 5 -> Color.parseColor("#FFD600")
                else -> Color.parseColor("#4CAF50")
            }
            canvas.drawText(
                "x${comboTracker.displayCombo} COMBO!",
                w / 2f, top + 52f, comboPaint
            )
        }

        // Event banner
        if (eventManager.isActive) {
            val bannerY = top + 120f
            val bannerH = 44f
            eventBgPaint.color = Color.parseColor("#44000000")
            canvas.drawRoundRect(
                w * 0.15f, bannerY, w * 0.85f, bannerY + bannerH,
                12f, 12f, eventBgPaint
            )
            eventBannerPaint.color = Color.parseColor("#FFEB3B")
            canvas.drawText(
                eventManager.bannerText ?: "",
                w / 2f, bannerY + 32f, eventBannerPaint
            )
        }

        if (gameOver && upgradeManager != null) {
            drawGameOverScreen(canvas, score, totalCoins, upgradeManager, safeArea)
        }
    }

    private fun drawGameOverScreen(
        canvas: Canvas,
        score: Int,
        totalCoins: Int,
        upgradeManager: UpgradeManager,
        safeArea: RectF
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

        canvas.drawRect(0f, 0f, w, h, overlayPaint)

        val cx = w / 2f
        var yPos = safeArea.top + 60f

        canvas.drawText("GAME OVER", cx, yPos, gameOverPaint)
        yPos += 50f
        canvas.drawText("Score: $score", cx, yPos, subtitlePaint)

        if (score >= upgradeManager.highScore) {
            yPos += 40f
            canvas.drawText("NEW HIGH SCORE!", cx, yPos, highScorePaint)
        } else {
            yPos += 40f
            canvas.drawText("Best: ${upgradeManager.highScore}", cx, yPos, btnDetailPaint)
        }

        yPos += 60f
        canvas.drawCircle(cx - 50f, yPos - 10f, 12f, coinIconPaint)
        coinPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("$totalCoins", cx, yPos, coinPaint)
        coinPaint.textAlign = Paint.Align.LEFT

        yPos += 50f
        val btnWidth = (safeArea.width() * 0.85f).coerceAtMost(w * 0.75f)
        val btnHeight = 90f
        val btnGap = 18f

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
            canvas.drawText(label, cx, yPos + 35f, btnTextPaint)
            canvas.drawText(upgradeManager.statSummary(type), cx, yPos + 60f, btnDetailPaint)

            costPaint.color = if (canAfford) Color.parseColor("#FFD600") else Color.parseColor("#607D8B")
            canvas.drawText("${upgradeManager.upgradeCost(type)} coins", cx, yPos + 82f, costPaint)

            yPos += btnHeight + btnGap
        }

        yPos += 20f
        val restartWidth = (safeArea.width() * 0.7f).coerceAtMost(w * 0.6f)
        val restartHeight = 70f
        restartBtnRect = RectF(
            cx - restartWidth / 2f, yPos,
            cx + restartWidth / 2f, yPos + restartHeight
        )
        canvas.drawRoundRect(restartBtnRect, 16f, 16f, restartBtnPaint)
        canvas.drawText("PLAY AGAIN", cx, yPos + 46f, restartTextPaint)
    }

    private fun drawHeart(canvas: Canvas, x: Float, y: Float, size: Float, filled: Boolean) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (filled) Color.parseColor("#F44336") else Color.parseColor("#44F44336")
            style = Paint.Style.FILL
        }
        val r = size / 4f
        canvas.drawCircle(x + r, y, r, paint)
        canvas.drawCircle(x + 3 * r, y, r, paint)

        val trianglePath = android.graphics.Path().apply {
            moveTo(x, y)
            lineTo(x + size / 2f, y + size * 0.6f)
            lineTo(x + size, y)
            close()
        }
        canvas.drawPath(trianglePath, paint)
    }
}
