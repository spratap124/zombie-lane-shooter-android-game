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

    private val dimOverlayPaint = Paint().apply {
        color = Color.parseColor("#CC000000")
        style = Paint.Style.FILL
    }

    private val popCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A2E")
        style = Paint.Style.FILL
    }

    private val popOkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00C853")
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

        // Chest strip (icons)
        yPos += 36f * s
        val chipW = (safeArea.width() - padH * 2f - 18f * s) / 4f
        val chipH = (safeArea.width() * 0.22f).coerceIn(118f * s, 182f * s)
        val gap = 8f * s
        val rowLeft = safeArea.left + padH
        chestsNavRect = RectF(rowLeft, yPos, rowLeft + 4 * chipW + 3 * gap, yPos + chipH + 32f * s)
        for (i in 0 until ChestManager.MAX_SLOTS) {
            val left = rowLeft + i * (chipW + gap)
            tmpRect.set(left, yPos, left + chipW, yPos + chipH)
            val slot = chestSlots.getOrNull(i)
            drawChestChip(canvas, tmpRect, slot, nowMs, s, i, menuUi)
        }
        menuSubPaint.textSize = 26f * s
        menuSubPaint.setShadowLayer(5f * s, 0f, 1.5f * s, Color.BLACK)
        canvas.drawText("Streak $streakDays  ·  tap chests", cx, yPos + chipH + 26f * s, menuSubPaint)
        menuSubPaint.clearShadowLayer()

        yPos += chipH + 40f * s
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
        playBtnRect = RectF(cx - playW / 2f, yPos, cx + playW / 2f, yPos + playH)
        val pulse = 1f + sin(frameCount * 0.065).toFloat() * 0.04f
        val pw = playW * pulse
        val ph = playH * pulse
        tmpRect.set(cx - pw / 2f, yPos + (playH - ph) / 2f, cx + pw / 2f, yPos + (playH + ph) / 2f)
        drawBitmapFit(canvas, menuUi.playButton, tmpRect)

        yPos += playH + 36f * s
        val subGap = 14f * s
        val subW = (safeArea.width() - padH * 2f - subGap) / 2f
        val subH = (safeArea.width() * 0.11f).coerceIn(96f * s, 132f * s)
        shopBtnRect.set(safeArea.left + padH, yPos, safeArea.left + padH + subW, yPos + subH)
        settingsBtnRect.set(shopBtnRect.right + subGap, yPos, safeArea.right - padH, yPos + subH)

        val shopPulse = 1f + sin(frameCount * 0.055 + 1f).toFloat() * 0.02f
        tmpRect2.set(shopBtnRect)
        tmpRect2.inset(-2f * s * shopPulse, -2f * s * shopPulse)
        shopGlowPaint.color = Color.argb(100, 255, 145, 0)
        canvas.drawRoundRect(tmpRect2, 18f * s, 18f * s, shopGlowPaint)
        canvas.drawRoundRect(shopBtnRect, 18f * s, 18f * s, shopBtnPaint)
        canvas.save()
        canvas.translate(shopBtnRect.left + 32f * s, shopBtnRect.centerY())
        canvas.scale(1.28f, 1.28f)
        canvas.drawPath(weaponPath, iconPaint)
        canvas.restore()
        btnTextPaint.textSize = 36f * s
        canvas.drawText("WEAPONS", shopBtnRect.centerX() + 22f * s, shopBtnRect.centerY() + 13f * s, btnTextPaint)

        val setPulse = 1f + sin(frameCount * 0.055 + 2f).toFloat() * 0.02f
        tmpRect2.set(settingsBtnRect)
        tmpRect2.inset(-2f * s * setPulse, -2f * s * setPulse)
        settingsGlowPaint.color = Color.argb(90, 150, 180, 255)
        canvas.drawRoundRect(tmpRect2, 18f * s, 18f * s, settingsGlowPaint)
        canvas.drawRoundRect(settingsBtnRect, 18f * s, 18f * s, settingsBtnPaint)
        canvas.save()
        canvas.translate(settingsBtnRect.left + 32f * s, settingsBtnRect.centerY())
        canvas.scale(1.28f, 1.28f)
        canvas.drawPath(gearPath, iconPaint)
        canvas.restore()
        btnTextPaint.textSize = 36f * s
        canvas.drawText("SETTINGS", settingsBtnRect.centerX() + 20f * s, settingsBtnRect.centerY() + 13f * s, btnTextPaint)

        infoPaint.textSize = 24f * s
        canvas.drawText("v1.0", cx, safeArea.bottom - 14f * s, infoPaint)

        streakOkRect.setEmpty()
        if (streakPopupTitle != null && streakPopupMessage != null) {
            canvas.drawRect(0f, 0f, w, h, dimOverlayPaint)
            val pw = safeArea.width() * 0.88f
            val ph = 220f * s
            val px = cx - pw / 2f
            val py = safeArea.top + safeArea.height() * 0.28f
            tmpRect.set(px, py, px + pw, py + ph)
            canvas.drawRoundRect(tmpRect, 20f * s, 20f * s, popCardPaint)
            titlePaint.textSize = 32f * s
            titlePaint.color = Color.parseColor("#FFD600")
            titlePaint.setShadowLayer(8f, 0f, 0f, Color.BLACK)
            canvas.drawText(streakPopupTitle, cx, py + 48f * s, titlePaint)
            titlePaint.clearShadowLayer()
            streakBodyPaint.textSize = 24f * s
            val words = streakPopupMessage.chunked(36)
            var ty = py + 92f * s
            for (line in words.take(4)) {
                canvas.drawText(line, cx, ty, streakBodyPaint)
                ty += 30f * s
            }
            val okW = pw * 0.45f
            val okH = 48f * s
            streakOkRect = RectF(cx - okW / 2f, py + ph - okH - 20f * s, cx + okW / 2f, py + ph - 20f * s)
            canvas.drawRoundRect(streakOkRect, 12f * s, 12f * s, popOkPaint)
            btnTextPaint.textSize = 26f * s
            canvas.drawText("OK", cx, streakOkRect.centerY() + 9f * s, btnTextPaint)
        }
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

        if (slot == null) {
            canvas.drawRoundRect(r, 12f * s, 12f * s, chestBodyPaint)
            canvas.drawRoundRect(r, 12f * s, 12f * s, chestStrokePaint)
            val iconH = r.height() - 30f * s
            tmpRect2.set(r.left + 3f * s, r.top + 3f * s, r.right - 3f * s, r.top + 3f * s + iconH)
            bitmapPaint.alpha = 85
            drawBitmapFit(canvas, menuUi.chest(ChestType.COMMON), tmpRect2)
            bitmapPaint.alpha = 255
            chestTimerPaint.textSize = (r.height() * 0.16f).coerceIn(20f * s, 28f * s)
            chestTimerPaint.color = Color.parseColor("#616161")
            canvas.drawText("—", r.centerX(), r.bottom - 10f * s, chestTimerPaint)
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

        val iconH = r.height() - 30f * s
        tmpRect2.set(r.left + 2f * s, r.top + 2f * s, r.right - 2f * s, r.top + 2f * s + iconH)
        drawBitmapFit(canvas, menuUi.chest(slot.type), tmpRect2)

        chestTimerPaint.textSize = (r.height() * 0.17f).coerceIn(22f * s, 30f * s)
        chestTimerPaint.color = if (ready) Color.parseColor("#FFF59D") else Color.parseColor("#B0BEC5")
        val label = if (ready) "OPEN" else formatRemaining(slot.remainingMs(nowMs))
        canvas.drawText(label, r.centerX(), r.bottom - 10f * s, chestTimerPaint)
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
