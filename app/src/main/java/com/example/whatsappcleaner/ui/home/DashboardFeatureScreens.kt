@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.example.whatsappcleaner.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.whatsappcleaner.ai.StorageReport
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.components.FriendlyState
import com.example.whatsappcleaner.ui.components.LegitButton
import com.example.whatsappcleaner.ui.components.LegitCard
import com.example.whatsappcleaner.ui.components.StorageHeatMap
import com.example.whatsappcleaner.ui.components.StorageRing
import com.example.whatsappcleaner.ui.theme.AccentBlue
import com.example.whatsappcleaner.ui.theme.AccentGreen
import com.example.whatsappcleaner.ui.theme.BrandNavy
import com.example.whatsappcleaner.ui.theme.PrimaryBackground
import com.example.whatsappcleaner.ui.theme.TextMain
import com.example.whatsappcleaner.ui.theme.TextSecondary
import kotlinx.coroutines.delay

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
                itemsIndexed(items.take(20), key = { _, item -> item.uri.toString() }) { index, item ->
                    AnimatedListItem(index = index) {
                        SimpleActionCard(Icons.Default.DeleteSweep, item.name, formatSize(item.sizeKb.toLong() * 1024L), { onOpenInSystem(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsScreen(report: StorageReport, imageCount: Int, videoCount: Int, memeCount: Int, duplicateCount: Int, spamCount: Int, onBack: () -> Unit) {
    val indexedProgress = if (report.totalFiles == 0) 0f else (imageCount + videoCount).toFloat() / report.totalFiles.toFloat()
    val indexedPercent = (indexedProgress * 100).toInt()

    FeatureScreenScaffold("Storage Analytics", "Breakdown, heatmap, and storage health", onBack) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                LegitCard {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📊 Storage snapshot", color = TextMain, style = MaterialTheme.typography.titleLarge)
                            Text("${report.totalFiles} files • ${formatSize(report.totalSize)}", color = TextSecondary)
                            Text("$indexedPercent% storage used", color = AccentBlue, fontWeight = FontWeight.Bold)
                        }
                        StorageRing(
                            progress = indexedProgress,
                            label = "$indexedPercent%",
                            subtitle = "storage used"
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
                itemsIndexed(items.take(20), key = { _, item -> item.uri.toString() }) { index, item ->
                    AnimatedListItem(index = index) {
                        SimpleActionCard(Icons.Default.Security, item.name, item.mimeType ?: "Unknown", { onOpenInSystem(item) })
                    }
                }
            }
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun PolishedSmartCleanScreen(
    duplicateItems: List<SimpleMediaItem>,
    spamItems: List<SimpleMediaItem>,
    largeFileItems: List<SimpleMediaItem>,
    sentFiles: List<SimpleMediaItem>,
    onOpenInSystem: (SimpleMediaItem) -> Unit,
    onDeleteItemsRequested: (List<SimpleMediaItem>) -> Unit,
    onBack: () -> Unit,
    onShareResult: () -> Unit,
    onCleanupRecorded: (Long) -> Unit
) {
    val smartCleanItems = remember(duplicateItems, spamItems, largeFileItems, sentFiles) {
        (duplicateItems + spamItems + largeFileItems + sentFiles).distinctBy { item -> item.uri }
    }
    val selectedItems = remember { mutableStateListOf<SimpleMediaItem>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Clean", color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextMain)
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${smartCleanItems.size} smart-clean candidates",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                AnimatedVisibility(visible = selectedItems.isNotEmpty()) {
                    LegitButton(
                        text = "Delete (${selectedItems.size})",
                        onClick = {
                            val toDelete = selectedItems.toList()
                            onCleanupRecorded(toDelete.sumOf { it.size })
                            onDeleteItemsRequested(toDelete)
                            selectedItems.clear()
                        }
                    )
                }
            }

            if (smartCleanItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    FriendlyState(
                        icon = Icons.Filled.CheckCircle,
                        title = "You're all caught up",
                        subtitle = "No duplicate, large, spam, or screenshot/video files were found."
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(smartCleanItems, key = { item -> item.uri.toString() }) { item ->
                        val isSelected = selectedItems.any { selected -> selected.uri == item.uri }
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 5.dp else 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        selectedItems.removeAll { selected -> selected.uri == item.uri }
                                    } else {
                                        selectedItems.add(item)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = item.uri,
                                    contentDescription = item.name,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFE8ECFF))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextMain,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatSize(item.sizeKb.toLong() * 1024L),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            if (!selectedItems.any { selected -> selected.uri == item.uri }) {
                                                selectedItems.add(item)
                                            }
                                        } else {
                                            selectedItems.removeAll { selected -> selected.uri == item.uri }
                                        }
                                    }
                                )
                            }
                            LegitButton(
                                text = "Open",
                                onClick = { onOpenInSystem(item) },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 0.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

private data class SmartCleanCategoryUiModel(
    val title: String,
    val items: List<SimpleMediaItem>,
    val icon: ImageVector
)

@Composable
private fun SmartCleanCategoryCard(
    category: SmartCleanCategoryUiModel,
    onReview: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMain
                    )
                    Text(
                        text = "${category.items.size} files",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            category.items
                .take(3)
                .forEach { item ->
                    Text(
                        text = "• ${item.name}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            LegitButton(
                text = "Review",
                onClick = onReview,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PremiumMetricCard(totalFiles: Int, totalSize: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Smart Clean", color = Color(0xFF1A1A1A), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("AI-powered cleanup suggestions", color = Color(0xFF6B7280).copy(alpha = 0.82f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumPillStat("$totalFiles", "Total files")
                PremiumPillStat(totalSize, "Total size")
            }
        }
    }
}

@Composable
private fun PremiumPillStat(value: String, label: String) {
    Surface(color = Color(0xFFF7F8FA), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(value, color = Color(0xFF1A1A1A), fontWeight = FontWeight.Bold)
            Text(label, color = Color(0xFF6B7280), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SmartCleaningActionButton(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "smart_clean_cta", animationSpec = spring())
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF4A6CF7), Color(0xFF8A5CF6))))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            ) {
                pressed = true
                onClick()
                pressed = false
            },
        contentAlignment = Alignment.Center
    ) {
        Text("✨ Start Smart Cleaning", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StoragePremiumCard(usedBytes: Long, freeBytes: Long, cleanableBytes: Long, progress: Float) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Storage overview", color = Color(0xFF1A1A1A), style = MaterialTheme.typography.titleMedium)
                Text("Used ${formatSize(usedBytes)} • Free ${formatSize(freeBytes)}", color = Color(0xFF6B7280))
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFE9F9EE)) {
                    Text(
                        "Cleanable ${formatSize(cleanableBytes)}",
                        color = Color(0xFF1F9D55),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = progress, color = Color(0xFF4A6CF7), trackColor = Color(0xFFE8ECFF), strokeWidth = 7.dp, modifier = Modifier.size(58.dp))
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = Color(0xFF1A1A1A))
            }
        }
    }
}

@Composable
private fun QuickActionChip(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
        shape = RoundedCornerShape(50.dp),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected, borderColor = Color(0xFFE5E7EB), selectedBorderColor = Color(0xFF4A6CF7)),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFFF7F8FA),
            selectedContainerColor = Color(0xFFE8ECFF),
            labelColor = Color(0xFF1A1A1A),
            selectedLabelColor = Color(0xFF1A1A1A),
            iconColor = Color(0xFF6B7280),
            selectedLeadingIconColor = Color(0xFF4A6CF7)
        )
    )
}

@Composable
private fun FileCandidateCard(
    item: SimpleMediaItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8ECFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF4A6CF7))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = Color(0xFF1A1A1A), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
                Text(formatSize(item.sizeKb.toLong() * 1024L), color = Color(0xFF6B7280), style = MaterialTheme.typography.bodyMedium)
            }
            Icon(
                imageVector = if (isSelected) Icons.Default.Done else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF4A6CF7) else Color(0xFF9CA3AF),
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onCheckedChange(!isSelected) }
            )
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
                itemsIndexed(memes, key = { _, item -> item.uri.toString() }) { index, item ->
                    AnimatedListItem(index = index) {
                        SimpleActionCard(Icons.Default.Image, item.name, formatSize(item.sizeKb.toLong() * 1024L), { onOpenInSystem(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedListItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((index * 70).toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(420)) + slideInVertically(
            animationSpec = tween(420),
            initialOffsetY = { offset -> offset / 3 }
        ),
        exit = fadeOut(animationSpec = tween(320)) + shrinkVertically(animationSpec = tween(320))
    ) {
        content()
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
