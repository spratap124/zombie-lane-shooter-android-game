package com.zombielane.shooter.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.zombielane.shooter.data.ChestManager
import com.zombielane.shooter.data.ChestSlot
import com.zombielane.shooter.data.ChestType
import com.zombielane.shooter.data.DailyMissionManager
import com.zombielane.shooter.data.ShooterManager
import com.zombielane.shooter.data.UpgradeManager
import com.zombielane.shooter.objects.PlayerAssets
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Main menu: full-screen background art (softened in assets), scrim + vignette for contrast,
 * ship, glowing UI, chest chips with timers.
 */
class MenuScreen {

    companion object {
        private const val PLAY_TAP_FEEDBACK_MS = 220L
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val shopBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6D00")
        style = Paint.Style.FILL
    }

    private val shopGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val settingsBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#37474F")
        style = Paint.Style.FILL
    }

    private val settingsGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90A4AE")
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }

    private val menuSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B39DDB")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val streakBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CFD8DC")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val highScorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFE082")
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }

    private val coinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD600")
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }

    private val menuScrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val equippedLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78909C")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val equippedNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val nebulaPaintA = Paint(Paint.ANTI_ALIAS_FLAG)
    private val nebulaPaintB = Paint(Paint.ANTI_ALIAS_FLAG)
    private val nebulaPaintC = Paint(Paint.ANTI_ALIAS_FLAG)
    private val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val shipGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val shipGlowInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val chestBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val chestStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }
    private val chestReadyGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val chestTimerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val chestNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val dimOverlayPaint = Paint().apply {
        color = Color.parseColor("#CC000000")
        style = Paint.Style.FILL
    }

    private val popCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A2E")
        style = Paint.Style.FILL
    }

    private val streakPopFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val streakPopBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#FFCA28")
    }

    private val streakPopOuterGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#00E5FF")
    }

    private val streakSparklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isFilterBitmap = true
    }

    private val streakEmojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val streakRibbonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val popOkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val popOkStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#E8F5E9")
    }

    private val popOkShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#40000000")
        style = Paint.Style.FILL
    }

    private val toastPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A5D6A7")
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val bgTopPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    /** Extra readability over busy menu background art (top / mid / bottom alpha). */
    private val readabilityOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val chestDimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val chestShimmerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val chestHintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val chestFeaturedBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val playGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val statBarBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33252A3A")
        style = Paint.Style.FILL
    }
    private val statBarFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val freeRewardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val freeRewardStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val freeRewardTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val sparklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val coinHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val gearPath = Path()
    private val weaponPath = Path()
    private val tmpRect = RectF()
    private val tmpRect2 = RectF()
    private val tmpRect3 = RectF()
    private val chestClipPath = Path()

    var playBtnRect = RectF()
    var shopBtnRect = RectF()
    var settingsBtnRect = RectF()
    var chestsNavRect = RectF()
    var dailyMissionsBtnRect = RectF()
    var freeRewardAdRect = RectF()
    var streakOkRect = RectF()

    private var playTapStartMs = 0L

    private var frameCount = 0L
    private var bgW = 0f
    private var bgH = 0f
    private var stars: FloatArray = FloatArray(0)

    init {
        buildIconPaths()
    }

    private fun buildIconPaths() {
        gearPath.reset()
        val teeth = 8
        val rOuter = 14f
        val rInner = 9f
        for (i in 0 until teeth) {
            val a0 = (i * Math.PI * 2 / teeth).toFloat()
            val a1 = ((i + 0.45) * Math.PI * 2 / teeth).toFloat()
            val a2 = ((i + 0.55) * Math.PI * 2 / teeth).toFloat()
            val a3 = ((i + 1) * Math.PI * 2 / teeth).toFloat()
            if (i == 0) gearPath.moveTo(cos(a0) * rOuter, sin(a0) * rOuter)
            else gearPath.lineTo(cos(a0) * rOuter, sin(a0) * rOuter)
            gearPath.lineTo(cos(a1) * rInner, sin(a1) * rInner)
            gearPath.lineTo(cos(a2) * rInner, sin(a2) * rInner)
            gearPath.lineTo(cos(a3) * rOuter, sin(a3) * rOuter)
        }
        gearPath.close()

        weaponPath.reset()
        weaponPath.addRoundRect(RectF(-16f, -4f, 10f, 4f), 2f, 2f, Path.Direction.CW)
        weaponPath.addRect(-6f, -7f, 4f, -4f, Path.Direction.CW)
        weaponPath.addOval(RectF(6f, -6f, 14f, 6f), Path.Direction.CW)
    }

    private fun ensureBackground(w: Float, h: Float) {
        if (w == bgW && h == bgH && stars.isNotEmpty()) return
        bgW = w
        bgH = h
        val rnd = Random(12345L)
        val n = 90
        stars = FloatArray(n * 5)
        var i = 0
        repeat(n) {
            stars[i++] = rnd.nextFloat() * w
            stars[i++] = rnd.nextFloat() * h
            stars[i++] = 0.8f + rnd.nextFloat() * 2.2f
            stars[i++] = rnd.nextInt(3).toFloat()
            stars[i++] = rnd.nextFloat() * 6.283f
        }

        nebulaPaintA.shader = RadialGradient(
            w * 0.25f, h * 0.15f, w * 0.55f,
            intArrayOf(0x668020FF.toInt(), 0x20404080, 0x00000000),
            floatArrayOf(0f, 0.35f, 1f),
            Shader.TileMode.CLAMP
        )
        nebulaPaintB.shader = RadialGradient(
            w * 0.85f, h * 0.35f, w * 0.45f,
            intArrayOf(0x55FF4080.toInt(), 0x10801060, 0x00000000),
            floatArrayOf(0f, 0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        nebulaPaintC.shader = RadialGradient(
            w * 0.5f, h * 0.75f, w * 0.6f,
            intArrayOf(0x4400BCD4.toInt(), 0x00081040, 0x00000000),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        // Darken edges and overall; keeps center slightly clearer for title block.
        vignettePaint.shader = RadialGradient(
            w / 2f, h / 2f, max(w, h) * 0.86f,
            intArrayOf(0x38000000, 0xEC000000.toInt()),
            floatArrayOf(0.12f, 1f),
            Shader.TileMode.CLAMP
        )
        menuScrimPaint.shader = LinearGradient(
            0f, 0f, 0f, h,
            intArrayOf(
                0xD9080C14.toInt(),
                0x7204060C.toInt(),
                0xE0080C14.toInt()
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        bgTopPaint.shader = LinearGradient(
            0f, 0f, 0f, h,
            intArrayOf(0xFF0A0E1A.toInt(), 0xFF060812.toInt(), 0xFF030510.toInt()),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        readabilityOverlayPaint.shader = LinearGradient(
            0f, 0f, 0f, h,
            intArrayOf(
                Color.argb((255 * 0.4f).toInt(), 0, 0, 0),
                Color.argb((255 * 0.3f).toInt(), 0, 0, 0),
                Color.argb((255 * 0.6f).toInt(), 0, 0, 0)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    fun bumpPlayTapFeedback(atMs: Long) {
        playTapStartMs = atMs
    }

    fun draw(
        canvas: Canvas,
        safeArea: RectF,
        highScore: Int,
        totalCoins: Int,
        shooterManager: ShooterManager,
        playerAssets: PlayerAssets,
        menuUi: MenuUiAssets,
        chestSlots: List<ChestSlot?>,
        streakDays: Int,
        chestToast: String?,
        streakPopupTitle: String?,
        streakPopupMessage: String?,
        upgradeManager: UpgradeManager,
        dailyMissionsClaimed: Int,
        freeRewardReady: Boolean,
        freeRewardLoading: Boolean,
        nowMs: Long
    ) {
        frameCount++
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        ensureBackground(w, h)

        drawMenuBackgroundCover(canvas, menuUi.menuBackground, w, h, bitmapPaint)
        drawParallaxStars(canvas, w, h)
        canvas.drawRect(0f, 0f, w, h, menuScrimPaint)
        canvas.drawRect(0f, 0f, w, h, vignettePaint)
        canvas.drawRect(0f, 0f, w, h, readabilityOverlayPaint)

        titlePaint.color = Color.WHITE
        subtitlePaint.color = Color.parseColor("#4DD0E1")

        val tAnim = frameCount * 0.045f
        val coinBob = sin(frameCount * 0.085).toFloat() * 5f * s
        val coinSpin = 0.92f + 0.08f * kotlin.math.abs(cos(frameCount * 0.11).toFloat())

        // Horizontal inset so labels stay inside safe area (score was clipped when align was CENTER on left edge).
        val padH = kotlin.math.max(24f * s, safeArea.width() * 0.048f).coerceAtMost(56f * s)

        val bannerReserve = (120f * s).coerceIn(100f, 168f)

        // Top bar: settings only (coins + best score flank the loadout ship below).
        val settingsAnchorY = safeArea.top + 40f * s
        val settingsSize = (88f * s).coerceIn(72f, 112f)
        val stRight = safeArea.right - padH
        val stTop = settingsAnchorY - settingsSize / 2f
        settingsBtnRect.set(stRight - settingsSize, stTop, stRight, stTop + settingsSize)
        drawBitmapFit(canvas, menuUi.settingsIcon, settingsBtnRect)

        // Title + glow pulse + scale
        var yPos = safeArea.top + safeArea.height() * 0.055f
        val titleScale = 1f + sin(tAnim * 1.1f).toFloat() * 0.04f
        val uiFlicker = 1f + 0.04f * sin(frameCount * 0.11f).toFloat()
        val glowR = (18f + sin(frameCount * 0.07).toFloat() * 5f) * s * uiFlicker
        titlePaint.textSize = 68f * s * titleScale
        titlePaint.setShadowLayer(glowR, 0f, 0f, Color.parseColor("#00BCD4"))
        canvas.drawText("ZOMBIE LANE", cx, yPos, titlePaint)
        yPos += 60f * s * titleScale
        subtitlePaint.textSize = 40f * s
        subtitlePaint.setShadowLayer(14f * s, 0f, 0f, Color.parseColor("#006064"))
        canvas.drawText("SHOOTER", cx, yPos, subtitlePaint)
        subtitlePaint.clearShadowLayer()
        titlePaint.clearShadowLayer()

        yPos += 31f * s
        infoPaint.textSize = 26f * s
        infoPaint.setShadowLayer(5f * s, 0f, 1.5f * s, Color.BLACK)
        canvas.drawText("ARCADE · ENDLESS SECTORS", cx, yPos, infoPaint)
        infoPaint.clearShadowLayer()

        // Ship center (loadout): extra float, tilt, layered glow
        yPos += 31f * s
        val shipTarget = (safeArea.width() * 0.22f).coerceIn(140f * s, 220f * s)
        val bmp = playerAssets.get(shooterManager.equipped)
        val bob = sin(frameCount * 0.055).toFloat() * 10f * s
        val loadoutFloat = sin(frameCount * 0.048f).toFloat() * 5f * s
        val sway = sin(frameCount * 0.038).toFloat() * 6f * s
        val shipCx = cx + sway
        val shipCy = yPos + shipTarget / 2f + bob + loadoutFloat
        val shipTiltDeg = sin(frameCount * 0.05f).toFloat() * 2.8f
        val scale = shipTarget / bmp.width.toFloat()

        shipGlowPaint.color = Color.argb(
            (26 + (sin(frameCount * 0.06) * 14).toInt()).coerceIn(16, 48),
            0, 229, 255
        )
        canvas.drawCircle(shipCx, shipCy, shipTarget * 0.72f, shipGlowPaint)
        shipGlowPaint.color = Color.argb(
            (60 + (sin(frameCount * 0.09) * 40).toInt()).coerceIn(40, 110),
            0, 229, 255
        )
        shipGlowInnerPaint.color = Color.argb(90, 100, 200, 255)
        canvas.drawCircle(shipCx, shipCy, shipTarget * 0.55f, shipGlowPaint)
        canvas.drawCircle(shipCx, shipCy, shipTarget * 0.38f, shipGlowInnerPaint)

        canvas.save()
        canvas.translate(shipCx, shipCy)
        canvas.rotate(shipTiltDeg)
        canvas.scale(scale, scale)
        canvas.drawBitmap(bmp, -bmp.width / 2f, -bmp.height / 2f, null)
        canvas.restore()

        val haloR = shipTarget * 0.55f
        // Clear space between loadout ship glow and flank stats (scale + % of ship for large icons).
        val flankGap = max(44f * s, 36f) + shipTarget * 0.16f
        val edgePad = safeArea.left + padH
        val edgeRight = safeArea.right - padH
        // Hard stop: stats must not enter the ship disc + gap.
        val shipInnerLeft = shipCx - haloR - flankGap
        val shipInnerRight = shipCx + haloR + flankGap
        // Same geometry as chest strip below — coin column starts at right edge of rightmost chest.
        val chipGap = 16f * s
        val rowInnerW = safeArea.width() - padH * 2f
        val chipW = (rowInnerW - 3f * chipGap) / 4f
        val rowLeft = safeArea.left + padH
        val rightmostChestRight = rowLeft + 4f * chipW + 3f * chipGap

        val labelTs = 22f * s
        val scoreTs = 32f * s
        val lineGap = 8f * s
        var wBestCol = 0f
        var labelBaseline = shipCy
        var scoreBaseline = shipCy
        val scoreStr = if (highScore > 0) highScore.toString() else ""
        if (highScore > 0) {
            highScorePaint.textAlign = Paint.Align.LEFT
            highScorePaint.textSize = scoreTs
            highScorePaint.color = Color.parseColor("#FFE082")
            val fmScore = highScorePaint.fontMetrics
            val wScore = highScorePaint.measureText(scoreStr)
            highScorePaint.textSize = labelTs
            highScorePaint.color = Color.parseColor("#B0BEC5")
            val fmLabel = highScorePaint.fontMetrics
            val wLabel = highScorePaint.measureText("BEST")
            wBestCol = max(wLabel, wScore)
            val blockH = (fmLabel.descent - fmLabel.ascent) + lineGap + (fmScore.descent - fmScore.ascent)
            labelBaseline = shipCy - blockH / 2f - fmLabel.ascent
            highScorePaint.textSize = scoreTs
            val fmScoreLine = highScorePaint.fontMetrics
            scoreBaseline = labelBaseline + fmLabel.descent + lineGap - fmScoreLine.ascent
        }

        coinPaint.textSize = 36f * s
        coinPaint.color = Color.parseColor("#FFE082")
        val coinStr = totalCoins.toString()
        val coinBmp = menuUi.coin
        val coinDraw = coinBmp.width * coinSpin
        val fmCoin = coinPaint.fontMetrics
        val coinTextW = coinPaint.measureText(coinStr)
        val coinColW = max(coinDraw, coinTextW)
        val stackGap = 8f * s
        val coinStackH = coinDraw + stackGap + (fmCoin.descent - fmCoin.ascent)
        val coinNudgeLeft = 74f * s
        val coinAnchorX = rightmostChestRight - coinNudgeLeft

        if (highScore > 0) {
            var bestLeftX = edgePad
            if (bestLeftX + wBestCol > shipInnerLeft) {
                bestLeftX = (shipInnerLeft - wBestCol).coerceAtLeast(edgePad)
            }
            highScorePaint.textSize = labelTs
            highScorePaint.color = Color.parseColor("#B0BEC5")
            highScorePaint.setShadowLayer(6f * s * uiFlicker, 0f, 2f * s, Color.BLACK)
            canvas.drawText("BEST", bestLeftX, labelBaseline, highScorePaint)
            highScorePaint.textSize = scoreTs
            highScorePaint.color = Color.parseColor("#FFE082")
            canvas.drawText(scoreStr, bestLeftX, scoreBaseline, highScorePaint)
            highScorePaint.clearShadowLayer()
            highScorePaint.textSize = 34f * s
        }

        var coinLeftX = coinAnchorX
        if (coinLeftX + coinColW > edgeRight) {
            coinLeftX = edgeRight - coinColW
        }
        coinLeftX = max(coinLeftX, coinAnchorX)
        val stackTop = shipCy + coinBob - coinStackH / 2f
        val iconLeft = coinLeftX + (coinColW - coinDraw) / 2f
        val coinHaloPulse = (42 + 38 * sin(frameCount * 0.088f)).toInt().coerceIn(30, 120)
        coinHaloPaint.shader = RadialGradient(
            iconLeft + coinDraw / 2f,
            stackTop + coinDraw / 2f,
            coinDraw * 0.55f,
            Color.argb(coinHaloPulse, 255, 210, 80),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(iconLeft + coinDraw / 2f, stackTop + coinDraw / 2f, coinDraw * 0.5f, coinHaloPaint)
        coinHaloPaint.shader = null
        tmpRect.set(iconLeft, stackTop, iconLeft + coinDraw, stackTop + coinDraw)
        drawBitmapFit(canvas, coinBmp, tmpRect)
        coinPaint.textAlign = Paint.Align.CENTER
        coinPaint.setShadowLayer(10f * s * uiFlicker, 0f, 2f * s, Color.parseColor("#80501000"))
        val coinNumBaseline = stackTop + coinDraw + stackGap - fmCoin.ascent
        canvas.drawText(coinStr, coinLeftX + coinColW / 2f, coinNumBaseline, coinPaint)
        coinPaint.clearShadowLayer()
        coinPaint.textAlign = Paint.Align.LEFT

        yPos += shipTarget + 26f * s
        equippedLabelPaint.textSize = 26f * s
        equippedLabelPaint.setShadowLayer(5f * s, 0f, 1.5f * s, Color.BLACK)
        canvas.drawText("LOADOUT", cx, yPos, equippedLabelPaint)
        equippedLabelPaint.clearShadowLayer()
        yPos += 32f * s
        val equipped = shooterManager.getEquipped()
        equippedNamePaint.textSize = 36f * s
        equippedNamePaint.color = equipped.bulletColor
        equippedNamePaint.setShadowLayer(6f, 0f, 0f, equipped.bulletColor and 0x88FFFFFF.toInt())
        canvas.drawText(equipped.name, cx, yPos, equippedNamePaint)
        equippedNamePaint.clearShadowLayer()
        drawLoadoutStatBars(canvas, cx, yPos + 14f * s, rowInnerW, s, upgradeManager)

        // Free reward (rewarded ad) + chest strip
        yPos += 50f * s
        drawFreeRewardPill(canvas, safeArea, padH, edgeRight, yPos, s, freeRewardReady, freeRewardLoading)
        yPos += 40f * s

        val chipH = (safeArea.width() * 0.245f).coerceIn(132f * s, 200f * s)
        val streakBelowChip = 32f * s
        val featuredChestIdx = featuredChestIndex(chestSlots, nowMs)
        for (i in 0 until ChestManager.MAX_SLOTS) {
            val left = rowLeft + i * (chipW + chipGap)
            tmpRect.set(left, yPos, left + chipW, yPos + chipH)
            val slot = chestSlots.getOrNull(i)
            drawChestChip(
                canvas, tmpRect, slot, nowMs, s, i, menuUi,
                isFeatured = i == featuredChestIdx
            )
        }
        drawChestRowSparkles(canvas, rowLeft, yPos, chipW, chipGap, chipH, s)
        val streakBaseline = yPos + chipH + streakBelowChip
        menuSubPaint.textSize = 26f * s
        menuSubPaint.setShadowLayer(5f * s * uiFlicker, 0f, 1.5f * s, Color.BLACK)
        canvas.drawText("Streak $streakDays  ·  tap chests", cx, streakBaseline, menuSubPaint)
        menuSubPaint.clearShadowLayer()

        chestsNavRect = RectF(
            rowLeft,
            yPos,
            rowLeft + 4 * chipW + 3 * chipGap,
            streakBaseline + 19f * s
        )

        yPos += chipH + streakBelowChip + 44f * s
        if (chestToast != null) {
            toastPaint.textSize = 20f * s
            canvas.drawText(chestToast, cx, yPos, toastPaint)
            yPos += 36f * s
        }

        val playW = safeArea.width() * 0.88f
        val bmpAr = menuUi.playButton.height.toFloat() / menuUi.playButton.width.coerceAtLeast(1)
        var playH = playW * bmpAr
        playH = playH.coerceIn(safeArea.height() * 0.075f, safeArea.height() * 0.26f)
        val playBreath = 1f + sin(frameCount * 0.04f).toFloat() * 0.06f
        val tapElapsed = (nowMs - playTapStartMs).toFloat().coerceAtLeast(0f)
        val tapMul = if (tapElapsed < PLAY_TAP_FEEDBACK_MS) {
            val u = 1f - tapElapsed / PLAY_TAP_FEEDBACK_MS.toFloat()
            1f - 0.12f * u * u
        } else 1f
        val playPulse = playBreath * tapMul
        val tapGlowBoost = if (tapElapsed < PLAY_TAP_FEEDBACK_MS) {
            (1f - tapElapsed / PLAY_TAP_FEEDBACK_MS.toFloat()).coerceIn(0f, 1f)
        } else 0f
        val glowAlpha = (38 + 22 * sin(frameCount * 0.042f).toFloat() + tapGlowBoost * 55f).toInt().coerceIn(0, 160)
        tmpRect3.set(cx - playW / 2f, yPos, cx + playW / 2f, yPos + playH)
        tmpRect3.inset(-10f * s, -6f * s)
        playGlowPaint.shader = RadialGradient(
            cx, yPos + playH / 2f, playW * 0.55f,
            Color.argb(glowAlpha, 0, 230, 255),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(tmpRect3, 20f * s, 20f * s, playGlowPaint)
        playGlowPaint.shader = null
        val pw = playW * playPulse
        val ph = playH * playPulse
        tmpRect.set(cx - pw / 2f, yPos + (playH - ph) / 2f, cx + pw / 2f, yPos + (playH + ph) / 2f)
        drawBitmapFit(canvas, menuUi.playButton, tmpRect)
        playBtnRect.set(tmpRect)

        yPos += playH + 20f * s

        val dailyBmp = menuUi.dailyMissionsButton
        val weaponBmp = menuUi.weaponsButton
        val pairGap = 12f * s
        val pairInnerW = safeArea.width() - 2f * padH
        val halfW = (pairInnerW - pairGap) / 2f
        val dailyAr = dailyBmp.height.toFloat() / dailyBmp.width.coerceAtLeast(1)
        val weaponAr = weaponBmp.height.toFloat() / weaponBmp.width.coerceAtLeast(1)
        val rowHUnscaled = max(halfW * dailyAr, halfW * weaponAr)
        val rowH = rowHUnscaled.coerceIn(safeArea.height() * 0.072f, safeArea.height() * 0.16f)
        val pairRowLeft = safeArea.left + padH
        dailyMissionsBtnRect.set(pairRowLeft, yPos, pairRowLeft + halfW, yPos + rowH)
        shopBtnRect.set(pairRowLeft + halfW + pairGap, yPos, pairRowLeft + pairInnerW, yPos + rowH)
        tmpRect.set(dailyMissionsBtnRect)
        drawBitmapFit(canvas, dailyBmp, tmpRect)
        tmpRect.set(shopBtnRect)
        drawBitmapFit(canvas, weaponBmp, tmpRect)
        drawDailyMissionProgressDots(
            canvas,
            dailyMissionsBtnRect.centerX(),
            yPos + rowH + 6f * s,
            s,
            dailyMissionsClaimed
        )

        yPos += rowH + 22f * s

        infoPaint.textSize = 24f * s
        infoPaint.textAlign = Paint.Align.LEFT
        infoPaint.setShadowLayer(4f * s, 0f, 1f * s, Color.BLACK)
        canvas.drawText("v1.0", safeArea.left + padH, h - bannerReserve * 0.55f, infoPaint)
        infoPaint.clearShadowLayer()
        infoPaint.textAlign = Paint.Align.CENTER

        streakOkRect.setEmpty()
        if (streakPopupTitle != null && streakPopupMessage != null) {
            drawStreakRewardPopup(
                canvas, w, h, cx, s, safeArea, streakPopupTitle, streakPopupMessage, nowMs
            )
        }
    }

    private fun drawStreakRewardPopup(
        canvas: Canvas,
        w: Float,
        h: Float,
        cx: Float,
        s: Float,
        safeArea: RectF,
        streakPopupTitle: String,
        streakPopupMessage: String,
        nowMs: Long
    ) {
        val pulse = (sin(nowMs * 0.004).toFloat() * 0.5f + 0.5f)
        val pulse2 = (sin(nowMs * 0.003 + 1.2f).toFloat() * 0.5f + 0.5f)

        val vignette = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, safeArea.top + safeArea.height() * 0.42f,
                w * 0.85f,
                Color.parseColor("#88101028"),
                Color.parseColor("#F0101018"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, dimOverlayPaint)
        canvas.drawRect(0f, 0f, w, h, vignette)

        val pw = safeArea.width() * 0.88f
        val ph = 288f * s
        val px = cx - pw / 2f
        val py = safeArea.top + safeArea.height() * 0.22f
        val r = 22f * s

        tmpRect.set(px, py, px + pw, py + ph)

        streakPopOuterGlowPaint.strokeWidth = (10f + pulse * 4f) * s
        streakPopOuterGlowPaint.alpha = (35 + pulse * 50).toInt().coerceIn(0, 255)
        tmpRect2.set(px - 10f * s, py - 10f * s, px + pw + 10f * s, py + ph + 10f * s)
        canvas.drawRoundRect(tmpRect2, r + 8f * s, r + 8f * s, streakPopOuterGlowPaint)
        streakPopOuterGlowPaint.alpha = 255

        streakPopFillPaint.shader = LinearGradient(
            px, py, px + pw, py + ph,
            intArrayOf(
                Color.parseColor("#3D2B7A"),
                Color.parseColor("#1B2638"),
                Color.parseColor("#0D1520")
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(tmpRect, r, r, streakPopFillPaint)
        streakPopFillPaint.shader = null

        streakRibbonPaint.shader = LinearGradient(
            px, py, px + pw, py + 56f * s,
            Color.parseColor("#66FFCA28"),
            Color.parseColor("#00FFCA28"),
            Shader.TileMode.CLAMP
        )
        tmpRect2.set(px + 8f * s, py + 8f * s, px + pw - 8f * s, py + 44f * s)
        canvas.drawRoundRect(tmpRect2, 14f * s, 14f * s, streakRibbonPaint)
        streakRibbonPaint.shader = null

        streakPopBorderPaint.strokeWidth = (3f + pulse2 * 0.8f) * s
        canvas.drawRoundRect(tmpRect, r, r, streakPopBorderPaint)

        val sparkles = listOf(
            Triple(px + pw * 0.12f, py + ph * 0.18f, 4f),
            Triple(px + pw * 0.88f, py + ph * 0.22f, 5f),
            Triple(px + pw * 0.08f, py + ph * 0.55f, 3.5f),
            Triple(px + pw * 0.92f, py + ph * 0.62f, 4f),
            Triple(px + pw * 0.18f, py + ph * 0.88f, 3f),
            Triple(px + pw * 0.82f, py + ph * 0.85f, 4.5f)
        )
        for ((i, t) in sparkles.withIndex()) {
            val tw = t.third * s
            val a = (80 + pulse * 120 + (i * 23) % 40).toInt().coerceIn(40, 255)
            streakSparklePaint.color = Color.argb(a, 255, 236, 150)
            canvas.drawCircle(t.first, t.second, tw, streakSparklePaint)
            streakSparklePaint.color = Color.argb(a / 2, 255, 255, 255)
            canvas.drawCircle(t.first - tw * 0.3f, t.second - tw * 0.3f, tw * 0.35f, streakSparklePaint)
        }

        streakEmojiPaint.textSize = 52f * s
        streakEmojiPaint.setShadowLayer(12f * s, 0f, 4f * s, Color.parseColor("#80000000"))
        canvas.drawText("🎁", cx, py + 58f * s, streakEmojiPaint)
        streakEmojiPaint.clearShadowLayer()

        titlePaint.textSize = 34f * s
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.shader = LinearGradient(
            cx - pw * 0.4f, py + 100f * s, cx + pw * 0.4f, py + 100f * s,
            intArrayOf(
                Color.parseColor("#FFEA00"),
                Color.parseColor("#FFAB00"),
                Color.parseColor("#FFEA00")
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        titlePaint.setShadowLayer(10f * s, 0f, 2f * s, Color.parseColor("#C0000000"))
        canvas.drawText(streakPopupTitle, cx, py + 104f * s, titlePaint)
        titlePaint.clearShadowLayer()
        titlePaint.shader = null

        streakBodyPaint.textSize = 23f * s
        streakBodyPaint.color = Color.parseColor("#ECEFF1")
        streakBodyPaint.setShadowLayer(3f * s, 0f, 1f * s, Color.parseColor("#80000000"))
        val words = streakPopupMessage.chunked(34)
        var ty = py + 148f * s
        for (line in words.take(4)) {
            canvas.drawText(line, cx, ty, streakBodyPaint)
            ty += 32f * s
        }
        streakBodyPaint.clearShadowLayer()

        val okW = pw * 0.62f
        val okH = 54f * s
        streakOkRect = RectF(cx - okW / 2f, py + ph - okH - 22f * s, cx + okW / 2f, py + ph - 22f * s)
        tmpRect2.set(
            streakOkRect.left + 3f * s,
            streakOkRect.bottom + 2f * s,
            streakOkRect.right - 3f * s,
            streakOkRect.bottom + 8f * s
        )
        canvas.drawRoundRect(tmpRect2, 8f * s, 8f * s, popOkShadowPaint)

        popOkPaint.shader = LinearGradient(
            streakOkRect.left, streakOkRect.top,
            streakOkRect.left, streakOkRect.bottom,
            Color.parseColor("#76FF03"),
            Color.parseColor("#00C853"),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(streakOkRect, 16f * s, 16f * s, popOkPaint)
        popOkPaint.shader = null

        popOkStrokePaint.strokeWidth = 2.5f * s
        canvas.drawRoundRect(streakOkRect, 16f * s, 16f * s, popOkStrokePaint)

        btnTextPaint.textSize = 28f * s
        btnTextPaint.color = Color.WHITE
        btnTextPaint.setShadowLayer(4f * s, 0f, 2f * s, Color.parseColor("#60000000"))
        canvas.drawText("AWESOME!", cx, streakOkRect.centerY() + 10f * s, btnTextPaint)
        btnTextPaint.clearShadowLayer()
    }

    /** Uniform scale to cover the canvas; centers crop (same idea as ImageView centerCrop). */
    private fun drawMenuBackgroundCover(canvas: Canvas, bmp: Bitmap, w: Float, h: Float, paint: Paint) {
        if (bmp.width <= 0 || bmp.height <= 0) return
        val scale = max(w / bmp.width, h / bmp.height)
        val rw = bmp.width * scale
        val rh = bmp.height * scale
        val l = (w - rw) / 2f
        val t = (h - rh) / 2f
        tmpRect.set(l, t, l + rw, t + rh)
        canvas.drawBitmap(bmp, null, tmpRect, paint)
    }

    private fun drawParallaxStars(canvas: Canvas, w: Float, h: Float) {
        val scroll = frameCount * 0.35f
        var idx = 0
        while (idx < stars.size) {
            val sx = stars[idx]
            val sy = stars[idx + 1]
            val sz = stars[idx + 2]
            val layer = stars[idx + 3].toInt()
            val phase = stars[idx + 4]
            idx += 5
            val speed = 0.15f + layer * 0.35f
            var y = (sy + scroll * speed) % h
            if (y < 0) y += h
            val tw = 0.45f + 0.55f * ((sin(frameCount * 0.08f + phase) * 0.5 + 0.5).toFloat())
            val alpha = (255 * tw * (0.5f + layer * 0.2f)).toInt().coerceIn(30, 255)
            starPaint.alpha = alpha
            canvas.drawCircle(sx, y, sz, starPaint)
        }
        starPaint.alpha = 255
    }

    private fun drawNebulaDrift(canvas: Canvas, w: Float, h: Float, nowMs: Long) {
        val drift = nowMs * 0.00002
        canvas.save()
        canvas.translate(sin(drift).toFloat() * 20f, cos(drift * 0.7).toFloat() * 14f)
        canvas.drawRect(0f, 0f, w, h, nebulaPaintA)
        canvas.restore()
        canvas.save()
        canvas.translate(cos(drift * 0.9).toFloat() * -16f, sin(drift * 1.1).toFloat() * 22f)
        canvas.drawRect(0f, 0f, w, h, nebulaPaintB)
        canvas.restore()
        canvas.save()
        canvas.translate(sin(drift * 0.5).toFloat() * 12f, cos(drift).toFloat() * -10f)
        canvas.drawRect(0f, 0f, w, h, nebulaPaintC)
        canvas.restore()
    }

    private fun featuredChestIndex(slots: List<ChestSlot?>, now: Long): Int {
        var bestReady = -1
        for (i in 0 until ChestManager.MAX_SLOTS) {
            val slot = slots.getOrNull(i) ?: continue
            if (slot.isReady(now) && (bestReady < 0 || i < bestReady)) bestReady = i
        }
        if (bestReady >= 0) return bestReady
        var soonIdx = -1
        var bestRem = Long.MAX_VALUE
        for (i in 0 until ChestManager.MAX_SLOTS) {
            val slot = slots.getOrNull(i) ?: continue
            if (!slot.isReady(now)) {
                val rem = slot.remainingMs(now)
                if (rem < bestRem) {
                    bestRem = rem
                    soonIdx = i
                }
            }
        }
        if (soonIdx >= 0) return soonIdx
        return (0 until ChestManager.MAX_SLOTS).firstOrNull { slots.getOrNull(it) == null } ?: 0
    }

    private fun drawLoadoutStatBars(
        canvas: Canvas,
        cx: Float,
        topY: Float,
        rowInnerW: Float,
        s: Float,
        upgradeManager: UpgradeManager
    ) {
        val barTotalW = min(rowInnerW * 0.58f, 280f * s)
        val barH = 5.5f * s
        val gapY = 8f * s
        val left = cx - barTotalW / 2f
        val dmgN = min(1f, upgradeManager.damage / 22f)
        val rofN = min(1f, upgradeManager.fireRateReductionMs / 80f)
        val hpN = min(1f, (upgradeManager.maxHealth - 3) / 18f)
        val rows = listOf(
            Pair(Color.parseColor("#EF5350"), dmgN),
            Pair(Color.parseColor("#FFCA28"), rofN),
            Pair(Color.parseColor("#66BB6A"), hpN)
        )
        var y = topY
        for ((col, fill) in rows) {
            tmpRect2.set(left, y, left + barTotalW, y + barH)
            canvas.drawRoundRect(tmpRect2, barH / 2f, barH / 2f, statBarBgPaint)
            tmpRect2.right = left + barTotalW * fill
            statBarFillPaint.color = col
            canvas.drawRoundRect(tmpRect2, barH / 2f, barH / 2f, statBarFillPaint)
            y += barH + gapY
        }
    }

    private fun drawFreeRewardPill(
        canvas: Canvas,
        safeArea: RectF,
        padH: Float,
        edgeRight: Float,
        top: Float,
        s: Float,
        ready: Boolean,
        loading: Boolean
    ) {
        val pillH = 36f * s
        freeRewardTextPaint.textSize = (19f * s).coerceIn(15f, 22f)
        val label = when {
            loading -> "LOADING…"
            ready -> "FREE REWARD"
            else -> "TAP WHEN READY"
        }
        val text = "🎁 $label"
        val tw = freeRewardTextPaint.measureText(text) + 26f * s
        val pillLeft = (edgeRight - tw).coerceAtLeast(safeArea.left + padH)
        freeRewardAdRect.set(pillLeft, top, edgeRight, top + pillH)
        val r = 18f * s
        freeRewardFillPaint.shader = LinearGradient(
            freeRewardAdRect.left, freeRewardAdRect.top,
            freeRewardAdRect.right, freeRewardAdRect.bottom,
            intArrayOf(Color.parseColor("#3D2E5C"), Color.parseColor("#1E1A2E")),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(freeRewardAdRect, r, r, freeRewardFillPaint)
        freeRewardFillPaint.shader = null
        val pulse = (0.5f + 0.5f * sin(frameCount * 0.09f))
        freeRewardStrokePaint.color = when {
            loading -> Color.parseColor("#5C5C7A")
            ready -> Color.argb((140 + pulse * 115).toInt(), 255, 215, 80)
            else -> Color.parseColor("#6D6D8A")
        }
        freeRewardStrokePaint.strokeWidth = (2f + pulse * 0.8f) * s
        canvas.drawRoundRect(freeRewardAdRect, r, r, freeRewardStrokePaint)
        freeRewardTextPaint.color = if (ready && !loading) Color.parseColor("#FFECB3") else Color.parseColor("#B0BEC5")
        freeRewardTextPaint.setShadowLayer(3f * s, 0f, 1f * s, Color.parseColor("#80000000"))
        canvas.drawText(text, freeRewardAdRect.centerX(), freeRewardAdRect.centerY() + 7f * s, freeRewardTextPaint)
        freeRewardTextPaint.clearShadowLayer()
    }

    private fun drawChestRowSparkles(
        canvas: Canvas,
        rowLeft: Float,
        rowTop: Float,
        chipW: Float,
        chipGap: Float,
        chipH: Float,
        s: Float
    ) {
        val seeds = floatArrayOf(0.12f, 0.37f, 0.62f, 0.88f)
        for (i in seeds.indices) {
            val phase = frameCount * 0.07f + i * 1.7f
            val a = (35 + 55 * (0.5f + 0.5f * sin(phase))).toInt().coerceIn(0, 120)
            if (a < 12) continue
            val slotCenter = rowLeft + chipW / 2f + i * (chipW + chipGap)
            val sx = slotCenter + sin(phase * 1.1f) * 6f * s
            val sy = rowTop + chipH * 0.18f + sin(phase * 0.9f) * 5f * s
            sparklePaint.color = Color.argb(a, 255, 245, 200)
            canvas.drawCircle(sx, sy, (1.8f + sin(phase) * 0.6f) * s, sparklePaint)
        }
    }

    private fun drawDailyMissionProgressDots(
        canvas: Canvas,
        cx: Float,
        baselineY: Float,
        s: Float,
        claimed: Int
    ) {
        val dotR = 5f * s
        val gap = 14f * s
        val total = DailyMissionManager.MISSION_COUNT
        for (i in 0 until total) {
            val x = cx - (total - 1) * gap / 2f + i * gap
            statBarBgPaint.color = Color.parseColor("#33252A3A")
            canvas.drawCircle(x, baselineY, dotR, statBarBgPaint)
            if (i < claimed) {
                statBarFillPaint.color = Color.parseColor("#4DD0E1")
                canvas.drawCircle(x, baselineY, dotR * 0.65f, statBarFillPaint)
            }
        }
        statBarBgPaint.color = Color.parseColor("#33252A3A")
    }

    private fun drawChestChip(
        canvas: Canvas,
        r: RectF,
        slot: ChestSlot?,
        nowMs: Long,
        s: Float,
        index: Int,
        menuUi: MenuUiAssets,
        isFeatured: Boolean
    ) {
        val chipPulse = 0.5f + 0.5f * sin(frameCount * 0.12f)
        val scaleFeatured = if (isFeatured) 1f + 0.05f * chipPulse else 1f

        canvas.save()
        canvas.translate(r.centerX(), r.centerY())
        canvas.scale(scaleFeatured, scaleFeatured)
        canvas.translate(-r.centerX(), -r.centerY())

        chestBodyPaint.color = Color.parseColor("#12121C")
        chestStrokePaint.strokeWidth = 2f * s

        val textBand = if (isFeatured && slot != null) 52f * s else 44f * s
        val inner = 4f * s
        val iconTop = r.top + inner
        val iconBottom = r.bottom - textBand
        val iconH = (iconBottom - iconTop).coerceAtLeast(28f)

        if (slot == null) {
            chestStrokePaint.color = if (isFeatured) Color.parseColor("#5C6BC0") else Color.parseColor("#3D3D5C")
            canvas.drawRoundRect(r, 12f * s, 12f * s, chestBodyPaint)
            canvas.drawRoundRect(r, 12f * s, 12f * s, chestStrokePaint)
            tmpRect2.set(r.left + inner, iconTop, r.right - inner, iconTop + iconH)
            bitmapPaint.alpha = if (isFeatured) 110 else 85
            drawBitmapFit(canvas, menuUi.chest(ChestType.COMMON), tmpRect2)
            bitmapPaint.alpha = 255
            chestNamePaint.textSize = (12f * s).coerceIn(10f, 16f)
            chestNamePaint.color = Color.parseColor("#616161")
            canvas.drawText("EMPTY", r.centerX(), r.bottom - (if (isFeatured) 34f else 28f) * s, chestNamePaint)
            chestTimerPaint.textSize = (r.height() * 0.12f).coerceIn(16f * s, 24f * s)
            chestTimerPaint.color = Color.parseColor("#616161")
            canvas.drawText("—", r.centerX(), r.bottom - (if (isFeatured) 20f else 9f) * s, chestTimerPaint)
            if (isFeatured) {
                chestHintPaint.textSize = 9f * s
                chestHintPaint.color = Color.argb(200, 158, 158, 180)
                canvas.drawText("Play to earn", r.centerX(), r.bottom - 6f * s, chestHintPaint)
            }
            if (!isFeatured) {
                chestDimPaint.color = Color.argb(55, 0, 0, 0)
                canvas.drawRoundRect(r, 12f * s, 12f * s, chestDimPaint)
            }
            canvas.restore()
            return
        }

        val ready = slot.isReady(nowMs)
        val rarityBorder = when (slot.type) {
            ChestType.COMMON -> Color.parseColor("#B0A070")
            ChestType.RARE -> Color.parseColor("#64B5F6")
            ChestType.EPIC -> Color.parseColor("#CE93D8")
            ChestType.SUPER -> Color.parseColor("#FFD54F")
        }

        if (isFeatured) {
            val borderCol = when (slot.type) {
                ChestType.EPIC -> Color.argb((145 + chipPulse * 110).toInt(), 186, 104, 200)
                ChestType.SUPER, ChestType.COMMON -> Color.argb((150 + chipPulse * 105).toInt(), 255, 210, 100)
                ChestType.RARE -> Color.argb((140 + chipPulse * 100).toInt(), 100, 200, 255)
            }
            chestFeaturedBorderPaint.color = borderCol
            chestFeaturedBorderPaint.strokeWidth = (3.2f + chipPulse * 1.8f) * s
            tmpRect2.set(r)
            tmpRect2.inset(-7f * s, -7f * s)
            canvas.drawRoundRect(tmpRect2, 17f * s, 17f * s, chestFeaturedBorderPaint)
        }

        if (ready && isFeatured) {
            chestReadyGlowPaint.color = Color.argb((95 + chipPulse * 160).toInt(), 255, 224, 100)
            chestReadyGlowPaint.strokeWidth = (4f + chipPulse * 2.5f) * s
            tmpRect2.set(r)
            tmpRect2.inset(-5f * s, -5f * s)
            canvas.drawRoundRect(tmpRect2, 16f * s, 16f * s, chestReadyGlowPaint)
        } else if (ready && !isFeatured) {
            chestReadyGlowPaint.color = Color.argb(70, 180, 170, 120)
            chestReadyGlowPaint.strokeWidth = 2f * s
            tmpRect2.set(r)
            tmpRect2.inset(-3f * s, -3f * s)
            canvas.drawRoundRect(tmpRect2, 14f * s, 14f * s, chestReadyGlowPaint)
        }

        canvas.drawRoundRect(r, 12f * s, 12f * s, chestBodyPaint)
        chestStrokePaint.color = if (isFeatured) rarityBorder else Color.parseColor("#3D3D5C")
        canvas.drawRoundRect(r, 12f * s, 12f * s, chestStrokePaint)

        tmpRect2.set(r.left + inner, iconTop, r.right - inner, iconTop + iconH)
        drawBitmapFit(canvas, menuUi.chest(slot.type), tmpRect2)

        chestClipPath.reset()
        chestClipPath.addRoundRect(r, 12f * s, 12f * s, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(chestClipPath)
        val shimmerT = (frameCount * 0.045f + index * 0.4f) % 2f
        val sx0 = r.left + r.width() * (shimmerT * 0.5f - 0.15f)
        chestShimmerPaint.shader = LinearGradient(
            sx0, r.top, sx0 + r.width() * 0.55f, r.bottom,
            intArrayOf(0x00000000, 0x33FFFFFF, 0x00000000),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(r, chestShimmerPaint)
        chestShimmerPaint.shader = null
        canvas.restore()

        chestNamePaint.textSize = (12f * s).coerceIn(10f, 16f)
        chestNamePaint.color = when (slot.type) {
            ChestType.COMMON -> Color.parseColor("#90A4AE")
            ChestType.RARE -> Color.parseColor("#64B5F6")
            ChestType.EPIC -> Color.parseColor("#CE93D8")
            ChestType.SUPER -> Color.parseColor("#FFD54F")
        }
        val nameY = r.bottom - (if (isFeatured) 34f else 28f) * s
        canvas.drawText(slot.type.displayName.uppercase(), r.centerX(), nameY, chestNamePaint)

        chestTimerPaint.textSize = (r.height() * 0.12f).coerceIn(16f * s, 24f * s)
        chestTimerPaint.color = if (ready) Color.parseColor("#FFF59D") else Color.parseColor("#B0BEC5")
        val statusLabel = when {
            ready && isFeatured -> "READY"
            ready -> "OPEN"
            else -> formatRemaining(slot.remainingMs(nowMs))
        }
        val timerY = r.bottom - (if (isFeatured) 20f else 9f) * s
        canvas.drawText(statusLabel, r.centerX(), timerY, chestTimerPaint)

        if (isFeatured && ready) {
            chestHintPaint.textSize = 9f * s
            chestHintPaint.color = Color.argb(220, 200, 200, 220)
            canvas.drawText("Tap to open", r.centerX(), r.bottom - 5f * s, chestHintPaint)
        }

        if (!isFeatured) {
            chestDimPaint.color = Color.argb(72, 0, 0, 0)
            canvas.drawRoundRect(r, 12f * s, 12f * s, chestDimPaint)
        }

        canvas.restore()
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
        tmpRect.set(l, t, l + rw, t + rh)
        canvas.drawBitmap(bmp, null, tmpRect, bitmapPaint)
    }

    private fun formatRemaining(ms: Long): String {
        val sec = (ms / 1000).toInt()
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val secR = sec % 60
        return if (h > 0) String.format("%d:%02d", h, m)
        else String.format("%d:%02d", m, secR)
    }
}
