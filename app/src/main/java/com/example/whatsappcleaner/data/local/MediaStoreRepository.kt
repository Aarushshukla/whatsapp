package com.example.whatsappcleaner.data

import android.content.ContentUris
import android.content.Context
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
            MediaStore.Images.Media.DISPLAY_NAME
        )

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Images.Media.SIZE} > 0",
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn).orEmpty()
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                list.add(
                    MediaItem(
                        id = id,
                        uri = contentUri,
                        name = name
                    )
                )
            }
        }

        val bounded = if (offset > 0 || limit >= 0) {
            list.drop(offset).let { dropped ->
                if (limit >= 0) dropped.take(limit) else dropped
            }
        } else {
            list
        }
        Log.d(TAG, "MEDIA_DEBUG: Loaded items = ${bounded.size}")
        return bounded
    }
}
