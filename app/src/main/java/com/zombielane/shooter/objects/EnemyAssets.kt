package com.zombielane.shooter.objects

import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.zombielane.shooter.R
import java.io.IOException
import kotlin.math.max

/**
 * Decodes enemy portraits once at a fixed pixel size (sources are often 2K+) for stable memory use.
 * Bosses 1–18 load from `res/drawable`; bosses 19–35 from `assets/images/enemy_boss_*.png`.
 */
class EnemyAssets(resources: Resources) {

    private val normal: Bitmap
    private val fast: Bitmap
    private val zigzag: Bitmap
    private val splitter: Bitmap
    private val bosses: List<Bitmap>

    init {
        val reg = REGULAR_PX
        val bossPx = BOSS_PX
        val assets: AssetManager = resources.assets
        normal = decodeToSquare(resources, R.drawable.enemy_normal, reg)
        fast = decodeToSquare(resources, R.drawable.enemy_fast, reg)
        zigzag = decodeToSquare(resources, R.drawable.enemy_zigzag, reg)
        splitter = decodeToSquare(resources, R.drawable.enemy_splitter, reg)

        val resIds = listOf(
            R.drawable.enemy_boss_1,
            R.drawable.enemy_boss_2,
            R.drawable.enemy_boss_3,
            R.drawable.enemy_boss_4,
            R.drawable.enemy_boss_5,
            R.drawable.enemy_boss_6,
            R.drawable.enemy_boss_7,
            R.drawable.enemy_boss_8,
            R.drawable.enemy_boss_9,
            R.drawable.enemy_boss_10,
            R.drawable.enemy_boss_11,
            R.drawable.enemy_boss_12,
            R.drawable.enemy_boss_13,
            R.drawable.enemy_boss_14,
            R.drawable.enemy_boss_15,
            R.drawable.enemy_boss_16,
            R.drawable.enemy_boss_17,
            R.drawable.enemy_boss_18
        )
        require(resIds.size == RES_DRAWABLE_BOSS_COUNT)

        val fromRes = resIds.map { decodeToSquare(resources, it, bossPx) }
        val fromAssets = (ASSET_BOSS_START..ASSET_BOSS_END).map { n ->
            decodeBossFromAsset(assets, "images/enemy_boss_$n.png", bossPx)
        }
        bosses = fromRes + fromAssets
        require(bosses.size == BOSS_SKIN_COUNT) { "boss list must match BOSS_SKIN_COUNT" }
    }

    fun bossBitmap(skinIndex: Int): Bitmap = bosses[skinIndex.mod(BOSS_SKIN_COUNT)]

    fun bitmap(type: EnemyType, bossSkinIndex: Int): Bitmap {
        return when (type) {
            EnemyType.NORMAL -> normal
            EnemyType.FAST -> fast
            EnemyType.ZIGZAG -> zigzag
            EnemyType.SPLITTER -> splitter
            EnemyType.BOSS -> bosses[bossSkinIndex.mod(BOSS_SKIN_COUNT)]
        }
    }

    private fun decodeToSquare(res: Resources, id: Int, targetPx: Int): Bitmap {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(res, id, opts)
        val longSide = max(opts.outWidth, opts.outHeight).coerceAtLeast(1)
        var sample = 1
        while (longSide / sample > targetPx * 1.4f) sample *= 2
        opts.inJustDecodeBounds = false
        opts.inSampleSize = sample
        val raw = BitmapFactory.decodeResource(res, id, opts)
            ?: Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        val scaled = Bitmap.createScaledBitmap(raw, targetPx, targetPx, true)
        if (scaled != raw) raw.recycle()
        return scaled
    }

    /**
     * Decode from a single in-memory copy of the asset so we only open the asset file once
     * (two decode passes share bytes). Reduces peak file-descriptor use during startup.
     */
    private fun decodeBossFromAsset(assets: AssetManager, path: String, targetPx: Int): Bitmap {
        val bytes = try {
            assets.open(path).use { it.readBytes() }
        } catch (e: IOException) {
            throw IOException("Failed to read asset: $path", e)
        }
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        val longSide = max(opts.outWidth, opts.outHeight).coerceAtLeast(1)
        var sample = 1
        while (longSide / sample > targetPx * 1.4f) sample *= 2
        opts.inJustDecodeBounds = false
        opts.inSampleSize = sample
        val raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            ?: Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        val scaled = Bitmap.createScaledBitmap(raw, targetPx, targetPx, true)
        if (scaled != raw) raw.recycle()
        return scaled
    }

    companion object {
        const val REGULAR_PX = 160
        const val BOSS_PX = 256

        private const val RES_DRAWABLE_BOSS_COUNT = 18
        private const val ASSET_BOSS_START = 19
        private const val ASSET_BOSS_END = 35

        /** Total boss portrait variants (enemy_boss_1 … enemy_boss_35). */
        const val BOSS_SKIN_COUNT = RES_DRAWABLE_BOSS_COUNT + (ASSET_BOSS_END - ASSET_BOSS_START + 1)
    }
}
