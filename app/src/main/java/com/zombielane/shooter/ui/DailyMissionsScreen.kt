package com.zombielane.shooter.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.zombielane.shooter.data.ChestType
import com.zombielane.shooter.data.DailyMissionManager
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Quest-style daily missions: glowing cards, animated progress, CLAIM pulse, milestones, particles.
 */
class DailyMissionsScreen {

    private val bgBasePaint = Paint().apply {
        color = Color.parseColor("#FF0A0612")
        style = Paint.Style.FILL
    }
    private val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val resetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90A4AE")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B39DDB")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }

    private val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val cardShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80000000")
        style = Paint.Style.FILL
    }
    private val glowStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    private val missionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }
    private val missionSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val progressNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#ECEFF1")
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.RIGHT
    }

    private val barBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33222835")
        style = Paint.Style.FILL
    }
    private val barFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val rewardLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

    private val claimFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val claimGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val claimTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val claimedBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#546E7A")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val backCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val backStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val backArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
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

    private val tmpCard = RectF()
    private val tmpShadow = RectF()
    private val tmpBar = RectF()
    private val tmpFill = RectF()
    private val tmpDailyBar = RectF()
    private val tmpMilestone = RectF()

    /** Top-left ← (matches Chests); drawn last so it stays above cards. */
    var backBtnRect = RectF()
    /** Wide BACK above the ad banner reserve. */
    var wideBackBtnRect = RectF()
    val claimBtnRects: Array<RectF> = Array(DailyMissionManager.MISSION_COUNT) { RectF() }

    private val barAnimFrac = FloatArray(DailyMissionManager.MISSION_COUNT) { 0f }
    private var particleW = -1f
    private var particleH = -1f
    private var particleData = FloatArray(0)

    companion object {
        private const val MS_1 = 1
        private const val MS_2 = 2
        private const val MS_3 = 4
    }

    fun draw(
        canvas: Canvas,
        safeArea: RectF,
        rows: List<DailyMissionManager.DailyMissionRow>,
        nowMs: Long,
        menuUi: MenuUiAssets,
        claimedCount: Int,
        milestoneBits: Int
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val s = w / 1080f
        /** Slightly larger type on short/narrow layouts so text stays readable. */
        val ts = max(s, min(w, safeArea.height()) / 720f)

        canvas.drawRect(0f, 0f, w, h, bgBasePaint)
        drawAmbientGlow(canvas, w, h, cx, nowMs)
        ensureParticles(w, h)
        drawParticles(canvas, nowMs)

        vignettePaint.shader = RadialGradient(
            cx, safeArea.top + safeArea.height() * 0.35f, w * 0.95f,
            intArrayOf(Color.TRANSPARENT, Color.argb(200, 0, 0, 0)),
            floatArrayOf(0.25f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, vignettePaint)
        vignettePaint.shader = null

        // Banner overlays the bottom of the SurfaceView; reserve space like MenuScreen / ChestScreen.
        val s1080 = w / 1080f
        val bannerReserve = (120f * s1080).coerceIn(100f, 168f)
        val contentBottom = safeArea.bottom - bannerReserve
        val safeH = (contentBottom - safeArea.top).coerceAtLeast(120f * ts)

        val backH = 54f * ts
        val backMargin = 16f * ts
        val cardGap = 14f * ts
        val blockGap = 14f * ts

        titlePaint.textSize = 54f * ts
        val fmTitleHeader = titlePaint.fontMetrics
        resetPaint.textSize = 26f * ts
        val fmResetHeader = resetPaint.fontMetrics
        val titleToResetGap = 14f * ts
        val resetToProgressGap = 26f * ts
        val headerBlockH =
            (-fmTitleHeader.ascent + fmTitleHeader.descent) +
                titleToResetGap +
                (-fmResetHeader.ascent + fmResetHeader.descent) +
                resetToProgressGap

        val progressSectionH = 132f * ts

        val cardW = safeArea.width() * 0.92f
        val left = safeArea.left + (safeArea.width() - cardW) / 2f
        val usableForCards = safeH - headerBlockH - progressSectionH - backH - backMargin - blockGap * 2f
        val rawCardH = usableForCards / 3f - cardGap
        val cardH = rawCardH.coerceIn(132f * ts, 280f * ts)

        val questsStackH = headerBlockH + progressSectionH + blockGap + 3f * (cardH + cardGap)
        val verticalPad = ((safeH - questsStackH - backH - backMargin).coerceAtLeast(12f * ts)) / 2f
        val yTop = safeArea.top + verticalPad

        val titleBaseline = yTop - fmTitleHeader.ascent
        titlePaint.setShadowLayer(10f * ts, 0f, 3f * ts, Color.parseColor("#6A1B9A"))
        canvas.drawText("DAILY QUESTS", cx, titleBaseline, titlePaint)
        titlePaint.clearShadowLayer()

        val resetText = rows.firstOrNull()?.let { formatReset(it.timeUntilResetMs) } ?: ""
        val resetBaseline =
            titleBaseline + fmTitleHeader.descent + titleToResetGap - fmResetHeader.ascent
        canvas.drawText(resetText, cx, resetBaseline, resetPaint)

        val progressTop = yTop + headerBlockH
        drawDailyProgressBar(canvas, safeArea, ts, cx, progressTop, claimedCount, milestoneBits, nowMs)
        var y = progressTop + progressSectionH + blockGap

        rows.forEachIndexed { index, row ->
            val targetFrac = if (row.target > 0) (row.progress.toFloat() / row.target).coerceIn(0f, 1f) else 0f
            barAnimFrac[index] += (targetFrac - barAnimFrac[index]) * 0.22f
            if (kotlin.math.abs(targetFrac - barAnimFrac[index]) < 0.002f) barAnimFrac[index] = targetFrac

            drawQuestCard(
                canvas, left, y, cardW, cardH, ts, row, index, menuUi, nowMs
            )
            y += cardH + cardGap
        }

        val backTop = contentBottom - backH - backMargin
        val backWideHalfW = 140f * ts
        wideBackBtnRect.set(cx - backWideHalfW, backTop, cx + backWideHalfW, backTop + backH)
        canvas.drawRoundRect(wideBackBtnRect, 14f * ts, 14f * ts, backBtnPaint)
        btnTextPaint.textSize = 30f * ts
        canvas.drawText("BACK", wideBackBtnRect.centerX(), wideBackBtnRect.centerY() + 11f * ts, btnTextPaint)

        val backSize = 46f * s1080
        backBtnRect.set(
            safeArea.left + 8f * s1080,
            safeArea.top + 8f * s1080,
            safeArea.left + 8f * s1080 + backSize,
            safeArea.top + 8f * s1080 + backSize
        )
        backCirclePaint.color = Color.parseColor("#1A2238")
        canvas.drawRoundRect(backBtnRect, 12f * s1080, 12f * s1080, backCirclePaint)
        backStrokePaint.color = Color.parseColor("#3D5270")
        backStrokePaint.strokeWidth = 2f * s1080
        canvas.drawRoundRect(backBtnRect, 12f * s1080, 12f * s1080, backStrokePaint)
        backArrowPaint.textSize = 32f * s1080
        backArrowPaint.color = Color.WHITE
        backArrowPaint.setShadowLayer(5f * s1080, 0f, 2f * s1080, Color.BLACK)
        canvas.drawText("←", backBtnRect.centerX(), backBtnRect.centerY() + 11f * s1080, backArrowPaint)
        backArrowPaint.clearShadowLayer()
    }

    private fun drawAmbientGlow(canvas: Canvas, w: Float, h: Float, cx: Float, nowMs: Long) {
        val pulse = (sin(nowMs * 0.0008).toFloat() * 0.5f + 0.5f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        paint.shader = RadialGradient(
            cx, h * 0.28f, w * 0.75f,
            intArrayOf(
                Color.argb((35 + pulse * 40).toInt(), 106, 27, 154),
                Color.argb((18 + pulse * 20).toInt(), 25, 15, 55),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)
    }

    private fun ensureParticles(w: Float, h: Float) {
        if (w == particleW && h == particleH && particleData.isNotEmpty()) return
        particleW = w
        particleH = h
        val rnd = Random(4242L)
        val n = 48
        particleData = FloatArray(n * 4)
        var i = 0
        repeat(n) {
            particleData[i++] = rnd.nextFloat() * w
            particleData[i++] = rnd.nextFloat() * h
            particleData[i++] = rnd.nextFloat() * 6.283f
            particleData[i++] = 2f + rnd.nextFloat() * 3.5f
        }
    }

    private fun drawParticles(canvas: Canvas, nowMs: Long) {
        val t = nowMs * 0.0012f
        var i = 0
        while (i < particleData.size) {
            val bx = particleData[i]
            val by = particleData[i + 1]
            val ph = particleData[i + 2]
            val rad = particleData[i + 3]
            val ox = sin(t + ph).toFloat() * 12f
            val oy = cos(t * 0.9f + ph).toFloat() * 10f
            val a = (40 + 55 * sin(t * 1.3 + ph)).toInt().coerceIn(25, 95)
            particlePaint.color = Color.argb(a, 200, 180, 255)
            canvas.drawCircle(bx + ox, by + oy, rad, particlePaint)
            i += 4
        }
    }

    private fun drawDailyProgressBar(
        canvas: Canvas,
        safeArea: RectF,
        s: Float,
        cx: Float,
        top: Float,
        claimed: Int,
        msBits: Int,
        nowMs: Long
    ) {
        val barW = safeArea.width() * 0.88f
        val barL = cx - barW / 2f
        val barH = 20f * s
        val padTop = 6f * s

        sectionPaint.textSize = 26f * s
        progressNumPaint.textSize = 24f * s
        val fmSec = sectionPaint.fontMetrics
        val fmNum = progressNumPaint.fontMetrics
        val labelRowAscent = max(-fmSec.ascent, -fmNum.ascent)
        val labelBaseline = top + padTop + labelRowAscent

        val countStr = "$claimed / 3"
        progressNumPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(countStr, barL + barW, labelBaseline, progressNumPaint)

        sectionPaint.textAlign = Paint.Align.LEFT
        val countW = progressNumPaint.measureText(countStr)
        val labelReserve = countW + 20f * s
        drawTextTruncated(
            canvas,
            "DAILY PROGRESS",
            barL,
            labelBaseline,
            (barW - labelReserve).coerceAtLeast(80f * s),
            sectionPaint
        )
        sectionPaint.textAlign = Paint.Align.CENTER

        val gapLabelToBar = 20f * s
        val barT = labelBaseline + max(fmSec.descent, fmNum.descent) + gapLabelToBar

        tmpDailyBar.set(barL, barT, barL + barW, barT + barH)
        canvas.drawRoundRect(tmpDailyBar, barH / 2f, barH / 2f, barBgPaint)

        val fillFrac = (claimed / 3f).coerceIn(0f, 1f)
        if (fillFrac > 0f) {
            tmpFill.set(barL, barT, barL + barW * fillFrac, barT + barH)
            barFillPaint.shader = LinearGradient(
                tmpFill.left, tmpFill.top, tmpFill.right, tmpFill.top,
                Color.parseColor("#7C4DFF"),
                Color.parseColor("#00E5FF"),
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(tmpFill, barH / 2f, barH / 2f, barFillPaint)
            barFillPaint.shader = null
        }

        val milestones = listOf(
            Triple(1, MS_1, "+75"),
            Triple(2, MS_2, "+125"),
            Triple(3, MS_3, "RARE")
        )
        val diamondR = 13f * s
        rewardLabelPaint.textSize = 18f * s
        val fmMile = rewardLabelPaint.fontMetrics
        val gapBarToMilestones = 16f * s
        val milestoneBaseline = barT + barH + gapBarToMilestones - fmMile.ascent
        for ((need, bit, label) in milestones) {
            val x = barL + barW * (need / 3f)
            val cy = barT + barH / 2f
            val earned = claimed >= need
            val granted = (msBits and bit) != 0
            val pulse = if (earned && !granted) (sin(nowMs * 0.008).toFloat() * 0.5f + 0.5f) else 1f
            val glowA = if (earned) (120 + pulse * 80).toInt().coerceIn(80, 200) else 50
            glowStrokePaint.color = Color.argb(glowA, 255, 215, 0)
            glowStrokePaint.strokeWidth = (3.5f + pulse * 2f) * s
            canvas.drawCircle(x, cy, diamondR + 4f * s, glowStrokePaint)
            particlePaint.color = when {
                granted -> Color.parseColor("#69F0AE")
                earned -> Color.parseColor("#FFD54F")
                else -> Color.parseColor("#455A64")
            }
            canvas.drawCircle(x, cy, diamondR, particlePaint)
            rewardLabelPaint.color = Color.parseColor("#ECEFF1")
            rewardLabelPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(label, x, milestoneBaseline, rewardLabelPaint)
        }
        rewardLabelPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawQuestCard(
        canvas: Canvas,
        left: Float,
        top: Float,
        cardW: Float,
        cardH: Float,
        s: Float,
        row: DailyMissionManager.DailyMissionRow,
        index: Int,
        menuUi: MenuUiAssets,
        nowMs: Long
    ) {
        val r = 18f * s
        tmpCard.set(left, top, left + cardW, top + cardH)
        tmpShadow.set(tmpCard.left + 4f * s, tmpCard.top + 6f * s, tmpCard.right + 3f * s, tmpCard.bottom + 6f * s)
        canvas.drawRoundRect(tmpShadow, r, r, cardShadowPaint)

        cardFillPaint.shader = LinearGradient(
            tmpCard.left, tmpCard.top, tmpCard.left, tmpCard.bottom,
            Color.parseColor("#E6221F35"),
            Color.parseColor("#CC151525"),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(tmpCard, r, r, cardFillPaint)
        cardFillPaint.shader = null

        val accent = when (row.kind) {
            DailyMissionManager.Kind.STAGE -> Color.parseColor("#29B6F6")
            DailyMissionManager.Kind.SHOOTER -> Color.parseColor("#FF7043")
            DailyMissionManager.Kind.KILL -> Color.parseColor("#AB47BC")
        }
        for (layer in 0..2) {
            val ex = (4f + layer * 5f) * s
            glowStrokePaint.strokeWidth = (2.5f - layer * 0.5f) * s
            glowStrokePaint.color = Color.argb(45 - layer * 12, Color.red(accent), Color.green(accent), Color.blue(accent))
            tmpShadow.set(tmpCard.left - ex, tmpCard.top - ex, tmpCard.right + ex, tmpCard.bottom + ex)
            canvas.drawRoundRect(tmpShadow, r + 2f * s, r + 2f * s, glowStrokePaint)
        }
        glowStrokePaint.color = Color.argb(200, Color.red(accent), Color.green(accent), Color.blue(accent))
        glowStrokePaint.strokeWidth = 1.5f * s
        canvas.drawRoundRect(tmpCard, r, r, glowStrokePaint)

        val pad = 16f * s
        val iconColW = 56f * s
        val iconCx = tmpCard.left + pad + iconColW / 2f
        val claimW = 120f * s
        val claimH = 42f * s
        val claimLeft = tmpCard.right - pad - claimW
        val claimTop = tmpCard.top + pad + 2f * s
        claimBtnRects[index].set(claimLeft, claimTop, claimLeft + claimW, claimTop + claimH)

        val textLeft = tmpCard.left + pad + iconColW + 14f * s
        val textRight = claimLeft - 14f * s
        val textMaxW = (textRight - textLeft).coerceAtLeast(40f * s)

        val barH = (18f * s).coerceAtLeast(14f)
        val barBottom = tmpCard.bottom - pad
        val barTop = barBottom - barH
        val barLeft = tmpCard.left + pad
        val barRight = tmpCard.right - pad

        progressNumPaint.textSize = (22f * s).coerceAtLeast(13f)
        progressNumPaint.textAlign = Paint.Align.LEFT
        val fmProgress = progressNumPaint.fontMetrics
        val labelY = barTop - 12f * s - fmProgress.descent

        val contentTop = tmpCard.top + pad
        missionTitlePaint.textSize = 28f * s
        val fmTitle = missionTitlePaint.fontMetrics
        val titleBaseline = contentTop - fmTitle.ascent
        drawTextTruncated(canvas, row.title, textLeft, titleBaseline, textMaxW, missionTitlePaint)

        missionSubPaint.textSize = 21f * s
        val fmSub = missionSubPaint.fontMetrics
        val gapTitleSub = 8f * s
        val subBaseline = titleBaseline + fmTitle.descent + gapTitleSub - fmSub.ascent
        drawTextTruncated(canvas, row.subtitle, textLeft, subBaseline, textMaxW, missionSubPaint)

        val textBlockBottom = subBaseline + fmSub.descent
        val textBlockMidY = (contentTop + textBlockBottom) / 2f
        iconPaint.textSize = 44f * s
        val fmIcon = iconPaint.fontMetrics
        val iconBaseline = textBlockMidY - (fmIcon.ascent + fmIcon.descent) / 2f
        canvas.drawText(kindEmoji(row.kind), iconCx, iconBaseline, iconPaint)

        val gapBeforeRewards = 12f * s
        rewardLabelPaint.textAlign = Paint.Align.LEFT
        rewardLabelPaint.textSize = (22f * s).coerceAtLeast(14f)
        val fmReward = rewardLabelPaint.fontMetrics
        val rewardBaseline = textBlockBottom + gapBeforeRewards - fmReward.ascent
        val rewardRowBottom = rewardBaseline + fmReward.descent
        val minGapRewardToProgress = 14f * s
        val maxRewardBaseline = labelY + fmProgress.ascent - minGapRewardToProgress - fmReward.descent
        val adjustedRewardBaseline = min(rewardBaseline, maxRewardBaseline)

        var rx = textLeft
        val rewardMidY = adjustedRewardBaseline + (fmReward.ascent + fmReward.descent) / 2f

        if (row.rewardCoins > 0) {
            val coinBmp: Bitmap = menuUi.coin
            val ch = (28f * s).coerceAtLeast(18f)
            val sc = ch / coinBmp.height.coerceAtLeast(1)
            val cW = coinBmp.width * sc
            val coinTop = rewardMidY - ch / 2f
            tmpFill.set(rx, coinTop, rx + cW, coinTop + ch)
            canvas.drawBitmap(coinBmp, null, tmpFill, bitmapPaint)
            rx += cW + 10f * s
            rewardLabelPaint.color = Color.parseColor("#FFD54F")
            canvas.drawText("${row.rewardCoins}", rx, adjustedRewardBaseline, rewardLabelPaint)
            rx += rewardLabelPaint.measureText("${row.rewardCoins}") + 14f * s
        }
        if (row.rewardChest != null) {
            val chestBmp = menuUi.chest(row.rewardChest)
            val chestH = (26f * s).coerceAtLeast(16f)
            val cSc = chestH / chestBmp.height.coerceAtLeast(1)
            val chestW = chestBmp.width * cSc
            val chestTop = rewardMidY - chestH / 2f
            if (rx + chestW + 6f * s <= textRight) {
                tmpFill.set(rx, chestTop, rx + chestW, chestTop + chestH)
                canvas.drawBitmap(chestBmp, null, tmpFill, bitmapPaint)
                rx += chestW + 8f * s
            }
            rewardLabelPaint.color = chestRarityColor(row.rewardChest)
            val chestMaxW = (textRight - rx).coerceAtLeast(0f)
            drawTextTruncated(
                canvas,
                row.rewardChest.displayName,
                rx,
                adjustedRewardBaseline,
                chestMaxW,
                rewardLabelPaint
            )
        }

        canvas.drawText("${row.progress} / ${row.target}", barLeft, labelY, progressNumPaint)

        tmpBar.set(barLeft, barTop, barRight, barBottom)
        canvas.drawRoundRect(tmpBar, 7f * s, 7f * s, barBgPaint)

        val frac = barAnimFrac[index].coerceIn(0f, 1f)
        val shimmer = 0.92f + 0.08f * sin(nowMs * 0.004 + index).toFloat()
        if (frac > 0.01f) {
            val wFill = (tmpBar.width() * frac * shimmer).coerceAtMost(tmpBar.width())
            tmpFill.set(barLeft, barTop, barLeft + wFill, barBottom)
            barFillPaint.shader = LinearGradient(
                tmpFill.left, tmpFill.top, tmpFill.right, tmpFill.top,
                Color.parseColor("#66BB6A"),
                Color.parseColor("#1B5E20"),
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(tmpFill, 7f * s, 7f * s, barFillPaint)
            barFillPaint.shader = null
        }

        when {
            row.canClaim -> {
                val pulse = 1f + 0.06f * sin(nowMs * 0.007).toFloat()
                val cw = claimW * pulse
                val ch = claimH * pulse
                val cx = claimBtnRects[index].centerX()
                val cy = claimBtnRects[index].centerY()
                tmpFill.set(cx - cw / 2f, cy - ch / 2f, cx + cw / 2f, cy + ch / 2f)
                for (g in 0..2) {
                    val gex = (4f + g * 5f) * s * pulse
                    claimGlowPaint.color = Color.argb(50 - g * 12, 255, 193, 7)
                    claimGlowPaint.strokeWidth = (4f - g) * s
                    tmpShadow.set(tmpFill.left - gex, tmpFill.top - gex, tmpFill.right + gex, tmpFill.bottom + gex)
                    canvas.drawRoundRect(tmpShadow, 12f * s, 12f * s, claimGlowPaint)
                }
                claimFillPaint.shader = LinearGradient(
                    tmpFill.left, tmpFill.top, tmpFill.left, tmpFill.bottom,
                    Color.parseColor("#FFCA28"),
                    Color.parseColor("#F57F17"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(tmpFill, 12f * s, 12f * s, claimFillPaint)
                claimFillPaint.shader = null
                claimGlowPaint.color = Color.argb(220, 255, 255, 255)
                claimGlowPaint.strokeWidth = 1.5f * s
                canvas.drawRoundRect(tmpFill, 12f * s, 12f * s, claimGlowPaint)
                claimTextPaint.textSize = 24f * s
                claimTextPaint.setShadowLayer(4f * s, 0f, 2f * s, Color.parseColor("#80000000"))
                canvas.drawText("CLAIM", cx, cy + 8f * s, claimTextPaint)
                claimTextPaint.clearShadowLayer()
            }
            row.claimed -> {
                claimedBadgePaint.textSize = 22f * s
                canvas.drawText("CLAIMED", claimBtnRects[index].centerX(), claimBtnRects[index].centerY() + 8f * s, claimedBadgePaint)
            }
            else -> {
                claimBtnRects[index].setEmpty()
            }
        }
    }

    private fun drawTextTruncated(canvas: Canvas, text: String, x: Float, y: Float, maxW: Float, paint: Paint) {
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

    private fun kindEmoji(kind: DailyMissionManager.Kind): String = when (kind) {
        DailyMissionManager.Kind.STAGE -> "🚀"
        DailyMissionManager.Kind.SHOOTER -> "🔫"
        DailyMissionManager.Kind.KILL -> "💀"
    }

    private fun chestRarityColor(t: ChestType): Int = when (t) {
        ChestType.COMMON -> Color.parseColor("#90A4AE")
        ChestType.RARE -> Color.parseColor("#42A5F5")
        ChestType.EPIC -> Color.parseColor("#E040FB")
        ChestType.SUPER -> Color.parseColor("#FFD700")
    }

    private fun formatReset(ms: Long): String {
        if (ms <= 0L) return "Resets soon"
        val h = ms / 3_600_000L
        val m = (ms % 3_600_000L) / 60_000L
        return if (h > 0) "Resets in ${h}h ${m}m" else "Resets in ${m}m"
    }
}
