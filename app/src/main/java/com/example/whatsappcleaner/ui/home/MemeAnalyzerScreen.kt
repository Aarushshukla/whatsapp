package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.local.SimpleMediaItem

@Composable
fun MemeAnalyzerScreen(memes: List<SimpleMediaItem>, onOpenInSystem: (SimpleMediaItem) -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Text("Meme Analyzer", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        if (memes.isEmpty()) {
            Text("No memes detected yet.", modifier = Modifier.padding(horizontal = 16.dp))
        }
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(memes) { item ->
                Card(modifier = Modifier.fillMaxWidth(), onClick = { onOpenInSystem(item) }) {
                    Column(Modifier.padding(14.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text(item.mimeType ?: "Unknown", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("Next: Media Viewer") }
    }
}
