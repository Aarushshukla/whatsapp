package com.example.whatsappcleaner.data.local

private const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L

/**
 * Compatibility helper so callers don't depend on specific [MediaLoader] member methods.
 */
fun MediaLoader.loadTodayWhatsAppMediaCompat(nowMillis: Long = System.currentTimeMillis()): List<SimpleMediaItem> {
    val oneDayAgo = nowMillis - ONE_DAY_MILLIS

    val rangedMethod = MediaLoader::class.java.methods.firstOrNull { method ->
        method.name == "loadWhatsAppMediaInRange" &&
            method.parameterTypes.contentEquals(arrayOf(Long::class.javaPrimitiveType, Long::class.javaPrimitiveType))
    }

    @Suppress("UNCHECKED_CAST")
    val rangedItems = rangedMethod?.let { method ->
        runCatching { method.invoke(this, oneDayAgo, nowMillis) as? List<SimpleMediaItem> }.getOrNull()
    }

    if (rangedItems != null) {
        return rangedItems
            .filter { mediaItem -> mediaItem.addedMillis in oneDayAgo..nowMillis }
            .sortedByDescending { mediaItem -> mediaItem.addedMillis }
    }

    return (queryMediaStore("image", oneDayAgo, nowMillis) + queryMediaStore("video", oneDayAgo, nowMillis))
        .distinctBy { mediaItem -> mediaItem.uri }
        .filter { mediaItem -> mediaItem.addedMillis in oneDayAgo..nowMillis }
        .sortedByDescending { mediaItem -> mediaItem.addedMillis }
}
