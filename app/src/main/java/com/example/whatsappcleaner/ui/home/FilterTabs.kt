package com.example.whatsappcleaner.ui.home

import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun FilterTabs(
    currentFilter: MediaFilter,
    onFilterChange: (MediaFilter) -> Unit
) {
    val tabs = MediaFilter.entries

    ScrollableTabRow(
        selectedTabIndex = tabs.indexOf(currentFilter).coerceAtLeast(0)
    ) {
        tabs.forEach { filter ->
            Tab(
                selected = filter == currentFilter,
                onClick = { onFilterChange(filter) },
                text = { Text(filter.label()) }
            )
        }
    }
}

private fun MediaFilter.label(): String = when (this) {
    MediaFilter.ALL -> "All"
    MediaFilter.IMAGES -> "Images"
    MediaFilter.VIDEOS -> "Videos"
    MediaFilter.MEMES -> "Memes"
    MediaFilter.DUPLICATES -> "Duplicates"
    MediaFilter.OTHER -> "Other"
}
