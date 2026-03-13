package com.example.whatsappcleaner.data.local

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

private const val TINY_IMAGE_KB = 50
private const val BURST_WINDOW_MS = 5_000L

data class SmartJunkResult(
    val blurryImages: List<SimpleMediaItem> = emptyList(),
    val darkImages: List<SimpleMediaItem> = emptyList(),
    val burstDuplicates: List<SimpleMediaItem> = emptyList(),
    val tinyImages: List<SimpleMediaItem> = emptyList(),
    val cachedMedia: List<SimpleMediaItem> = emptyList()
) {
    val totalCandidates: Int
        get() = (blurryImages + darkImages + burstDuplicates + tinyImages + cachedMedia)
            .distinctBy { it.uri }
            .size
}

class SmartJunkAnalyzer(
    private val contentResolver: ContentResolver
) {

    suspend fun analyze(items: List<SimpleMediaItem>): SmartJunkResult = withContext(Dispatchers.Default) {
        val imageItems = items.filter { it.mimeType?.startsWith("image/") == true }

        val blurry = mutableListOf<SimpleMediaItem>()
        val dark = mutableListOf<SimpleMediaItem>()

        imageItems.forEach { item ->
            val bitmap = decodeBitmap(item.uri) ?: return@forEach
            if (isBlurry(bitmap)) blurry += item
            if (isDark(bitmap)) dark += item
            bitmap.recycle()
        }

        val burst = imageItems
            .sortedBy { it.addedMillis }
            .zipWithNext()
            .filter { (first, second) ->
                second.addedMillis - first.addedMillis in 0..BURST_WINDOW_MS &&
                    first.name.substringBeforeLast('.') == second.name.substringBeforeLast('.')
            }
            .flatMap { listOf(it.first, it.second) }
            .distinctBy { it.uri }

        val tiny = imageItems.filter { it.sizeKb in 1 until TINY_IMAGE_KB }
        val cached = items.filter { it.path.contains("cache", ignoreCase = true) }

        SmartJunkResult(
            blurryImages = blurry,
            darkImages = dark,
            burstDuplicates = burst,
            tinyImages = tiny,
            cachedMedia = cached
        )
    }

    private fun decodeBitmap(uri: Uri): Bitmap? {
        return runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }.getOrNull()
    }

    private fun isDark(bitmap: Bitmap): Boolean {
        val scaled = Bitmap.createScaledBitmap(bitmap, 48, 48, true)
        var luminanceSum = 0.0
        val pixelCount = scaled.width * scaled.height
        for (x in 0 until scaled.width) {
            for (y in 0 until scaled.height) {
                val pixel = scaled.getPixel(x, y)
                luminanceSum += (0.2126 * Color.red(pixel) + 0.7152 * Color.green(pixel) + 0.0722 * Color.blue(pixel))
            }
        }
        scaled.recycle()
        return (luminanceSum / pixelCount) < 45.0
    }

    private fun isBlurry(bitmap: Bitmap): Boolean {
        val scaled = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        val width = scaled.width
        val height = scaled.height
        val gray = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val p = scaled.getPixel(x, y)
                gray[y * width + x] = ((Color.red(p) + Color.green(p) + Color.blue(p)) / 3)
            }
        }

        val laplacianValues = mutableListOf<Int>()
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = gray[y * width + x] * 4
                val neighbors = gray[y * width + x - 1] + gray[y * width + x + 1] + gray[(y - 1) * width + x] + gray[(y + 1) * width + x]
                laplacianValues += abs(center - neighbors)
            }
        }
        scaled.recycle()

        if (laplacianValues.isEmpty()) return false
        val mean = laplacianValues.average()
        val variance = laplacianValues.map { (it - mean) * (it - mean) }.average()
        return variance < 120.0
    }
}
