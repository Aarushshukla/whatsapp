package com.example.whatsappcleaner.data.local

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log

data class SimpleMediaItem(
    val uri: Uri,
    val id: Long,
    val name: String,
    val size: Long,
    val addedMillis: Long,
    val mimeType: String?,
    val mediaType: String
) {
    val sizeKb: Int
        get() = (size / 1024L).toInt()

    // Kept for compatibility with existing analyzers. This is no longer file-path based.
    val path: String
        get() = uri.toString()
}

class MediaLoader(private val context: Context) {

    fun loadAllDeviceMedia(mediaType: String): List<SimpleMediaItem> =
        queryMediaStore(mediaType = mediaType, minDate = 0L, maxDate = System.currentTimeMillis())

    fun queryMediaStore(mediaType: String, minDate: Long, maxDate: Long): List<SimpleMediaItem> {
        val resolver: ContentResolver = context.contentResolver
        val collection = if (mediaType == "video") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        }

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.MIME_TYPE
        )

        val selection = "${MediaStore.MediaColumns.DATE_ADDED} BETWEEN ? AND ?"
        val selectionArgs = arrayOf((minDate / 1000).toString(), (maxDate / 1000).toString())

        val items = mutableListOf<SimpleMediaItem>()
        try {
            Log.d("MediaLoader", "Querying $mediaType media between $minDate and $maxDate")
            resolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.MediaColumns.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val displayName = cursor.getString(nameCol) ?: "Media"
                    val sizeBytes = cursor.getLong(sizeCol)
                    val dateAddedSec = cursor.getLong(dateCol)
                    val mimeType = cursor.getString(mimeCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    items.add(
                        SimpleMediaItem(
                            uri = uri,
                            id = id,
                            name = displayName,
                            size = sizeBytes,
                            addedMillis = dateAddedSec * 1000,
                            mimeType = mimeType,
                            mediaType = mediaType
                        )
                    )
                }
            }
        } catch (error: SecurityException) {
            Log.e("MediaLoader", "Permission denied while loading $mediaType media", error)
        } catch (error: Exception) {
            Log.e("MediaLoader", "Error loading $mediaType media", error)
        }
        return items
    }
}
