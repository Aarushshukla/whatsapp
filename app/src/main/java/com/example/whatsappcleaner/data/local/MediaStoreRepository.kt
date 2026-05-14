package com.example.whatsappcleaner.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreRepository(
    private val context: Context
) {

    companion object {
        private const val TAG = "MediaStoreRepository"

        private val CHAT_KEYWORDS = listOf(
            "whatsapp",
            "whatsapp images",
            "whatsapp video",
            "whatsapp audio",
            "whatsapp documents",
            "whatsapp animated gifs",
            "whatsapp stickers",
            ".statuses",
            "statuses",
            "stickers",
            "memes",
            "funny",
            "gif"
        )
    }

    suspend fun scanLikelyChatMedia(limit: Int = -1, offset: Int = 0): List<MediaItem> =
        withContext(Dispatchers.IO) {
            val images = queryCollection(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val videos = queryCollection(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            val audio = queryCollection(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            val files = queryCollection(MediaStore.Files.getContentUri("external"))

            val merged = (images + videos + audio + files)
                .distinctBy { "${it.id}-${it.uri}" }
                .sortedByDescending { it.dateModified }

            val bounded = merged.drop(offset).let { dropped ->
                if (limit >= 0) dropped.take(limit) else dropped
            }

            Log.d(TAG, "Likely chat media scan result size=${bounded.size} (total=${merged.size})")
            bounded
        }

    fun loadImages(limit: Int = 100, offset: Int = 0): List<MediaItem> =
        runCatching {
            kotlinx.coroutines.runBlocking { scanLikelyChatMedia(limit = limit, offset = offset) }
        }.getOrElse { error ->
            Log.e(TAG, "Failed to load images", error)
            emptyList()
        }

    private fun queryCollection(collectionUri: Uri): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATA
        )

        context.contentResolver.query(
            collectionUri,
            projection,
            "${MediaStore.MediaColumns.SIZE} > 0",
            null,
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)
            val relPathCol = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
            val bucketCol = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val dataCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol).orEmpty()
                val relativePath = if (relPathCol >= 0) cursor.getString(relPathCol) else null
                val bucketName = if (bucketCol >= 0) cursor.getString(bucketCol) else null
                val fullPath = if (dataCol >= 0) cursor.getString(dataCol) else null
                if (!isLikelyChatMedia(name, bucketName, relativePath, fullPath)) continue

                val uri = ContentUris.withAppendedId(collectionUri, id)
                val size = cursor.getLong(sizeCol)
                val mimeType = cursor.getString(mimeCol) ?: "application/octet-stream"
                val dateModified = cursor.getLong(dateModCol) * 1000L
                val width = cursor.getInt(widthCol).takeIf { it > 0 }
                val height = cursor.getInt(heightCol).takeIf { it > 0 }
                val duration = cursor.getLong(durationCol).takeIf { it > 0L }

                items.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        path = fullPath ?: relativePath,
                        name = name,
                        sizeBytes = size,
                        mimeType = mimeType,
                        dateModified = dateModified,
                        width = width,
                        height = height,
                        durationMs = duration,
                        bucketName = bucketName
                    )
                )
            }
        }

        return items
    }

    private fun isLikelyChatMedia(
        name: String?,
        bucketName: String?,
        relativePath: String?,
        fullPath: String?
    ): Boolean {
        val target = listOf(name, bucketName, relativePath, fullPath)
            .filterNotNull()
            .joinToString(" ")
            .lowercase()

        return CHAT_KEYWORDS.any { keyword -> target.contains(keyword) }
    }
}
