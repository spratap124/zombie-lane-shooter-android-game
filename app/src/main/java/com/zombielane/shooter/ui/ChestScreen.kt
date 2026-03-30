package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.zombielane.shooter.data.ChestManager
import com.zombielane.shooter.data.ChestSlot
import com.zombielane.shooter.data.ChestType
import kotlin.math.sin

class ChestScreen {

    var backBtnRect = RectF()
    var mergeBtnRect = RectF()
    val slotCardRects = Array(ChestManager.MAX_SLOTS) { RectF() }
    val slotOpenRects = Array(ChestManager.MAX_SLOTS) { RectF() }
    val slotSkipRects = Array(ChestManager.MAX_SLOTS) { RectF() }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90A4AE")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private var frame = 0L

    fun draw(
        canvas: Canvas,
        safeArea: RectF,
        slots: List<ChestSlot?>,
        now: Long,
        mergeMode: Boolean,
        mergeSelectedIndex: Int,
        streak: Int
    ) {
        frame++
        val w = canvas.width.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        canvas.drawColor(Color.parseColor("#0D0D18"))

        titlePaint.textSize = 44f * s
        subPaint.textSize = 24f * s
        canvas.drawText("CHESTS", cx, safeArea.top + 48f * s, titlePaint)
        canvas.drawText("Slots ${slots.count { it != null }}/${ChestManager.MAX_SLOTS}  ·  Streak $streak days", cx, safeArea.top + 86f * s, subPaint)

        var y = safeArea.top + 110f * s
        val cardH = 108f * s
        val gap = 12f * s
        val left = safeArea.left + 8f * s
        val cardW = safeArea.width() - 16f * s

        for (i in 0 until ChestManager.MAX_SLOTS) {
            slotCardRects[i].setEmpty()
            slotOpenRects[i].setEmpty()
            slotSkipRects[i].setEmpty()

            val slot = slots[i]
            val top = y
            val bottom = y + cardH
            slotCardRects[i].set(left, top, left + cardW, bottom)

            if (slot == null) {
                cardPaint.color = Color.parseColor("#1A1A2E")
                canvas.drawRoundRect(slotCardRects[i], 14f * s, 14f * s, cardPaint)
                strokePaint.color = Color.parseColor("#333355")
                canvas.drawRoundRect(slotCardRects[i], 14f * s, 14f * s, strokePaint)
                subPaint.textSize = 22f * s
                canvas.drawText("Empty slot ${i + 1}", cx, top + cardH * 0.55f, subPaint)
            } else {
                val tier = slot.type
                cardPaint.color = tierColor(tier, now)
                canvas.drawRoundRect(slotCardRects[i], 14f * s, 14f * s, cardPaint)
                if (tier == ChestType.SUPER) {
                    val glow = (sin(frame * 0.12) * 40 + 80).toInt().coerceIn(60, 120)
                    strokePaint.color = Color.argb(glow, 255, 215, 0)
                    strokePaint.strokeWidth = 4f
                    canvas.drawRoundRect(slotCardRects[i], 14f * s, 14f * s, strokePaint)
                    strokePaint.strokeWidth = 3f
                } else {
                    strokePaint.color = Color.parseColor("#444466")
                    canvas.drawRoundRect(slotCardRects[i], 14f * s, 14f * s, strokePaint)
                }

                titlePaint.textSize = 26f * s
                titlePaint.color = Color.WHITE
                val label = if (tier == ChestType.SUPER) "SUPER CHEST!" else "${tier.displayName.uppercase()} CHEST"
                canvas.drawText(label, left + cardW * 0.35f, top + 32f * s, titlePaint)

                subPaint.textSize = 22f * s
                val status = if (slot.isReady(now)) {
                    "READY TO OPEN"
                } else {
                    formatRemaining(slot.remainingMs(now))
                }
                subPaint.color = if (slot.isReady(now)) Color.parseColor("#A5D6A7") else Color.parseColor("#FFCC80")
                canvas.drawText(status, left + cardW * 0.35f, top + 62f * s, subPaint)

                val btnW = cardW * 0.28f
                val btnH = 44f * s
                val btnLeft = left + cardW - btnW - 14f * s
                val btnTop = top + cardH * 0.5f - btnH / 2f

                if (slot.isReady(now)) {
                    slotOpenRects[i].set(btnLeft, btnTop, btnLeft + btnW, btnTop + btnH)
                    btnPaint.color = Color.parseColor("#4CAF50")
                    canvas.drawRoundRect(slotOpenRects[i], 10f * s, 10f * s, btnPaint)
                    btnTextPaint.textSize = 22f * s
                    canvas.drawText("OPEN", slotOpenRects[i].centerX(), btnTop + btnH * 0.65f, btnTextPaint)
                } else {
                    slotSkipRects[i].set(btnLeft, btnTop, btnLeft + btnW, btnTop + btnH)
                    btnPaint.color = Color.parseColor("#FF6F00")
                    canvas.drawRoundRect(slotSkipRects[i], 10f * s, 10f * s, btnPaint)
                    btnTextPaint.textSize = 18f * s
                    canvas.drawText("SKIP (AD)", slotSkipRects[i].centerX(), btnTop + btnH * 0.4f, btnTextPaint)
                    btnTextPaint.textSize = 16f * s
                    canvas.drawText("📺", slotSkipRects[i].centerX(), btnTop + btnH * 0.72f, btnTextPaint)
                }

                if (mergeMode && mergeSelectedIndex == i) {
                    strokePaint.color = Color.parseColor("#00E676")
                    strokePaint.strokeWidth = 5f
                    canvas.drawRoundRect(RectF(left - 4f, top - 4f, left + cardW + 4f, bottom + 4f), 16f * s, 16f * s, strokePaint)
                    strokePaint.strokeWidth = 3f
                }
            }
            y = bottom + gap
        }

        y += 8f * s
        val rowW = safeArea.width() - 20f * s
        val half = (rowW - 10f * s) / 2f
        mergeBtnRect.set(left, y, left + half, y + 52f * s)
        backBtnRect.set(left + half + 10f * s, y, left + rowW, y + 52f * s)

        btnPaint.color = if (mergeMode) Color.parseColor("#6A1B9A") else Color.parseColor("#37474F")
        canvas.drawRoundRect(mergeBtnRect, 12f * s, 12f * s, btnPaint)
        btnTextPaint.textSize = 22f * s
        canvas.drawText(if (mergeMode) "MERGE: pick 2" else "MERGE", mergeBtnRect.centerX(), mergeBtnRect.centerY() + 8f * s, btnTextPaint)

        btnPaint.color = Color.parseColor("#455A64")
        canvas.drawRoundRect(backBtnRect, 12f * s, 12f * s, btnPaint)
        canvas.drawText("BACK", backBtnRect.centerX(), backBtnRect.centerY() + 8f * s, btnTextPaint)
    }

    private fun tierColor(t: ChestType, now: Long): Int = when (t) {
        ChestType.COMMON -> Color.parseColor("#37474F")
        ChestType.RARE -> Color.parseColor("#1565C0")
        ChestType.EPIC -> Color.parseColor("#6A1B9A")
        ChestType.SUPER -> {
            val pulse = (sin(now * 0.004) * 30).toInt()
            Color.argb(255, (100 + pulse).coerceIn(80, 140), 40, 120 + pulse)
        }
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
