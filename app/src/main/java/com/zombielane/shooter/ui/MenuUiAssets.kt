package com.zombielane.shooter.ui

import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.zombielane.shooter.R
import com.zombielane.shooter.data.ChestType
import kotlin.math.max

/**
 * Loads and scales main-menu art once. Chest closed/open pairs load from `assets/images/`:
 * `common_chest.png`, `common_chest_open.png`, etc. Missing files use a one-time colored placeholder bitmap.
 */
class MenuUiAssets(resources: Resources) {

    val menuBackground: Bitmap
    val playButton: Bitmap
    val dailyMissionsButton: Bitmap
    val coin: Bitmap
    val weaponsButton: Bitmap
    val settingsIcon: Bitmap
    private val chestBitmaps: Map<ChestType, Bitmap>
    private val chestOpenBitmaps: Map<ChestType, Bitmap>

    private val assets: AssetManager = resources.assets

    init {
        val dm = resources.displayMetrics
        val wPx = dm.widthPixels
        val density = dm.density

        val menuBgMaxSide = max(wPx, dm.heightPixels).coerceIn(900, 2200)
        val rawMenuBg = decodeMaxSide(resources, R.drawable.menu_background, menuBgMaxSide)
        menuBackground = softenMenuBackground(rawMenuBg)

        val playW = (wPx * 0.90f).toInt().coerceIn(320, 1080)
        playButton = decodeFitWidth(resources, R.drawable.play_button, playW)

        val dailyW = (wPx * 0.86f).toInt().coerceIn(280, 1000)
        dailyMissionsButton = try {
            decodeAssetFitWidth(assets, "images/daily_missions_bg.png", dailyW)
        } catch (_: Exception) {
            placeholderWideButtonBitmap(dailyW, Color.parseColor("#00838F"))
        }

        // Same decode width as daily so both read crisply at equal on-screen size in the menu row.
        val weaponsW = dailyW
        weaponsButton = try {
            decodeAssetFitWidth(assets, "images/weapons_button_2.png", weaponsW)
        } catch (_: Exception) {
            decodeFitWidth(resources, R.drawable.ui_weapons, weaponsW)
        }

        val settingsPx = (92f * density).toInt().coerceIn(72, 168)
        settingsIcon = decodeSquare(resources, R.drawable.ui_settings, settingsPx)

        val coinPx = (64f * density).toInt().coerceIn(56, 132)
        coin = decodeSquare(resources, R.drawable.ui_coin, coinPx)

        val chestPx = (wPx / 3.15f).toInt().coerceIn(120, 300)
        chestBitmaps = ChestType.entries.associateWith { type ->
            loadChestBitmap(closedAssetPath(type), chestPx, type, opened = false)
        }
        chestOpenBitmaps = ChestType.entries.associateWith { type ->
            loadChestBitmap(openAssetPath(type), chestPx, type, opened = true)
        }
    }

    fun chest(type: ChestType): Bitmap = chestBitmaps.getValue(type)

    fun chestOpen(type: ChestType): Bitmap = chestOpenBitmaps.getValue(type)

    private fun closedAssetPath(type: ChestType): String = when (type) {
        ChestType.COMMON -> "images/common_chest.png"
        ChestType.RARE -> "images/rare_chest.png"
        ChestType.EPIC -> "images/epic_chest.png"
        ChestType.SUPER -> "images/super_chest.png"
    }

    private fun openAssetPath(type: ChestType): String = when (type) {
        ChestType.COMMON -> "images/common_chest_open.png"
        ChestType.RARE -> "images/rare_chest_open.png"
        ChestType.EPIC -> "images/epic_chest_open.png"
        ChestType.SUPER -> "images/super_chest_open.png"
    }

    private fun loadChestBitmap(assetPath: String, size: Int, type: ChestType, opened: Boolean): Bitmap {
        return try {
            decodeAssetSquare(assets, assetPath, size)
        } catch (_: Exception) {
            placeholderChestBitmap(size, type, opened)
        }
    }

    private fun placeholderChestBitmap(size: Int, type: ChestType, opened: Boolean): Bitmap {
        val base = when (type) {
            ChestType.COMMON -> Color.parseColor("#455A64")
            ChestType.RARE -> Color.parseColor("#1565C0")
            ChestType.EPIC -> Color.parseColor("#6A1B9A")
            ChestType.SUPER -> Color.parseColor("#F9A825")
        }
        val c = if (opened) Color.argb(255, (Color.red(base) + 40).coerceAtMost(255), (Color.green(base) + 35).coerceAtMost(255), (Color.blue(base) + 25).coerceAtMost(255)) else base
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply { eraseColor(c) }
    }

    private fun placeholderWideButtonBitmap(targetW: Int, color: Int): Bitmap {
        val h = (targetW * 0.22f).toInt().coerceIn(48, 120)
        return Bitmap.createBitmap(targetW, h, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
    }

    private fun decodeAssetFitWidth(assetManager: AssetManager, path: String, targetW: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        assetManager.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0) throw IllegalArgumentException("bad bounds: $path")
        var sample = 1
        while (bounds.outWidth / sample > targetW * 1.25f) sample *= 2
        val opts = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = sample
        }
        var bmp = assetManager.open(path).use { stream ->
            BitmapFactory.decodeStream(stream, null, opts)
        } ?: throw IllegalArgumentException("decode failed: $path")
        val nh = (bmp.height * (targetW.toFloat() / bmp.width.coerceAtLeast(1))).toInt().coerceAtLeast(1)
        if (bmp.width != targetW) {
            val scaled = Bitmap.createScaledBitmap(bmp, targetW, nh, true)
            if (scaled != bmp) bmp.recycle()
            bmp = scaled
        }
        return bmp
    }

    private fun decodeAssetSquare(assetManager: AssetManager, path: String, targetSize: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        assetManager.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > targetSize * 1.25f) sample *= 2
        val opts = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = sample
        }
        val bmp = assetManager.open(path).use { stream ->
            BitmapFactory.decodeStream(stream, null, opts)
        } ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        if (bmp.width != targetSize || bmp.height != targetSize) {
            val scaled = Bitmap.createScaledBitmap(bmp, targetSize, targetSize, true)
            if (scaled != bmp) bmp.recycle()
            return scaled
        }
        return bmp
    }

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
