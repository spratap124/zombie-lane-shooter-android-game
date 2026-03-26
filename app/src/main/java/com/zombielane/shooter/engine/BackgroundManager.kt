package com.zombielane.shooter.engine

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.sin
import kotlin.random.Random

class BackgroundManager {

    private data class ParallaxDot(val baseX: Float, val baseY: Float, val size: Float, val speed: Float, val layer: Int)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private var stars = emptyList<Triple<Float, Float, Float>>()
    private var parallaxDots = emptyList<ParallaxDot>()
    private var screenW = 0
    private var screenH = 0

    fun init(w: Int, h: Int) {
        screenW = w
        screenH = h
        val wf = w.toFloat()
        val hf = h.toFloat()

        stars = List(80) {
            Triple(Random.nextFloat() * wf, Random.nextFloat() * hf, 1f + Random.nextFloat() * 2.5f)
        }
        parallaxDots = List(40) {
            ParallaxDot(
                baseX = Random.nextFloat() * wf,
                baseY = Random.nextFloat() * hf,
                size = 2f + Random.nextFloat() * 5f,
                speed = 0.3f + Random.nextFloat() * 0.7f,
                layer = Random.nextInt(3)
            )
        }
    }

    fun draw(canvas: Canvas, bgType: BackgroundType) {
        val palette = getPalette(bgType)
        val t = System.currentTimeMillis()
        val w = screenW.toFloat()
        val h = screenH.toFloat()

        canvas.drawColor(palette.sky)

        drawStars(canvas, palette, t)
        drawParallaxLayer(canvas, palette, t, w, h)
        drawThemeElements(canvas, bgType, palette, t, w, h)
    }

    // ── Palettes ────────────────────────────────────────────

    private data class Palette(
        val sky: Int,
        val starColor: Int,
        val starAlpha: Float,
        val layer0: Int,
        val layer1: Int,
        val layer2: Int,
        val accent: Int
    )

    private fun getPalette(type: BackgroundType) = when (type) {
        BackgroundType.SPACE -> Palette(
            sky = 0xFF0B0B1E.toInt(),
            starColor = Color.WHITE, starAlpha = 1f,
            layer0 = Color.parseColor("#112244"),
            layer1 = Color.parseColor("#0D1933"),
            layer2 = Color.parseColor("#081122"),
            accent = Color.parseColor("#3F51B5")
        )
        BackgroundType.CITY -> Palette(
            sky = 0xFF0C1024.toInt(),
            starColor = Color.parseColor("#BBDEFB"), starAlpha = 0.6f,
            layer0 = Color.parseColor("#1A237E"),
            layer1 = Color.parseColor("#0D1657"),
            layer2 = Color.parseColor("#1E88E5"),
            accent = Color.parseColor("#FFC107")
        )
        BackgroundType.LAVA -> Palette(
            sky = 0xFF1A0800.toInt(),
            starColor = Color.parseColor("#FFAB91"), starAlpha = 0.35f,
            layer0 = Color.parseColor("#BF360C"),
            layer1 = Color.parseColor("#E65100"),
            layer2 = Color.parseColor("#FF6D00"),
            accent = Color.parseColor("#FFD600")
        )
        BackgroundType.FOREST -> Palette(
            sky = 0xFF071A0A.toInt(),
            starColor = Color.parseColor("#C8E6C9"), starAlpha = 0.5f,
            layer0 = Color.parseColor("#1B5E20"),
            layer1 = Color.parseColor("#2E7D32"),
            layer2 = Color.parseColor("#388E3C"),
            accent = Color.parseColor("#76FF03")
        )
        BackgroundType.ICE -> Palette(
            sky = 0xFF0A1628.toInt(),
            starColor = Color.parseColor("#E1F5FE"), starAlpha = 0.9f,
            layer0 = Color.parseColor("#0277BD"),
            layer1 = Color.parseColor("#0288D1"),
            layer2 = Color.parseColor("#4FC3F7"),
            accent = Color.parseColor("#80DEEA")
        )
    }

    // ── Stars ───────────────────────────────────────────────

    private fun drawStars(canvas: Canvas, palette: Palette, t: Long) {
        starPaint.color = palette.starColor
        for ((sx, sy, sr) in stars) {
            val twinkle = (sin(t * 0.003 + sx.toDouble()) * 0.3 + 0.7).toFloat()
            starPaint.alpha = ((100 + (155 * twinkle).toInt()) * palette.starAlpha).toInt().coerceIn(0, 255)
            canvas.drawCircle(sx, sy, sr, starPaint)
        }
    }

    // ── Parallax dots ───────────────────────────────────────

    private fun drawParallaxLayer(canvas: Canvas, palette: Palette, t: Long, w: Float, h: Float) {
        val colors = intArrayOf(palette.layer0, palette.layer1, palette.layer2)
        val speeds = floatArrayOf(0.008f, 0.015f, 0.025f)
        val alphas = intArrayOf(25, 18, 12)

        for (dot in parallaxDots) {
            paint.color = colors[dot.layer]
            paint.alpha = alphas[dot.layer]
            val yOff = (t * speeds[dot.layer] * dot.speed) % h
            val dy = (dot.baseY + yOff) % h
            canvas.drawCircle(dot.baseX, dy, (dot.size * (dot.layer + 1)).coerceAtMost(12f), paint)
        }
    }

    // ── Theme-specific elements ─────────────────────────────

    private fun drawThemeElements(canvas: Canvas, type: BackgroundType, palette: Palette, t: Long, w: Float, h: Float) {
        when (type) {
            BackgroundType.SPACE -> drawSpaceElements(canvas, palette, t, w, h)
            BackgroundType.CITY -> drawCityElements(canvas, palette, t, w, h)
            BackgroundType.LAVA -> drawLavaElements(canvas, palette, t, w, h)
            BackgroundType.FOREST -> drawForestElements(canvas, palette, t, w, h)
            BackgroundType.ICE -> drawIceElements(canvas, palette, t, w, h)
        }
    }

    private fun drawSpaceElements(canvas: Canvas, palette: Palette, t: Long, w: Float, h: Float) {
        // empty — clean starfield only
    }

    private fun drawCityElements(canvas: Canvas, palette: Palette, t: Long, w: Float, h: Float) {
        val skylineY = h * 0.82f
        val buildingCount = 12

        for (i in 0 until buildingCount) {
            val bx = w * (i.toFloat() / buildingCount)
            val bw = w / buildingCount * 0.85f
            val bh = 60f + (i * 37 % 120)
            paint.color = palette.layer0
            paint.alpha = 40
            canvas.drawRect(bx, skylineY - bh, bx + bw, skylineY, paint)

            paint.color = palette.accent
            paint.alpha = 50
            val windowRows = (bh / 18).toInt()
            val windowCols = (bw / 14).toInt().coerceAtLeast(1)
            for (r in 0 until windowRows) {
                for (c in 0 until windowCols) {
                    val lit = ((i + r + c + (t / 800).toInt()) % 3 != 0)
                    if (lit) {
                        val wx = bx + 4f + c * (bw - 8f) / windowCols
                        val wy = skylineY - bh + 6f + r * 18f
                        canvas.drawRect(wx, wy, wx + 5f, wy + 7f, paint)
                    }
                }
            }
        }

        paint.color = palette.layer1
        paint.alpha = 25
        val farSkylineY = h * 0.78f
        for (i in 0..7) {
            val bx = w * (i / 8f) + 10f
            val bw = w / 8f * 0.7f
            val bh = 30f + (i * 23 % 60)
            canvas.drawRect(bx, farSkylineY - bh, bx + bw, farSkylineY, paint)
        }
    }

    private fun drawLavaElements(canvas: Canvas, palette: Palette, t: Long, w: Float, h: Float) {
        paint.color = palette.layer1
        paint.alpha = 30
        val lavaY = h * 0.90f
        canvas.drawRect(0f, lavaY, w, h, paint)

        paint.color = palette.accent
        paint.alpha = 18
        for (i in 0..5) {
            val bx = w * (i / 6f + 0.05f)
            val pulse = sin(t * 0.002 + i * 1.5).toFloat()
            val by = lavaY - 10f + pulse * 8f
            canvas.drawCircle(bx, by, 35f + i * 8f, paint)
        }

        paint.color = palette.layer2
        paint.alpha = 20
        for (i in 0..9) {
            val seed = (i * 7919).toLong()
            val ex = ((seed % w.toLong()) + w.toLong()) % w.toLong()
            val baseY = h - ((t * 0.03f + seed) % h)
            val ey = if (baseY < 0) baseY + h else baseY
            val size = 2f + (seed % 5)
            canvas.drawCircle(ex.toFloat(), ey, size.toFloat(), paint)
        }

        paint.color = palette.layer0
        paint.alpha = 22
        for (i in 0..3) {
            val rx = w * (0.05f + i * 0.28f)
            val ry = h * (0.4f + i * 0.12f)
            val rw = 40f + i * 12f
            val rh = 20f + i * 6f
            canvas.drawRoundRect(rx, ry, rx + rw, ry + rh, 8f, 8f, paint)
        }
    }

    private fun drawForestElements(canvas: Canvas, palette: Palette, t: Long, w: Float, h: Float) {
        val treeLineY = h * 0.85f

        paint.color = palette.layer0
        paint.alpha = 35
        for (i in 0..14) {
            val tx = w * (i / 15f) + sin(i.toFloat()).toFloat() * 12f
            val treeH = 50f + (i * 31 % 70)
            val trunkW = 6f
            canvas.drawRect(tx + 12f, treeLineY - treeH, tx + 12f + trunkW, treeLineY, paint)

            paint.color = palette.layer1
            paint.alpha = 30
            val path = Path().apply {
                moveTo(tx + 15f, treeLineY - treeH)
                lineTo(tx - 5f, treeLineY - treeH * 0.35f)
                lineTo(tx + 35f, treeLineY - treeH * 0.35f)
                close()
            }
            canvas.drawPath(path, paint)
            paint.color = palette.layer0
            paint.alpha = 35
        }

        paint.color = palette.layer2
        paint.alpha = 20
        for (i in 0..8) {
            val fx = w * (i / 9f)
            val farH = 25f + (i * 19 % 40)
            val path = Path().apply {
                moveTo(fx + 8f, treeLineY - 70f - farH)
                lineTo(fx - 8f, treeLineY - 70f)
                lineTo(fx + 24f, treeLineY - 70f)
                close()
            }
            canvas.drawPath(path, paint)
        }

        paint.color = palette.accent
        paint.alpha = 8
        for (i in 0..3) {
            val fy = sin(t * 0.0004 + i * 1.2).toFloat() * 6f
            val cy = h * (0.3f + i * 0.15f) + fy
            canvas.drawCircle(w * (0.2f + i * 0.2f), cy, 50f + i * 12f, paint)
        }
    }

    private fun drawIceElements(canvas: Canvas, palette: Palette, t: Long, w: Float, h: Float) {
        paint.color = palette.layer0
        paint.alpha = 20
        val iceLineY = h * 0.88f
        for (i in 0..7) {
            val ix = w * (i / 8f)
            val peakH = 30f + (i * 43 % 60)
            val path = Path().apply {
                moveTo(ix, iceLineY)
                lineTo(ix + w / 16f, iceLineY - peakH)
                lineTo(ix + w / 8f, iceLineY)
                close()
            }
            canvas.drawPath(path, paint)
        }

        paint.color = palette.accent
        paint.alpha = 10
        for (i in 0..5) {
            val shimmer = sin(t * 0.003 + i * 0.8).toFloat() * 0.5f + 0.5f
            paint.alpha = (10 * shimmer).toInt().coerceIn(0, 255)
            val cx = w * (0.1f + i * 0.16f)
            val cy = h * (0.25f + i * 0.1f)
            canvas.drawCircle(cx, cy, 40f + i * 10f, paint)
        }

        paint.color = palette.layer2
        paint.alpha = 14
        for (i in 0..11) {
            val seed = (i * 6271).toLong()
            val sx = ((seed % w.toLong()) + w.toLong()) % w.toLong()
            val drift = sin(t * 0.001 + seed * 0.01).toFloat() * 15f
            val baseY = ((t * 0.015f + seed) % h)
            canvas.drawCircle(sx.toFloat() + drift, baseY, 2f + (seed % 3), paint)
        }
    }
}
