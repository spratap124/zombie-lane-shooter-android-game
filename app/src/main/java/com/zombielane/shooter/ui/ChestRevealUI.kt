package com.zombielane.shooter.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.zombielane.shooter.data.ChestOpenResult
import com.zombielane.shooter.data.ChestRevealPhase
import com.zombielane.shooter.data.LuckyBonusKind
import kotlin.math.sin

/**
 * Full-screen chest opening animation + double-reward prompt. Paints are reused (no per-frame alloc).
 */
class ChestRevealUI {

    private val dimPaint = Paint().apply {
        color = Color.parseColor("#EE000000")
        style = Paint.Style.FILL
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val luckyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(6f, 2f, 2f, Color.parseColor("#FFFF00"))
    }
    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    var claimRect = RectF()
    var doubleAdRect = RectF()

    private val previewLabels = arrayOf("Coins", "Weapon", "Shield", "Rapid", "Bonus")

    fun draw(
        canvas: Canvas,
        safeArea: RectF,
        phase: ChestRevealPhase,
        phaseStartMs: Long,
        now: Long,
        result: ChestOpenResult?
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        canvas.drawRect(0f, 0f, w, h, dimPaint)

        if (result == null) return

        val elapsed = now - phaseStartMs
        titlePaint.textSize = 40f * s
        subPaint.textSize = 26f * s
        luckyPaint.textSize = 44f * s

        when (phase) {
            ChestRevealPhase.SPINNING -> {
                val pulse = (sin(now * 0.008) * 0.5 + 0.5).toFloat()
                titlePaint.color = Color.argb(255, 200, (180 + 75 * pulse).toInt(), 80)
                canvas.drawText("OPENING…", cx, safeArea.top + 120f * s, titlePaint)
                val idx = ((elapsed / 140L) % previewLabels.size).toInt()
                subPaint.color = Color.parseColor("#ECEFF1")
                subPaint.textSize = 36f * s
                canvas.drawText(previewLabels[idx], cx, h * 0.45f, subPaint)
                subPaint.textSize = 26f * s
                canvas.drawText("???", cx, h * 0.55f, subPaint)
            }
            ChestRevealPhase.REVEAL -> {
                titlePaint.color = Color.WHITE
                canvas.drawText("YOU GOT", cx, safeArea.top + 100f * s, titlePaint)
                subPaint.color = Color.parseColor("#A5D6A7")
                subPaint.textSize = 28f * s
                val lines = result.rewards.describe().split(" · ")
                var y = h * 0.42f
                for (line in lines) {
                    canvas.drawText(line, cx, y, subPaint)
                    y += 36f * s
                }
            }
            ChestRevealPhase.LUCKY -> {
                luckyPaint.alpha = 255
                canvas.drawText("LUCKY BONUS!", cx, safeArea.top + 110f * s, luckyPaint)
                val kind = when (result.luckyKind) {
                    LuckyBonusKind.DOUBLE_VALUES -> "DOUBLE REWARDS!"
                    LuckyBonusKind.RARITY_UP -> "RARITY UP!"
                    null -> "BONUS!"
                }
                subPaint.color = Color.parseColor("#FFF59D")
                subPaint.textSize = 30f * s
                canvas.drawText(kind, cx, safeArea.top + 170f * s, subPaint)
                subPaint.textSize = 24f * s
                canvas.drawText(result.rewards.describe(), cx, h * 0.5f, subPaint)
            }
            ChestRevealPhase.DOUBLE_OFFER -> {
                titlePaint.color = Color.WHITE
                canvas.drawText("DOUBLE REWARDS?", cx, safeArea.top + 90f * s, titlePaint)
                subPaint.color = Color.parseColor("#90A4AE")
                subPaint.textSize = 24f * s
                canvas.drawText("Watch a short video to multiply this drop ×2", cx, safeArea.top + 140f * s, subPaint)
                subPaint.textSize = 22f * s
                canvas.drawText(result.rewards.describe(), cx, h * 0.38f, subPaint)

                val bw = safeArea.width() * 0.42f
                val bh = 56f * s
                val gap = 12f * s
                doubleAdRect = RectF(cx - bw - gap / 2f, h * 0.58f, cx - gap / 2f, h * 0.58f + bh)
                claimRect = RectF(cx + gap / 2f, h * 0.58f, cx + bw + gap / 2f, h * 0.58f + bh)

                btnPaint.color = Color.parseColor("#FF6F00")
                canvas.drawRoundRect(doubleAdRect, 12f * s, 12f * s, btnPaint)
                btnTextPaint.textSize = 22f * s
                canvas.drawText("×2 (AD)", doubleAdRect.centerX(), doubleAdRect.centerY() + 10f * s, btnTextPaint)

                btnPaint.color = Color.parseColor("#37474F")
                canvas.drawRoundRect(claimRect, 12f * s, 12f * s, btnPaint)
                canvas.drawText("CLAIM", claimRect.centerX(), claimRect.centerY() + 10f * s, btnTextPaint)
            }
            else -> {}
        }
    }
}
