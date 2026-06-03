package com.example.whatsappcleaner.data.local

import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val exponent = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(0, units.lastIndex)
    val value = bytes / 1024.0.pow(exponent.toDouble())
    return if (exponent == 0) {
        "${bytes} B"
    } else {
        String.format(Locale.US, "%.1f %s", value, units[exponent])
    }
}

fun formatSize(sizeBytes: Long): String = formatBytes(sizeBytes)

fun formatCount(count: Int): String = if (count <= 0) "0" else String.format(Locale.US, "%,d", count)

fun formatPercent(part: Long, total: Long): String {
    if (part <= 0L || total <= 0L) return "0%"
    val percent = (part.toDouble() / total.toDouble()) * 100.0
    if (percent.isNaN() || percent.isInfinite()) return "0%"
    return if (percent >= 10.0) {
        String.format(Locale.US, "%.0f%%", percent)
    } else {
        String.format(Locale.US, "%.1f%%", percent)
    }
}

fun safeText(value: String?, fallback: String = "—"): String = value?.trim()?.takeIf { it.isNotEmpty() } ?: fallback
