package com.example.whatsappcleaner.ai

import com.example.whatsappcleaner.data.local.SimpleMediaItem

class SpamMediaAnalyzer {

    fun findSpamMedia(items: List<SimpleMediaItem>): List<SimpleMediaItem> {
        return items.filter { item ->
            SPAM_MARKERS.any { marker ->
                item.name.contains(marker, ignoreCase = true) || item.path.contains(marker, ignoreCase = true)
            }
        }
    }

    private companion object {
        private val SPAM_MARKERS = listOf("forwarded", "status", "temp", "sent")
    }
}
