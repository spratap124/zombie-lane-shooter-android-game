package com.zombielane.shooter.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class EnemyBullet(
    x: Float,
    y: Float,
    private val vx: Float = 0f,
    private val vy: Float = SPEED,
    private val bulletColor: Int = COLOR_NORMAL
) : GameObject(x - WIDTH / 2f, y, WIDTH, HEIGHT) {

    companion object {
        /** Large silhouette so shots read clearly during endless sectors. */
        const val WIDTH = 30f
        const val HEIGHT = 52f
        const val SPEED = 3f
        val COLOR_NORMAL = Color.parseColor("#FF1744")
        val COLOR_BOSS = Color.parseColor("#FFCA28")
        val COLOR_ZIGZAG = Color.parseColor("#EA80FC")
    }

    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val tailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
    }
    private val spokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(235, 12, 8, 20)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var flicker = 0

    override fun update(screenWidth: Int, screenHeight: Int, playfieldLeft: Float, playfieldRight: Float?) {
        x += vx
        y += vy
        flicker++
        if (y > screenHeight || y + height < 0 || x + width < 0 || x > screenWidth) active = false
    }

    override fun draw(canvas: Canvas) {
        val cx = x + width / 2f
        val cy = y + height / 2f
        val speed = hypot(vx.toDouble(), vy.toDouble()).toFloat().coerceAtLeast(0.001f)
        val tx = -vx / speed
        val ty = -vy / speed
        val pulse = 0.92f + 0.08f * sin(flicker * 0.38).toFloat()

        val pal = when (bulletColor) {
            COLOR_BOSS -> Palette.BOSS
            COLOR_ZIGZAG -> Palette.ZIGZAG
            else -> Palette.NORMAL
        }
        drawProjectile(canvas, cx, cy, tx, ty, pulse, pal)
    }

    private data class Palette(
        val haloInner: Int,
        val haloOuter: Int,
        val gradientColors: IntArray,
        val gradientStops: FloatArray,
        val tailStops: List<Triple<Float, Float, Int>>,
        val rimInner: Int,
        val rimOuter: Int,
        val spokeColor: Int,
        val emberRgb: Triple<Int, Int, Int>
    ) {
        companion object {
            val NORMAL = Palette(
                haloInner = Color.argb(100, 255, 80, 40),
                haloOuter = Color.argb(0, 255, 50, 0),
                gradientColors = intArrayOf(
                    Color.WHITE,
                    Color.parseColor("#FFF176"),
                    Color.parseColor("#FF9100"),
                    Color.parseColor("#FF3D00"),
                    Color.parseColor("#BF360C"),
                    Color.argb(0, 120, 10, 0)
                ),
                gradientStops = floatArrayOf(0f, 0.1f, 0.3f, 0.5f, 0.72f, 1f),
                tailStops = listOf(
                    Triple(1.05f, 0.5f, Color.argb(100, 80, 15, 5)),
                    Triple(0.88f, 0.44f, Color.argb(130, 180, 40, 10)),
                    Triple(0.7f, 0.38f, Color.argb(155, 255, 90, 25)),
                    Triple(0.52f, 0.32f, Color.argb(175, 255, 150, 50)),
                    Triple(0.34f, 0.26f, Color.argb(190, 255, 210, 100)),
                    Triple(0.16f, 0.2f, Color.argb(200, 255, 245, 200))
                ),
                rimInner = Color.parseColor("#80D8FF"),
                rimOuter = Color.parseColor("#00E5FF"),
                spokeColor = Color.argb(140, 120, 245, 255),
                emberRgb = Triple(255, 255, 200)
            )
            val BOSS = Palette(
                haloInner = Color.argb(110, 255, 220, 60),
                haloOuter = Color.argb(0, 255, 140, 0),
                gradientColors = intArrayOf(
                    Color.WHITE,
                    Color.parseColor("#FFF9C4"),
                    Color.parseColor("#FFEB3B"),
                    Color.parseColor("#FF9800"),
                    Color.parseColor("#E65100"),
                    Color.argb(0, 160, 60, 0)
                ),
                gradientStops = floatArrayOf(0f, 0.09f, 0.26f, 0.46f, 0.7f, 1f),
                tailStops = listOf(
                    Triple(1.05f, 0.5f, Color.argb(95, 100, 50, 5)),
                    Triple(0.88f, 0.44f, Color.argb(125, 200, 120, 15)),
                    Triple(0.7f, 0.38f, Color.argb(150, 255, 200, 40)),
                    Triple(0.52f, 0.32f, Color.argb(170, 255, 235, 90)),
                    Triple(0.34f, 0.26f, Color.argb(185, 255, 252, 160)),
                    Triple(0.16f, 0.2f, Color.argb(195, 255, 255, 230))
                ),
                rimInner = Color.parseColor("#EEFF41"),
                rimOuter = Color.parseColor("#FFFF8D"),
                spokeColor = Color.argb(150, 255, 255, 220),
                emberRgb = Triple(255, 255, 255)
            )
            val ZIGZAG = Palette(
                haloInner = Color.argb(95, 220, 100, 255),
                haloOuter = Color.argb(0, 100, 0, 180),
                gradientColors = intArrayOf(
                    Color.WHITE,
                    Color.parseColor("#F48FB1"),
                    Color.parseColor("#E040FB"),
                    Color.parseColor("#AA00FF"),
                    Color.parseColor("#4A148C"),
                    Color.argb(0, 40, 0, 90)
                ),
                gradientStops = floatArrayOf(0f, 0.12f, 0.34f, 0.54f, 0.76f, 1f),
                tailStops = listOf(
                    Triple(1.05f, 0.5f, Color.argb(105, 40, 0, 70)),
                    Triple(0.88f, 0.44f, Color.argb(135, 120, 30, 160)),
                    Triple(0.7f, 0.38f, Color.argb(160, 200, 80, 255)),
                    Triple(0.52f, 0.32f, Color.argb(180, 240, 150, 255)),
                    Triple(0.34f, 0.26f, Color.argb(195, 255, 200, 255)),
                    Triple(0.16f, 0.2f, Color.argb(205, 255, 240, 255))
                ),
                rimInner = Color.parseColor("#B9F6CA"),
                rimOuter = Color.parseColor("#76FF03"),
                spokeColor = Color.argb(130, 200, 255, 180),
                emberRgb = Triple(255, 180, 255)
            )
        }
    }

    private fun drawProjectile(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        tx: Float,
        ty: Float,
        pulse: Float,
        pal: Palette
    ) {
        val rMain = maxOf(width, height) * 0.5f * pulse

        val halo = RadialGradient(
            cx, cy, maxOf(width, height) * 1.05f,
            intArrayOf(pal.haloInner, pal.haloOuter),
            floatArrayOf(0.35f, 1f),
            Shader.TileMode.CLAMP
        )
        haloPaint.shader = halo
        canvas.drawOval(
            cx - width * 1.15f,
            cy - height * 1.15f,
            cx + width * 1.15f,
            cy + height * 1.15f,
            haloPaint
        )
        haloPaint.shader = null

        for ((dist, scale, col) in pal.tailStops) {
            tailPaint.color = col
            val ox = tx * height * dist
            val oy = ty * height * dist
            val rw = width * scale * 0.55f
            val rh = height * scale * 0.48f
            canvas.drawOval(cx + ox - rw, cy + oy - rh, cx + ox + rw, cy + oy + rh, tailPaint)
        }

        canvas.save()
        canvas.rotate((flicker * 13).toFloat(), cx, cy)
        val spokeLen = maxOf(width, height) * 0.62f
        spokePaint.strokeWidth = 3.5f
        repeat(6) { i ->
            val ang = i * 60.0 * PI / 180.0
            val c = cos(ang).toFloat()
            val s = sin(ang).toFloat()
            spokePaint.color = pal.spokeColor
            canvas.drawLine(cx + c * (rMain * 0.35f), cy + s * (rMain * 0.35f), cx + c * spokeLen, cy + s * spokeLen, spokePaint)
        }
        canvas.restore()

        val bodyShader = RadialGradient(
            cx, cy, rMain,
            pal.gradientColors,
            pal.gradientStops,
            Shader.TileMode.CLAMP
        )
        bodyPaint.shader = bodyShader
        val pad = 2f
        canvas.drawOval(x - pad, y - pad, x + width + pad, y + height + pad, bodyPaint)
        bodyPaint.shader = null

        val rimShader = RadialGradient(
            cx, cy, rMain * 1.05f,
            intArrayOf(pal.rimInner, Color.argb(0, Color.red(pal.rimOuter), Color.green(pal.rimOuter), Color.blue(pal.rimOuter))),
            floatArrayOf(0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        rimPaint.shader = rimShader
        rimPaint.strokeWidth = 5f
        canvas.drawOval(x - 2f, y - 2f, x + width + 2f, y + height + 2f, rimPaint)
        rimPaint.shader = null

        canvas.drawOval(x - 1f, y - 1f, x + width + 1f, y + height + 1f, outlinePaint)

        val coreR = minOf(width, height) * 0.16f * pulse
        corePaint.color = Color.argb(250, 255, 255, 255)
        canvas.drawOval(cx - coreR, cy - coreR, cx + coreR, cy + coreR, corePaint)

        drawOrbitingEmbers(canvas, cx, cy, pal)
    }

    private fun drawOrbitingEmbers(canvas: Canvas, cx: Float, cy: Float, pal: Palette) {
        val (er, eg, eb) = pal.emberRgb
        val orbit = minOf(width, height) * 0.42f
        repeat(8) { i ->
            val a = flicker * 0.42f + i * 0.7853982f
            val wobble = 0.88f + 0.12f * sin((flicker + i * 3) * 0.5).toFloat()
            val sx = cx + cos(a) * orbit * wobble
            val sy = cy + sin(a) * orbit * 0.82f * wobble
            val pr = 2.2f + (i and 2)
            tailPaint.color = Color.argb(220, er, eg, eb)
            canvas.drawCircle(sx, sy, pr, tailPaint)
        }
    }
}
