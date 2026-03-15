package com.example.whatsappcleaner.ai

import com.example.whatsappcleaner.data.local.SimpleMediaItem

data class StorageReport(
    val totalFiles: Int,
    val totalSize: Long,
    val memeCount: Int,
    val duplicateCount: Int,
    val spamCount: Int,
    val oldFiles: Int
)

class PhoneRealityAnalyzer {

    fun generateReport(items: List<SimpleMediaItem>): StorageReport {
        val spamItems = SpamMediaAnalyzer().findSpamMedia(items)
        val duplicateCount = items.groupBy { it.sizeKb }
            .values
            .sumOf { group -> if (group.size > 1) group.size else 0 }

        val sixMonthsAgo = System.currentTimeMillis() - SIX_MONTHS_MS
        val oldFiles = items.count { it.addedMillis < sixMonthsAgo }

        return StorageReport(
            totalFiles = items.size,
            totalSize = items.sumOf { it.sizeKb.toLong() * 1024L },
            memeCount = items.count { item ->
                item.name.contains("meme", ignoreCase = true) || item.path.contains("meme", ignoreCase = true)
            },
            duplicateCount = duplicateCount,
            spamCount = spamItems.size,
            oldFiles = oldFiles
        )
    }

    private companion object {
        private const val SIX_MONTHS_MS = 1000L * 60 * 60 * 24 * 30 * 6
    }
}
