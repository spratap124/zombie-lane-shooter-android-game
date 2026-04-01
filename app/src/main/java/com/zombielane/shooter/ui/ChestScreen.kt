package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.zombielane.shooter.data.ChestManager
import com.zombielane.shooter.data.ChestRevealPhase
import com.zombielane.shooter.data.ChestSlot
import com.zombielane.shooter.data.ChestType
import com.zombielane.shooter.data.ChestVisualState
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Chest reward hub: compact 2×2 grid, tap to focus hero + detail panel, Canvas-only.
 */
class ChestScreen {

    var backBtnRect = RectF()
    var mergeBtnRect = RectF()
    val slotCardRects = Array(ChestManager.MAX_SLOTS) { RectF() }
    /** Legacy hit targets; unused in new layout (detail panel uses [detailOpenRect] / [detailSkipRect]). */
    val slotOpenRects = Array(ChestManager.MAX_SLOTS) { RectF() }
    val slotSkipRects = Array(ChestManager.MAX_SLOTS) { RectF() }

    var detailOpenRect = RectF()
    var detailSkipRect = RectF()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val headerSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val rarityPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val statePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val timerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val lockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val cardStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val glowStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val flashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

    private val rarityBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val emptyChestFilter = ColorMatrixColorFilter(
        ColorMatrix().apply { setSaturation(0.2f) }
    )

    private val arcadeBtnFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val arcadeBtnRim = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val mergeBtnFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val mergeGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val backCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val backStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    private val tmpCard = RectF()
    private val tmpInner = RectF()
    private val tmpBitmapDst = RectF()
    private val tmpBtn = RectF()

    fun draw(
        canvas: Canvas,
        safeArea: RectF,
        slots: List<ChestSlot?>,
        now: Long,
        mergeMode: Boolean,
        mergeSelectedIndex: Int,
        streak: Int,
        selectedSlotIndex: Int,
        openingSlotIndex: Int,
        openingPhase: ChestRevealPhase,
        openingPhaseStartMs: Long,
        menuUi: MenuUiAssets,
        slotVisualStates: Array<ChestVisualState>
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val s = w / 1080f
        val sw = safeArea.width()

        detailOpenRect.setEmpty()
        detailSkipRect.setEmpty()
        for (i in 0 until ChestManager.MAX_SLOTS) {
            slotOpenRects[i].setEmpty()
            slotSkipRects[i].setEmpty()
        }

        // Solid fill avoids vertical banding next to cards and the detail strip.
        bgPaint.color = Color.parseColor("#0A0E18")
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        val backSize = 46f * s
        backBtnRect.set(safeArea.left + 8f * s, safeArea.top + 8f * s, safeArea.left + 8f * s + backSize, safeArea.top + 8f * s + backSize)
        backCirclePaint.color = Color.parseColor("#1A2238")
        canvas.drawRoundRect(backBtnRect, 12f * s, 12f * s, backCirclePaint)
        backStrokePaint.color = Color.parseColor("#3D5270")
        backStrokePaint.strokeWidth = 2f * s
        canvas.drawRoundRect(backBtnRect, 12f * s, 12f * s, backStrokePaint)
        titlePaint.textSize = 32f * s
        titlePaint.color = Color.WHITE
        titlePaint.setShadowLayer(5f * s, 0f, 2f * s, Color.BLACK)
        canvas.drawText("←", backBtnRect.centerX(), backBtnRect.centerY() + 11f * s, titlePaint)
        titlePaint.clearShadowLayer()

        val titleY = safeArea.top + 58f * s
        titlePaint.textSize = 36f * s
        titlePaint.shader = LinearGradient(
            cx - 160f * s, titleY, cx + 160f * s, titleY,
            intArrayOf(Color.parseColor("#FFE082"), Color.parseColor("#FFAB00"), Color.parseColor("#FFE082")),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        titlePaint.setShadowLayer(6f * s, 0f, 2f * s, Color.parseColor("#80000000"))
        canvas.drawText("CHESTS", cx, titleY, titlePaint)
        titlePaint.clearShadowLayer()
        titlePaint.shader = null

        val filled = slots.count { it != null }
        headerSubPaint.textSize = 20f * s
        headerSubPaint.color = Color.parseColor("#90A4AE")
        val streakLabel = if (streak == 1) "day" else "days"
        canvas.drawText("$filled/${ChestManager.MAX_SLOTS}  ·  $streak-$streakLabel streak", cx, safeArea.top + 92f * s, headerSubPaint)

        val pad = 12f * s
        val colGap = max(20f * s, sw * 0.028f).coerceAtLeast(14f)
        val rowGap = max(20f * s, sw * 0.028f).coerceAtLeast(14f)
        val headerBottom = safeArea.top + 102f * s
        val mergeH = max(56f * s, sw * 0.12f).coerceAtMost(96f)
        val mergeBottomMargin = 12f * s
        // Match MenuScreen: ad banner sits on top of the view at the bottom; reserve space so buttons stay tappable.
        val bannerReserve = (120f * s).coerceIn(100f, 168f)
        val contentBottom = safeArea.bottom - bannerReserve
        val mergeTop = contentBottom - mergeH - mergeBottomMargin
        val panelGap = 8f * s

        val detailPanelBottom = mergeTop - panelGap
        val hasSelection = selectedSlotIndex in 0 until ChestManager.MAX_SLOTS
        val availW = safeArea.width() - pad * 2f
        val colWFull = (availW - colGap) / 2f
        val gridTop = headerBottom + 8f * s
        val gapUnderGrid = max(14f * s, sw * 0.02f)
        // Room for large hero chest + labels + tall arcade button.
        val minDetailContentH = max(300f * s, sw * 0.42f + 150f * s).coerceAtMost(h * 0.38f)

        val cellSide: Float
        val detailPanelTop: Float
        if (hasSelection) {
            val maxCellByDetail = (
                (detailPanelBottom - minDetailContentH - gridTop - rowGap - gapUnderGrid) / 2f
                ).coerceAtLeast(56f * s)
            cellSide = min(colWFull, maxCellByDetail)
            val gridContentH = cellSide * 2f + rowGap
            detailPanelTop = gridTop + gridContentH + gapUnderGrid
        } else {
            val maxCellByHeight = ((detailPanelBottom - gridTop - 8f * s - rowGap) / 2f).coerceAtLeast(1f)
            cellSide = min(colWFull, maxCellByHeight)
            detailPanelTop = detailPanelBottom
        }

        val gridTotalW = cellSide * 2f + colGap
        val hStart = safeArea.left + pad + (availW - gridTotalW) * 0.5f
        val left0 = hStart
        val left1 = hStart + cellSide + colGap
        val gridContentH = cellSide * 2f + rowGap

        val col = intArrayOf(0, 1, 0, 1)
        val row = intArrayOf(0, 0, 1, 1)
        for (i in 0 until ChestManager.MAX_SLOTS) {
            val l = if (col[i] == 0) left0 else left1
            val t = gridTop + row[i] * (cellSide + rowGap)
            tmpCard.set(l, t, l + cellSide, t + cellSide)
            slotCardRects[i].set(tmpCard)
            drawCompactSlot(
                canvas, menuUi, s, i, tmpCard, slots[i], now,
                mergeMode, mergeSelectedIndex, selectedSlotIndex,
                openingSlotIndex, openingPhase, openingPhaseStartMs,
                slotVisualStates[i]
            )
        }

        if (hasSelection) {
            drawDetailPanel(
                canvas, menuUi, s, safeArea, cx, detailPanelTop, detailPanelBottom,
                slots[selectedSlotIndex], now, selectedSlotIndex,
                slotVisualStates[selectedSlotIndex]
            )
        }

        mergeBtnRect.set(safeArea.left + pad, mergeTop, safeArea.right - pad, mergeTop + mergeH)
        val mergePulse = (sin(now * 0.005).toFloat() * 0.5f + 0.5f)
        mergeGlowPaint.color = Color.argb((36 + mergePulse * 48).toInt(), 171, 71, 188)
        mergeGlowPaint.strokeWidth = (5f + mergePulse * 3f) * s
        tmpInner.set(
            mergeBtnRect.left - 3f * s, mergeBtnRect.top - 3f * s,
            mergeBtnRect.right + 3f * s, mergeBtnRect.bottom + 3f * s
        )
        canvas.drawRoundRect(tmpInner, 18f * s, 18f * s, mergeGlowPaint)

        mergeBtnFillPaint.shader = LinearGradient(
            mergeBtnRect.left, mergeBtnRect.top, mergeBtnRect.left, mergeBtnRect.bottom,
            if (mergeMode) Color.parseColor("#8E24AA") else Color.parseColor("#5E35B1"),
            if (mergeMode) Color.parseColor("#4A148C") else Color.parseColor("#311B92"),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(mergeBtnRect, 14f * s, 14f * s, mergeBtnFillPaint)
        mergeBtnFillPaint.shader = null

        mergeGlowPaint.color = Color.parseColor("#E1BEE7")
        mergeGlowPaint.strokeWidth = 2f * s
        canvas.drawRoundRect(mergeBtnRect, 14f * s, 14f * s, mergeGlowPaint)

        btnTextPaint.textSize = max(24f * s, sw * 0.038f).coerceAtMost(34f)
        btnTextPaint.color = Color.WHITE
        btnTextPaint.setShadowLayer(3f * s, 0f, 1.5f * s, Color.parseColor("#60000000"))
        val mergeLabel = if (mergeMode) "Tap two chests to merge" else "Merge chests"
        canvas.drawText(mergeLabel, mergeBtnRect.centerX(), mergeBtnRect.centerY() + 8f * s, btnTextPaint)
        btnTextPaint.clearShadowLayer()
    }

    private fun drawCompactSlot(
        canvas: Canvas,
        menuUi: MenuUiAssets,
        s: Float,
        index: Int,
        card: RectF,
        slot: ChestSlot?,
        now: Long,
        mergeMode: Boolean,
        mergeSelectedIndex: Int,
        selectedSlotIndex: Int,
        openingSlotIndex: Int,
        openingPhase: ChestRevealPhase,
        openingPhaseStartMs: Long,
        visualState: ChestVisualState
    ) {
        val openingElapsed = if (openingSlotIndex == index && openingPhase == ChestRevealPhase.SPINNING) {
            (now - openingPhaseStartMs).coerceAtLeast(0L)
        } else {
            0L
        }
        val readyGlow = slot != null && slot.isReady(now)
        val unlocking = slot != null && !slot.isReady(now)
        val breathe = if (unlocking) sin(now * 0.011 + index * 0.9f).toFloat() * 0.04f else 0f
        val pulse = when {
            openingElapsed > 0L -> 1f + sin(openingElapsed * 0.018).toFloat() * 0.1f
            readyGlow -> 1f + sin(now * 0.008 + index * 0.7f).toFloat() * 0.045f
            unlocking -> 1f + breathe
            else -> 1f
        }

        val glowRgb = when (slot?.type) {
            ChestType.RARE -> Triple(33, 150, 243)
            ChestType.EPIC -> Triple(224, 64, 251)
            ChestType.SUPER -> Triple(255, 215, 0)
            else -> Triple(120, 144, 156)
        }

        if (readyGlow) {
            val tier = slot!!.type
            val gp = (sin(now * 0.007 + index).toFloat() * 0.5f + 0.5f)
            if (tier != ChestType.COMMON) {
                for (layer in 0..2) {
                    val expand = (5f + layer * 4f) * s * pulse
                    tmpInner.set(card.left - expand, card.top - expand, card.right + expand, card.bottom + expand)
                    val a = ((32 - layer * 9) * (0.55f + gp * 0.45f)).toInt().coerceIn(6, 42)
                    glowStrokePaint.color = Color.argb(a, glowRgb.first, glowRgb.second, glowRgb.third)
                    glowStrokePaint.strokeWidth = (3.5f - layer * 0.8f) * s
                    canvas.drawRoundRect(tmpInner, 14f * s, 14f * s, glowStrokePaint)
                }
            } else {
                tmpInner.set(card.left - 5f * s * pulse, card.top - 5f * s * pulse, card.right + 5f * s * pulse, card.bottom + 5f * s * pulse)
                glowStrokePaint.color = Color.argb((22 + gp * 28).toInt(), 158, 158, 158)
                glowStrokePaint.strokeWidth = 2.5f * s
                canvas.drawRoundRect(tmpInner, 14f * s, 14f * s, glowStrokePaint)
            }
        }

        val cornerR = 14f * s
        val side = min(card.width(), card.height()) * 0.82f * pulse
        val iconCy = card.centerY() + (if (unlocking) sin(now * 0.009 + index).toFloat() * 3f * s else 0f)

        if (slot == null) {
            cardFillPaint.color = Color.parseColor("#12182A")
            canvas.drawRoundRect(card, cornerR, cornerR, cardFillPaint)
            cardStrokePaint.color = Color.parseColor("#252E42")
            cardStrokePaint.strokeWidth = 1.5f * s
            canvas.drawRoundRect(card, cornerR, cornerR, cardStrokePaint)

            cardFillPaint.color = Color.argb(70, 0, 0, 0)
            canvas.drawRoundRect(card, cornerR, cornerR, cardFillPaint)

            lockPaint.textSize = min(40f * s, side * 0.85f)
            lockPaint.color = Color.argb(100, 180, 190, 210)
            canvas.drawText("🔒", card.centerX(), iconCy + lockPaint.textSize * 0.35f, lockPaint)
        } else {
            val tier = slot.type
            cardFillPaint.color = tierCardFill(tier, now)
            canvas.drawRoundRect(card, cornerR, cornerR, cardFillPaint)

            cardStrokePaint.color = tierStroke(tier, now)
            cardStrokePaint.strokeWidth = if (tier == ChestType.SUPER) {
                (2f + sin(now * 0.004).toFloat() * 0.6f) * s
            } else {
                2f * s
            }
            canvas.drawRoundRect(card, cornerR, cornerR, cardStrokePaint)

            drawChestBitmap(
                canvas, menuUi, tier, card.centerX(), iconCy, side,
                emptySlot = false,
                unlocking = unlocking,
                visualState = visualState
            )
        }

        if (selectedSlotIndex == index) {
            cardStrokePaint.color = Color.parseColor("#FFD54F")
            cardStrokePaint.strokeWidth = 3.5f * s
            tmpInner.set(card.left - 2f * s, card.top - 2f * s, card.right + 2f * s, card.bottom + 2f * s)
            canvas.drawRoundRect(tmpInner, cornerR + 2f * s, cornerR + 2f * s, cardStrokePaint)
        }

        if (openingElapsed > 0L) {
            val flash = (255 * (1f - (openingElapsed.coerceAtMost(450L) / 450f))).toInt().coerceIn(0, 255)
            if (flash > 0) {
                flashPaint.color = Color.argb(flash, 255, 255, 255)
                canvas.drawRoundRect(card, cornerR, cornerR, flashPaint)
            }
        }

        if (mergeMode && mergeSelectedIndex == index && slot != null) {
            cardStrokePaint.color = Color.parseColor("#00E676")
            cardStrokePaint.strokeWidth = 4f * s
            tmpInner.set(card.left - 3f * s, card.top - 3f * s, card.right + 3f * s, card.bottom + 3f * s)
            canvas.drawRoundRect(tmpInner, cornerR + 2f * s, cornerR + 2f * s, cardStrokePaint)
        }
    }

    private fun drawDetailPanel(
        canvas: Canvas,
        menuUi: MenuUiAssets,
        s: Float,
        safeArea: RectF,
        cx: Float,
        panelTop: Float,
        panelBottom: Float,
        slot: ChestSlot?,
        now: Long,
        slotIndex: Int,
        visualState: ChestVisualState
    ) {
        val panelH = panelBottom - panelTop
        val swPanel = safeArea.width()
        if (slot == null) {
            val icy = panelTop + min(48f * s, panelH * 0.28f)
            lockPaint.textSize = min(max(56f * s, swPanel * 0.12f), panelH * 0.38f)
            lockPaint.color = Color.argb(120, 150, 165, 185)
            canvas.drawText("🔒", cx, icy + lockPaint.textSize * 0.35f, lockPaint)
            statePaint.textSize = max(20f * s, swPanel * 0.04f)
            statePaint.color = Color.parseColor("#78909C")
            canvas.drawText("Locked", cx, icy + lockPaint.textSize + 26f * s, statePaint)
            return
        }

        val ready = slot.isReady(now)
        val unlocking = !ready
        val heroPulse = when {
            ready -> 1f + sin(now * 0.007 + slotIndex).toFloat() * 0.06f
            else -> 1f + sin(now * 0.01 + slotIndex).toFloat() * 0.035f
        }
        val tier = slot.type
        val heroReserveBelow = max(130f * s, swPanel * 0.26f)
        val heroSideRaw = min(swPanel * 0.52f, 260f * s).coerceAtLeast(160f * s)
        val heroSideBase = min(heroSideRaw, (panelH - heroReserveBelow).coerceAtLeast(120f * s))
        val heroSideDraw = heroSideBase * heroPulse
        val yHeroTop = panelTop + max(12f * s, swPanel * 0.018f)
        // Stable layout anchor: pulse/bounce only affect drawing, not label/timer/button Y.
        val heroCyLayout = yHeroTop + heroSideBase / 2f
        val unlockBob = if (unlocking) sin(now * 0.008).toFloat() * 3f * s else 0f
        val heroCyDraw = heroCyLayout + unlockBob

        val glowRgb = when (tier) {
            ChestType.RARE -> Triple(33, 150, 243)
            ChestType.EPIC -> Triple(224, 64, 251)
            ChestType.SUPER -> Triple(255, 215, 0)
            else -> Triple(158, 158, 158)
        }

        if (ready) {
            val gp = (sin(now * 0.006 + slotIndex).toFloat() * 0.5f + 0.5f)
            if (tier != ChestType.COMMON) {
                for (layer in 0..3) {
                    val expand = (10f + layer * 7f) * s * heroPulse
                    tmpCard.set(
                        cx - heroSideDraw / 2f - expand, heroCyDraw - heroSideDraw / 2f - expand,
                        cx + heroSideDraw / 2f + expand, heroCyDraw + heroSideDraw / 2f + expand
                    )
                    val a = ((40 - layer * 8) * (0.5f + gp * 0.5f)).toInt().coerceIn(5, 50)
                    glowStrokePaint.color = Color.argb(a, glowRgb.first, glowRgb.second, glowRgb.third)
                    glowStrokePaint.strokeWidth = (5f - layer * 0.9f) * s
                    val rr = heroSideDraw / 2f + expand
                    canvas.drawRoundRect(tmpCard, rr * 0.35f, rr * 0.35f, glowStrokePaint)
                }
            } else {
                val gp2 = (sin(now * 0.006).toFloat() * 0.5f + 0.5f)
                tmpCard.set(
                    cx - heroSideDraw / 2f - 8f * s, heroCyDraw - heroSideDraw / 2f - 8f * s,
                    cx + heroSideDraw / 2f + 8f * s, heroCyDraw + heroSideDraw / 2f + 8f * s
                )
                glowStrokePaint.color = Color.argb((28 + gp2 * 35).toInt(), 180, 190, 200)
                glowStrokePaint.strokeWidth = 3.5f * s
                canvas.drawRoundRect(tmpCard, 20f * s, 20f * s, glowStrokePaint)
            }
        }

        drawChestBitmap(
            canvas, menuUi, tier, cx, heroCyDraw, heroSideDraw,
            emptySlot = false, unlocking = unlocking, visualState = visualState
        )

        val labelGap = max(16f * s, swPanel * 0.028f) + heroSideBase * 0.04f
        val labelY = heroCyLayout + heroSideBase / 2f + labelGap
        val label = tier.displayName.uppercase()
        rarityPaint.textSize = max(17f * s, swPanel * 0.038f).coerceAtMost(30f)
        rarityPaint.color = Color.WHITE
        val tw = rarityPaint.measureText(label)
        val badgePadH = max(14f * s, swPanel * 0.022f)
        val badgePadV = max(5f * s, swPanel * 0.012f)
        tmpCard.set(cx - tw / 2f - badgePadH, labelY - 14f * s, cx + tw / 2f + badgePadH, labelY + badgePadV)
        rarityBadgePaint.color = Color.argb(130, Color.red(tierAccent(tier)), Color.green(tierAccent(tier)), Color.blue(tierAccent(tier)))
        canvas.drawRoundRect(tmpCard, 10f * s, 10f * s, rarityBadgePaint)
        canvas.drawText(label, cx, labelY, rarityPaint)

        val stateY = labelY + max(24f * s, swPanel * 0.042f)
        if (ready) {
            statePaint.textSize = max(16f * s, swPanel * 0.036f).coerceAtMost(28f)
            statePaint.color = Color.parseColor("#A5D6A7")
            canvas.drawText("Ready to open", cx, stateY, statePaint)
        } else {
            timerPaint.textSize = max(26f * s, swPanel * 0.055f).coerceAtMost(44f)
            timerPaint.color = Color.parseColor("#FFD54F")
            canvas.drawText(formatRemaining(slot.remainingMs(now)), cx, stateY, timerPaint)
        }

        val btnW = (swPanel - 32f * s).coerceAtLeast(200f)
        val btnH = max(56f * s, swPanel * 0.128f).coerceAtMost(100f)
        val btnLeft = cx - btnW / 2f
        // Pin to panel bottom so the hit target never shifts when timer text or hero animation changes.
        val btnTop = panelBottom - 10f * s - btnH
        tmpBtn.set(btnLeft, btnTop, btnLeft + btnW, btnTop + btnH)
        // Hit area larger than draw rect so taps on glow / edges still register (banner sits below; no vertical inflate down).
        val hitPadH = max(28f * s, swPanel * 0.04f)
        val hitPadTop = max(20f * s, swPanel * 0.028f)
        val hitPadBottom = min(12f * s, swPanel * 0.018f)

        if (ready) {
            detailOpenRect.set(
                tmpBtn.left - hitPadH, tmpBtn.top - hitPadTop,
                tmpBtn.right + hitPadH, tmpBtn.bottom + hitPadBottom
            )
            drawArcadeButton(
                canvas, tmpBtn, s, now, slotIndex * 1.7f,
                Color.parseColor("#66FF8A"),
                Color.parseColor("#1B5E20"),
                Triple(76, 175, 80),
                "⚡ Open"
            )
        } else {
            detailSkipRect.set(
                tmpBtn.left - hitPadH, tmpBtn.top - hitPadTop,
                tmpBtn.right + hitPadH, tmpBtn.bottom + hitPadBottom
            )
            drawArcadeButton(
                canvas, tmpBtn, s, now, slotIndex * 1.7f + 3f,
                Color.parseColor("#FFB74D"),
                Color.parseColor("#BF360C"),
                Triple(255, 152, 0),
                "📺 Skip"
            )
        }
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
        val p = (sin(nowMs * 0.007 + pulseSeed) * 0.5f + 0.5f)
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
        canvas.drawRoundRect(rect, 12f * s, 12f * s, arcadeBtnFill)
        arcadeBtnFill.shader = null

        arcadeBtnRim.color = Color.argb(200, 255, 255, 255)
        arcadeBtnRim.strokeWidth = 2f * s
        canvas.drawRoundRect(rect, 12f * s, 12f * s, arcadeBtnRim)

        arcadeBtnRim.color = Color.argb(90, 0, 0, 0)
        arcadeBtnRim.strokeWidth = 1f * s
        tmpInner.set(rect.left + 3f * s, rect.top + 3f * s, rect.right - 3f * s, rect.bottom - 3f * s)
        canvas.drawRoundRect(tmpInner, 9f * s, 9f * s, arcadeBtnRim)

        val rw = rect.width().coerceAtLeast(1f)
        btnTextPaint.textSize = (rw * 0.065f).coerceIn(22f * s, 36f)
        btnTextPaint.color = Color.WHITE
        btnTextPaint.setShadowLayer(4f * s, 0f, 2f * s, Color.parseColor("#80000000"))
        canvas.drawText(label, rect.centerX(), rect.centerY() + btnTextPaint.textSize * 0.35f, btnTextPaint)
        btnTextPaint.clearShadowLayer()
    }

    private fun drawChestBitmap(
        canvas: Canvas,
        menuUi: MenuUiAssets,
        type: ChestType,
        cx: Float,
        cy: Float,
        side: Float,
        emptySlot: Boolean,
        unlocking: Boolean,
        visualState: ChestVisualState = ChestVisualState.CLOSED
    ) {
        tmpBitmapDst.set(cx - side / 2f, cy - side / 2f, cx + side / 2f, cy + side / 2f)
        when {
            emptySlot -> {
                bitmapPaint.colorFilter = emptyChestFilter
                bitmapPaint.alpha = 100
            }
            unlocking -> {
                bitmapPaint.colorFilter = ColorMatrixColorFilter(
                    ColorMatrix(
                        floatArrayOf(
                            0.55f, 0f, 0f, 0f, 25f,
                            0f, 0.55f, 0f, 0f, 25f,
                            0f, 0f, 0.65f, 0f, 20f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
                bitmapPaint.alpha = 255
            }
            else -> {
                bitmapPaint.colorFilter = null
                bitmapPaint.alpha = 255
            }
        }
        val bmp = if (visualState == ChestVisualState.OPENED) menuUi.chestOpen(type) else menuUi.chest(type)
        canvas.drawBitmap(bmp, null, tmpBitmapDst, bitmapPaint)
        bitmapPaint.colorFilter = null
        bitmapPaint.alpha = 255
    }

    private fun tierCardFill(t: ChestType, now: Long): Int = when (t) {
        ChestType.COMMON -> Color.parseColor("#1A2436")
        ChestType.RARE -> Color.parseColor("#0D2840")
        ChestType.EPIC -> Color.parseColor("#180D28")
        ChestType.SUPER -> {
            val p = (sin(now * 0.003) * 22).toInt()
            Color.argb(255, (38 + p).coerceIn(28, 65), 26, 16)
        }
    }

    private fun tierStroke(t: ChestType, now: Long): Int = when (t) {
        ChestType.COMMON -> Color.parseColor("#455A64")
        ChestType.RARE -> Color.parseColor("#42A5F5")
        ChestType.EPIC -> Color.parseColor("#E040FB")
        ChestType.SUPER -> {
            val g = (sin(now * 0.005) * 38 + 218).toInt().coerceIn(175, 255)
            Color.rgb(g, (g * 0.85f).toInt(), 0)
        }
    }

    private fun tierAccent(t: ChestType): Int = when (t) {
        ChestType.COMMON -> Color.parseColor("#B0BEC5")
        ChestType.RARE -> Color.parseColor("#64B5F6")
        ChestType.EPIC -> Color.parseColor("#EA80FC")
        ChestType.SUPER -> Color.parseColor("#FFE082")
    }

    private fun formatRemaining(ms: Long): String {
        val sec = (ms / 1000).toInt()
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val secRem = sec % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, secRem)
        else String.format("%d:%02d", m, secRem)
    }
}
