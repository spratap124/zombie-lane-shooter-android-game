package com.zombielane.shooter.ui

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.zombielane.shooter.R
import com.zombielane.shooter.data.ChestType
import kotlin.math.max

/**
 * Loads and scales main-menu art once. Menu background is downscaled and scaled back up once to
 * soften detail (readable UI over busy art) without per-frame blur.
 */
class MenuUiAssets(resources: Resources) {

    val menuBackground: Bitmap
    val playButton: Bitmap
    val coin: Bitmap
    private val chestBitmaps: Map<ChestType, Bitmap>

    init {
        val dm = resources.displayMetrics
        val wPx = dm.widthPixels
        val density = dm.density

        val menuBgMaxSide = max(wPx, dm.heightPixels).coerceIn(900, 2200)
        val rawMenuBg = decodeMaxSide(resources, R.drawable.menu_background, menuBgMaxSide)
        menuBackground = softenMenuBackground(rawMenuBg)

        // Decode near final display width so on-screen bitmap stays sharp when scaled up.
        val playW = (wPx * 0.90f).toInt().coerceIn(320, 1080)
        playButton = decodeFitWidth(resources, R.drawable.play_button, playW)

        val coinPx = (64f * density).toInt().coerceIn(56, 132)
        coin = decodeSquare(resources, R.drawable.ui_coin, coinPx)

        val chestPx = (wPx / 3.15f).toInt().coerceIn(120, 300)
        chestBitmaps = mapOf(
            ChestType.COMMON to decodeSquare(resources, R.drawable.chest_common, chestPx),
            ChestType.RARE to decodeSquare(resources, R.drawable.chest_rare, chestPx),
            ChestType.EPIC to decodeSquare(resources, R.drawable.chest_epic, chestPx),
            ChestType.SUPER to decodeSquare(resources, R.drawable.chest_super, chestPx)
        )
    }

    fun chest(type: ChestType): Bitmap = chestBitmaps.getValue(type)

    /**
     * Cheap blur: shrink then expand with bilinear filtering so lasers/explosion edges smear
     * slightly (all API levels; works with Canvas software rendering).
     */
    private fun softenMenuBackground(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        if (w < 16 || h < 16) return src
        val sw = (w / 5).coerceIn(72, 420)
        val sh = max(1, (h * (sw.toFloat() / w)).toInt())
        val small = Bitmap.createScaledBitmap(src, sw, sh, true)
        val out = Bitmap.createScaledBitmap(small, w, h, true)
        if (small != src) small.recycle()
        if (out != src) src.recycle()
        return out
    }

    /** Subsample so the longer side is near [maxSide]; keeps full-screen menu art memory reasonable. */
    private fun decodeMaxSide(res: Resources, id: Int, maxSide: Int): Bitmap {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(res, id, opts)
        val longSide = max(opts.outWidth, opts.outHeight).coerceAtLeast(1)
        var sample = 1
        while (longSide / sample > maxSide * 1.2f) sample *= 2
        opts.inJustDecodeBounds = false
        opts.inSampleSize = sample
        return BitmapFactory.decodeResource(res, id, opts)
            ?: Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
    }

    private fun decodeFitWidth(res: Resources, id: Int, targetW: Int): Bitmap {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(res, id, opts)
        var sample = 1
        while (opts.outWidth / sample > targetW * 1.25f) sample *= 2
        opts.inJustDecodeBounds = false
        opts.inSampleSize = sample
        var bmp = BitmapFactory.decodeResource(res, id, opts) ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val nh = (bmp.height * (targetW.toFloat() / bmp.width)).toInt().coerceAtLeast(1)
        if (bmp.width != targetW) {
            val scaled = Bitmap.createScaledBitmap(bmp, targetW, nh, true)
            if (scaled != bmp) bmp.recycle()
            bmp = scaled
        }
        return bmp
    }

    private fun decodeSquare(res: Resources, id: Int, size: Int): Bitmap {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(res, id, opts)
        var sample = 1
        while (opts.outWidth / sample > size * 1.25f) sample *= 2
        opts.inJustDecodeBounds = false
        opts.inSampleSize = sample
        var bmp = BitmapFactory.decodeResource(res, id, opts) ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        if (bmp.width != size || bmp.height != size) {
            val scaled = Bitmap.createScaledBitmap(bmp, size, size, true)
            if (scaled != bmp) bmp.recycle()
            bmp = scaled
        }
        return bmp
    }
}
