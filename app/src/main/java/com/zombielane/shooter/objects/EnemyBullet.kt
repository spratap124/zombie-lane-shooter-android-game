package com.zombielane.shooter.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.hypot

class EnemyBullet(
    x: Float,
    y: Float,
    private val vx: Float = 0f,
    private val vy: Float = SPEED,
    private val bulletColor: Int = COLOR_NORMAL
) : GameObject(x - WIDTH / 2f, y, WIDTH, HEIGHT) {

    companion object {
        const val WIDTH = 22f
        const val HEIGHT = 40f
        const val SPEED = 3f
        val COLOR_NORMAL = Color.parseColor("#FF1744")
        val COLOR_BOSS = Color.parseColor("#FFCA28")
        val COLOR_ZIGZAG = Color.parseColor("#EA80FC")
    }

    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val tailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 35, 20, 15)
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }
    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(230, 255, 255, 240)
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

        val wobble = 1f + 0.04f * ((flicker and 3) - 1.5f)

        when (bulletColor) {
            COLOR_BOSS -> drawFireball(canvas, cx, cy, tx, ty, wobble, Palette.BOSS)
            COLOR_ZIGZAG -> drawFireball(canvas, cx, cy, tx, ty, wobble, Palette.ZIGZAG)
            else -> drawFireball(canvas, cx, cy, tx, ty, wobble, Palette.NORMAL)
        }
    }

    private data class Palette(
        val halo: Int,
        val gradientColors: IntArray,
        val gradientStops: FloatArray,
        val tailA: Int,
        val tailB: Int,
        val tailC: Int,
        val tailD: Int
    ) {
        companion object {
            val NORMAL = Palette(
                halo = Color.argb(55, 255, 60, 30),
                gradientColors = intArrayOf(
                    Color.WHITE,
                    Color.parseColor("#FFF59D"),
                    Color.parseColor("#FFAB40"),
                    Color.parseColor("#FF6D00"),
                    Color.parseColor("#D84315"),
                    Color.argb(0, 180, 40, 0)
                ),
                gradientStops = floatArrayOf(0f, 0.12f, 0.32f, 0.52f, 0.78f, 1f),
                tailA = Color.argb(120, 120, 25, 10),
                tailB = Color.argb(160, 230, 70, 20),
                tailC = Color.argb(190, 255, 130, 40),
                tailD = Color.argb(210, 255, 200, 80)
            )
            val BOSS = Palette(
                halo = Color.argb(60, 255, 200, 40),
                gradientColors = intArrayOf(
                    Color.WHITE,
                    Color.parseColor("#FFFDE7"),
                    Color.parseColor("#FFEE58"),
                    Color.parseColor("#FFC107"),
                    Color.parseColor("#FF6F00"),
                    Color.argb(0, 200, 100, 0)
                ),
                gradientStops = floatArrayOf(0f, 0.1f, 0.28f, 0.48f, 0.75f, 1f),
                tailA = Color.argb(110, 140, 80, 10),
                tailB = Color.argb(150, 255, 160, 30),
                tailC = Color.argb(185, 255, 210, 70),
                tailD = Color.argb(215, 255, 248, 150)
            )
            val ZIGZAG = Palette(
                halo = Color.argb(55, 200, 80, 255),
                gradientColors = intArrayOf(
                    Color.WHITE,
                    Color.parseColor("#F8BBD0"),
                    Color.parseColor("#E040FB"),
                    Color.parseColor("#7B1FA2"),
                    Color.parseColor("#311B92"),
                    Color.argb(0, 80, 0, 120)
                ),
                gradientStops = floatArrayOf(0f, 0.14f, 0.36f, 0.58f, 0.82f, 1f),
                tailA = Color.argb(115, 60, 20, 90),
                tailB = Color.argb(150, 130, 40, 180),
                tailC = Color.argb(185, 220, 100, 255),
                tailD = Color.argb(210, 255, 200, 255)
            )
        }
    }

    private fun drawFireball(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        tx: Float,
        ty: Float,
        wobble: Float,
        pal: Palette
    ) {
        val rMain = maxOf(width, height) * 0.48f * wobble

        haloPaint.color = pal.halo
        canvas.drawOval(
            cx - width * 0.95f,
            cy - height * 0.95f,
            cx + width * 0.95f,
            cy + height * 0.95f,
            haloPaint
        )

        val tailSteps = listOf(
            Triple(0.85f, 0.42f, pal.tailA),
            Triple(0.62f, 0.34f, pal.tailB),
            Triple(0.4f, 0.26f, pal.tailC),
            Triple(0.2f, 0.18f, pal.tailD)
        )
        for ((dist, scale, col) in tailSteps) {
            tailPaint.color = col
            val ox = tx * height * dist
            val oy = ty * height * dist
            val rw = width * scale * 0.5f
            val rh = height * scale * 0.45f
            canvas.drawOval(cx + ox - rw, cy + oy - rh, cx + ox + rw, cy + oy + rh, tailPaint)
        }

        val shader = RadialGradient(
            cx,
            cy,
            rMain,
            pal.gradientColors,
            pal.gradientStops,
            Shader.TileMode.CLAMP
        )
        bodyPaint.shader = shader
        val pad = 1.5f
        canvas.drawOval(x - pad, y - pad, x + width + pad, y + height + pad, bodyPaint)
        bodyPaint.shader = null

        canvas.drawOval(x - 0.5f, y - 0.5f, x + width + 0.5f, y + height + 0.5f, outlinePaint)

        val sparkR = minOf(width, height) * 0.14f * wobble
        val sparkOx = -tx * height * 0.08f
        val sparkOy = -ty * height * 0.08f
        canvas.drawOval(
            cx + sparkOx - sparkR,
            cy + sparkOy - sparkR,
            cx + sparkOx + sparkR,
            cy + sparkOy + sparkR,
            sparkPaint
        )
    }
}
