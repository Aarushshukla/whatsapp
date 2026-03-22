package com.example.whatsappcleaner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.ui.theme.AccentBlue
import com.example.whatsappcleaner.ui.theme.SurfaceMuted
import com.example.whatsappcleaner.ui.theme.TextMain
import com.example.whatsappcleaner.ui.theme.TextSecondary

@Composable
fun StorageHeatMap(
    imagesPct: Float,
    videosPct: Float,
    memesPct: Float,
    duplicatesPct: Float,
    spamPct: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HeatRow("Images", imagesPct)
        HeatRow("Videos", videosPct)
        HeatRow("Memes", memesPct)
        HeatRow("Duplicates", duplicatesPct)
        HeatRow("Spam / Junk", spamPct)
    }
}

@Composable
private fun HeatRow(label: String, percentage: Float) {
    Text(
        text = "$label ${(percentage * 100).toInt()}%",
        style = MaterialTheme.typography.labelLarge,
        color = TextMain,
        modifier = Modifier.padding(top = 8.dp)
    )
    LinearProgressIndicator(
        progress = percentage.coerceIn(0f, 1f),
        color = AccentBlue,
        trackColor = SurfaceMuted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    )
    Text(
        text = "Used for quick visual storage breakdown.",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        modifier = Modifier.padding(top = 4.dp)
    )
}
