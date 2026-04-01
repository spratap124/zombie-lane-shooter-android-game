package com.zombielane.shooter.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.zombielane.shooter.data.ChestType
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * Full-screen procedural reward backgrounds per chest rarity, drawn to a cached bitmap.
 * At runtime the bitmap is drawn with a slow scale pulse and slight rotation; a dark vignette
 * keeps the center bright for UI readability.
 */
class ChestRewardBackgroundSystem {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
        isDither = true
    }
    private val drawMatrix = Matrix()

    private var cached: Bitmap? = null
    private var cacheW = -1
    private var cacheH = -1
    private var cacheVisual: RewardBackgroundVisual? = null

    enum class RewardBackgroundVisual {
        GOLD_BURST,
        BLUE_CRYSTAL,
        PURPLE_ENERGY,
        GALAXY_BURST
    }

    fun visualForTier(tier: ChestType): RewardBackgroundVisual = when (tier) {
        ChestType.COMMON -> RewardBackgroundVisual.GOLD_BURST
        ChestType.RARE -> RewardBackgroundVisual.BLUE_CRYSTAL
        ChestType.EPIC -> RewardBackgroundVisual.PURPLE_ENERGY
        ChestType.SUPER -> RewardBackgroundVisual.GALAXY_BURST
    }

    /**
     * Draws animated full-screen background + vignette. Call before other reward UI.
     */
    fun draw(
        canvas: Canvas,
        w: Float,
        h: Float,
        nowMs: Long,
        visual: RewardBackgroundVisual
    ) {
        val iw = w.toInt().coerceAtLeast(1)
        val ih = h.toInt().coerceAtLeast(1)
        val bmp = ensureBitmap(iw, ih, visual) ?: return

        val cx = w / 2f
        val cy = h / 2f
        val t = nowMs * 0.001f
        val scale = 1.045f + 0.028f * sin(t * 0.85f).toFloat()
        val rotDeg = 1.35f * sin(t * 0.55f).toFloat()

        val hw = bmp.width / 2f
        val hh = bmp.height / 2f
        drawMatrix.reset()
        drawMatrix.postTranslate(-hw, -hh)
        drawMatrix.postScale(scale, scale)
        drawMatrix.postRotate(rotDeg)
        drawMatrix.postTranslate(cx, cy)

        canvas.drawBitmap(bmp, drawMatrix, bitmapPaint)
        drawVignette(canvas, w, h, cx, h * 0.36f)
    }

    fun recycle() {
        cached?.recycle()
        cached = null
        cacheW = -1
        cacheH = -1
        cacheVisual = null
    }

    private fun ensureBitmap(w: Int, h: Int, visual: RewardBackgroundVisual): Bitmap? {
        if (cached != null && cacheW == w && cacheH == h && cacheVisual == visual) {
            return cached
        }
        cached?.recycle()
        cacheW = w
        cacheH = h
        cacheVisual = visual
        val bmp = try {
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        } catch (_: OutOfMemoryError) {
            return null
        }
        val c = Canvas(bmp)
        when (visual) {
            RewardBackgroundVisual.GOLD_BURST -> drawGoldBurst(c, w.toFloat(), h.toFloat())
            RewardBackgroundVisual.BLUE_CRYSTAL -> drawBlueCrystal(c, w.toFloat(), h.toFloat())
            RewardBackgroundVisual.PURPLE_ENERGY -> drawPurpleEnergy(c, w.toFloat(), h.toFloat())
            RewardBackgroundVisual.GALAXY_BURST -> drawGalaxyBurst(c, w.toFloat(), h.toFloat())
        }
        cached = bmp
        return bmp
    }

    /** Brightest at [rcx],[rcy] — upper-center band where titles and chest sit. */
    private fun hotspot(w: Float, h: Float): Pair<Float, Float> = w / 2f to h * 0.36f

    private fun drawGoldBurst(c: Canvas, w: Float, h: Float) {
        val (rcx, rcy) = hotspot(w, h)
        val rMax = hypot(w.toDouble(), h.toDouble()).toFloat() * 0.95f
        fillPaint.shader = RadialGradient(
            rcx, rcy, rMax,
            intArrayOf(
                Color.parseColor("#FFFFF59D"),
                Color.parseColor("#FFFFE082"),
                Color.parseColor("#FFFFB74D"),
                Color.parseColor("#FF8D6E63"),
                Color.parseColor("#FF1A0F08")
            ),
            floatArrayOf(0f, 0.12f, 0.35f, 0.62f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, w, h, fillPaint)
        fillPaint.shader = null

        linePaint.strokeWidth = w * 0.006f
        val n = 22
        val rayLen = rMax * 0.92f
        c.save()
        c.translate(rcx, rcy)
        val step = 360f / n
        var i = 0
        while (i < n) {
            c.rotate(step)
            linePaint.color = Color.argb(90, 255, 236, 179)
            c.drawLine(0f, 0f, 0f, -rayLen * 0.55f, linePaint)
            linePaint.color = Color.argb(45, 255, 193, 7)
            c.drawLine(0f, 0f, 0f, -rayLen, linePaint)
            i++
        }
        c.restore()

        fillPaint.shader = RadialGradient(
            rcx, rcy, rMax * 0.28f,
            intArrayOf(Color.argb(120, 255, 255, 255), Color.argb(0, 255, 255, 255)),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, w, h, fillPaint)
        fillPaint.shader = null
    }

    private fun drawBlueCrystal(c: Canvas, w: Float, h: Float) {
        val (rcx, rcy) = hotspot(w, h)
        val rMax = hypot(w.toDouble(), h.toDouble()).toFloat() * 0.95f
        fillPaint.shader = RadialGradient(
            rcx, rcy, rMax,
            intArrayOf(
                Color.parseColor("#FFE1F5FE"),
                Color.parseColor("#FF4FC3F7"),
                Color.parseColor("#FF0277BD"),
                Color.parseColor("#FF01579B"),
                Color.parseColor("#FF061018")
            ),
            floatArrayOf(0f, 0.14f, 0.38f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, w, h, fillPaint)
        fillPaint.shader = null

        // Facet shards
        linePaint.strokeWidth = w * 0.007f
        val facets = 10
        c.save()
        c.translate(rcx, rcy)
        val step = 360f / facets
        var f = 0
        while (f < facets) {
            c.rotate(step)
            linePaint.color = Color.argb(200, 224, 247, 255)
            c.drawLine(0f, 0f, 0f, -rMax * 0.72f, linePaint)
            linePaint.color = Color.argb(70, 255, 255, 255)
            c.drawLine(0f, -rMax * 0.15f, rMax * 0.12f, -rMax * 0.45f, linePaint)
            c.drawLine(0f, -rMax * 0.15f, -rMax * 0.12f, -rMax * 0.45f, linePaint)
            f++
        }
        c.restore()

        fillPaint.shader = RadialGradient(
            rcx, rcy, rMax * 0.22f,
            intArrayOf(Color.argb(100, 255, 255, 255), Color.argb(0, 255, 255, 255)),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, w, h, fillPaint)
        fillPaint.shader = null
    }

    private fun drawPurpleEnergy(c: Canvas, w: Float, h: Float) {
        val (rcx, rcy) = hotspot(w, h)
        val rMax = hypot(w.toDouble(), h.toDouble()).toFloat() * 0.95f
        fillPaint.shader = RadialGradient(
            rcx, rcy, rMax,
            intArrayOf(
                Color.parseColor("#FFF3E5F5"),
                Color.parseColor("#FFE1BEE7"),
                Color.parseColor("#FFAB47BC"),
                Color.parseColor("#FF4A148C"),
                Color.parseColor("#FF12051C")
            ),
            floatArrayOf(0f, 0.1f, 0.32f, 0.58f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, w, h, fillPaint)
        fillPaint.shader = null

        linePaint.strokeWidth = w * 0.0055f
        val bolts = 28
        c.save()
        c.translate(rcx, rcy)
        val step = 360f / bolts
        var b = 0
        while (b < bolts) {
            c.rotate(step)
            val flicker = if (b % 3 == 0) 200 else 110
            linePaint.color = Color.argb(flicker, 233, 30, 99)
            c.drawLine(0f, 0f, 0f, -rMax * 0.88f, linePaint)
            linePaint.color = Color.argb(80, 186, 104, 200)
            c.drawLine(0f, 0f, 0f, -rMax * 0.5f, linePaint)
            b++
        }
        c.restore()

        fillPaint.shader = RadialGradient(
            rcx, rcy, rMax * 0.2f,
            intArrayOf(Color.argb(130, 255, 255, 255), Color.argb(0, 255, 255, 255)),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, w, h, fillPaint)
        fillPaint.shader = null
    }

    private fun drawGalaxyBurst(c: Canvas, w: Float, h: Float) {
        val (rcx, rcy) = hotspot(w, h)
        val rMax = hypot(w.toDouble(), h.toDouble()).toFloat() * 0.95f
        fillPaint.color = Color.parseColor("#FF03030A")
        c.drawRect(0f, 0f, w, h, fillPaint)

        // Nebula clouds (offset radials)
        val clouds = listOf(
            Triple(w * 0.42f, h * 0.32f, Color.argb(85, 99, 102, 241)),
            Triple(w * 0.58f, h * 0.38f, Color.argb(75, 171, 71, 188)),
            Triple(w * 0.5f, h * 0.44f, Color.argb(65, 236, 64, 122)),
            Triple(rcx, rcy, Color.argb(120, 79, 195, 247))
        )
        for ((ox, oy, col) in clouds) {
            fillPaint.shader = RadialGradient(
                ox, oy, rMax * 0.55f,
                intArrayOf(col, Color.argb(0, 0, 0, 0)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            c.drawRect(0f, 0f, w, h, fillPaint)
        }
        fillPaint.shader = null

        fillPaint.shader = RadialGradient(
            rcx, rcy, rMax * 0.45f,
            intArrayOf(
                Color.parseColor("#FFFFFDE7"),
                Color.parseColor("#FFB39DDB"),
                Color.parseColor("#FF4527A0"),
                Color.argb(0, 10, 5, 30)
            ),
            floatArrayOf(0f, 0.18f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, w, h, fillPaint)
        fillPaint.shader = null

        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val rng = Random((w.toLong() shl 32) xor h.toLong() xor 0xC0FFEE)
        val stars = ((w * h) / 9000f).coerceIn(60f, 160f).toInt()
        var s = 0
        while (s < stars) {
            val sx = rng.nextFloat() * w
            val sy = rng.nextFloat() * h
            val br = rng.nextInt(140, 256)
            starPaint.color = Color.argb(br, 255, 255, 255)
            val pr = 0.6f + rng.nextFloat() * 1.8f
            c.drawCircle(sx, sy, pr, starPaint)
            s++
        }

        linePaint.strokeWidth = w * 0.004f
        c.save()
        c.translate(rcx, rcy)
        val arms = 6
        val armStep = 360f / arms
        var a = 0
        while (a < arms) {
            c.rotate(armStep)
            linePaint.color = Color.argb(55, 179, 136, 255)
            c.drawLine(0f, 0f, 0f, -rMax * 0.75f, linePaint)
            a++
        }
        c.restore()

        fillPaint.shader = RadialGradient(
            rcx, rcy, rMax * 0.18f,
            intArrayOf(Color.argb(90, 255, 255, 255), Color.argb(0, 255, 255, 255)),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, w, h, fillPaint)
        fillPaint.shader = null
    }

    private fun drawVignette(canvas: Canvas, w: Float, h: Float, rcx: Float, rcy: Float) {
        val r = hypot(w.toDouble(), h.toDouble()).toFloat() * 0.72f
        vignettePaint.shader = RadialGradient(
            rcx, rcy, r,
            intArrayOf(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                Color.argb(100, 0, 0, 0),
                Color.argb(215, 0, 0, 0)
            ),
            floatArrayOf(0f, 0.35f, 0.72f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, vignettePaint)
        vignettePaint.shader = null

        // Extra edge darkening (corners)
        vignettePaint.shader = RadialGradient(
            0f, 0f, r * 0.55f,
            intArrayOf(Color.argb(90, 0, 0, 0), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w * 0.5f, h * 0.5f, vignettePaint)
        vignettePaint.shader = RadialGradient(
            w, 0f, r * 0.55f,
            intArrayOf(Color.argb(90, 0, 0, 0), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(w * 0.5f, 0f, w, h * 0.5f, vignettePaint)
        vignettePaint.shader = RadialGradient(
            0f, h, r * 0.55f,
            intArrayOf(Color.argb(90, 0, 0, 0), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, h * 0.5f, w * 0.5f, h, vignettePaint)
        vignettePaint.shader = RadialGradient(
            w, h, r * 0.55f,
            intArrayOf(Color.argb(90, 0, 0, 0), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(w * 0.5f, h * 0.5f, w, h, vignettePaint)
        vignettePaint.shader = null
    }
}
