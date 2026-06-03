package com.example.whatsappcleaner.ui

import com.example.whatsappcleaner.data.local.SimpleMediaItem

data class CategorySummaryUi(
    val title: String,
    val description: String,
    val count: Int,
    val sizeBytes: Long,
    val safetyBadge: String,
    val items: List<SimpleMediaItem>
)
