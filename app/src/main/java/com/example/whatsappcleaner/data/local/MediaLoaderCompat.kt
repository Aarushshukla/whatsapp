package com.example.whatsappcleaner.data.local

private const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L

/**
 * Compatibility helper so callers don't depend on specific [MediaLoader] member methods.
 */
fun MediaLoader.loadTodayWhatsAppMediaCompat(nowMillis: Long = System.currentTimeMillis()): List<SimpleMediaItem> {
    val oneDayAgo = nowMillis - ONE_DAY_MILLIS

    val rangedMethod = MediaLoader::class.java.methods.firstOrNull {
        it.name == "loadWhatsAppMediaInRange" &&
                it.parameterTypes.contentEquals(arrayOf(Long::class.javaPrimitiveType, Long::class.javaPrimitiveType))
    }

    @Suppress("UNCHECKED_CAST")
    val rangedItems = rangedMethod?.let {
        runCatching { it.invoke(this, oneDayAgo, nowMillis) as? List<SimpleMediaItem> }.getOrNull()
    }

    if (rangedItems != null) {
        return rangedItems
    }


    val allMedia = queryMediaStore("all", oneDayAgo, nowMillis)

    return allMedia
        .filter { it.addedMillis in oneDayAgo..nowMillis }
        .sortedByDescending { it.addedMillis }
}