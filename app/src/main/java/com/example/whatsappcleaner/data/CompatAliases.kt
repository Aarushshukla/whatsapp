package com.example.whatsappcleaner.data

import com.example.whatsappcleaner.data.local.formatSize as localFormatSize

// Backward-compatible aliases for legacy imports still used in older screens/files.
typealias MediaLoader = com.example.whatsappcleaner.data.local.MediaLoader
typealias SimpleMediaItem = com.example.whatsappcleaner.data.local.SimpleMediaItem
typealias UserPrefs = com.example.whatsappcleaner.data.local.UserPrefs

fun formatSize(bytes: Long): String = localFormatSize(bytes)
