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
        val images = items.filter { it.mimeType?.startsWith("image/") == true }
        val videos = items.filter { it.mimeType?.startsWith("video/") == true }
        val screenshots = images.filter {
            it.path.contains("screenshots", true) ||
                it.name.contains("screenshot", true) ||
                it.name.contains("screen_shot", true)
        }

        val duplicates = items
            .groupBy { Triple(it.name.lowercase(), it.sizeKb, it.mimeType.orEmpty()) }
            .values
            .sumOf { group -> (group.size - 1).coerceAtLeast(0) }

        val oldestPhotoMillis = images.minOfOrNull { it.addedMillis }
        val oldestPhotoDate = oldestPhotoMillis?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
        } ?: "-"

        return PhoneRealityReport(
            totalPhotos = images.size,
            totalVideos = videos.size,
            totalScreenshots = screenshots.size,
            totalMemes = memeItems.size,
            duplicateFiles = duplicates,
            totalStorageUsedBytes = items.sumOf { it.sizeKb.toLong() * 1024L },
            largestFileSizeBytes = items.maxOfOrNull { it.sizeKb.toLong() * 1024L } ?: 0L,
            oldestPhotoDate = oldestPhotoDate,
            filesDeletedToday = filesDeletedToday,
            storageFreedTodayBytes = storageFreedTodayBytes,
            cleaningStreak = cleaningStreak
        )
    }
}
