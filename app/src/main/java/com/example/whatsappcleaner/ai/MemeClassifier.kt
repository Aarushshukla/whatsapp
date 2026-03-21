package com.example.whatsappcleaner.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class ImageCategory { MEME, PHOTO, SCREENSHOT, DOCUMENT, SELFIE, UNKNOWN }

data class ClassificationResult(
    val category: ImageCategory,
    val confidence: Float
)

class MemeClassifier(
    private val context: Context,
    private val modelAssetName: String = "meme_classifier.tflite"
) {
    companion object {
        private const val TAG = "MemeClassifier"
    }

    private var interpreter: Interpreter? = null

    suspend fun classify(uri: Uri): ClassificationResult = withContext(Dispatchers.Default) {
        val bitmap = decodeBitmap(uri) ?: return@withContext ClassificationResult(ImageCategory.UNKNOWN, 0f)
        val input = preprocess(bitmap)
        val output = Array(1) { FloatArray(ImageCategory.entries.size - 1) }

        return@withContext runCatching {
            ensureInterpreter()
            val activeInterpreter = interpreter ?: return@runCatching ClassificationResult(ImageCategory.UNKNOWN, 0f)
            activeInterpreter.run(input, output)
            mapOutput(output[0])
        }.onFailure { error ->
            Log.e(TAG, "Unable to classify media item: $uri", error)
        }.getOrElse {
            ClassificationResult(ImageCategory.UNKNOWN, 0f)
        }.also {
            bitmap.recycle()
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    private fun ensureInterpreter() {
        if (interpreter != null) return
        val bytes = context.assets.open(modelAssetName).use { it.readBytes() }
        val direct = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
        direct.put(bytes).rewind()
        interpreter = Interpreter(direct)
    }

    private fun decodeBitmap(uri: Uri): Bitmap? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    }.onFailure { error ->
        Log.e(TAG, "Unable to decode bitmap for $uri", error)
    }.getOrNull()

    private fun preprocess(source: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(source, 224, 224, true)
        val buffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3).order(ByteOrder.nativeOrder())
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resized.getPixel(x, y)
                buffer.putFloat(((pixel shr 16 and 0xFF) / 255f))
                buffer.putFloat(((pixel shr 8 and 0xFF) / 255f))
                buffer.putFloat(((pixel and 0xFF) / 255f))
            }
        }
        resized.recycle()
        buffer.rewind()
        return buffer
    }

    private fun mapOutput(probabilities: FloatArray): ClassificationResult {
        if (probabilities.isEmpty()) return ClassificationResult(ImageCategory.UNKNOWN, 0f)
        val maxIdx = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
        if (maxIdx == -1) return ClassificationResult(ImageCategory.UNKNOWN, 0f)
        val label = when (maxIdx) {
            0 -> ImageCategory.MEME
            1 -> ImageCategory.PHOTO
            2 -> ImageCategory.SCREENSHOT
            3 -> ImageCategory.DOCUMENT
            4 -> ImageCategory.SELFIE
            else -> ImageCategory.UNKNOWN
        }
        return ClassificationResult(label, probabilities[maxIdx])
    }
}
