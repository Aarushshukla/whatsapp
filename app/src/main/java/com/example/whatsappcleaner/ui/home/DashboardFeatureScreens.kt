@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.example.whatsappcleaner.ui.home

import android.os.Environment
import android.os.StatFs
import android.text.format.DateFormat
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Policy
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
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
    allItems: List<SimpleMediaItem>,
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
    var isScanning by remember { mutableStateOf(true) }
    var scanningStatus by remember { mutableStateOf("Scanning your files...") }
    var activeCategory by remember { mutableStateOf("Duplicates") }

    val duplicateKeys = remember(allItems, duplicateItems) {
        val knownDuplicateKeys = duplicateItems
            .map { item -> "${item.name.lowercase()}_${item.size}" }
            .toSet()

        allItems
            .groupBy { mediaItem -> "${mediaItem.name.lowercase()}_${mediaItem.size}" }
            .filterValues { groupedItems -> groupedItems.size > 1 }
            .keys + knownDuplicateKeys
    }

    val fileItems = remember(allItems, duplicateKeys) {
        val groups = allItems.groupBy { mediaItem -> "${mediaItem.name.lowercase()}_${mediaItem.size}" }
        val bestByKey = groups.mapValues { (_, items) ->
            items.maxWithOrNull(compareBy<SimpleMediaItem> { it.addedMillis }.thenBy { it.size })?.uri?.toString()
        }

        allItems.map { mediaItem ->
            val duplicateKey = "${mediaItem.name.lowercase()}_${mediaItem.size}"
            val isDup = duplicateKeys.contains(duplicateKey)
            FileItem(
                uri = mediaItem.uri,
                name = mediaItem.name,
                size = mediaItem.size,
                lastModified = mediaItem.addedMillis,
                path = mediaItem.path,
                mimeType = mediaItem.mimeType.orEmpty(),
                isDuplicate = isDup,
                isScreenshot = mediaItem.name.contains("screenshot", ignoreCase = true) || mediaItem.path.contains("screenshot", ignoreCase = true),
                isWhatsapp = mediaItem.path.contains("whatsapp", ignoreCase = true) || mediaItem.uri.toString().contains("whatsapp", ignoreCase = true),
                duplicateGroupKey = if (isDup) duplicateKey else null,
                isBestDuplicateCopy = isDup && bestByKey[duplicateKey] == mediaItem.uri.toString()
            )
        }
    }

    val mediaByUri = remember(allItems) { allItems.associateBy { it.uri.toString() } }
    val smartCandidates = remember(fileItems, spamItems, sentFiles, largeFileItems) {
        val extraUris = (spamItems + sentFiles + largeFileItems).map { it.uri.toString() }.toSet()
        fileItems.filter { file -> calculateScore(file) >= 30 || file.uri.toString() in extraUris }
    }

    val duplicateCategoryItems = remember(smartCandidates) { smartCandidates.filter { it.isDuplicate } }
    val largeCategoryItems = remember(smartCandidates) { smartCandidates.filter { it.size > LARGE_FILE_BYTES } }
    val screenshotCategoryItems = remember(smartCandidates) { smartCandidates.filter { it.isScreenshot } }
    val videoCategoryItems = remember(smartCandidates) { smartCandidates.filter { it.mimeType.startsWith("video") } }

    val categories = remember(duplicateCategoryItems, largeCategoryItems, screenshotCategoryItems, videoCategoryItems) {
        listOf(
            SmartCleanCategoryUiModel("Duplicates", duplicateCategoryItems, Icons.Default.ContentCopy),
            SmartCleanCategoryUiModel("Large Files", largeCategoryItems, Icons.Default.FolderOpen),
            SmartCleanCategoryUiModel("Screenshots", screenshotCategoryItems, Icons.Default.Image),
            SmartCleanCategoryUiModel("Videos", videoCategoryItems, Icons.Default.VideoLibrary)
        )
    }
    val selectedCategory = remember(activeCategory, categories) {
        categories.firstOrNull { it.title == activeCategory } ?: categories.first()
    }

    val selectedUris = remember { mutableStateListOf<String>() }
    var previewItem by remember { mutableStateOf<FileItem?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var friendlyMessage by remember { mutableStateOf<String?>(null) }
    val selectedSimpleItems = remember(selectedUris, mediaByUri) {
        selectedUris.mapNotNull { uri -> mediaByUri[uri] }
    }

    LaunchedEffect(selectedCategory.items, activeCategory) {
        selectedUris.clear()
        val autoSelectedUris = if (activeCategory == "Duplicates") {
            selectedCategory.items.filter { it.isDuplicate && !it.isBestDuplicateCopy }
        } else {
            selectedCategory.items.filter { it.selectionHint().safety == AutoSelectionSafety.SAFE_TO_DELETE }
        }
            .map { it.uri.toString() }
            .distinct()
        selectedUris.addAll(autoSelectedUris)
    }

    val statFs = remember { runCatching { StatFs(Environment.getDataDirectory().path) }.getOrNull() }
    val totalStorage = statFs?.totalBytes ?: 0L
    val usedStorage = ((statFs?.totalBytes ?: 0L) - (statFs?.availableBytes ?: 0L)).coerceAtLeast(0L).coerceAtMost(totalStorage)
    val freeStorage = (totalStorage - usedStorage).coerceAtLeast(0L)
    val usedPercent = if (totalStorage <= 0L) 0f else ((usedStorage.toFloat() / totalStorage.toFloat()) * 100f).coerceIn(0f, 100f)

    LaunchedEffect(fileItems, smartCandidates) {
        isScanning = true
        val startedAt = System.currentTimeMillis()
        listOf(
            "Scanning your files...",
            "Finding duplicates...",
            "Analyzing large videos..."
        ).forEach { stage ->
            scanningStatus = stage
            delay(kotlin.random.Random.nextLong(280L, 520L))
        }
        val elapsed = System.currentTimeMillis() - startedAt
        val minDelay = 1100L
        if (elapsed < minDelay) delay(minDelay - elapsed)
        isScanning = false
    }

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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    CircularProgressIndicator()
                    Text(scanningStatus, color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
                }
            }
            return@Scaffold
        }

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 86.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PremiumMetricCard(
                    totalFiles = smartCandidates.size,
                    totalSize = formatSize(smartCandidates.sumOf { file -> file.size })
                )
            }

            item {
                StoragePremiumCard(
                    usedBytes = usedStorage,
                    freeBytes = freeStorage,
                    cleanableBytes = smartCandidates.sumOf { file -> file.size },
                    percent = usedPercent
                )
            }

            item {
                Text("Categories", style = MaterialTheme.typography.titleMedium, color = TextMain)
            }

            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categories.forEach { category ->
                        QuickActionChip(
                            label = "${category.title} (${category.items.size})",
                            icon = category.icon,
                            selected = activeCategory == category.title,
                            onClick = { activeCategory = category.title }
                        )
                    }
                }
            }

            item {
                if (selectedCategory.items.isEmpty()) {
                    FriendlyState(
                        icon = Icons.Default.CheckCircle,
                        title = "Nothing to clean here",
                        subtitle = "No files matched ${selectedCategory.title.lowercase()} right now."
                    )
                } else {
                    Text(
                        text = "${selectedCategory.items.size} files in ${selectedCategory.title}",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                }
            }

            if (selectedCategory.items.isNotEmpty()) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegitButton(
                            text = "Select safe files",
                            onClick = {
                                val safe = selectedCategory.items.filter {
                                    if (activeCategory == "Duplicates") it.isDuplicate && !it.isBestDuplicateCopy
                                    else it.selectionHint().safety == AutoSelectionSafety.SAFE_TO_DELETE
                                }.map { it.uri.toString() }
                                selectedUris.clear()
                                selectedUris.addAll(safe)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LegitButton(
                            text = "Deselect all",
                            onClick = { selectedUris.clear() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(120.dp),
                        modifier = Modifier.fillMaxWidth().height(500.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(selectedCategory.items, key = { it.uri.toString() }) { item ->
                    val isSelected = selectedUris.contains(item.uri.toString())
                    SmartCleanFileCard(
                        file = item,
                        isSelected = isSelected,
                        onOpenPreview = {
                            previewItem = item
                        },
                        onToggleSelection = {
                            if (isSelected) {
                                selectedUris.remove(item.uri.toString())
                            } else {
                                selectedUris.add(item.uri.toString())
                            }
                        }
                    )
                }
                    }
                }

            }
        }
            AnimatedVisibility(
                visible = selectedSimpleItems.isNotEmpty(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${selectedSimpleItems.size} selected • ${formatSize(selectedSimpleItems.sumOf { it.size })}", modifier = Modifier.weight(1f))
                        LegitButton(text = "Delete selected", onClick = {
                            if (selectedSimpleItems.isEmpty()) {
                                friendlyMessage = "Select files first, then try deleting."
                            } else {
                                showDeleteConfirm = true
                            }
                        })
                    }
                }
            }
        }
    }
    previewItem?.let { file ->
        PreviewDialog(file = file, isSelected = selectedUris.contains(file.uri.toString()), onToggleSelection = {
            if (selectedUris.contains(file.uri.toString())) selectedUris.remove(file.uri.toString()) else selectedUris.add(file.uri.toString())
        }, onBack = { previewItem = null })
    }
    if (showDeleteConfirm) {
        val selectedBytes = selectedSimpleItems.sumOf { it.size }
        val categoryNames = selectedSimpleItems
            .mapNotNull { item ->
                selectedCategory.items.firstOrNull { it.uri == item.uri }?.let { selectedCategory.title }
            }
            .toSet()
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete selected files?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("You will free ${formatSize(selectedBytes)}.")
                    Text("Selected files will be removed from your device.")
                    Text("This action cannot be undone.")
                    Text("Nothing else will be deleted.")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteItemsRequested(selectedSimpleItems)
                    onCleanupRecorded(selectedBytes)
                    friendlyMessage = buildString {
                        append("Your chat media is lighter now • ${selectedSimpleItems.size} files")
                        if (categoryNames.isNotEmpty()) append(" • ${categoryNames.joinToString()}")
                        append(" • ${DateFormat.format(\"MMM d, h:mm a\", System.currentTimeMillis())}")
                    }
                }) { Text("Delete Safely") }
            }
        )
    }
    friendlyMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { friendlyMessage = null },
            title = { Text("Cleanup receipt") },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { friendlyMessage = null }) { Text("OK") } }
        )
    }
}

private const val LARGE_FILE_BYTES = 50L * 1024L * 1024L

data class FileItem(
    val uri: android.net.Uri,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val isDuplicate: Boolean = false,
    val isScreenshot: Boolean = false,
    val isWhatsapp: Boolean = false,
    val path: String = "",
    val mimeType: String = "",
    val duplicateGroupKey: String? = null,
    val isBestDuplicateCopy: Boolean = false
)


private enum class AutoSelectionSafety {
    SAFE_TO_DELETE,
    REVIEW_CAREFULLY,
    PROTECTED
}

private data class SelectionHint(
    val safety: AutoSelectionSafety,
    val label: String
)

private fun FileItem.selectionHint(nowMillis: Long = System.currentTimeMillis()): SelectionHint {
    val ageDays = (nowMillis - lastModified).coerceAtLeast(0L) / (1000L * 60L * 60L * 24L)
    val isVeryRecent = ageDays <= 3
    val isVideo = mimeType.startsWith("video", ignoreCase = true)
    val isUniqueLargeVideo = isVideo && size > LARGE_FILE_BYTES && !isDuplicate
    val isSentFolderMedia = path.contains("/sent", ignoreCase = true) || path.contains(" sent", ignoreCase = true)
    val personalPhotoLike = mimeType.startsWith("image", ignoreCase = true) && !isDuplicate &&
        !path.contains("status", ignoreCase = true) &&
        !path.contains("sticker", ignoreCase = true) &&
        !name.contains("meme", ignoreCase = true) && !path.contains("meme", ignoreCase = true)

    val shouldProtect = isVeryRecent || isUniqueLargeVideo || isSentFolderMedia || personalPhotoLike ||
        (!isDuplicate &&
            !path.contains("status", ignoreCase = true) &&
            !path.contains("sticker", ignoreCase = true) &&
            !name.contains("meme", ignoreCase = true) && !path.contains("meme", ignoreCase = true))

    return when {
        shouldProtect && (isVeryRecent || isUniqueLargeVideo || isSentFolderMedia) -> SelectionHint(AutoSelectionSafety.PROTECTED, "Protected from auto-selection")
        shouldProtect -> SelectionHint(AutoSelectionSafety.REVIEW_CAREFULLY, "Review carefully")
        else -> SelectionHint(AutoSelectionSafety.SAFE_TO_DELETE, "Safe to delete")
    }
}


private fun calculateScore(file: FileItem): Int {
    var score = 0
    if (file.size > LARGE_FILE_BYTES) score += 20
    val daysOld = (System.currentTimeMillis() - file.lastModified) / (1000L * 60L * 60L * 24L)
    if (daysOld > 30) score += 20
    if (file.isDuplicate) score += 40
    if (file.isScreenshot) score += 10
    if (file.isWhatsapp) score += 10
    return score.coerceIn(0, 100)
}

private fun junkReasons(file: FileItem): List<String> {
    val daysOld = (System.currentTimeMillis() - file.lastModified) / (1000L * 60L * 60L * 24L)
    return buildList {
        if (file.isDuplicate) add("Duplicate file")
        if (daysOld > 30) add("Not used for ${daysOld} days")
        if (file.size > LARGE_FILE_BYTES) add("Large file")
        if (file.isWhatsapp) add("WhatsApp media")
        if (file.isScreenshot) add("Screenshot")
    }
}

@Composable
private fun SmartCleanFileCard(
    file: FileItem,
    isSelected: Boolean,
    onOpenPreview: () -> Unit,
    onToggleSelection: () -> Unit
) {
    var imageFailed by remember(file.uri.toString()) { mutableStateOf(false) }
    val reasonText = remember(file) { junkReasons(file).firstOrNull().orEmpty() }
    val hint = remember(file) { file.selectionHint() }
    val statusText = when {
        file.isDuplicate -> if (file.isBestDuplicateCopy) "Best copy kept" else "Duplicates selected"
        else -> hint.label
    }

    Card(
        modifier = Modifier
            .fillMaxWidth().combinedClickable(onClick = onOpenPreview, onLongClick = onToggleSelection),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 5.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(contentAlignment = Alignment.TopEnd) {
                AsyncImage(
                    model = file.uri,
                    contentDescription = file.name,
                    contentScale = ContentScale.Crop,
                    onError = { imageFailed = true },
                    modifier = Modifier.size(110.dp).clip(RoundedCornerShape(12.dp))
                )
                Checkbox(checked = isSelected, onCheckedChange = { onToggleSelection() })
                if (imageFailed) Text("No preview", modifier = Modifier.align(Alignment.Center))
            }
            Text(formatSize(file.size), style = MaterialTheme.typography.labelSmall)
            if (file.mimeType.startsWith("video")) Text("Video", style = MaterialTheme.typography.labelSmall)
            Text(file.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
            Text(if (hint.safety == AutoSelectionSafety.SAFE_TO_DELETE) "Safe" else "Risk", style = MaterialTheme.typography.labelSmall, color = AccentBlue)
            if (file.isBestDuplicateCopy) Text("Best copy", style = MaterialTheme.typography.labelSmall, color = AccentGreen)
            if (hint.safety == AutoSelectionSafety.PROTECTED) Text("Protected", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun PreviewDialog(file: FileItem, isSelected: Boolean, onToggleSelection: () -> Unit, onBack: () -> Unit) {
    AlertDialog(onDismissRequest = onBack, title = { Text("Preview") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AsyncImage(model = file.uri, contentDescription = file.name, modifier = Modifier.fillMaxWidth().height(220.dp), contentScale = ContentScale.Fit)
            Text("File: ${file.name}")
            Text("Size: ${formatSize(file.size)}")
            Text("Date: ${DateFormat.format("yyyy-MM-dd HH:mm", file.lastModified)}")
            Text("Folder: ${file.path.substringBeforeLast("/", "")}")
            Text("Category: ${if (file.isDuplicate) "Duplicates" else if (file.mimeType.startsWith("video")) "Videos" else "General"}")
            Text("Risk: ${file.selectionHint().label}")
        }
    }, confirmButton = { TextButton(onClick = onToggleSelection) { Text(if (isSelected) "Unselect" else "Select") } }, dismissButton = { TextButton(onClick = onBack) { Text("Back") } })
}

private data class SmartCleanCategoryUiModel(
    val title: String,
    val items: List<FileItem>,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${category.items.size} files",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            category.items
                .take(3)
                .forEach { item ->
                    Text(
                        text = "• ${item.name}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Smart Clean", color = MaterialTheme.colorScheme.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("AI-powered cleanup suggestions", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumPillStat("$totalFiles", "Total files")
                PremiumPillStat(totalSize, "Total size")
            }
        }
    }
}

@Composable
private fun PremiumPillStat(value: String, label: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
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
        Text("✨ Start Smart Cleaning", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StoragePremiumCard(usedBytes: Long, freeBytes: Long, cleanableBytes: Long, percent: Float) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Storage overview", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                Text("Used: ${formatSize(usedBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Free: ${formatSize(freeBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        "Cleanable ${formatSize(cleanableBytes)}",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { (percent / 100f).coerceIn(0f, 1f) },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 7.dp,
                    modifier = Modifier.size(58.dp)
                )
                Text("${percent.toInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
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
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Text(item.name, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
                Text(formatSize(item.sizeKb.toLong() * 1024L), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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
private fun SimpleActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showAction: Boolean = true
) {
    LegitCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, contentDescription = null, tint = AccentBlue)
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TextMain, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (showAction) {
                LegitButton("Open", onClick = onClick)
            }
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

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    FeatureScreenScaffold("Privacy Policy", "How your data is handled", onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SimpleActionCard(
                    icon = Icons.Default.Policy,
                    title = "No personal data collection",
                    subtitle = "This app does not collect personal information, contacts, or message contents.",
                    onClick = {},
                    showAction = false
                )
            }
            item {
                SimpleActionCard(
                    icon = Icons.Default.Security,
                    title = "Local processing only",
                    subtitle = "Scanning and cleanup recommendations run on your device.",
                    onClick = {},
                    showAction = false
                )
            }
            item {
                SimpleActionCard(
                    icon = Icons.Default.FolderOpen,
                    title = "Storage permission usage",
                    subtitle = "Media permission is used only to read files for analysis and cleanup.",
                    onClick = {},
                    showAction = false
                )
            }
            item {
                SimpleActionCard(
                    icon = Icons.Default.CheckCircle,
                    title = "No data sharing",
                    subtitle = "Your media files are not uploaded, sold, or shared with third parties.",
                    onClick = {},
                    showAction = false
                )
            }
            item {
                SimpleActionCard(
                    icon = Icons.Default.Info,
                    title = "Independent utility app",
                    subtitle = "This app is not affiliated with, endorsed by, or sponsored by WhatsApp LLC or Meta Platforms, Inc.",
                    onClick = {},
                    showAction = false
                )
            }
        }
    }
}

@Composable
fun TermsAndConditionsScreen(onBack: () -> Unit) {
    FeatureScreenScaffold("Terms & Conditions", "Usage responsibilities", onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SimpleActionCard(
                    icon = Icons.Default.Gavel,
                    title = "Suggestions, not guarantees",
                    subtitle = "The app provides cleanup suggestions to help you decide what to remove.",
                    onClick = {},
                    showAction = false
                )
            }
            item {
                SimpleActionCard(
                    icon = Icons.Default.DeleteSweep,
                    title = "User responsibility",
                    subtitle = "You are responsible for confirming deletions, and deleted files may not be recoverable.",
                    onClick = {},
                    showAction = false
                )
            }
            item {
                SimpleActionCard(
                    icon = Icons.Default.Info,
                    title = "No affiliation",
                    subtitle = "ChatSweep is an independent utility and is not affiliated with WhatsApp LLC or Meta Platforms, Inc.",
                    onClick = {},
                    showAction = false
                )
            }
        }
    }
}
