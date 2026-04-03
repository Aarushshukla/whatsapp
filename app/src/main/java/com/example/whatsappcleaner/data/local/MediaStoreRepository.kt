package com.example.whatsappcleaner.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

class MediaStoreRepository(
    private val context: Context
) {

    fun loadImages(limit: Int = 100, offset: Int = 0): List<MediaItem> {
        val list = mutableListOf<MediaItem>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
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

        return list
    }
}
