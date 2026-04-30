package com.zombielane.shooter.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.zombielane.shooter.data.ChestOpenResult
import com.zombielane.shooter.data.ChestRevealPhase
import com.zombielane.shooter.data.ChestRewards
import com.zombielane.shooter.data.ChestType
import com.zombielane.shooter.data.LuckyBonusKind
import com.zombielane.shooter.data.Shooter
import kotlin.math.sin

/**
 * Full-screen chest reveal, lucky bonus, and double-reward prompt with arcade-style presentation.
 * Paints are reused. Full-screen rarity backgrounds use [ChestRewardBackgroundSystem].
 */
class ChestRevealUI {

    private val rewardBackground = ChestRewardBackgroundSystem()
    private val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC1A2332")
        style = Paint.Style.FILL
    }
    private val subtitleBackingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#99000000")
        style = Paint.Style.FILL
    }
    private val cardStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val glowStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val arcadeBtnFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val arcadeBtnRim = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val rewardLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }
    private val luckyTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFECB3")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val btnLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val sparklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val tmpInner = RectF()
    private val tmpBitmapDst = RectF()
    private val cardRect = RectF()

    var claimRect = RectF()
    var doubleAdRect = RectF()

    fun draw(
        canvas: Canvas,
        safeArea: RectF,
        phase: ChestRevealPhase,
        phaseStartMs: Long,
        now: Long,
        result: ChestOpenResult?,
        menuUi: MenuUiAssets
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        if (result == null) return

        val tier = result.baseType
        val accent = tierAccentArgb(tier)
        val glowRgb = tierGlowRgb(tier)

        when (phase) {
            ChestRevealPhase.SPINNING -> return
            ChestRevealPhase.REVEAL -> {
                rewardBackground.draw(canvas, w, h, now, rewardBackground.visualForTier(tier))
                drawChestHero(canvas, menuUi, tier, cx, safeArea.top + 72f * s, 128f * s)

                titlePaint.textSize = 52f * s
                titlePaint.color = Color.WHITE
                titlePaint.setShadowLayer(12f * s, 0f, 4f * s, Color.argb(200, Color.red(accent), Color.green(accent), Color.blue(accent)))
                canvas.drawText("YOU GOT", cx, safeArea.top + 230f * s, titlePaint)
                titlePaint.clearShadowLayer()

                subPaint.textSize = 24f * s
                subPaint.color = Color.parseColor("#B0BEC5")
                canvas.drawText("Loot from your chest", cx, safeArea.top + 268f * s, subPaint)

                layoutRewardCard(canvas, cx, h * 0.36f, w * 0.88f, s, tier, accent, menuUi, result.rewards, now)
            }
            ChestRevealPhase.LUCKY -> {
                val goldBurst = Color.parseColor("#FFFF8F")
                rewardBackground.draw(
                    canvas, w, h, now + 500L,
                    ChestRewardBackgroundSystem.RewardBackgroundVisual.GOLD_BURST
                )

                luckyTitlePaint.textSize = 56f * s
                luckyTitlePaint.setShadowLayer(14f * s, 0f, 3f * s, Color.parseColor("#80FF6F00"))
                canvas.drawText("LUCKY BONUS!", cx, safeArea.top + 100f * s, luckyTitlePaint)
                luckyTitlePaint.clearShadowLayer()

                val kind = when (result.luckyKind) {
                    LuckyBonusKind.DOUBLE_VALUES -> "DOUBLE REWARDS!"
                    LuckyBonusKind.RARITY_UP -> "RARITY UP!"
                    null -> "BONUS!"
                }
                subPaint.color = Color.parseColor("#FFF59D")
                subPaint.textSize = 32f * s
                canvas.drawText(kind, cx, safeArea.top + 168f * s, subPaint)

                layoutRewardCard(canvas, cx, h * 0.34f, w * 0.88f, s, tier, goldBurst, menuUi, result.rewards, now)
            }
            ChestRevealPhase.DOUBLE_OFFER -> {
                rewardBackground.draw(canvas, w, h, now, rewardBackground.visualForTier(tier))
                val heroTop = safeArea.top + 72f * s
                drawChestHero(canvas, menuUi, tier, cx, heroTop, 122f * s)

                val titleBaseline = heroTop + 122f * s + 36f * s
                titlePaint.textSize = 56f * s
                titlePaint.color = Color.WHITE
                titlePaint.setShadowLayer(18f * s, 0f, 5f * s, Color.argb(235, 12, 14, 22))
                canvas.drawText("DOUBLE THE LOOT?", cx, titleBaseline, titlePaint)
                titlePaint.clearShadowLayer()

                val subText = "Watch a short video to multiply this drop ×2"
                subPaint.textSize = 30f * s
                subPaint.color = Color.parseColor("#FFFDE7")
                val subFm = subPaint.fontMetrics
                subPaint.setShadowLayer(10f * s, 0f, 3f * s, Color.argb(230, 0, 0, 0))
                val subBaseline = titleBaseline + 52f * s
                val subW = subPaint.measureText(subText)
                val pillPadX = 20f * s
                val pillPadY = 12f * s
                tmpInner.set(
                    cx - subW / 2f - pillPadX,
                    subBaseline + subFm.ascent - pillPadY,
                    cx + subW / 2f + pillPadX,
                    subBaseline + subFm.descent + pillPadY
                )
                canvas.drawRoundRect(tmpInner, 16f * s, 16f * s, subtitleBackingPaint)
                canvas.drawText(subText, cx, subBaseline, subPaint)
                subPaint.clearShadowLayer()

                val cardTop = subBaseline + subFm.descent + 28f * s
                layoutRewardCard(canvas, cx, cardTop, w * 0.88f, s, tier, accent, menuUi, result.rewards, now)

                val bw = safeArea.width() * 0.44f
                val bh = 86f * s
                val gap = 16f * s
                val btnTopMax = safeArea.bottom - bh - 20f * s
                val btnY = minOf(h * 0.62f, btnTopMax - 8f * s)
                val pulse = (sin(now * 0.007) * 0.5f + 0.5f).toFloat()
                val adExpand = 5f * pulse * s
                doubleAdRect.set(cx - bw - gap / 2f - adExpand, btnY - adExpand, cx - gap / 2f + adExpand, btnY + bh + adExpand)
                claimRect.set(cx + gap / 2f, btnY, cx + bw + gap / 2f, btnY + bh)

                drawArcadeButton(
                    canvas, doubleAdRect, s, now, 0f,
                    Color.parseColor("#FFA726"), Color.parseColor("#E65100"),
                    Triple(255, 213, 79), "×2  AD"
                )
                drawArcadeButton(
                    canvas, claimRect, s, now, 2.5f,
                    Color.parseColor("#455A64"), Color.parseColor("#1C2529"),
                    Triple(179, 229, 252), "CLAIM"
                )
            }
            else -> {}
        }
    }

    private fun tierAccentArgb(type: ChestType): Int = when (type) {
        ChestType.COMMON -> Color.parseColor("#ECEFF1")
        ChestType.RARE -> Color.parseColor("#42A5F5")
        ChestType.EPIC -> Color.parseColor("#AB47BC")
        ChestType.SUPER -> Color.parseColor("#FFD700")
    }

    private fun tierGlowRgb(type: ChestType): Triple<Int, Int, Int> {
        val c = tierAccentArgb(type)
        return Triple(Color.red(c), Color.green(c), Color.blue(c))
    }

    private fun drawChestHero(canvas: Canvas, menuUi: MenuUiAssets, type: ChestType, cx: Float, top: Float, side: Float) {
        val bmp: Bitmap = menuUi.chestOpen(type)
        tmpBitmapDst.set(cx - side / 2f, top, cx + side / 2f, top + side)
        canvas.drawBitmap(bmp, null, tmpBitmapDst, bitmapPaint)
    }

    private fun layoutRewardCard(
        canvas: Canvas,
        cx: Float,
        top: Float,
        cardW: Float,
        s: Float,
        tier: ChestType,
        accent: Int,
        menuUi: MenuUiAssets,
        rewards: ChestRewards,
        now: Long
    ) {
        val pad = 22f * s
        var contentH = pad * 2f
        val rowGap = 16f * s
        val rowH = 48f * s
        if (rewards.coins > 0) contentH += rowH + rowGap
        rewards.tempShooter?.let { contentH += rowH + rowGap }
        if (rewards.nextRunShield) contentH += rowH + rowGap
        if (rewards.nextRunRapidMs > 0) contentH += rowH + rowGap
        contentH -= rowGap.coerceAtMost(contentH)

        val cardH = contentH.coerceAtLeast(96f * s)
        cardRect.set(cx - cardW / 2f, top, cx + cardW / 2f, top + cardH)

        val rgb = tierGlowRgb(tier)
        cardStrokePaint.color = Color.argb(200, rgb.first, rgb.second, rgb.third)
        cardStrokePaint.strokeWidth = 3f * s
        canvas.drawRoundRect(cardRect, 20f * s, 20f * s, cardFillPaint)
        canvas.drawRoundRect(cardRect, 20f * s, 20f * s, cardStrokePaint)

        var y = cardRect.top + pad + rowH * 0.65f
        val textLeft = cardRect.left + pad
        val maxTextW = cardRect.width() - pad * 2f - 48f * s

        rewardLinePaint.textSize = 36f * s
        if (rewards.coins > 0) {
            val coinBmp = menuUi.coin
            val ch = 40f * s
            val scale = ch / coinBmp.height
            val cw = coinBmp.width * scale
            tmpBitmapDst.set(textLeft, y - ch * 0.85f, textLeft + cw, y + ch * 0.15f)
            canvas.drawBitmap(coinBmp, null, tmpBitmapDst, bitmapPaint)
            rewardLinePaint.color = Color.parseColor("#FFCA28")
            val label = "+${rewards.coins}  COINS"
            canvas.drawText(label, textLeft + cw + 12f * s, y, rewardLinePaint)
            y += rowH + rowGap
        }
        rewards.tempShooter?.let { st ->
            val name = Shooter.get(st).name
            val mins = (rewards.tempShooterDurationMs / 60000).coerceAtLeast(1)
            rewardLinePaint.color = Color.parseColor("#4DD0E1")
            val line = "⚡ $name  ·  ${mins}m trial"
            drawEllipsized(canvas, line, textLeft, y, maxTextW, rewardLinePaint)
            y += rowH + rowGap
        }
        if (rewards.nextRunShield) {
            rewardLinePaint.color = Color.parseColor("#90CAF9")
            drawEllipsized(canvas, "🛡  Shield next run", textLeft, y, maxTextW, rewardLinePaint)
            y += rowH + rowGap
        }
        if (rewards.nextRunRapidMs > 0) {
            rewardLinePaint.color = Color.parseColor("#FFAB91")
            val sec = rewards.nextRunRapidMs / 1000
            drawEllipsized(canvas, "⏱  Rapid start  ·  ${sec}s", textLeft, y, maxTextW, rewardLinePaint)
        }

        val tick = ((now / 50) % 24).toInt()
        sparklePaint.color = Color.argb(180, Color.red(accent), Color.green(accent), Color.blue(accent))
        val corners = 20f * s
        val x = cardRect.left + corners + tick * 4f
        if (x < cardRect.right - corners) {
            canvas.drawCircle(x, cardRect.top + 2f * s, 3f * s, sparklePaint)
        }
    }

    private fun drawEllipsized(canvas: Canvas, text: String, x: Float, y: Float, maxW: Float, paint: Paint) {
        if (paint.measureText(text) <= maxW) {
            canvas.drawText(text, x, y, paint)
            return
        }
        var t = text
        while (t.length > 3 && paint.measureText("$t…") > maxW) {
            t = t.dropLast(1)
        }
        canvas.drawText("$t…", x, y, paint)
    }

    private fun drawArcadeButton(
        canvas: Canvas,
        rect: RectF,
        s: Float,
        nowMs: Long,
        pulseSeed: Float,
        topColor: Int,
        bottomColor: Int,
        glowRgb: Triple<Int, Int, Int>,
        label: String
    ) {
        val p = (sin(nowMs * 0.007 + pulseSeed) * 0.5f + 0.5f).toFloat()
        for (layer in 0..2) {
            val expand = (5f + layer * 6f) * s
            tmpInner.set(rect.left - expand, rect.top - expand, rect.right + expand, rect.bottom + expand)
            val a = ((38 - layer * 10) * (0.55f + p * 0.45f)).toInt().coerceIn(8, 45)
            glowStrokePaint.color = Color.argb(a, glowRgb.first, glowRgb.second, glowRgb.third)
            glowStrokePaint.strokeWidth = (5f - layer * 1.2f) * s
            canvas.drawRoundRect(tmpInner, 16f * s, 16f * s, glowStrokePaint)
        }

        arcadeBtnFill.shader = LinearGradient(
            rect.left, rect.top, rect.left, rect.bottom,
            topColor, bottomColor, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, 16f * s, 16f * s, arcadeBtnFill)
        arcadeBtnFill.shader = null

        arcadeBtnRim.color = Color.argb(220, 255, 255, 255)
        arcadeBtnRim.strokeWidth = 2.5f * s
        canvas.drawRoundRect(rect, 16f * s, 16f * s, arcadeBtnRim)

        arcadeBtnRim.color = Color.argb(100, 0, 0, 0)
        arcadeBtnRim.strokeWidth = 1.2f * s
        tmpInner.set(rect.left + 4f * s, rect.top + 4f * s, rect.right - 4f * s, rect.bottom - 4f * s)
        canvas.drawRoundRect(tmpInner, 12f * s, 12f * s, arcadeBtnRim)

        val rw = rect.width().coerceAtLeast(1f)
        btnLabelPaint.textSize = (rw * 0.078f).coerceIn(28f * s, 44f * s)
        btnLabelPaint.color = Color.WHITE
        btnLabelPaint.setShadowLayer(5f * s, 0f, 2.5f * s, Color.parseColor("#B0000000"))
        val btnFm = btnLabelPaint.fontMetrics
        canvas.drawText(label, rect.centerX(), rect.centerY() - (btnFm.ascent + btnFm.descent) / 2f, btnLabelPaint)
        btnLabelPaint.clearShadowLayer()
    }
}
