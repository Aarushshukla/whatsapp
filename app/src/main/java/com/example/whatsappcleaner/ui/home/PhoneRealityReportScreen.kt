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
import com.example.whatsappcleaner.data.local.PhoneRealityReport
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.components.LegitCard

@Composable
fun PhoneRealityReportScreen(
    report: PhoneRealityReport,
    onShareReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = listOf(
        "Total Photos" to report.totalPhotos.toString(),
        "Total Videos" to report.totalVideos.toString(),
        "Total Screenshots" to report.totalScreenshots.toString(),
        "Total Memes" to report.totalMemes.toString(),
        "Duplicate Files" to report.duplicateFiles.toString(),
        "Total Storage Used" to formatSize(report.totalStorageUsedBytes),
        "Largest File" to formatSize(report.largestFileSizeBytes),
        "Oldest Photo" to report.oldestPhotoDate,
        "Files Deleted Today" to report.filesDeletedToday.toString(),
        "Storage Freed Today" to formatSize(report.storageFreedTodayBytes),
        "Cleaning Streak" to "${report.cleaningStreak} days"
    )

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Phone Reality Report",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries) { (title, value) ->
                LegitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = title, style = MaterialTheme.typography.labelLarge)
                        Text(text = value, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Button(
            onClick = onShareReport,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Share My Phone Report")
        }
    }
}
