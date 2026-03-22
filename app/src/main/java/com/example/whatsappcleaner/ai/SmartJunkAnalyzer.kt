package com.example.whatsappcleaner.ai

import com.example.whatsappcleaner.data.local.SimpleMediaItem

data class JunkBreakdown(
    val duplicates: List<SimpleMediaItem> = emptyList(),
    val largeFiles: List<SimpleMediaItem> = emptyList(),
    val forwardedMedia: List<SimpleMediaItem> = emptyList(),
    val sentFiles: List<SimpleMediaItem> = emptyList()
)

class SmartJunkAnalyzer {

    fun findJunk(items: List<SimpleMediaItem>): List<SimpleMediaItem> {
        val breakdown = buildBreakdown(items)
        return (breakdown.duplicates + breakdown.largeFiles + breakdown.forwardedMedia + breakdown.sentFiles)
            .distinctBy { mediaItem -> mediaItem.uri }
    }

    fun buildBreakdown(items: List<SimpleMediaItem>): JunkBreakdown {
        val duplicates = items.groupBy { mediaItem -> mediaItem.sizeKb }
            .values
            .filter { groupedItems -> groupedItems.size > 1 }
            .flatten()

        val largeFiles = items.filter { mediaItem -> mediaItem.sizeKb >= LARGE_FILE_KB_THRESHOLD }
        val forwardedMedia = items.filter { item ->
            KEYWORDS_FORWARDED.any { key ->
                item.name.contains(key, ignoreCase = true) || item.path.contains(key, ignoreCase = true)
            }
        }
        val sentFiles = items.filter { item ->
            KEYWORDS_SENT.any { key ->
                item.name.contains(key, ignoreCase = true) || item.path.contains(key, ignoreCase = true)
            }
        }

        return JunkBreakdown(
            duplicates = duplicates,
            largeFiles = largeFiles,
            forwardedMedia = forwardedMedia,
            sentFiles = sentFiles
        )
    }

    private companion object {
        private const val LARGE_FILE_KB_THRESHOLD = 10 * 1024
        private val KEYWORDS_FORWARDED = listOf("forwarded", "whatsapp")
        private val KEYWORDS_SENT = listOf("sent", "send")
    }
}
