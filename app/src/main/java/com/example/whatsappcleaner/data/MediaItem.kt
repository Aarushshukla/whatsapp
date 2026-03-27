package com.example.whatsappcleaner.data

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String
)
