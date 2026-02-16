package com.example.whatsappcleaner.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log

data class SimpleMediaItem(
    val uri: Uri,
    val name: String,
    val sizeKb: Int,
    val path: String,
    val addedMillis: Long,
    val mimeType: String?
)

class MediaLoader(private val context: Context) {

    fun loadWhatsAppMediaInRange(fromMillis: Long, toMillis: Long): List<SimpleMediaItem> {
        val resolver: ContentResolver = context.contentResolver

        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.RELATIVE_PATH,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val fromSec = fromMillis / 1000
        val toSec = toMillis / 1000

        val selection =
            "${MediaStore.Files.FileColumns.DATE_ADDED} >= ? AND " +
                    "${MediaStore.Files.FileColumns.DATE_ADDED} <= ?"

        val selectionArgs = arrayOf(
            fromSec.toString(),
            toSec.toString()
        )

        val items = mutableListOf<SimpleMediaItem>()

        resolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val displayName = cursor.getString(nameCol) ?: "Media"
                val sizeBytes = cursor.getLong(sizeCol)
                val dateAddedSec = cursor.getLong(dateCol)
                val relativePath = cursor.getString(pathCol) ?: ""
                val mimeType = cursor.getString(mimeCol)

                Log.d("MediaDebug", "name=$displayName mime=$mimeType path=$relativePath")

                val uri = Uri.withAppendedPath(collection, id.toString())
                items.add(
                    SimpleMediaItem(
                        uri = uri,
                        name = displayName,
                        sizeKb = (sizeBytes / 1024).toInt(),
                        path = relativePath,
                        addedMillis = dateAddedSec * 1000,
                        mimeType = mimeType
                    )
                )
            }
        }

        // If you want to restrict to WhatsApp only, you can re‑add a path filter here.

        return items
    }

    fun loadTodayWhatsAppMedia(): List<SimpleMediaItem> {
        val now = System.currentTimeMillis()
        val startOfDay = (now / 86_400_000L) * 86_400_000L
        return loadWhatsAppMediaInRange(startOfDay, now)
    }
}
