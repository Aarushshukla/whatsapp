package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
    onOpenInSystem: (SimpleMediaItem) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val groups = listOf(
        "Duplicates" to duplicateItems,
        "Spam Media" to spamItems,
        "Large Files" to largeFileItems,
        "Sent Files" to sentFiles
    )

    Column(modifier = modifier.fillMaxSize()) {
        Text("Smart Clean", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(groups) { group ->
                val (title, groupItems) = group
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text("${groupItems.size} files", style = MaterialTheme.typography.bodyLarge)
                        groupItems.take(3).forEach { item ->
                            Text("• ${item.name}", modifier = Modifier.padding(top = 4.dp))
                        }
                        groupItems.firstOrNull()?.let { firstItem ->
                            Button(onClick = { onOpenInSystem(firstItem) }, modifier = Modifier.padding(top = 8.dp)) { Text("Open sample") }
                        }
                    }
                }
            }
        }
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("Next: Phone Reality") }
    }
}
