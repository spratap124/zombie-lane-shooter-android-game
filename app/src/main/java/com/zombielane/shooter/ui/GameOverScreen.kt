package com.zombielane.shooter.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import com.zombielane.shooter.data.UpgradeManager
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Arcade-style game over: animated title/score/coins, compact stats, reward CTAs.
 */
class GameOverScreen {

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#E8000000")
        style = Paint.Style.FILL
    }

    private val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val statPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val coinGoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val walletPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78909C")
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
        textSize = 22f
    }

    private val chestBannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val playBtnFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val playBtnGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val doubleBtnFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val doubleBtnGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val btnLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val upgradeMiniFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val upgradeMiniCost = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val shopMiniPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val tmpRect = RectF()
    private val celebrateGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    var upgradeBtnRects = mutableListOf<RectF>()
    var shopBtnRect = RectF()
    var playAgainBtnRect = RectF()
    var menuBtnRect = RectF()
    var doubleRewardsBtnRect = RectF()
    var backBtnRect = RectF()

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
        rewardedAdReady: Boolean,
        nowMs: Long,
        overlayEnterMs: Long,
        wasNewHighScore: Boolean,
        backButton: Bitmap
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val sw = safeArea.width()
        val s1080 = w / 1080f
        // Scale up on typical phones/tablets so type and taps stay readable (width-responsive).
        val widthBoost = (sw / 400f).coerceIn(1f, 1.95f)
        val s = max(s1080, s1080 * widthBoost * 1.12f).coerceAtMost(2.35f)

        val bannerReserve = (120f * s1080).coerceIn(100f, 176f)
        val contentBottom = (safeArea.bottom - bannerReserve).coerceAtLeast(safeArea.top + 200f)
        val availH = contentBottom - safeArea.top

        val padH = max(16f * s, sw * 0.028f)
        val btnW = (sw - padH * 2f).coerceAtLeast(200f)
        val padX = cx - btnW / 2f
        // Extra space so stacked buttons (and their glow strokes) read as separate targets.
        val gap = max(22f * s, sw * 0.036f).coerceAtLeast(18f)
        val playH = max(58f * s, sw * 0.128f).coerceAtMost(104f)
        val doubleH = max(54f * s, sw * 0.118f).coerceAtMost(96f)
        val upgradeRowH = max(52f * s, sw * 0.12f).coerceAtMost(86f)
        val shopH = max(52f * s, sw * 0.115f).coerceAtMost(84f)

        val tEnter = if (overlayEnterMs > 0L) (nowMs - overlayEnterMs).coerceAtLeast(0L) else 0L

        canvas.drawRect(0f, 0f, w, h, overlayPaint)

        vignettePaint.shader = RadialGradient(
            cx, h * 0.35f, h * 0.9f,
            Color.parseColor("#40204060"), Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, vignettePaint)
        vignettePaint.shader = null

        drawAmbientParticles(canvas, w, h, safeArea, nowMs, s)

        val chestExtra = if (chestBanner != null) max(36f * s, sw * 0.09f) else 0f
        val doubleExtra = if (coinsEarnedThisRun > 0) doubleH + gap else 0f
        val highExtra = if (wasNewHighScore) max(44f * s, sw * 0.1f) else max(32f * s, sw * 0.075f)

        val titleBlock = max(56f * s, sw * 0.11f)
        val scoreBlock = max(72f * s, sw * 0.14f)
        val statsBlock = max(40f * s, sw * 0.085f)
        val coinBlock = max(52f * s, sw * 0.11f)
        val walletBlock = max(30f * s, sw * 0.065f)
        val topPad = max(20f * s, sw * 0.04f)
        val betweenSections = max(12f * s, sw * 0.024f)

        val blockH = topPad + titleBlock + scoreBlock + highExtra + betweenSections + statsBlock + coinBlock +
            walletBlock + chestExtra + betweenSections + playH + gap + doubleExtra + upgradeRowH + gap + shopH + padH

        var y = safeArea.top + ((availH - blockH) * 0.5f).coerceAtLeast(8f * s)

        val titleFade = smoothstep(0f, 520f, tEnter.toFloat())
        val titlePulse = (sin(nowMs * 0.006f).toFloat() * 0.5f + 0.5f)
        val titleSize = max(52f * s, sw * 0.072f).coerceAtMost(86f)
        titlePaint.textSize = titleSize
        titlePaint.alpha = (255 * titleFade).toInt().coerceIn(0, 255)
        val glowR = max(12f, titleSize * 0.22f) * (0.85f + titlePulse * 0.15f)
        titlePaint.setShadowLayer(glowR, 0f, 0f, Color.parseColor("#E64A148C"))
        titlePaint.shader = LinearGradient(
            cx - sw * 0.42f, y, cx + sw * 0.42f, y,
            intArrayOf(Color.parseColor("#FF8A80"), Color.parseColor("#FF5252"), Color.parseColor("#FF8A80")),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        val titleBaseline = y + titleBlock * 0.72f
        canvas.drawText("GAME OVER", cx, titleBaseline, titlePaint)
        titlePaint.shader = null
        titlePaint.clearShadowLayer()
        titlePaint.alpha = 255

        y += titleBlock + betweenSections * 0.5f

        val scorePulse = 1f + sin(nowMs * 0.009f).toFloat() * 0.035f
        val scoreSize = max(60f * s, sw * 0.11f).coerceAtMost(120f) * scorePulse
        scorePaint.textSize = scoreSize
        scorePaint.setShadowLayer(max(14f * s, sw * 0.028f), 0f, 4f * s, Color.parseColor("#80000000"))
        canvas.drawText("$score", cx, y + scoreBlock * 0.78f, scorePaint)
        scorePaint.clearShadowLayer()
        scorePaint.textSize = max(60f * s, sw * 0.11f).coerceAtMost(120f)

        y += scoreBlock

        if (wasNewHighScore) {
            drawNewHighCelebration(canvas, cx, y, nowMs, s, sw)
            val nbPulse = (sin(nowMs * 0.008f).toFloat() * 0.5f + 0.5f)
            scorePaint.textSize = max(24f * s, sw * 0.038f).coerceAtMost(40f)
            scorePaint.color = Color.parseColor("#FFD54F")
            scorePaint.setShadowLayer(12f * s * nbPulse, 0f, 0f, Color.parseColor("#FFFF8D"))
            canvas.drawText("NEW HIGH SCORE", cx, y + highExtra * 0.55f, scorePaint)
            scorePaint.clearShadowLayer()
            scorePaint.color = Color.WHITE
            scorePaint.textSize = max(60f * s, sw * 0.11f).coerceAtMost(120f)
            y += highExtra
        } else {
            scorePaint.textSize = max(22f * s, sw * 0.036f).coerceAtMost(34f)
            scorePaint.color = Color.parseColor("#78909C")
            canvas.drawText("Best ${upgradeManager.highScore}", cx, y + highExtra * 0.5f, scorePaint)
            scorePaint.color = Color.WHITE
            scorePaint.textSize = max(60f * s, sw * 0.11f).coerceAtMost(120f)
            y += highExtra
        }

        y += betweenSections
        statPaint.textSize = max(22f * s, sw * 0.038f).coerceAtMost(30f)
        val statLine = "${enemiesKilled} kills   ·   x$maxCombo best   ·   ${formatTime(timeSurvivedMs)}"
        canvas.drawText(statLine, cx, y + statsBlock * 0.62f, statPaint)

        y += statsBlock

        val runTarget = if (doubleCoinsUsed) coinsEarnedThisRun * 2 else coinsEarnedThisRun
        val countT = smoothstep(0f, COIN_COUNT_UP_MS, tEnter.toFloat())
        val displayedRun = (runTarget * countT).toInt().coerceIn(0, runTarget.coerceAtLeast(0))

        coinGoldPaint.textSize = max(44f * s, sw * 0.095f).coerceAtMost(92f)
        coinGoldPaint.color = Color.parseColor("#FFD54F")
        coinGoldPaint.setShadowLayer(max(10f * s, sw * 0.02f), 0f, 2f * s, Color.parseColor("#A0FF6F00"))
        canvas.drawText("+$displayedRun", cx, y + coinBlock * 0.78f, coinGoldPaint)
        coinGoldPaint.clearShadowLayer()

        y += coinBlock

        walletPaint.textSize = max(22f * s, sw * 0.038f).coerceAtMost(32f)
        canvas.drawText("Wallet  $totalCoinsDisplay", cx, y + walletBlock * 0.55f, walletPaint)

        y += walletBlock

        if (chestBanner != null) {
            chestBannerPaint.textSize = max(20f * s, sw * 0.034f).coerceAtMost(28f)
            chestBannerPaint.color = if (chestBannerOk) Color.parseColor("#A5D6A7") else Color.parseColor("#FFAB91")
            canvas.drawText(chestBanner, cx, y + chestExtra * 0.5f, chestBannerPaint)
            y += chestExtra
        }

        y += betweenSections

        playAgainBtnRect.set(padX, y, padX + btnW, y + playH)
        drawPrimaryPlayButton(canvas, playAgainBtnRect, s, nowMs)
        btnLabelPaint.textSize = max(26f * s, sw * 0.042f).coerceAtMost(36f)
        canvas.drawText("PLAY AGAIN", playAgainBtnRect.centerX(), playAgainBtnRect.centerY() + max(10f * s, sw * 0.018f), btnLabelPaint)

        y += playH + gap

        if (coinsEarnedThisRun > 0) {
            doubleRewardsBtnRect.set(padX, y, padX + btnW, y + doubleH)
            val canTap = !doubleCoinsUsed && !doubleAdInFlight && rewardedAdReady
            drawDoubleRewardsButton(canvas, doubleRewardsBtnRect, s, nowMs, canTap, doubleCoinsUsed, doubleAdInFlight, rewardedAdReady)
            btnLabelPaint.textSize = max(24f * s, sw * 0.04f).coerceAtMost(34f)
            btnLabelPaint.color = when {
                doubleCoinsUsed -> Color.parseColor("#C8E6C9")
                canTap -> Color.WHITE
                else -> Color.parseColor("#90A4AE")
            }
            val doubleLabel = when {
                doubleCoinsUsed -> "✓  Doubled"
                doubleAdInFlight -> "Ad playing…"
                !rewardedAdReady -> "📺 DOUBLE REWARDS"
                else -> "📺 DOUBLE REWARDS"
            }
            canvas.drawText(doubleLabel, doubleRewardsBtnRect.centerX(), doubleRewardsBtnRect.centerY() + max(3f * s, sw * 0.012f), btnLabelPaint)
            if (!doubleCoinsUsed && !doubleAdInFlight && !rewardedAdReady) {
                btnLabelPaint.textSize = max(18f * s, sw * 0.032f).coerceAtMost(26f)
                btnLabelPaint.color = Color.parseColor("#B0BEC5")
                canvas.drawText("Loading ad…", doubleRewardsBtnRect.centerX(), doubleRewardsBtnRect.centerY() + max(22f * s, sw * 0.048f), btnLabelPaint)
            }
            btnLabelPaint.color = Color.WHITE
            btnLabelPaint.textSize = max(26f * s, sw * 0.042f).coerceAtMost(36f)
            y += doubleH + gap
        } else {
            doubleRewardsBtnRect.setEmpty()
        }

        menuBtnRect.setEmpty()

        drawUpgradeRow(canvas, cx, y, btnW, s, sw, upgradeRowH, upgradeManager)
        y += upgradeRowH + gap

        val shopW = btnW * 0.88f
        shopBtnRect.set(cx - shopW / 2f, y, cx + shopW / 2f, y + shopH)
        shopMiniPaint.color = Color.parseColor("#E65100")
        canvas.drawRoundRect(shopBtnRect, 14f * s, 14f * s, shopMiniPaint)
        btnLabelPaint.textSize = max(24f * s, sw * 0.04f).coerceAtMost(34f)
        canvas.drawText("WEAPONS", shopBtnRect.centerX(), shopBtnRect.centerY() + max(8f * s, sw * 0.016f), btnLabelPaint)

        val backSz = 68f * s1080
        backBtnRect.set(
            safeArea.left + 8f * s1080,
            safeArea.top + 8f * s1080,
            safeArea.left + 8f * s1080 + backSz,
            safeArea.top + 8f * s1080 + backSz
        )
        MenuUiAssets.drawBackButton(canvas, backBtnRect, backButton)
    }

    private fun drawAmbientParticles(canvas: Canvas, w: Float, h: Float, safeArea: RectF, nowMs: Long, s: Float) {
        val n = 28
        for (i in 0 until n) {
            val phase = i * 1.713f
            val px = safeArea.left + (w * 0.08f + abs(sin(nowMs * 0.0004f + phase)) * safeArea.width() * 0.84f)
            val py = safeArea.top + ((nowMs * 0.025f + i * 47f) % safeArea.height()).coerceAtLeast(0f)
            val a = (35 + (sin(nowMs * 0.003f + phase) * 28f).toInt()).coerceIn(18, 80)
            particlePaint.color = Color.argb(a, 255, 200, 120)
            val rad = (1.2f + (i % 4) * 0.6f) * s
            canvas.drawCircle(px, py, rad, particlePaint)
        }
    }

    private fun drawNewHighCelebration(canvas: Canvas, cx: Float, y: Float, nowMs: Long, s: Float, sw: Float) {
        val gp = (sin(nowMs * 0.007f).toFloat() * 0.5f + 0.5f)
        val halfW = max(100f * s, sw * 0.38f).coerceAtMost(sw * 0.46f)
        val boxH = max(28f * s, sw * 0.065f)
        for (layer in 0..3) {
            val expand = (6f + layer * 10f) * s
            tmpRect.set(cx - halfW - expand, y - 8f * s - expand, cx + halfW + expand, y + boxH + expand)
            celebrateGlowPaint.color = Color.argb(((40 - layer * 8) * (0.4f + gp * 0.6f)).toInt().coerceIn(8, 40), 255, 215, 0)
            celebrateGlowPaint.strokeWidth = (4f - layer * 0.8f) * s
            canvas.drawRoundRect(tmpRect, 20f * s, 20f * s, celebrateGlowPaint)
        }
        for (i in 0 until 8) {
            val ang = nowMs * 0.002f + i * 0.785f
            val dist = max(72f * s, sw * 0.16f) + sin(nowMs * 0.005f + i).toFloat() * 12f * s
            val spx = cx + cos(ang) * dist
            val spy = y + 12f * s + sin(ang) * dist * 0.45f
            particlePaint.color = Color.argb(180, 255, 235, 150)
            canvas.drawCircle(spx, spy, max(3f * s, sw * 0.006f), particlePaint)
        }
    }

    private fun drawPrimaryPlayButton(canvas: Canvas, rect: RectF, s: Float, nowMs: Long) {
        val p = (sin(nowMs * 0.005f).toFloat() * 0.5f + 0.5f)
        playBtnGlow.color = Color.argb((50 + p * 60).toInt(), 76, 175, 80)
        playBtnGlow.strokeWidth = (5f + p * 3f) * s
        tmpRect.set(rect.left - 3f * s, rect.top - 3f * s, rect.right + 3f * s, rect.bottom + 3f * s)
        canvas.drawRoundRect(tmpRect, 18f * s, 18f * s, playBtnGlow)

        playBtnFill.shader = LinearGradient(
            rect.left, rect.top, rect.left, rect.bottom,
            Color.parseColor("#66BB6A"), Color.parseColor("#2E7D32"), Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, 16f * s, 16f * s, playBtnFill)
        playBtnFill.shader = null
        playBtnGlow.strokeWidth = 2f * s
        playBtnGlow.color = Color.parseColor("#C8E6C9")
        canvas.drawRoundRect(rect, 16f * s, 16f * s, playBtnGlow)
    }

    private fun drawDoubleRewardsButton(
        canvas: Canvas,
        rect: RectF,
        s: Float,
        nowMs: Long,
        canTap: Boolean,
        used: Boolean,
        inFlight: Boolean,
        adReady: Boolean
    ) {
        val p = (sin(nowMs * 0.006f + 1f).toFloat() * 0.5f + 0.5f)
        val activeGlow = canTap || inFlight
        if (activeGlow) {
            doubleBtnGlow.color = Color.argb((45 + p * 55).toInt(), 255, 193, 7)
            doubleBtnGlow.strokeWidth = (5f + p * 4f) * s
            tmpRect.set(rect.left - 3f * s, rect.top - 3f * s, rect.right + 3f * s, rect.bottom + 3f * s)
            canvas.drawRoundRect(tmpRect, 16f * s, 16f * s, doubleBtnGlow)
        }

        val topCol = when {
            used -> Color.parseColor("#558B2F")
            !adReady && !inFlight -> Color.parseColor("#455A64")
            else -> Color.parseColor("#FFB300")
        }
        val botCol = when {
            used -> Color.parseColor("#33691E")
            !adReady && !inFlight -> Color.parseColor("#37474F")
            else -> Color.parseColor("#E65100")
        }
        doubleBtnFill.shader = LinearGradient(rect.left, rect.top, rect.left, rect.bottom, topCol, botCol, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(rect, 14f * s, 14f * s, doubleBtnFill)
        doubleBtnFill.shader = null

        doubleBtnGlow.strokeWidth = 2f * s
        doubleBtnGlow.color = Color.argb(200, 255, 255, 255)
        canvas.drawRoundRect(rect, 14f * s, 14f * s, doubleBtnGlow)
    }

    private fun drawUpgradeRow(
        canvas: Canvas,
        cx: Float,
        y: Float,
        totalW: Float,
        s: Float,
        sw: Float,
        rowH: Float,
        upgradeManager: UpgradeManager
    ) {
        upgradeBtnRects.clear()
        val types = UpgradeManager.UpgradeType.entries
        val gap = max(12f * s, sw * 0.024f).coerceAtLeast(10f)
        val cellW = (totalW - gap * 2f) / 3f
        val h = rowH
        val left0 = cx - totalW / 2f
        val labelTs = max(17f * s, sw * 0.034f).coerceAtMost(28f)
        val costTs = max(15f * s, sw * 0.03f).coerceAtMost(24f)

        for (i in types.indices) {
            val type = types[i]
            val l = left0 + i * (cellW + gap)
            val rect = RectF(l, y, l + cellW, y + h)
            upgradeBtnRects.add(rect)

            val can = upgradeManager.canAfford(type)
            upgradeMiniFill.color = if (can) Color.parseColor("#263238") else Color.parseColor("#1A222C")
            canvas.drawRoundRect(rect, 12f * s, 12f * s, upgradeMiniFill)

            val short = when (type) {
                UpgradeManager.UpgradeType.DAMAGE -> "DMG"
                UpgradeManager.UpgradeType.FIRE_RATE -> "ROF"
                UpgradeManager.UpgradeType.HEALTH -> "HP"
            }
            upgradeMiniCost.textSize = labelTs
            upgradeMiniCost.color = Color.parseColor("#ECEFF1")
            canvas.drawText(short, rect.centerX(), rect.top + h * 0.38f, upgradeMiniCost)

            upgradeMiniCost.textSize = costTs
            upgradeMiniCost.color = if (can) Color.parseColor("#FFD54F") else Color.parseColor("#546E7A")
            canvas.drawText("${upgradeManager.upgradeCost(type)}", rect.centerX(), rect.bottom - h * 0.18f, upgradeMiniCost)
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).toInt()
        val min = totalSec / 60
        val sec = totalSec % 60
        return if (min > 0) "${min}m ${sec}s" else "${sec}s"
    }

    private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    companion object {
        private const val COIN_COUNT_UP_MS = 900f
    }
}
