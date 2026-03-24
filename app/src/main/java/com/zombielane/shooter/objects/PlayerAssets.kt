package com.zombielane.shooter.objects

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.zombielane.shooter.R
import com.zombielane.shooter.data.ShooterType

class PlayerAssets(resources: Resources) {

    private val bitmaps = mutableMapOf<ShooterType, Bitmap>()

    private val resMap = mapOf(
        ShooterType.BASIC  to R.drawable.shooter_basic,
        ShooterType.DOUBLE to R.drawable.shooter_double,
        ShooterType.SPREAD to R.drawable.shooter_spread,
        ShooterType.RAPID  to R.drawable.shooter_rapid,
        ShooterType.LASER  to R.drawable.shooter_laser
    )

    init {
        for ((type, resId) in resMap) {
            val raw = BitmapFactory.decodeResource(resources, resId) ?: continue
            bitmaps[type] = Bitmap.createScaledBitmap(raw, SPRITE_SIZE, SPRITE_SIZE, true)
            if (raw !== bitmaps[type]) raw.recycle()
        }
    }

    fun get(type: ShooterType): Bitmap =
        bitmaps[type] ?: bitmaps[ShooterType.BASIC]!!

    companion object {
        const val SPRITE_SIZE = 256
    }
}
