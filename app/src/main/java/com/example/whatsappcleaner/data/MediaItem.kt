package com.example.whatsappcleaner.data

import android.net.Uri

data class MediaItem(
    val uri: Uri,
    val name: String,
    val size: Long,
    val path: String
)
