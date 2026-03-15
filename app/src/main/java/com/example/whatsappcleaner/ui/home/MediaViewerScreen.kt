package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.local.SimpleMediaItem

@Composable
fun MediaViewerScreen(allItems: List<SimpleMediaItem>, spamItems: List<SimpleMediaItem>, duplicateItems: List<SimpleMediaItem>, onOpenInSystem: (SimpleMediaItem) -> Unit, modifier: Modifier = Modifier) {
    var tab by remember { mutableStateOf(MediaFilter.ALL) }
    val filtered = when (tab) {
        MediaFilter.IMAGES -> allItems.filter { it.mimeType?.startsWith("image") == true }
        MediaFilter.VIDEOS -> allItems.filter { it.mimeType?.startsWith("video") == true }
        else -> allItems
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text("Media Viewer", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        FilterTabs(currentFilter = tab, onFilterChange = { tab = it })
        Text(
            "Spam candidates: ${spamItems.size} • Duplicates: ${duplicateItems.size}",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { item ->
                Card(modifier = Modifier.fillMaxWidth(), onClick = { onOpenInSystem(item) }) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text(item.mimeType ?: "Unknown", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
