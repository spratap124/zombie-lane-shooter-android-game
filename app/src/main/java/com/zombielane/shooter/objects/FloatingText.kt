package com.zombielane.shooter.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

class FloatingText(
    private var x: Float,
    private var y: Float,
    private val text: String,
    private val color: Int = Color.WHITE,
    private val size: Float = 36f,
    private var life: Int = 40
) {
    var active = true
    private val maxLife = life

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = this@FloatingText.color
        textSize = this@FloatingText.size
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    fun update() {
        y -= 2.5f
        life--
        if (life <= 0) active = false
        paint.alpha = ((life.toFloat() / maxLife) * 255).toInt().coerceIn(0, 255)
    }

    fun draw(canvas: Canvas) {
        canvas.drawText(text, x, y, paint)
    }
}
