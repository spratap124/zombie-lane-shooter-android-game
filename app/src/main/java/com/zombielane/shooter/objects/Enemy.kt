package com.zombielane.shooter.objects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import kotlin.math.sin

enum class EnemyType {
    NORMAL,
    ZIGZAG,
    FAST,
    SPLITTER,
    BOSS
}

class Enemy(
    x: Float,
    y: Float,
    private val speed: Float,
    val scoreValue: Int,
    val coinValue: Int,
    private val bodyColor: Int,
    var health: Int,
    val maxHealth: Int = health,
    val type: EnemyType = EnemyType.NORMAL,
    val canShoot: Boolean = false,
    val shootInterval: Long = 2000L,
    /** Boss portrait index (0..[EnemyAssets.BOSS_SKIN_COUNT]-1), chosen from stage when spawned. */
    val bossSkinIndex: Int = 0
) : GameObject(x, y, if (type == EnemyType.BOSS) BOSS_SIZE else SIZE, if (type == EnemyType.BOSS) BOSS_SIZE else SIZE) {

    companion object {
        const val SIZE = 64f
        const val BOSS_SIZE = 128f

        private lateinit var assets: EnemyAssets

        fun bindAssets(a: EnemyAssets) {
            assets = a
        }
    }

    private var frameAge = 0
    private val spawnX = x
    var lastShotTimeMs = 0L

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }

    private val drawDst = RectF()

    private val healthTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = if (type == EnemyType.BOSS) 40f else 28f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    /** Avoid [Paint.getFontMetrics] every frame per enemy. */
    private val healthLabelBaselineShift = -healthTextPaint.fontMetrics.ascent

    private val healthBarBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#44FFFFFF")
        style = Paint.Style.FILL
    }

    private val healthBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    fun takeDamage(amount: Int) {
        health -= amount
        if (health <= 0) {
            health = 0
            active = false
        }
    }

    val isDead: Boolean get() = health <= 0

    override fun update(screenWidth: Int, screenHeight: Int, playfieldLeft: Float, playfieldRight: Float?) {
        frameAge++
        val laneRight = playfieldRight ?: screenWidth.toFloat()
        val maxX = (laneRight - width).coerceAtLeast(playfieldLeft)

        when (type) {
            EnemyType.ZIGZAG -> {
                y += speed
                x = spawnX + sin(frameAge * 0.08).toFloat() * 80f
                x = x.coerceIn(playfieldLeft, maxX)
            }
            EnemyType.FAST -> {
                y += speed
            }
            EnemyType.BOSS -> {
                y += speed
                x = spawnX + sin(frameAge * 0.03).toFloat() * 100f
                x = x.coerceIn(playfieldLeft, maxX)
            }
            else -> {
                y += speed
            }
        }

        if (y + height > screenHeight) {
            if (type == EnemyType.BOSS) {
                y = (screenHeight - height).coerceAtLeast(0f)
            } else {
                active = false
            }
        }
    }

    override fun draw(canvas: Canvas) {
        val bmp: Bitmap = assets.bitmap(type, bossSkinIndex)
        drawDst.set(x, y, x + width, y + height)
        canvas.drawBitmap(bmp, null, drawDst, bitmapPaint)

        val cx = x + width / 2f
        val scale = width / SIZE
        val barHeight = if (maxHealth > 1) (if (type == EnemyType.BOSS) 10f else 6f) else 0f
        val barTop = y - 14f
        val gap = 6f * scale
        val textTopY = if (maxHealth > 1) barTop - gap else y - gap
        val labelBaseline = textTopY + healthLabelBaselineShift

        canvas.drawText(health.toString(), cx, labelBaseline, healthTextPaint)

        if (maxHealth > 1) {
            val barWidth = width + 8f
            val barX = x - 4f
            val healthFraction = health.toFloat() / maxHealth

            canvas.drawRoundRect(barX, barTop, barX + barWidth, barTop + barHeight, 3f, 3f, healthBarBgPaint)

            healthBarPaint.color = when {
                healthFraction > 0.5f -> Color.parseColor("#4CAF50")
                healthFraction > 0.25f -> Color.parseColor("#FF9800")
                else -> Color.parseColor("#F44336")
            }
            canvas.drawRoundRect(barX, barTop, barX + barWidth * healthFraction, barTop + barHeight, 3f, 3f, healthBarPaint)
        }
    }
}
