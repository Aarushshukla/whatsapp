package com.example.whatsappcleaner.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

class MediaStoreRepository(
    private val context: Context
) {

    companion object {
        private const val TAG = "MediaStoreRepository"
    }

    fun loadImages(limit: Int = 100, offset: Int = 0): List<MediaItem> {
        val list = mutableListOf<MediaItem>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE
        )

        val sortOrder = buildString {
            append("${MediaStore.Images.Media.DATE_ADDED} DESC")
            append(" LIMIT ")
            append(limit)
            if (offset > 0) {
                append(" OFFSET ")
                append(offset)
            }
        }

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn).orEmpty()
                val size = cursor.getLong(sizeColumn)

                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                list.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        name = name,
                        size = size
                    )
                )
            }
        }

        return list
    }

    fun deleteMedia(uris: List<Uri>): Int {
        if (uris.isEmpty()) return 0
        var deletedCount = 0
        uris.forEach { uri ->
            val rows = runCatching { context.contentResolver.delete(uri, null, null) }
                .onFailure { error -> Log.w(TAG, "Failed to delete URI: $uri", error) }
                .getOrDefault(0)
            if (rows > 0) deletedCount += 1
        }
        return deletedCount
    }
}
