package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.SmartJunkResult
import com.example.whatsappcleaner.ui.components.LegitCard

@Composable
fun SmartCleanScreen(
    result: SmartJunkResult,
    onReviewCategory: (String, List<SimpleMediaItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    val groups = listOf(
        "Blurry images" to result.blurryImages,
        "Dark images" to result.darkImages,
        "Burst duplicates" to result.burstDuplicates,
        "Tiny images" to result.tinyImages,
        "Cached media" to result.cachedMedia
    ).filter { it.second.isNotEmpty() }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Smart Clean",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (groups.isEmpty()) {
            Text(
                text = "No junk candidates detected.",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            return
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groups) { (title, items) ->
                LegitCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text("${items.size} files", style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = { onReviewCategory(title, items) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Text("Review")
                        }
                    }
                }
            }
        }
    }
}
