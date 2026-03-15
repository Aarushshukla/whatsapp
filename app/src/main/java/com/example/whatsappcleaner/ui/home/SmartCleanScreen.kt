package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.local.SimpleMediaItem

@Composable
fun SmartCleanScreen(
    duplicateItems: List<SimpleMediaItem>,
    spamItems: List<SimpleMediaItem>,
    largeFileItems: List<SimpleMediaItem>,
    sentFiles: List<SimpleMediaItem>,
    modifier: Modifier = Modifier
) {
    val groups = listOf(
        "Duplicates" to duplicateItems,
        "Spam Media" to spamItems,
        "Large Files" to largeFileItems,
        "Sent Files" to sentFiles
    )

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Smart Clean",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groups) { (title, items) ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text("${items.size} files", style = MaterialTheme.typography.bodyLarge)
                        items.take(3).forEach {
                            Text("• ${it.name}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
