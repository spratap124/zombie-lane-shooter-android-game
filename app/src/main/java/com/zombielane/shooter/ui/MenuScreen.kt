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
import com.zombielane.shooter.data.ShooterManager
import com.zombielane.shooter.objects.PlayerAssets
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * Main menu: full-screen background art (softened in assets), scrim + vignette for contrast,
 * ship, glowing UI, chest chips with timers.
 */
class MenuScreen {

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

    private val gearPath = Path()
    private val weaponPath = Path()
    private val tmpRect = RectF()
    private val tmpRect2 = RectF()

    var playBtnRect = RectF()
    var shopBtnRect = RectF()
    var settingsBtnRect = RectF()
    var chestsNavRect = RectF()
    var streakOkRect = RectF()

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
        nowMs: Long
    ) {
        frameCount++
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val s = w / 1080f

        ensureBackground(w, h)

        drawMenuBackgroundCover(canvas, menuUi.menuBackground, w, h, bitmapPaint)

        canvas.drawRect(0f, 0f, w, h, menuScrimPaint)
        canvas.drawRect(0f, 0f, w, h, vignettePaint)

        titlePaint.color = Color.WHITE
        subtitlePaint.color = Color.parseColor("#4DD0E1")

        val tAnim = frameCount * 0.045f
        val coinBob = sin(frameCount * 0.085).toFloat() * 5f * s
        val coinSpin = 0.92f + 0.08f * kotlin.math.abs(cos(frameCount * 0.11).toFloat())

        // Horizontal inset so labels stay inside safe area (score was clipped when align was CENTER on left edge).
        val padH = kotlin.math.max(24f * s, safeArea.width() * 0.048f).coerceAtMost(56f * s)

        val bannerReserve = (120f * s).coerceIn(100f, 168f)

        // Top bar: high score + coins
        highScorePaint.textSize = 34f * s
        infoPaint.textSize = 22f * s
        if (highScore > 0) {
            highScorePaint.setShadowLayer(8f * s, 0f, 2f * s, Color.BLACK)
            canvas.drawText(
                "BEST $highScore",
                safeArea.left + padH,
                safeArea.top + 42f * s,
                highScorePaint
            )
            highScorePaint.clearShadowLayer()
        }
        val coinBmp = menuUi.coin
        val coinDraw = coinBmp.width * coinSpin
        coinPaint.textSize = 36f * s
        coinPaint.color = Color.parseColor("#FFE082")
        val coinStr = totalCoins.toString()
        val coinTextW = coinPaint.measureText(coinStr)
        val coinGap = 14f * s
        val coinCy = safeArea.top + 36f * s + coinBob
        val textRight = safeArea.right - padH
        val coinCx = textRight - coinTextW - coinGap - coinDraw / 2f
        tmpRect.set(
            coinCx - coinDraw / 2f,
            coinCy - coinDraw / 2f,
            coinCx + coinDraw / 2f,
            coinCy + coinDraw / 2f
        )
        drawBitmapFit(canvas, coinBmp, tmpRect)
        coinPaint.textAlign = Paint.Align.LEFT
        coinPaint.setShadowLayer(8f * s, 0f, 2f * s, Color.BLACK)
        canvas.drawText(
            coinStr,
            coinCx + coinDraw / 2f + coinGap,
            coinCy + 12f * s,
            coinPaint
        )
        coinPaint.clearShadowLayer()

        // Title + glow pulse + scale
        var yPos = safeArea.top + safeArea.height() * 0.055f
        val titleScale = 1f + sin(tAnim * 1.1f).toFloat() * 0.04f
        val glowR = (18f + sin(frameCount * 0.07).toFloat() * 5f) * s
        titlePaint.textSize = 68f * s * titleScale
        titlePaint.setShadowLayer(glowR, 0f, 0f, Color.parseColor("#00BCD4"))
        canvas.drawText("ZOMBIE LANE", cx, yPos, titlePaint)
        yPos += 60f * s * titleScale
        subtitlePaint.textSize = 40f * s
        subtitlePaint.setShadowLayer(14f * s, 0f, 0f, Color.parseColor("#006064"))
        canvas.drawText("SHOOTER", cx, yPos, subtitlePaint)
        subtitlePaint.clearShadowLayer()
        titlePaint.clearShadowLayer()

        yPos += 36f * s
        infoPaint.textSize = 26f * s
        infoPaint.setShadowLayer(5f * s, 0f, 1.5f * s, Color.BLACK)
        canvas.drawText("ARCADE · ENDLESS SECTORS", cx, yPos, infoPaint)
        infoPaint.clearShadowLayer()

        // Ship center
        yPos += 36f * s
        val shipTarget = (safeArea.width() * 0.22f).coerceIn(140f * s, 220f * s)
        val bmp = playerAssets.get(shooterManager.equipped)
        val bob = sin(frameCount * 0.055).toFloat() * 10f * s
        val sway = sin(frameCount * 0.038).toFloat() * 6f * s
        val shipCx = cx + sway
        val shipCy = yPos + shipTarget / 2f + bob
        val scale = shipTarget / bmp.width.toFloat()

        shipGlowPaint.color = Color.argb(
            (60 + (sin(frameCount * 0.09) * 40).toInt()).coerceIn(40, 110),
            0, 229, 255
        )
        shipGlowInnerPaint.color = Color.argb(90, 100, 200, 255)
        canvas.drawCircle(shipCx, shipCy, shipTarget * 0.55f, shipGlowPaint)
        canvas.drawCircle(shipCx, shipCy, shipTarget * 0.38f, shipGlowInnerPaint)

        canvas.save()
        canvas.translate(shipCx, shipCy)
        canvas.scale(scale, scale)
        canvas.drawBitmap(bmp, -bmp.width / 2f, -bmp.height / 2f, null)
        canvas.restore()

        yPos += shipTarget + 30f * s
        equippedLabelPaint.textSize = 26f * s
        equippedLabelPaint.setShadowLayer(5f * s, 0f, 1.5f * s, Color.BLACK)
        canvas.drawText("LOADOUT", cx, yPos, equippedLabelPaint)
        equippedLabelPaint.clearShadowLayer()
        yPos += 38f * s
        val equipped = shooterManager.getEquipped()
        equippedNamePaint.textSize = 36f * s
        equippedNamePaint.color = equipped.bulletColor
        equippedNamePaint.setShadowLayer(6f, 0f, 0f, equipped.bulletColor and 0x88FFFFFF.toInt())
        canvas.drawText(equipped.name, cx, yPos, equippedNamePaint)
        equippedNamePaint.clearShadowLayer()

        // Chest strip (icons + type name + status)
        yPos += 36f * s
        val chipGap = 16f * s
        val rowInnerW = safeArea.width() - padH * 2f
        val chipW = (rowInnerW - 3f * chipGap) / 4f
        val chipH = (safeArea.width() * 0.245f).coerceIn(132f * s, 200f * s)
        val rowLeft = safeArea.left + padH
        val streakBelowChip = 38f * s
        for (i in 0 until ChestManager.MAX_SLOTS) {
            val left = rowLeft + i * (chipW + chipGap)
            tmpRect.set(left, yPos, left + chipW, yPos + chipH)
            val slot = chestSlots.getOrNull(i)
            drawChestChip(canvas, tmpRect, slot, nowMs, s, i, menuUi)
        }
        val streakBaseline = yPos + chipH + streakBelowChip
        menuSubPaint.textSize = 26f * s
        menuSubPaint.setShadowLayer(5f * s, 0f, 1.5f * s, Color.BLACK)
        canvas.drawText("Streak $streakDays  ·  tap chests", cx, streakBaseline, menuSubPaint)
        menuSubPaint.clearShadowLayer()

        chestsNavRect = RectF(
            rowLeft,
            yPos,
            rowLeft + 4 * chipW + 3 * chipGap,
            streakBaseline + 22f * s
        )

        yPos += chipH + streakBelowChip + 52f * s
        if (chestToast != null) {
            toastPaint.textSize = 20f * s
            canvas.drawText(chestToast, cx, yPos, toastPaint)
            yPos += 36f * s
        }

        yPos += 22f * s
        val playW = safeArea.width() * 0.88f
        val bmpAr = menuUi.playButton.height.toFloat() / menuUi.playButton.width.coerceAtLeast(1)
        var playH = playW * bmpAr
        // Fill vertical space: wide art must not be squashed to a tiny strip (old cap caused micro-button).
        playH = playH.coerceIn(safeArea.height() * 0.075f, safeArea.height() * 0.26f)
        val pulse = 1f + sin(frameCount * 0.065).toFloat() * 0.04f
        val pw = playW * pulse
        val ph = playH * pulse
        tmpRect.set(cx - pw / 2f, yPos + (playH - ph) / 2f, cx + pw / 2f, yPos + (playH + ph) / 2f)
        drawBitmapFit(canvas, menuUi.playButton, tmpRect)
        // Hit target matches visible art only (container was huge; empty margin was starting the game).
        playBtnRect.set(tmpRect)

        yPos += playH + 28f * s
        val weaponW = (safeArea.width() * 0.78f).coerceAtMost(playW * 1.08f)
        val weaponH = menuUi.weaponsButton.height * (weaponW / menuUi.weaponsButton.width.coerceAtLeast(1))
        shopBtnRect.set(cx - weaponW / 2f, yPos, cx + weaponW / 2f, yPos + weaponH)
        drawBitmapFit(canvas, menuUi.weaponsButton, shopBtnRect)

        yPos += weaponH + 20f * s
        val settingsSize = (96f * s).coerceIn(76f, 128f)
        settingsBtnRect.set(cx - settingsSize / 2f, yPos, cx + settingsSize / 2f, yPos + settingsSize)
        drawBitmapFit(canvas, menuUi.settingsIcon, settingsBtnRect)

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

    private fun drawChestChip(
        canvas: Canvas,
        r: RectF,
        slot: ChestSlot?,
        nowMs: Long,
        s: Float,
        index: Int,
        menuUi: MenuUiAssets
    ) {
        val pulse = sin(frameCount * 0.1f + index * 0.7f).toFloat() * 0.5f + 0.5f
        chestBodyPaint.color = Color.parseColor("#12121C")
        chestStrokePaint.color = Color.parseColor("#3D3D5C")
        chestStrokePaint.strokeWidth = 2f * s

        val textBand = 44f * s
        val inner = 4f * s
        val iconTop = r.top + inner
        val iconBottom = r.bottom - textBand
        val iconH = (iconBottom - iconTop).coerceAtLeast(28f)

        if (slot == null) {
            canvas.drawRoundRect(r, 12f * s, 12f * s, chestBodyPaint)
            canvas.drawRoundRect(r, 12f * s, 12f * s, chestStrokePaint)
            tmpRect2.set(r.left + inner, iconTop, r.right - inner, iconTop + iconH)
            bitmapPaint.alpha = 85
            drawBitmapFit(canvas, menuUi.chest(ChestType.COMMON), tmpRect2)
            bitmapPaint.alpha = 255
            chestNamePaint.textSize = (12f * s).coerceIn(10f, 16f)
            chestNamePaint.color = Color.parseColor("#616161")
            canvas.drawText("EMPTY", r.centerX(), r.bottom - 28f * s, chestNamePaint)
            chestTimerPaint.textSize = (r.height() * 0.13f).coerceIn(18f * s, 26f * s)
            chestTimerPaint.color = Color.parseColor("#616161")
            canvas.drawText("—", r.centerX(), r.bottom - 9f * s, chestTimerPaint)
            return
        }

        val ready = slot.isReady(nowMs)
        if (ready) {
            chestReadyGlowPaint.color = Color.argb((110 + pulse * 145).toInt(), 255, 230, 90)
            chestReadyGlowPaint.strokeWidth = (3.5f + pulse * 2f) * s
            tmpRect2.set(r)
            tmpRect2.inset(-5f * s, -5f * s)
            canvas.drawRoundRect(tmpRect2, 16f * s, 16f * s, chestReadyGlowPaint)
        }
        canvas.drawRoundRect(r, 12f * s, 12f * s, chestBodyPaint)
        canvas.drawRoundRect(r, 12f * s, 12f * s, chestStrokePaint)

        tmpRect2.set(r.left + inner, iconTop, r.right - inner, iconTop + iconH)
        drawBitmapFit(canvas, menuUi.chest(slot.type), tmpRect2)

        chestNamePaint.textSize = (12f * s).coerceIn(10f, 16f)
        chestNamePaint.color = when (slot.type) {
            ChestType.COMMON -> Color.parseColor("#90A4AE")
            ChestType.RARE -> Color.parseColor("#64B5F6")
            ChestType.EPIC -> Color.parseColor("#CE93D8")
            ChestType.SUPER -> Color.parseColor("#FFD54F")
        }
        canvas.drawText(slot.type.displayName.uppercase(), r.centerX(), r.bottom - 28f * s, chestNamePaint)

        chestTimerPaint.textSize = (r.height() * 0.13f).coerceIn(18f * s, 26f * s)
        chestTimerPaint.color = if (ready) Color.parseColor("#FFF59D") else Color.parseColor("#B0BEC5")
        val label = if (ready) "OPEN" else formatRemaining(slot.remainingMs(nowMs))
        canvas.drawText(label, r.centerX(), r.bottom - 9f * s, chestTimerPaint)
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
