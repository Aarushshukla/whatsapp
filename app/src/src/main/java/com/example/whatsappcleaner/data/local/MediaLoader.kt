package com.example.whatsappcleaner.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

data class SimpleMediaItem(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val sizeKb: Int,
    val path: String,
    val addedMillis: Long,
    val mimeType: String?,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Long? = null,
    val hash: String? = null,
    val duplicateGroupId: String? = null,
    val isBestCopy: Boolean = false,
    val category: MediaCategory = MediaCategory.NORMAL,
    val risk: MediaRisk = MediaRisk.REVIEW_CAREFULLY
)

enum class MediaCategory { NORMAL, DUPLICATE, FORWARDED_DUPLICATE }
enum class MediaRisk { SAFE_TO_DELETE, PROBABLY_JUNK, REVIEW_CAREFULLY }

class MediaLoader(private val context: Context) {

    suspend fun loadWhatsAppMediaInRange(fromMillis: Long, toMillis: Long): List<SimpleMediaItem> = withContext(Dispatchers.IO) {
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
                        sizeBytes = sizeBytes,
                        sizeKb = (sizeBytes / 1024).toInt(),
                        path = relativePath,
                        addedMillis = dateAddedSec * 1000,
                        mimeType = mimeType
                    )
                )
            }
        }

        // If you want to restrict to WhatsApp only, you can re‑add a path filter here.

        return@withContext MediaDuplicateDetector(context).detect(items)
    }

    suspend fun loadTodayWhatsAppMedia(): List<SimpleMediaItem> {
        val now = System.currentTimeMillis()
        val startOfDay = (now / 86_400_000L) * 86_400_000L
        return loadWhatsAppMediaInRange(startOfDay, now)
    }
}

private class MediaDuplicateDetector(private val context: Context) {
    fun detect(rawItems: List<SimpleMediaItem>): List<SimpleMediaItem> {
        if (rawItems.size < 2) return rawItems

        val withMetadata = rawItems.map { it.enrichMetadata(context.contentResolver) }
        val hashCache = mutableMapOf<Uri, String>()
        val groupedBySize = withMetadata.groupBy { it.sizeBytes }.filterValues { it.size > 1 }

        val exactGroups = groupedBySize.values.mapNotNull { candidates ->
            val withHashes = candidates.map { item ->
                val hash = hashCache.getOrPut(item.uri) { computeHash(context.contentResolver, item.uri) }
                item.copy(hash = hash)
            }
            val hashes = withHashes.groupBy { it.hash }
                .filterKeys { !it.isNullOrEmpty() }
                .values
                .filter { it.size > 1 }
            hashes.ifEmpty { null }
        }.flatten()

        val exactItemUpdates = mutableMapOf<Uri, SimpleMediaItem>()
        exactGroups.forEachIndexed { idx, group ->
            val groupId = "DUP-${idx + 1}"
            val best = group.maxByOrNull { qualityScore(it) } ?: return@forEachIndexed
            group.forEach { item ->
                exactItemUpdates[item.uri] = item.copy(
                    duplicateGroupId = groupId,
                    isBestCopy = item.uri == best.uri,
                    category = MediaCategory.DUPLICATE,
                    risk = if (item.uri == best.uri) MediaRisk.REVIEW_CAREFULLY else MediaRisk.SAFE_TO_DELETE
                )
            }
        }

        val remaining = withMetadata.filter { it.uri !in exactItemUpdates }
        val forwardedCandidates = remaining.groupBy { forwardedPatternKey(it) }
            .filterKeys { it != null }
            .values
            .filter { it.size > 1 }

        val forwardedUpdates = mutableMapOf<Uri, SimpleMediaItem>()
        forwardedCandidates.forEach { group ->
            group.forEach { item ->
                val repeated = group.size >= 3
                forwardedUpdates[item.uri] = item.copy(
                    category = MediaCategory.FORWARDED_DUPLICATE,
                    risk = if (repeated) MediaRisk.PROBABLY_JUNK else MediaRisk.SAFE_TO_DELETE
                )
            }
        }

        return withMetadata.map { item ->
            exactItemUpdates[item.uri] ?: forwardedUpdates[item.uri] ?: item
        }
    }

    private fun qualityScore(item: SimpleMediaItem): Long {
        val resolution = (item.width ?: 0) * (item.height ?: 0)
        val newer = item.addedMillis / 1000
        val path = item.path.lowercase()
        val nonStatus = if ("status" !in path) 1L else 0L
        val nonSticker = if ("sticker" !in path) 1L else 0L
        return resolution.toLong() * 1_000_000_000L + newer * 10_000L + nonStatus * 100L + nonSticker * 10L + item.sizeBytes
    }

    private fun forwardedPatternKey(item: SimpleMediaItem): String? {
        val lower = item.name.lowercase()
        val matchesPattern = lower.matches(Regex("(img|vid|ptt)-\\d{8}-wa\\d+.*")) || "forward" in lower
        if (!matchesPattern) return null
        return listOf(item.sizeBytes, item.width ?: 0, item.height ?: 0, item.durationMs ?: 0L, item.mimeType.orEmpty()).joinToString("|")
    }

    private fun computeHash(resolver: ContentResolver, uri: Uri): String {
        return runCatching {
            resolver.openInputStream(uri)?.use { input ->
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val count = input.read(buffer)
                    if (count <= 0) break
                    digest.update(buffer, 0, count)
                }
                digest.digest().joinToString("") { "%02x".format(it) }
            }
        }.getOrNull().orEmpty()
    }

    private fun SimpleMediaItem.enrichMetadata(resolver: ContentResolver): SimpleMediaItem {
        if (mimeType?.startsWith("video/") == true) {
            val mmr = MediaMetadataRetriever()
            return runCatching {
                resolver.openFileDescriptor(uri, "r")?.use { pfd -> mmr.setDataSource(pfd.fileDescriptor) }
                val width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
                val height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
                val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                copy(width = width, height = height, durationMs = duration)
            }.getOrElse { this }.also { mmr.release() }
        }

        if (mimeType?.startsWith("image/") == true) {
            return runCatching {
                resolver.openInputStream(uri)?.use { input ->
                    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(input, null, opts)
                    copy(width = opts.outWidth.takeIf { it > 0 }, height = opts.outHeight.takeIf { it > 0 })
                } ?: this
            }.getOrElse { this }
        }
        return this
    }
}
