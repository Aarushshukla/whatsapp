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
import com.example.whatsappcleaner.ai.StorageReport
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.components.StorageHeatMap

@Composable
fun PhoneRealityReportScreen(
    report: StorageReport,
    imagesCount: Int,
    videosCount: Int,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = listOf(
        "Total Files" to report.totalFiles.toString(),
        "Storage Used" to formatSize(report.totalSize),
        "Memes" to report.memeCount.toString(),
        "Duplicates" to report.duplicateCount.toString(),
        "Spam Media" to report.spamCount.toString(),
        "Files older than 6 months" to report.oldFiles.toString()
    )

    Column(modifier = modifier.fillMaxSize()) {
        Text("Phone Reality Report", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(entries) { (title, value) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = title, style = MaterialTheme.typography.labelLarge)
                        Text(text = value, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Storage Heatmap", style = MaterialTheme.typography.titleMedium)
                        StorageHeatMap(
                            imagesPct = percentage(imagesCount, report.totalFiles),
                            videosPct = percentage(videosCount, report.totalFiles),
                            memesPct = percentage(report.memeCount, report.totalFiles),
                            duplicatesPct = percentage(report.duplicateCount, report.totalFiles),
                            spamPct = percentage(report.spamCount, report.totalFiles)
                        )
                    }
                }
            }
        }
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("Next: Meme Analyzer") }
    }
}

private fun percentage(value: Int, total: Int): Float = if (total <= 0) 0f else value.toFloat() / total.toFloat()
