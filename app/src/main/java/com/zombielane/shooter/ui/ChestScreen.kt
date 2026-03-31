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
import kotlin.math.sin

/**
 * Chest inventory: 2×2 grid, rarity-themed cards, glow for ready chests, Canvas-only.
 */
class ChestScreen {

    var backBtnRect = RectF()
    var mergeBtnRect = RectF()
    val slotCardRects = Array(ChestManager.MAX_SLOTS) { RectF() }
    val slotOpenRects = Array(ChestManager.MAX_SLOTS) { RectF() }
    val slotSkipRects = Array(ChestManager.MAX_SLOTS) { RectF() }

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

    private val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val cardStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val glowStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val flashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

    private val rarityBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    /** Desaturated chest art for empty slots. */
    private val emptyChestFilter = ColorMatrixColorFilter(
        ColorMatrix().apply { setSaturation(0.2f) }
    )

    private val openBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val skipBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
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

    private var frame = 0L

    fun draw(
        canvas: Canvas,
        safeArea: RectF,
        slots: List<ChestSlot?>,
        now: Long,
        mergeMode: Boolean,
        mergeSelectedIndex: Int,
        streak: Int,
        openingSlotIndex: Int,
        openingPhase: ChestRevealPhase,
        openingPhaseStartMs: Long,
        menuUi: MenuUiAssets
    ) {
        frame++
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        bgPaint.shader = LinearGradient(
            0f, 0f, 0f, h,
            Color.parseColor("#0A0E18"),
            Color.parseColor("#12182E"),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, bgPaint)
        bgPaint.shader = null

        val backSize = 48f * s
        backBtnRect.set(safeArea.left + 10f * s, safeArea.top + 10f * s, safeArea.left + 10f * s + backSize, safeArea.top + 10f * s + backSize)
        backCirclePaint.color = Color.parseColor("#1E2740")
        canvas.drawRoundRect(backBtnRect, 14f * s, 14f * s, backCirclePaint)
        backStrokePaint.color = Color.parseColor("#3D4F6F")
        backStrokePaint.strokeWidth = 2f * s
        canvas.drawRoundRect(backBtnRect, 14f * s, 14f * s, backStrokePaint)
        titlePaint.textSize = 34f * s
        titlePaint.color = Color.WHITE
        titlePaint.setShadowLayer(6f * s, 0f, 2f * s, Color.BLACK)
        canvas.drawText("←", backBtnRect.centerX(), backBtnRect.centerY() + 12f * s, titlePaint)
        titlePaint.clearShadowLayer()

        val titleY = safeArea.top + 72f * s
        titlePaint.textSize = 40f * s
        titlePaint.shader = LinearGradient(
            cx - 180f * s, titleY, cx + 180f * s, titleY,
            intArrayOf(Color.parseColor("#FFD54F"), Color.parseColor("#FFAB00"), Color.parseColor("#FFD54F")),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        titlePaint.setShadowLayer(8f * s, 0f, 2f * s, Color.parseColor("#80000000"))
        canvas.drawText("🎁  CHESTS", cx, titleY, titlePaint)
        titlePaint.clearShadowLayer()
        titlePaint.shader = null

        val filled = slots.count { it != null }
        headerSubPaint.textSize = 22f * s
        headerSubPaint.color = Color.parseColor("#B0BEC5")
        val streakLabel = if (streak == 1) "day" else "days"
        canvas.drawText("📦  $filled / ${ChestManager.MAX_SLOTS} slots     ·     🔥  $streak $streakLabel streak", cx, safeArea.top + 112f * s, headerSubPaint)

        val pad = 14f * s
        val colGap = 12f * s
        val rowGap = 12f * s
        val headerBottom = safeArea.top + 128f * s
        val mergeH = 52f * s
        val mergeBottomMargin = 14f * s
        val mergeTop = safeArea.bottom - mergeH - mergeBottomMargin
        val gridBottom = mergeTop - 10f * s
        val availW = safeArea.width() - pad * 2f
        val availVert = (gridBottom - headerBottom).coerceAtLeast(1f)
        val cellW = (availW - colGap) / 2f
        val maxRowH = ((availVert - rowGap) / 2f).coerceAtLeast(1f)
        val cellH = (cellW * 0.78f).coerceAtMost(maxRowH)
        val rowTotalH = cellH * 2f + rowGap
        val gridTop = headerBottom + ((availVert - rowTotalH) * 0.5f).coerceAtLeast(0f)
        val left0 = safeArea.left + pad
        val left1 = left0 + cellW + colGap

        for (i in 0 until ChestManager.MAX_SLOTS) {
            slotCardRects[i].setEmpty()
            slotOpenRects[i].setEmpty()
            slotSkipRects[i].setEmpty()
        }

        val col = intArrayOf(0, 1, 0, 1)
        val row = intArrayOf(0, 0, 1, 1)
        for (i in 0 until ChestManager.MAX_SLOTS) {
            val l = if (col[i] == 0) left0 else left1
            val t = gridTop + row[i] * (cellH + rowGap)
            tmpCard.set(l, t, l + cellW, t + cellH)
            slotCardRects[i].set(tmpCard)
            drawSlotCard(
                canvas, menuUi, s, i, tmpCard, slots[i], now, mergeMode, mergeSelectedIndex,
                openingSlotIndex, openingPhase, openingPhaseStartMs
            )
        }
        mergeBtnRect.set(safeArea.left + pad, mergeTop, safeArea.right - pad, mergeTop + mergeH)
        val mergePulse = (sin(now * 0.005).toFloat() * 0.5f + 0.5f)
        mergeGlowPaint.color = Color.argb((40 + mergePulse * 50).toInt(), 171, 71, 188)
        mergeGlowPaint.strokeWidth = (6f + mergePulse * 4f) * s
        tmpInner.set(
            mergeBtnRect.left - 4f * s, mergeBtnRect.top - 4f * s,
            mergeBtnRect.right + 4f * s, mergeBtnRect.bottom + 4f * s
        )
        canvas.drawRoundRect(tmpInner, 20f * s, 20f * s, mergeGlowPaint)

        mergeBtnFillPaint.shader = LinearGradient(
            mergeBtnRect.left, mergeBtnRect.top, mergeBtnRect.left, mergeBtnRect.bottom,
            if (mergeMode) Color.parseColor("#8E24AA") else Color.parseColor("#5E35B1"),
            if (mergeMode) Color.parseColor("#4A148C") else Color.parseColor("#311B92"),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(mergeBtnRect, 16f * s, 16f * s, mergeBtnFillPaint)
        mergeBtnFillPaint.shader = null

        mergeGlowPaint.color = Color.parseColor("#E1BEE7")
        mergeGlowPaint.strokeWidth = 2f * s
        canvas.drawRoundRect(mergeBtnRect, 16f * s, 16f * s, mergeGlowPaint)

        btnTextPaint.textSize = 24f * s
        btnTextPaint.color = Color.WHITE
        btnTextPaint.setShadowLayer(4f * s, 0f, 2f * s, Color.parseColor("#60000000"))
        val mergeLabel = if (mergeMode) "🔀  Tap two chests to merge" else "🔀  Merge Chests"
        canvas.drawText(mergeLabel, mergeBtnRect.centerX(), mergeBtnRect.centerY() + 9f * s, btnTextPaint)
        btnTextPaint.clearShadowLayer()
    }

    private fun drawSlotCard(
        canvas: Canvas,
        menuUi: MenuUiAssets,
        s: Float,
        index: Int,
        card: RectF,
        slot: ChestSlot?,
        now: Long,
        mergeMode: Boolean,
        mergeSelectedIndex: Int,
        openingSlotIndex: Int,
        openingPhase: ChestRevealPhase,
        openingPhaseStartMs: Long
    ) {
        val openingElapsed = if (openingSlotIndex == index && openingPhase == ChestRevealPhase.SPINNING) {
            (now - openingPhaseStartMs).coerceAtLeast(0L)
        } else {
            0L
        }
        val readyGlow = slot != null && slot.isReady(now)
        val pulse = when {
            openingElapsed > 0L -> 1f + sin(openingElapsed * 0.018).toFloat() * 0.08f
            readyGlow -> 1f + sin(now * 0.007 + index * 0.7f).toFloat() * 0.035f
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
            if (tier != ChestType.COMMON) {
                val gp = (sin(now * 0.006 + index).toFloat() * 0.5f + 0.5f)
                for (layer in 0..2) {
                    val expand = (8f + layer * 6f) * s * pulse
                    tmpInner.set(card.left - expand, card.top - expand, card.right + expand, card.bottom + expand)
                    val a = ((35 - layer * 10) * (0.5f + gp * 0.5f)).toInt().coerceIn(8, 45)
                    glowStrokePaint.color = Color.argb(a, glowRgb.first, glowRgb.second, glowRgb.third)
                    glowStrokePaint.strokeWidth = (4f - layer) * s
                    canvas.drawRoundRect(tmpInner, 20f * s, 20f * s, glowStrokePaint)
                }
            } else {
                val gp = (sin(now * 0.006 + index).toFloat() * 0.5f + 0.5f)
                tmpInner.set(card.left - 6f * s * pulse, card.top - 6f * s * pulse, card.right + 6f * s * pulse, card.bottom + 6f * s * pulse)
                glowStrokePaint.color = Color.argb((25 + gp * 30).toInt(), 158, 158, 158)
                glowStrokePaint.strokeWidth = 3f * s
                canvas.drawRoundRect(tmpInner, 20f * s, 20f * s, glowStrokePaint)
            }
        }

        val ch = card.height()
        val cw = card.width()
        val cornerR = 16f * s

        if (slot == null) {
            cardFillPaint.color = Color.parseColor("#151B2E")
            canvas.drawRoundRect(card, cornerR, cornerR, cardFillPaint)
            cardStrokePaint.color = Color.parseColor("#2A3448")
            cardStrokePaint.strokeWidth = 2f * s
            canvas.drawRoundRect(card, cornerR, cornerR, cardStrokePaint)

            val art = (cw - 12f * s).coerceAtMost(ch * 0.5f)
            val artTop = card.top + 5f * s
            drawChestBitmap(canvas, menuUi, ChestType.COMMON, card.centerX(), artTop + art / 2f, art, emptySlot = true, unlocking = false)

            statePaint.textSize = (13f * s).coerceIn(11f * s, 16f * s)
            statePaint.color = Color.parseColor("#90A4AE")
            canvas.drawText("EMPTY", card.centerX(), artTop + art + 14f * s, statePaint)
            statePaint.textSize = (11f * s).coerceIn(10f * s, 14f * s)
            canvas.drawText("Locked slot", card.centerX(), artTop + art + 30f * s, statePaint)
        } else {
            val tier = slot.type
            cardFillPaint.color = tierCardFill(tier, now)
            canvas.drawRoundRect(card, cornerR, cornerR, cardFillPaint)

            cardStrokePaint.color = tierStroke(tier, now)
            cardStrokePaint.strokeWidth = if (tier == ChestType.SUPER) {
                (2.5f + sin(now * 0.004).toFloat() * 0.8f) * s
            } else {
                2.5f * s
            }
            canvas.drawRoundRect(card, cornerR, cornerR, cardStrokePaint)

            val art = (cw - 10f * s).coerceAtMost(ch * 0.48f) * pulse
            val artTop = card.top + 4f * s
            drawChestBitmap(
                canvas, menuUi, tier, card.centerX(), artTop + art / 2f, art,
                emptySlot = false,
                unlocking = !slot.isReady(now)
            )

            val textY = artTop + art + 6f * s
            val badgePadH = 10f * s
            val badgePadV = 3f * s
            rarityPaint.textSize = (12f * s).coerceIn(10f * s, 15f * s)
            rarityPaint.color = Color.WHITE
            val label = tier.displayName.uppercase()
            val tw = rarityPaint.measureText(label)
            tmpInner.set(
                card.centerX() - tw / 2f - badgePadH,
                textY - 12f * s,
                card.centerX() + tw / 2f + badgePadH,
                textY + badgePadV
            )
            rarityBadgePaint.color = Color.argb(120, Color.red(tierAccent(tier)), Color.green(tierAccent(tier)), Color.blue(tierAccent(tier)))
            canvas.drawRoundRect(tmpInner, 8f * s, 8f * s, rarityBadgePaint)
            canvas.drawText(label, card.centerX(), textY, rarityPaint)

            val subY = textY + 18f * s
            if (slot.isReady(now)) {
                statePaint.textSize = (12f * s).coerceIn(10f * s, 14f * s)
                statePaint.color = Color.parseColor("#A5D6A7")
                canvas.drawText("● READY", card.centerX(), subY, statePaint)
            } else {
                timerPaint.textSize = (14f * s).coerceIn(12f * s, 18f * s)
                timerPaint.color = Color.parseColor("#FFD54F")
                canvas.drawText(formatRemaining(slot.remainingMs(now)), card.centerX(), subY, timerPaint)
            }

            val btnW = cw - 12f * s
            val btnH = (32f * s).coerceIn(28f * s, 38f * s)
            val btnLeft = card.left + 6f * s
            val btnTop = card.bottom - btnH - 5f * s

            if (slot.isReady(now)) {
                slotOpenRects[index].set(btnLeft, btnTop, btnLeft + btnW, btnTop + btnH)
                openBtnPaint.shader = LinearGradient(
                    btnLeft, btnTop, btnLeft, btnTop + btnH,
                    Color.parseColor("#66BB6A"),
                    Color.parseColor("#2E7D32"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(slotOpenRects[index], 10f * s, 10f * s, openBtnPaint)
                openBtnPaint.shader = null
                cardStrokePaint.color = Color.parseColor("#A5D6A7")
                cardStrokePaint.strokeWidth = 1.5f * s
                canvas.drawRoundRect(slotOpenRects[index], 10f * s, 10f * s, cardStrokePaint)
                btnTextPaint.textSize = 17f * s
                btnTextPaint.color = Color.WHITE
                canvas.drawText("OPEN", slotOpenRects[index].centerX(), btnTop + btnH * 0.7f, btnTextPaint)
            } else {
                slotSkipRects[index].set(btnLeft, btnTop, btnLeft + btnW, btnTop + btnH)
                skipBtnPaint.shader = LinearGradient(
                    btnLeft, btnTop, btnLeft, btnTop + btnH,
                    Color.parseColor("#FF8F00"),
                    Color.parseColor("#E65100"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(slotSkipRects[index], 10f * s, 10f * s, skipBtnPaint)
                skipBtnPaint.shader = null
                btnTextPaint.textSize = 13f * s
                canvas.drawText("📺 SKIP", slotSkipRects[index].centerX(), btnTop + btnH * 0.65f, btnTextPaint)
            }
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

    private fun drawChestBitmap(
        canvas: Canvas,
        menuUi: MenuUiAssets,
        type: ChestType,
        cx: Float,
        cy: Float,
        side: Float,
        emptySlot: Boolean,
        unlocking: Boolean
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
        canvas.drawBitmap(menuUi.chest(type), null, tmpBitmapDst, bitmapPaint)
        bitmapPaint.colorFilter = null
        bitmapPaint.alpha = 255
    }

    private fun tierCardFill(t: ChestType, now: Long): Int = when (t) {
        ChestType.COMMON -> Color.parseColor("#1E2838")
        ChestType.RARE -> Color.parseColor("#0D2840")
        ChestType.EPIC -> Color.parseColor("#1A0D28")
        ChestType.SUPER -> {
            val p = (sin(now * 0.003) * 25).toInt()
            Color.argb(255, (40 + p).coerceIn(30, 70), 28, 18)
        }
    }

    private fun tierStroke(t: ChestType, now: Long): Int = when (t) {
        ChestType.COMMON -> Color.parseColor("#546E7A")
        ChestType.RARE -> Color.parseColor("#42A5F5")
        ChestType.EPIC -> Color.parseColor("#E040FB")
        ChestType.SUPER -> {
            val g = (sin(now * 0.005) * 40 + 215).toInt().coerceIn(180, 255)
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
        val s = sec % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
        else String.format("%d:%02d", m, s)
    }
}
