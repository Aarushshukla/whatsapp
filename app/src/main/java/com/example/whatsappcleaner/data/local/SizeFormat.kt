package com.example.whatsappcleaner.data.local

import java.util.Locale

fun formatSize(sizeBytes: Long): String {
    if (sizeBytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(
        Locale.US,
        "%.1f %s",
        sizeBytes / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups]
    )
}