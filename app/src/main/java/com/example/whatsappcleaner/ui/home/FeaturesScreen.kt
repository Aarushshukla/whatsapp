package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.ui.components.LegitCard
import com.example.whatsappcleaner.ui.theme.AccentBlue
import com.example.whatsappcleaner.ui.theme.AccentGreen
import com.example.whatsappcleaner.ui.theme.AccentPurple
import com.example.whatsappcleaner.ui.theme.PrimaryBackground
import com.example.whatsappcleaner.ui.theme.SurfaceWhite
import com.example.whatsappcleaner.ui.theme.TextMain
import com.example.whatsappcleaner.ui.theme.TextSecondary

private data class FeatureCardModel(
    val title: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

@Composable
fun FeaturesScreen(
    onBack: () -> Unit,
    onDuplicatesClick: () -> Unit,
    onMemeRadarClick: () -> Unit,
    onSpamShieldClick: () -> Unit,
    onWhatsAppCleanerClick: () -> Unit,
    onScreenshotsCleanerClick: () -> Unit,
    onLargeFilesClick: () -> Unit
) {
    val cards = listOf(
        FeatureCardModel("Duplicates Cleaner", Icons.Default.AutoDelete, AccentBlue, onDuplicatesClick),
        FeatureCardModel("Meme Radar", Icons.Default.Mood, AccentGreen, onMemeRadarClick),
        FeatureCardModel("Spam Shield", Icons.Default.Shield, AccentPurple, onSpamShieldClick),
        FeatureCardModel("WhatsApp Cleaner", Icons.Default.Description, AccentBlue, onWhatsAppCleanerClick),
        FeatureCardModel("Screenshots Cleaner", Icons.Default.Image, AccentGreen, onScreenshotsCleanerClick),
        FeatureCardModel("Large Files", Icons.Default.FolderOpen, AccentPurple, onLargeFilesClick)
    )

    Scaffold(
        containerColor = PrimaryBackground,
        topBar = {
            TopAppBar(
                title = { Text("Features", color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cards) { model ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = model.onClick),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            model.icon,
                            contentDescription = model.title,
                            tint = model.accent,
                            modifier = Modifier
                                .size(32.dp)
                                .background(model.accent.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                                .padding(6.dp)
                        )
                        Text(model.title, style = MaterialTheme.typography.titleMedium, color = TextMain)
                        Text("Tap to open", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturePlaceholderScreen(title: String, onBack: () -> Unit) {
    Scaffold(
        containerColor = PrimaryBackground,
        topBar = {
            TopAppBar(
                title = { Text(title, color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LegitCard(modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CleaningServices, contentDescription = null, tint = AccentBlue)
                Text("$title coming soon", color = TextMain, style = MaterialTheme.typography.titleMedium)
                Text("This module is available from the new Features screen.", color = TextSecondary)
            }
        }
    }
}
