package com.zombielane.shooter.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.zombielane.shooter.data.BossCodexUnlockRules
import com.zombielane.shooter.data.BossUnlockManager
import com.zombielane.shooter.data.BossUnlockState
import com.zombielane.shooter.data.UpgradeManager
import com.zombielane.shooter.objects.EnemyAssets
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Full-screen boss gallery: clear portraits, rewarded-ad temp access, permanent coin unlock.
 */
class BossCodexScreen {

    var backBtnRect = RectF()
    var watchAdBtnRect = RectF()
    var permUnlockBtnRect = RectF()
    val cellRects: Array<RectF> = Array(EnemyAssets.BOSS_SKIN_COUNT) { RectF() }

    var gridRect = RectF()
    var lastMaxScrollY = 0f

    private val bgPaint = Paint().apply { color = Color.parseColor("#FF0A0612"); style = Paint.Style.FILL }
    private val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90A4AE")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90A4AE")
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val bestStageDebugPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val lockEmojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.CENTER
    }
    private val cellStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }
    /** Full-cell dim when boss is locked in the codex. */
    private val lockedOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(175, 8, 10, 14)
    }
    private val unlockHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
    /** Locked cells under gating: muted colors (blur omitted for broad API compatibility). */
    private val desatBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0.2f) })
    }
    private val btnFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val toastPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A5D6A7")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val ctaFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val ctaGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val ctaRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val ctaShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val ctaHiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val ctaShimmerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val ctaSparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val ctaLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    /** Cached vignette; rebuilt only when canvas / safe-area geometry changes (not every frame). */
    private var vignetteGradient: Shader? = null
    private var vignetteCacheW = 0
    private var vignetteCacheH = 0
    private var vignetteCacheCx = 0f
    private var vignetteCacheCy = 0f
    private var vignetteCacheR = 0f

    private val tmpCard = RectF()
    private val tmpInner = RectF()
    private val tmpBitmapDst = RectF()
    private val tmpBtnDraw = RectF()
    private val tmpShimmer = RectF()

    fun maxScrollY(gridH: Float, contentH: Float): Float =
        max(0f, contentH - gridH)

    fun draw(
        canvas: Canvas,
        safeArea: RectF,
        enemyAssets: EnemyAssets,
        bossUnlock: BossUnlockManager,
        upgradeManager: UpgradeManager,
        selectedIndex: Int,
        scrollY: Float,
        nowMs: Long,
        menuUi: MenuUiAssets,
        briefToast: String?,
        rewardedAdReady: Boolean,
        watchAdInFlight: Boolean,
        watchAdPressed: Boolean = false,
        permUnlockPressed: Boolean = false
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val s = w / 1080f
        val ts = max(s, min(w, safeArea.height()) / 720f)

        canvas.drawRect(0f, 0f, w, h, bgPaint)
        val vignetteCy = safeArea.top + safeArea.height() * 0.3f
        val vignetteR = w * 0.9f
        if (vignetteGradient == null ||
            vignetteCacheW != canvas.width ||
            vignetteCacheH != canvas.height ||
            vignetteCacheCx != cx ||
            vignetteCacheCy != vignetteCy ||
            vignetteCacheR != vignetteR
        ) {
            vignetteCacheW = canvas.width
            vignetteCacheH = canvas.height
            vignetteCacheCx = cx
            vignetteCacheCy = vignetteCy
            vignetteCacheR = vignetteR
            vignetteGradient = RadialGradient(
                cx, vignetteCy, vignetteR,
                intArrayOf(Color.TRANSPARENT, Color.argb(200, 0, 0, 0)),
                floatArrayOf(0.2f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        vignettePaint.shader = vignetteGradient
        canvas.drawRect(0f, 0f, w, h, vignettePaint)

        val bannerReserve = (120f * s).coerceIn(100f, 168f)
        val contentBottom = safeArea.bottom - bannerReserve

        val backSize = (72f * ts).coerceIn(56f, 96f)
        backBtnRect.set(safeArea.left + 8f * ts, safeArea.top + 8f * ts, safeArea.left + 8f * ts + backSize, safeArea.top + 8f * ts + backSize)
        MenuUiAssets.drawBackButton(canvas, backBtnRect, menuUi.backButton)

        titlePaint.textSize = 44f * ts
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.setShadowLayer(8f * ts, 0f, 2f * ts, Color.parseColor("#60000000"))
        canvas.drawText("BOSS CODEX", cx, safeArea.top + 52f * ts, titlePaint)
        titlePaint.clearShadowLayer()

        val life = upgradeManager.lifetimeMaxStage
        val coins = upgradeManager.totalCoins
        val gatingOn = BossCodexUnlockRules.CODEX_MONETIZATION_ALWAYS_ACTIVE

        subPaint.textSize = 22f * ts
        val fmSub = subPaint.fontMetrics
        val baselineStats = safeArea.top + 86f * ts
        val coinStr = coins.toString()
        val coinBmp = menuUi.coin
        val coinSize = ((-fmSub.ascent + fmSub.descent) * 1.15f).coerceIn(20f * ts, 30f * ts)
        val gapAfterCoin = 6f * ts
        val wNum = subPaint.measureText(coinStr)
        val blockW = coinSize + gapAfterCoin + wNum
        var xBlock = cx - blockW / 2f
        val iconTop = baselineStats + fmSub.ascent + ((-fmSub.ascent + fmSub.descent) - coinSize) / 2f
        tmpBitmapDst.set(xBlock, iconTop, xBlock + coinSize, iconTop + coinSize)
        drawBitmapFit(canvas, coinBmp, tmpBitmapDst)
        xBlock += coinSize + gapAfterCoin
        canvas.drawText(coinStr, xBlock, baselineStats, subPaint)

        bestStageDebugPaint.textSize = 18f * ts
        val fmBest = bestStageDebugPaint.fontMetrics
        val bestStageBaseline = baselineStats + (-fmSub.ascent + fmSub.descent) + 12f * ts
        canvas.drawText("Best Stage: $life", cx, bestStageBaseline, bestStageDebugPaint)

        hintPaint.textSize = 15f * ts
        val fmHint = hintPaint.fontMetrics
        val hintGap = 8f * ts
        var hintBaseline = bestStageBaseline + (-fmBest.ascent + fmBest.descent) + hintGap
        canvas.drawText("Boss #1 to #14 are always available.", cx, hintBaseline, hintPaint)
        hintBaseline += (-fmHint.ascent + fmHint.descent) + 4f * ts
        canvas.drawText("Bosses #15+ unlock with rewarded ads (30 min) or coins.", cx, hintBaseline, hintPaint)

        val headerBottom = hintBaseline + (-fmHint.ascent + fmHint.descent) + 10f * ts
        val detailH = (200f * ts).coerceIn(168f, 260f)
        val gridTop = headerBottom + 8f * ts
        val gridBottom = contentBottom - detailH - 14f * ts
        val gridH = (gridBottom - gridTop).coerceAtLeast(120f * ts)

        val cols = 3
        val gap = 10f * ts
        val innerW = safeArea.width() - 24f * ts
        val cellW = (innerW - (cols - 1) * gap) / cols
        val cellH = cellW
        val rows = (EnemyAssets.BOSS_SKIN_COUNT + cols - 1) / cols
        val contentH = rows * cellH + (rows - 1).coerceAtLeast(0) * gap

        val gridLeft = safeArea.left + 12f * ts
        gridRect.set(gridLeft, gridTop, gridLeft + innerW, gridTop + gridH)

        val maxScroll = maxScrollY(gridH, contentH)
        lastMaxScrollY = maxScroll
        val scroll = scrollY.coerceIn(0f, maxScroll)

        canvas.save()
        canvas.clipRect(gridRect)

        for (i in 0 until EnemyAssets.BOSS_SKIN_COUNT) {
            val row = i / cols
            val col = i % cols
            val left = gridLeft + col * (cellW + gap)
            val top = gridTop + row * (cellH + gap) - scroll
            tmpCard.set(left, top, left + cellW, top + cellH)
            cellRects[i].set(tmpCard)

            val avail = BossCodexUnlockRules.isBossUnlockedInCodex(i, life, bossUnlock, nowMs)
            val bmp = enemyAssets.bossBitmap(i)
            val rCell = 8f * ts
            if (gatingOn && !avail) {
                canvas.drawBitmap(bmp, null, tmpCard, desatBitmapPaint)
                canvas.drawRoundRect(tmpCard, rCell, rCell, lockedOverlayPaint)
                lockEmojiPaint.textSize = min(cellW, cellH) * 0.28f
                val fmE = lockEmojiPaint.fontMetrics
                val ey = tmpCard.centerY() - (fmE.ascent + fmE.descent) / 2f
                canvas.drawText("🔒", tmpCard.centerX(), ey, lockEmojiPaint)
            } else {
                canvas.drawBitmap(bmp, null, tmpCard, bitmapPaint)
                val monetizedCell = i >= BossUnlockManager.FIRST_MONETIZED_BOSS_SKIN_INDEX
                if (gatingOn && avail && monetizedCell) {
                    unlockHaloPaint.color = Color.argb(130, 0, 229, 255)
                    unlockHaloPaint.strokeWidth = 5f * ts
                    canvas.drawRoundRect(tmpCard, rCell, rCell, unlockHaloPaint)
                }
            }

            val sel = i == selectedIndex
            cellStrokePaint.color = if (sel) Color.parseColor("#00E5FF") else Color.parseColor("#455A64")
            cellStrokePaint.strokeWidth = if (sel) 4f else 2f
            canvas.drawRoundRect(tmpCard, 8f * ts, 8f * ts, cellStrokePaint)

            labelPaint.textSize = 15f * ts
            labelPaint.color = if (avail) Color.parseColor("#ECEFF1") else Color.parseColor("#B0BEC5")
            canvas.drawText("#${i + 1}", tmpCard.centerX(), tmpCard.bottom - 8f * ts, labelPaint)
        }
        canvas.restore()

        val detailTop = gridBottom + 10f * ts
        tmpCard.set(safeArea.left + 12f * ts, detailTop, safeArea.right - 12f * ts, detailTop + detailH)
        btnFillPaint.shader = LinearGradient(
            tmpCard.left, tmpCard.top, tmpCard.left, tmpCard.bottom,
            Color.parseColor("#263238"),
            Color.parseColor("#102027"),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(tmpCard, 16f * ts, 16f * ts, btnFillPaint)
        btnFillPaint.shader = null
        cellStrokePaint.color = Color.parseColor("#37474F")
        cellStrokePaint.strokeWidth = 2f
        canvas.drawRoundRect(tmpCard, 16f * ts, 16f * ts, cellStrokePaint)

        val sel = selectedIndex.coerceIn(0, EnemyAssets.BOSS_SKIN_COUNT - 1)
        val portraitSize = (detailH - 28f * ts).coerceIn(96f, detailH - 20f * ts)
        tmpInner.set(
            tmpCard.left + 14f * ts,
            tmpCard.top + 14f * ts,
            tmpCard.left + 14f * ts + portraitSize,
            tmpCard.top + 14f * ts + portraitSize
        )
        val selBmp = enemyAssets.bossBitmap(sel)
        val previewState = bossUnlock.getBossState(sel, life, nowMs)
        canvas.drawBitmap(selBmp, null, tmpInner, bitmapPaint)

        val textLeft = tmpInner.right + 14f * ts
        var ty = tmpCard.top + 32f * ts
        titlePaint.textSize = 26f * ts
        titlePaint.textAlign = Paint.Align.LEFT
        titlePaint.color = Color.WHITE
        canvas.drawText("Boss #${sel + 1}", textLeft, ty, titlePaint)
        ty += 36f * ts
        subPaint.textSize = 20f * ts
        subPaint.textAlign = Paint.Align.LEFT

        val permSel = bossUnlock.isPermanentUnlocked(sel)
        val monetized = sel >= BossUnlockManager.FIRST_MONETIZED_BOSS_SKIN_INDEX

        subPaint.color = Color.parseColor("#CFD8DC")
        val statusPrimary = previewStatusLine(previewState, sel, bossUnlock, nowMs)
        canvas.drawText(statusPrimary, textLeft, ty, subPaint)
        subPaint.color = Color.parseColor("#90A4AE")
        ty += 28f * ts

        watchAdBtnRect.setEmpty()
        permUnlockBtnRect.setEmpty()

        val showWatchAd = monetized && !permSel && !previewState.isUnlocked
        val showPermCoins = monetized && !permSel

        if (showWatchAd || showPermCoins) {
            val pairTotal = (tmpCard.right - textLeft - 22f * ts).coerceAtLeast(40f * ts)
            val gapBtn = 12f * ts
            val bottom = tmpCard.bottom - 16f * ts
            val hWatch = 50f * ts
            val hCoin = 43f * ts
            val wWatch = when {
                showWatchAd && showPermCoins -> ((pairTotal - gapBtn) * 0.58f).coerceAtLeast(88f * ts)
                showWatchAd -> pairTotal
                else -> pairTotal
            }
            val wCoin = when {
                showWatchAd && showPermCoins -> (pairTotal - gapBtn - wWatch).coerceAtLeast(72f * ts)
                showPermCoins -> pairTotal
                else -> 0f
            }
            val xWatch = textLeft
            val xCoin = xWatch + if (showWatchAd) wWatch + gapBtn else 0f

            if (showWatchAd) {
                watchAdBtnRect.set(xWatch, bottom - hWatch, xWatch + wWatch, bottom)
                val adReady = rewardedAdReady && !watchAdInFlight
                val adLabel = when {
                    watchAdInFlight -> "Loading ad…"
                    adReady -> "▶  Watch Ad - 30m Unlock"
                    else -> "Ad loading…"
                }
                drawPremiumWatchAdButton(
                    canvas, watchAdBtnRect, ts, nowMs,
                    pressed = watchAdPressed,
                    enabled = adReady,
                    inFlight = watchAdInFlight,
                    label = adLabel
                )
            }
            if (showPermCoins) {
                val pc = BossUnlockManager.permanentUnlockCost(sel)
                permUnlockBtnRect.set(xCoin, bottom - hCoin, xCoin + wCoin, bottom)
                val coinLabel = "🪙  Unlock Forever - $pc"
                drawPremiumCoinUnlockButton(
                    canvas, permUnlockBtnRect, ts, nowMs,
                    pressed = permUnlockPressed,
                    label = coinLabel
                )
            }
        }

        titlePaint.textAlign = Paint.Align.CENTER

        if (briefToast != null) {
            toastPaint.textSize = 20f * ts
            canvas.drawText(briefToast, cx, contentBottom - 8f * ts, toastPaint)
        }
    }

    private fun pillRxRy(h: Float) = h / 2f

    private fun applyPressScale(rect: RectF, pressed: Boolean, out: RectF) {
        val sc = if (pressed) 0.95f else 1f
        val iw = rect.width() * (1f - sc) * 0.5f
        val ih = rect.height() * (1f - sc) * 0.5f
        out.set(rect.left + iw, rect.top + ih, rect.right - iw, rect.bottom - ih)
    }

    /** Primary CTA: cyan pill, pulse + shimmer (Daily Missions CLAIM / menu play glow style). */
    private fun drawPremiumWatchAdButton(
        canvas: Canvas,
        rect: RectF,
        ts: Float,
        nowMs: Long,
        pressed: Boolean,
        enabled: Boolean,
        inFlight: Boolean,
        label: String
    ) {
        applyPressScale(rect, pressed, tmpBtnDraw)
        val r = pillRxRy(tmpBtnDraw.height())

        tmpShimmer.set(tmpBtnDraw)
        tmpShimmer.offset(0f, 4f * ts)
        ctaShadowPaint.color = Color.argb(120, 0, 0, 0)
        canvas.drawRoundRect(tmpShimmer, r, r, ctaShadowPaint)

        val pulse = if (enabled && !inFlight) 1f + 0.055f * sin(nowMs * 0.006).toFloat() else 1f
        val pressGlow = if (pressed) 22 else 0
        val layers = if (enabled && !inFlight) 5 else 2
        for (g in 0 until layers) {
            val ex = (2.5f + g * 3.8f) * ts * pulse
            val alpha = ((50 - g * 8 + pressGlow) * (if (enabled) 1f else 0.35f)).toInt().coerceIn(8, 130)
            ctaGlowPaint.color = Color.argb(alpha, 0, 229, 255)
            ctaGlowPaint.strokeWidth = (4f - g * 0.55f) * ts
            tmpShimmer.set(
                tmpBtnDraw.left - ex, tmpBtnDraw.top - ex,
                tmpBtnDraw.right + ex, tmpBtnDraw.bottom + ex
            )
            val rr = pillRxRy(tmpShimmer.height())
            canvas.drawRoundRect(tmpShimmer, rr, rr, ctaGlowPaint)
        }

        if (enabled && !inFlight) {
            ctaFillPaint.shader = LinearGradient(
                tmpBtnDraw.left, tmpBtnDraw.top, tmpBtnDraw.left, tmpBtnDraw.bottom,
                Color.parseColor("#26FFFF"),
                Color.parseColor("#004D5C"),
                Shader.TileMode.CLAMP
            )
        } else {
            ctaFillPaint.shader = LinearGradient(
                tmpBtnDraw.left, tmpBtnDraw.top, tmpBtnDraw.left, tmpBtnDraw.bottom,
                Color.parseColor("#546E7A"),
                Color.parseColor("#263238"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(tmpBtnDraw, r, r, ctaFillPaint)
        ctaFillPaint.shader = null

        tmpShimmer.set(
            tmpBtnDraw.left + r * 0.12f,
            tmpBtnDraw.top + r * 0.1f,
            tmpBtnDraw.right - r * 0.12f,
            tmpBtnDraw.top + tmpBtnDraw.height() * 0.42f
        )
        val hr = pillRxRy(tmpShimmer.height())
        ctaHiPaint.shader = LinearGradient(
            tmpShimmer.left, tmpShimmer.top, tmpShimmer.left, tmpShimmer.bottom,
            Color.argb(55, 255, 255, 255),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(tmpShimmer, hr, hr, ctaHiPaint)
        ctaHiPaint.shader = null

        if (enabled && !inFlight) {
            canvas.save()
            // Rect clip avoids per-frame clipPath (lighter on HWUI / display lists than round clipPath).
            val inset = (r * 0.32f).coerceAtLeast(2.5f * ts)
            canvas.clipRect(
                tmpBtnDraw.left + inset,
                tmpBtnDraw.top + inset,
                tmpBtnDraw.right - inset,
                tmpBtnDraw.bottom - inset
            )
            val sweep = (nowMs % 2200L) / 2200f
            val sx = tmpBtnDraw.left + tmpBtnDraw.width() * sweep
            ctaShimmerPaint.shader = LinearGradient(
                sx - 44f * ts, tmpBtnDraw.top,
                sx + 44f * ts, tmpBtnDraw.bottom,
                Color.TRANSPARENT,
                Color.argb(100, 255, 255, 255),
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(tmpBtnDraw.left, tmpBtnDraw.top, tmpBtnDraw.right, tmpBtnDraw.bottom, ctaShimmerPaint)
            ctaShimmerPaint.shader = null
            canvas.restore()
        }

        for (i in 0..2) {
            val px = tmpBtnDraw.left + tmpBtnDraw.width() * (0.18f + i * 0.32f)
            val py = tmpBtnDraw.top + 6f * ts
            val a = (28 + 32 * sin(nowMs * 0.009 + i * 2.1f)).toInt().coerceIn(12, 85)
            ctaSparkPaint.color = Color.argb(a, 200, 255, 255)
            canvas.drawCircle(px, py, 2.2f * ts, ctaSparkPaint)
        }

        ctaRimPaint.style = Paint.Style.STROKE
        ctaRimPaint.color = Color.argb(if (enabled) 230 else 140, 120, 255, 255)
        ctaRimPaint.strokeWidth = 2f * ts
        canvas.drawRoundRect(tmpBtnDraw, r, r, ctaRimPaint)

        ctaLabelPaint.textSize = 12.5f * ts
        ctaLabelPaint.color = Color.WHITE
        ctaLabelPaint.setShadowLayer(3.5f * ts, 0f, 1.8f * ts, Color.argb(200, 0, 0, 0))
        val fm = ctaLabelPaint.fontMetrics
        canvas.drawText(
            label,
            tmpBtnDraw.centerX(),
            tmpBtnDraw.centerY() - (fm.ascent + fm.descent) / 2f,
            ctaLabelPaint
        )
        ctaLabelPaint.clearShadowLayer()
    }

    /** Secondary CTA: purple / gold gradient, softer glow (Chest / arcade chip style). */
    private fun drawPremiumCoinUnlockButton(
        canvas: Canvas,
        rect: RectF,
        ts: Float,
        nowMs: Long,
        pressed: Boolean,
        label: String
    ) {
        applyPressScale(rect, pressed, tmpBtnDraw)
        val r = pillRxRy(tmpBtnDraw.height())

        tmpShimmer.set(tmpBtnDraw)
        tmpShimmer.offset(0f, 3f * ts)
        ctaShadowPaint.color = Color.argb(95, 0, 0, 0)
        canvas.drawRoundRect(tmpShimmer, r, r, ctaShadowPaint)

        val pressGlow = if (pressed) 18 else 0
        for (g in 0..2) {
            val ex = (2f + g * 3f) * ts
            val alpha = (38 - g * 10 + pressGlow).coerceIn(10, 90)
            ctaGlowPaint.color = Color.argb(alpha, 186, 104, 200)
            ctaGlowPaint.strokeWidth = (3f - g * 0.5f) * ts
            tmpShimmer.set(
                tmpBtnDraw.left - ex, tmpBtnDraw.top - ex,
                tmpBtnDraw.right + ex, tmpBtnDraw.bottom + ex
            )
            val rr = pillRxRy(tmpShimmer.height())
            canvas.drawRoundRect(tmpShimmer, rr, rr, ctaGlowPaint)
        }

        ctaFillPaint.shader = LinearGradient(
            tmpBtnDraw.left, tmpBtnDraw.top, tmpBtnDraw.right, tmpBtnDraw.bottom,
            Color.parseColor("#FFD54F"),
            Color.parseColor("#4A148C"),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(tmpBtnDraw, r, r, ctaFillPaint)
        ctaFillPaint.shader = null

        tmpShimmer.set(
            tmpBtnDraw.left + r * 0.1f,
            tmpBtnDraw.top + r * 0.08f,
            tmpBtnDraw.right - r * 0.1f,
            tmpBtnDraw.top + tmpBtnDraw.height() * 0.4f
        )
        val hr = pillRxRy(tmpShimmer.height())
        ctaHiPaint.shader = LinearGradient(
            tmpShimmer.left, tmpShimmer.top, tmpShimmer.left, tmpShimmer.bottom,
            Color.argb(70, 255, 255, 255),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(tmpShimmer, hr, hr, ctaHiPaint)
        ctaHiPaint.shader = null

        ctaRimPaint.color = Color.argb(200, 255, 213, 79)
        ctaRimPaint.strokeWidth = 1.6f * ts
        canvas.drawRoundRect(tmpBtnDraw, r, r, ctaRimPaint)

        ctaLabelPaint.textSize = 11f * ts
        ctaLabelPaint.color = Color.WHITE
        ctaLabelPaint.setShadowLayer(3f * ts, 0f, 1.5f * ts, Color.argb(180, 0, 0, 0))
        val fm = ctaLabelPaint.fontMetrics
        canvas.drawText(
            label,
            tmpBtnDraw.centerX(),
            tmpBtnDraw.centerY() - (fm.ascent + fm.descent) / 2f,
            ctaLabelPaint
        )
        ctaLabelPaint.clearShadowLayer()
    }

    private fun previewStatusLine(
        state: BossUnlockState,
        sel: Int,
        bossUnlock: BossUnlockManager,
        nowMs: Long
    ): String {
        if (!state.isUnlocked) return "Locked - Unlock to use"
        if (state.isTemporaryUnlocked) {
            val rm = bossUnlock.temporaryRemainingMs(sel, nowMs)
            val mins = (rm / 60000L).toInt().coerceAtLeast(0)
            return "Unlocked ($mins min remaining)"
        }
        return "Unlocked - Ready to use"
    }

    private fun drawBitmapFit(canvas: Canvas, bmp: Bitmap, dst: RectF) {
        if (bmp.width <= 0 || bmp.height <= 0) return
        val ar = bmp.width.toFloat() / bmp.height.toFloat()
        var rw = dst.width()
        var rh = rw / ar
        if (rh > dst.height()) {
            rh = dst.height()
            rw = rh * ar
        }
        val l = dst.centerX() - rw / 2f
        val t = dst.centerY() - rh / 2f
        tmpBitmapDst.set(l, t, l + rw, t + rh)
        canvas.drawBitmap(bmp, null, tmpBitmapDst, bitmapPaint)
    }
}
