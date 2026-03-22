package com.example.whatsappcleaner.data.local

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PhoneRealityReport(
    val totalPhotos: Int = 0,
    val totalVideos: Int = 0,
    val totalScreenshots: Int = 0,
    val totalMemes: Int = 0,
    val duplicateFiles: Int = 0,
    val totalStorageUsedBytes: Long = 0,
    val largestFileSizeBytes: Long = 0,
    val oldestPhotoDate: String = "-",
    val filesDeletedToday: Int = 0,
    val storageFreedTodayBytes: Long = 0,
    val cleaningStreak: Int = 0
)

class PhoneRealityAnalyzer {

    fun analyze(
        items: List<SimpleMediaItem>,
        memeItems: List<SimpleMediaItem> = emptyList(),
        filesDeletedToday: Int = 0,
        storageFreedTodayBytes: Long = 0,
        cleaningStreak: Int = 0
    ): PhoneRealityReport {
        val images = items.filter { mediaItem -> mediaItem.mimeType?.startsWith("image/") == true }
        val videos = items.filter { mediaItem -> mediaItem.mimeType?.startsWith("video/") == true }
        val screenshots = images.filter { mediaItem ->
            mediaItem.path.contains("screenshots", true) ||
                mediaItem.name.contains("screenshot", true) ||
                mediaItem.name.contains("screen_shot", true)
        }

        val duplicates = items
            .groupBy { mediaItem -> Triple(mediaItem.name.lowercase(), mediaItem.sizeKb, mediaItem.mimeType.orEmpty()) }
            .values
            .sumOf { group -> (group.size - 1).coerceAtLeast(0) }

        val oldestPhotoMillis = images.minOfOrNull { mediaItem -> mediaItem.addedMillis }
        val oldestPhotoDate = oldestPhotoMillis?.let { millis ->
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
        } ?: "-"

        return PhoneRealityReport(
            totalPhotos = images.size,
            totalVideos = videos.size,
            totalScreenshots = screenshots.size,
            totalMemes = memeItems.size,
            duplicateFiles = duplicates,
            totalStorageUsedBytes = items.sumOf { mediaItem -> mediaItem.sizeKb.toLong() * 1024L },
            largestFileSizeBytes = items.maxOfOrNull { mediaItem -> mediaItem.sizeKb.toLong() * 1024L } ?: 0L,
            oldestPhotoDate = oldestPhotoDate,
            filesDeletedToday = filesDeletedToday,
            storageFreedTodayBytes = storageFreedTodayBytes,
            cleaningStreak = cleaningStreak
        )
    }
}
