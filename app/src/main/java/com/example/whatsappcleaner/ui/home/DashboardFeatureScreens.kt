@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.whatsappcleaner.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.ai.StorageReport
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.components.FriendlyState
import com.example.whatsappcleaner.ui.components.LegitButton
import com.example.whatsappcleaner.ui.components.LegitCard
import com.example.whatsappcleaner.ui.components.StorageRing
import com.example.whatsappcleaner.ui.components.StorageHeatMap
import com.example.whatsappcleaner.ui.theme.AccentBlue
import com.example.whatsappcleaner.ui.theme.AccentGreen
import com.example.whatsappcleaner.ui.theme.BrandNavy
import com.example.whatsappcleaner.ui.theme.PrimaryBackground
import com.example.whatsappcleaner.ui.theme.TextMain
import com.example.whatsappcleaner.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FeatureScreenScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, color = BrandNavy)
                        Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandNavy)
                    }
                }
            )
        },
        containerColor = PrimaryBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) { content() }
    }
}

@Composable
fun JunkFilesScreen(items: List<SimpleMediaItem>, onOpenInSystem: (SimpleMediaItem) -> Unit, onBack: () -> Unit) {
    FeatureScreenScaffold("Junk Files", "Large, old, and easy wins", onBack) {
        if (items.isEmpty()) {
            FriendlyState(Icons.Default.CleaningServices, "✨ Junk under control", "No junk files are currently flagged.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items.take(20), key = { it.uri.toString() }) { item ->
                    SimpleActionCard(Icons.Default.DeleteSweep, item.name, formatSize(item.sizeKb.toLong() * 1024L), { onOpenInSystem(item) })
                }
            }
        }
    }
}

@Composable
fun AnalyticsScreen(report: StorageReport, imageCount: Int, videoCount: Int, memeCount: Int, duplicateCount: Int, spamCount: Int, onBack: () -> Unit) {
    FeatureScreenScaffold("Storage Analytics", "Breakdown, heatmap, and storage health", onBack) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                LegitCard {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📊 Storage snapshot", color = TextMain, style = MaterialTheme.typography.titleLarge)
                            Text("${report.totalFiles} files • ${formatSize(report.totalSize)}", color = TextSecondary)
                        }
                        StorageRing(
                            progress = if (report.totalFiles == 0) 0f else (imageCount + videoCount).toFloat() / report.totalFiles.toFloat(),
                            label = formatSize(report.totalSize),
                            subtitle = "indexed media"
                        )
                    }
                }
            }
            item {
                LegitCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Storage heatmap", color = TextMain, style = MaterialTheme.typography.titleMedium)
                        StorageHeatMap(
                            imagesPct = if (report.totalFiles == 0) 0f else imageCount.toFloat() / report.totalFiles,
                            videosPct = if (report.totalFiles == 0) 0f else videoCount.toFloat() / report.totalFiles,
                            memesPct = if (report.totalFiles == 0) 0f else memeCount.toFloat() / report.totalFiles,
                            duplicatesPct = if (report.totalFiles == 0) 0f else duplicateCount.toFloat() / report.totalFiles,
                            spamPct = if (report.totalFiles == 0) 0f else spamCount.toFloat() / report.totalFiles
                        )
                    }
                }
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    statBox("🖼 $imageCount", "Photos")
                    statBox("🎥 $videoCount", "Videos")
                    statBox("😂 $memeCount", "Memes")
                    statBox("♻️ $duplicateCount", "Duplicates")
                    statBox("🛡 $spamCount", "Spam")
                }
            }
        }
    }
}

@Composable
fun SpamMediaScreen(items: List<SimpleMediaItem>, onOpenInSystem: (SimpleMediaItem) -> Unit, onBack: () -> Unit) {
    FeatureScreenScaffold("Spam Media", "Review suspicious WhatsApp forwards", onBack) {
        if (items.isEmpty()) {
            FriendlyState(Icons.Default.Security, "🛡 Inbox looks clean", "No spam-like media was found.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items.take(20), key = { it.uri.toString() }) { item ->
                    SimpleActionCard(Icons.Default.Security, item.name, item.mimeType ?: "Unknown", { onOpenInSystem(item) })
                }
            }
        }
    }
}

@Composable
fun PolishedSmartCleanScreen(
    duplicateItems: List<SimpleMediaItem>,
    spamItems: List<SimpleMediaItem>,
    largeFileItems: List<SimpleMediaItem>,
    sentFiles: List<SimpleMediaItem>,
    onOpenInSystem: (SimpleMediaItem) -> Unit,
    onBack: () -> Unit
) {
    var celebrate by remember { mutableStateOf(false) }
    val recoverableBytes = (duplicateItems + spamItems + largeFileItems + sentFiles).distinctBy { it.uri }.sumOf { it.sizeKb.toLong() * 1024L }
    val scale by animateFloatAsState(if (celebrate) 1f else 0.92f, label = "success_scale")

    FeatureScreenScaffold("Smart Clean", "High-confidence cleanup suggestions", onBack) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                LegitCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Potential cleanup", color = TextMain, style = MaterialTheme.typography.titleLarge)
                        Text("We found ${duplicateItems.size + spamItems.size + largeFileItems.size + sentFiles.size} review items.", color = TextSecondary)
                        LegitButton("Open best candidate", onClick = {
                            celebrate = true
                            (largeFileItems.firstOrNull() ?: duplicateItems.firstOrNull() ?: spamItems.firstOrNull() ?: sentFiles.firstOrNull())?.let(onOpenInSystem)
                        })
                        AnimatedVisibility(
                            visible = celebrate,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Text(
                                text = "🎉 ${formatSize(recoverableBytes)} Freed!",
                                color = AccentGreen,
                                modifier = Modifier.padding(top = 4.dp).graphicsLayer { scaleX = scale; scaleY = scale },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    statBox("♻️ ${duplicateItems.size}", "Duplicates")
                    statBox("🧹 ${largeFileItems.size}", "Large files")
                    statBox("📤 ${sentFiles.size}", "Sent files")
                    statBox("🛡 ${spamItems.size}", "Spam")
                }
            }
            items(
                listOf(
                    Triple("Duplicates", duplicateItems, Icons.Default.AutoAwesome),
                    Triple("Spam Media", spamItems, Icons.Default.Security),
                    Triple("Large Files", largeFileItems, Icons.Default.Analytics),
                    Triple("Sent Files", sentFiles, Icons.Default.VideoLibrary)
                )
            ) { (title, items, icon) ->
                LegitCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(icon, contentDescription = null, tint = AccentBlue)
                            Text(title, color = TextMain, style = MaterialTheme.typography.titleMedium)
                        }
                        Text("${items.size} files", color = TextSecondary)
                        items.take(3).forEach { item ->
                            Text("• ${item.name}", color = TextMain)
                        }
                        items.firstOrNull()?.let { first ->
                            LegitButton("Review sample", onClick = { onOpenInSystem(first) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PolishedPhoneRealityScreen(report: StorageReport, imageCount: Int, videoCount: Int, onBack: () -> Unit) {
    FeatureScreenScaffold("Phone Reality", "What your gallery is really storing", onBack) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    statBox("😂 ${report.memeCount}", "Memes")
                    statBox("⚠ ${report.spamCount}", "Junk files")
                    statBox("📦 ${formatSize(report.totalSize)}", "Used")
                    statBox("🎥 $videoCount", "Videos")
                    statBox("🖼 $imageCount", "Photos")
                }
            }
            item {
                LegitCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Reality dashboard", color = TextMain, style = MaterialTheme.typography.titleLarge)
                        Text("Old files: ${report.oldFiles} • Duplicates: ${report.duplicateCount}", color = TextSecondary)
                        StorageHeatMap(
                            imagesPct = if (report.totalFiles == 0) 0f else imageCount.toFloat() / report.totalFiles,
                            videosPct = if (report.totalFiles == 0) 0f else videoCount.toFloat() / report.totalFiles,
                            memesPct = if (report.totalFiles == 0) 0f else report.memeCount.toFloat() / report.totalFiles,
                            duplicatesPct = if (report.totalFiles == 0) 0f else report.duplicateCount.toFloat() / report.totalFiles,
                            spamPct = if (report.totalFiles == 0) 0f else report.spamCount.toFloat() / report.totalFiles
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PolishedMemeScreen(memes: List<SimpleMediaItem>, onOpenInSystem: (SimpleMediaItem) -> Unit, onBack: () -> Unit) {
    FeatureScreenScaffold("Meme Detector", "Funniest files first", onBack) {
        if (memes.isEmpty()) {
            FriendlyState(Icons.Default.Image, "😂 Meme folder is calm", "No memes were detected in the current scan.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(memes, key = { it.uri.toString() }) { item ->
                    SimpleActionCard(Icons.Default.Image, item.name, formatSize(item.sizeKb.toLong() * 1024L), { onOpenInSystem(item) })
                }
            }
        }
    }
}

@Composable
private fun SimpleActionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    LegitCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, contentDescription = null, tint = AccentBlue)
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TextMain, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            LegitButton("Open", onClick = onClick)
        }
    }
}

@Composable
private fun statBox(value: String, label: String) {
    LegitCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, color = TextMain, style = MaterialTheme.typography.titleMedium)
            Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
