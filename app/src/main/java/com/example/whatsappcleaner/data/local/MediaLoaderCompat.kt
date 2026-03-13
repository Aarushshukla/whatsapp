package com.example.whatsappcleaner.data.local

private const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L

/**
 * Compatibility helper so callers don't depend on specific [MediaLoader] member methods.
 */
fun MediaLoader.loadTodayWhatsAppMediaCompat(nowMillis: Long = System.currentTimeMillis()): List<SimpleMediaItem> {
    val oneDayAgo = nowMillis - ONE_DAY_MILLIS

    return loadWhatsAppMediaInRange(oneDayAgo, nowMillis)
}
