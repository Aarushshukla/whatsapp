@file:OptIn(ExperimentalFoundationApi::class)

package com.example.whatsappcleaner.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ScreenshotMonitor
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.components.LegitButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class AiFeature(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val actionLabel: String
) {
    SMART_SUGGESTIONS(
        title = "Smart Suggestions",
        subtitle = "AI-picked files to review",
        description = "Review AI-ranked media candidates before cleanup.",
        icon = Icons.Default.AutoAwesome,
        actionLabel = "Review Files"
    ),
    DUPLICATE_DETECTOR(
        title = "Duplicate Detector",
        subtitle = "Find repeated media",
        description = "Detect repeated photos and videos to recover fast storage.",
        icon = Icons.Default.ContentCopy,
        actionLabel = "Scan Now"
    ),
    LARGE_FILES_FINDER(
        title = "Large Files Finder",
        subtitle = "Clean biggest space hogs",
        description = "Surface heavy media files that occupy the most storage.",
        icon = Icons.Default.FolderOpen,
        actionLabel = "Scan Now"
    ),
    OLD_MEDIA_CLEANER(
        title = "Old Media Cleaner",
        subtitle = "Review stale files",
        description = "Find older media that is likely safe to clean.",
        icon = Icons.Default.Schedule,
        actionLabel = "Review Files"
    ),
    WHATSAPP_MEDIA_CLEANER(
        title = "WhatsApp Media Cleaner",
        subtitle = "Tame forwarded media",
        description = "Inspect forwarded WhatsApp media and remove noise quickly.",
        icon = Icons.Default.Collections,
        actionLabel = "Auto Select"
    ),
    MEME_CLEANER(
        title = "Meme Cleaner",
        subtitle = "Review funny but bulky files",
        description = "Browse meme-heavy media and keep only the best ones.",
        icon = Icons.Default.Image,
        actionLabel = "Review Files"
    ),
    BLURRY_PHOTOS(
        title = "Blurry Photos",
        subtitle = "Identify weak-quality shots",
        description = "Collect low-clarity photos for quick cleanup decisions.",
        icon = Icons.Default.BrokenImage,
        actionLabel = "Scan Now"
    ),
    SCREENSHOTS_CLEANER(
        title = "Screenshots Cleaner",
        subtitle = "Remove temporary captures",
        description = "Review screenshots and delete temporary captures in one pass.",
        icon = Icons.Default.ScreenshotMonitor,
        actionLabel = "Review Files"
    ),
    SPAM_MEDIA_DETECTOR(
        title = "Spam Media Detector",
        subtitle = "Detect likely junk media",
        description = "Prioritize media that appears promotional, duplicated, or spammy.",
        icon = Icons.Default.Shield,
        actionLabel = "Scan Now"
    )
}

val AiFeatureItems: List<AiFeature> = listOf(
    AiFeature.SMART_SUGGESTIONS,
    AiFeature.DUPLICATE_DETECTOR,
    AiFeature.LARGE_FILES_FINDER,
    AiFeature.OLD_MEDIA_CLEANER,
    AiFeature.WHATSAPP_MEDIA_CLEANER,
    AiFeature.MEME_CLEANER,
    AiFeature.BLURRY_PHOTOS,
    AiFeature.SCREENSHOTS_CLEANER,
    AiFeature.SPAM_MEDIA_DETECTOR
)

data class AiGroupData(
    val title: String,
    val features: List<AiFeature>
)

private val AiGroups = listOf(
    AiGroupData(
        title = "Smart AI",
        features = listOf(
            AiFeature.SMART_SUGGESTIONS,
            AiFeature.DUPLICATE_DETECTOR,
            AiFeature.SPAM_MEDIA_DETECTOR
        )
    ),
    AiGroupData(
        title = "Storage",
        features = listOf(
            AiFeature.LARGE_FILES_FINDER,
            AiFeature.OLD_MEDIA_CLEANER,
            AiFeature.WHATSAPP_MEDIA_CLEANER
        )
    ),
    AiGroupData(
        title = "Media Types",
        features = listOf(
            AiFeature.MEME_CLEANER,
            AiFeature.BLURRY_PHOTOS,
            AiFeature.SCREENSHOTS_CLEANER
        )
    )
)

@Composable
fun PremiumSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AiFeatureCard(
    feature: AiFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "ai_feature_card_scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 6.dp,
        animationSpec = tween(durationMillis = 180),
        label = "ai_feature_card_elevation"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isPressed) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(durationMillis = 150),
        label = "ai_feature_card_container"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = feature.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open ${feature.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AiGroup(
    group: AiGroupData,
    onFeatureClick: (AiFeature) -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 380)) +
            slideInVertically(
                animationSpec = tween(durationMillis = 420),
                initialOffsetY = { it / 5 }
            ),
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                group.features.forEach { feature ->
                    AiFeatureCard(
                        feature = feature,
                        onClick = { onFeatureClick(feature) },
                        modifier = Modifier.fillMaxWidth(0.5f)
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AiToolsSection(
    onFeatureClick: (AiFeature) -> Unit,
    modifier: Modifier = Modifier
) {
    var groupsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(60)
        groupsVisible = true
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PremiumSectionHeader(
                title = "AI Tools",
                subtitle = "Premium cleanup intelligence built for faster media decisions."
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AiGroups.forEach { group ->
                    AiGroup(
                        group = group,
                        onFeatureClick = onFeatureClick,
                        visible = groupsVisible
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiFeatureScreenScaffold(
    feature: AiFeature,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(feature.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                        Text(feature.subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            content()
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    helper: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(helper, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SmoothPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LegitButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun AiFeatureDetailScreen(
    feature: AiFeature,
    stats: List<Triple<String, String, String>>,
    items: List<com.example.whatsappcleaner.data.local.SimpleMediaItem>,
    onBack: () -> Unit,
    onActionClick: () -> Unit,
    onDeleteItemsRequested: (List<com.example.whatsappcleaner.data.local.SimpleMediaItem>) -> Unit,
    processItems: suspend (List<com.example.whatsappcleaner.data.local.SimpleMediaItem>) -> List<com.example.whatsappcleaner.data.local.SimpleMediaItem> = { source -> source }
) {
    var isProcessing by remember { mutableStateOf(false) }
    var hasResults by remember { mutableStateOf(false) }
    var processedItems by remember(items) { mutableStateOf(items) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var previewUri by remember { mutableStateOf<android.net.Uri?>(null) }

    LaunchedEffect(processedItems) {
        selectedIds = selectedIds.filter { selectedId -> processedItems.any { it.id == selectedId } }.toSet()
    }

    AiFeatureScreenScaffold(feature = feature, onBack = onBack) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (hasResults && processedItems.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SmoothPrimaryButton(
                            text = "Select All",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedIds = if (selectedIds.size == processedItems.size) {
                                    emptySet()
                                } else {
                                    processedItems.map { it.id }.toSet()
                                }
                            }
                        )
                        SmoothPrimaryButton(
                            text = "Delete Selected (${selectedIds.size})",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val selectedItems = processedItems.filter { it.id in selectedIds }
                                if (selectedItems.isNotEmpty()) {
                                    onDeleteItemsRequested(selectedItems)
                                    selectedIds = emptySet()
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(feature.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        stats.forEach { (title, value, helper) -> StatsCard(title = title, value = value, helper = helper) }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(14.dp).animateContentSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = "Take action", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            SmoothPrimaryButton(
                                text = if (isProcessing) "Scanning..." else feature.actionLabel,
                                onClick = {
                                    if (isProcessing) return@SmoothPrimaryButton
                                    onActionClick()
                                    isProcessing = true
                                }
                            )
                            if (isProcessing) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text("Scanning...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                LaunchedEffect(feature, items) {
                                    processedItems = withContext(Dispatchers.Default) { processItems(items) }
                                    hasResults = true
                                    isProcessing = false
                                }
                            }
                            if (hasResults && processedItems.isNotEmpty()) {
                                val totalBytes = processedItems.sumOf { it.size }
                                Text(
                                    text = "${processedItems.size} images found • ${formatSize(totalBytes)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (!hasResults || processedItems.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateCard(
                            title = "No processed results yet",
                            body = "Run ${feature.actionLabel.lowercase()} to prepare an actionable list for review."
                        )
                    }
                } else {
                    items(
                        processedItems,
                        key = { it.id }
                    ) { mediaItem ->
                        AiResultImageCard(
                            mediaItem = mediaItem,
                            selected = mediaItem.id in selectedIds,
                            onOpenPreview = { previewUri = mediaItem.uri },
                            onToggleSelection = {
                                selectedIds = if (mediaItem.id in selectedIds) {
                                    selectedIds - mediaItem.id
                                } else {
                                    selectedIds + mediaItem.id
                                }
                            }
                        )
                    }
                }
            }
        }

        previewUri?.let { uri ->
            ImagePreviewScreen(uri = uri, onBack = { previewUri = null })
        }
    }
}

@Composable
private fun AiResultImageCard(
    mediaItem: com.example.whatsappcleaner.data.local.SimpleMediaItem,
    selected: Boolean,
    onOpenPreview: () -> Unit,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(6.dp)
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onOpenPreview,
                onLongClick = onToggleSelection
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            coil.compose.AsyncImage(
                model = mediaItem.uri,
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color(0x55000000))
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                )
            }
        }
    }
}

@Composable
fun SmartSuggestionsFeatureScreen(totalSuggested: Int, totalSpaceToFree: Long, onBack: () -> Unit, items: List<com.example.whatsappcleaner.data.local.SimpleMediaItem>, onDeleteItemsRequested: (List<com.example.whatsappcleaner.data.local.SimpleMediaItem>) -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.SMART_SUGGESTIONS,
        stats = listOf(
            Triple("Suggestions", totalSuggested.toString(), "AI-prioritized items ready for review"),
            Triple("Potential savings", formatSize(totalSpaceToFree), "Estimated recoverable storage")
        ),
        items = items,
        onBack = onBack,
        onActionClick = {},
        onDeleteItemsRequested = onDeleteItemsRequested
    )
}

@Composable
fun DuplicateDetectorFeatureScreen(duplicateCount: Int, onBack: () -> Unit, items: List<com.example.whatsappcleaner.data.local.SimpleMediaItem>, onDeleteItemsRequested: (List<com.example.whatsappcleaner.data.local.SimpleMediaItem>) -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.DUPLICATE_DETECTOR,
        stats = listOf(
            Triple("Detected duplicates", duplicateCount.toString(), "Matched by file name + size")
        ),
        items = items,
        onBack = onBack,
        onActionClick = {},
        onDeleteItemsRequested = onDeleteItemsRequested,
        processItems = { source ->
            source.groupBy { "${it.name.lowercase()}_${it.size}" }
                .values
                .filter { group -> group.size > 1 }
                .flatten()
        }
    )
}

@Composable
fun LargeFilesFinderFeatureScreen(count: Int, totalBytes: Long, onBack: () -> Unit, items: List<com.example.whatsappcleaner.data.local.SimpleMediaItem>, onDeleteItemsRequested: (List<com.example.whatsappcleaner.data.local.SimpleMediaItem>) -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.LARGE_FILES_FINDER,
        stats = listOf(
            Triple("Large files", count.toString(), "Files over 10MB"),
            Triple("Heavy media size", formatSize(totalBytes), "Sorted largest first")
        ),
        items = items,
        onBack = onBack,
        onActionClick = {},
        onDeleteItemsRequested = onDeleteItemsRequested,
        processItems = { source -> source.sortedByDescending { it.size } }
    )
}

@Composable
fun OldMediaCleanerFeatureScreen(count: Int, onBack: () -> Unit, items: List<com.example.whatsappcleaner.data.local.SimpleMediaItem>, onDeleteItemsRequested: (List<com.example.whatsappcleaner.data.local.SimpleMediaItem>) -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.OLD_MEDIA_CLEANER,
        stats = listOf(Triple("Old files", count.toString(), "Older than 30 days")),
        items = items,
        onBack = onBack,
        onActionClick = {},
        onDeleteItemsRequested = onDeleteItemsRequested
    )
}

@Composable
fun WhatsAppMediaCleanerFeatureScreen(sentCount: Int, onBack: () -> Unit, items: List<com.example.whatsappcleaner.data.local.SimpleMediaItem>, onDeleteItemsRequested: (List<com.example.whatsappcleaner.data.local.SimpleMediaItem>) -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.WHATSAPP_MEDIA_CLEANER,
        stats = listOf(Triple("Junk candidates", sentCount.toString(), "Small WhatsApp images under 200KB")),
        items = items,
        onBack = onBack,
        onActionClick = {},
        onDeleteItemsRequested = onDeleteItemsRequested
    )
}

@Composable
fun MemeCleanerFeatureScreen(memeCount: Int, onBack: () -> Unit, items: List<com.example.whatsappcleaner.data.local.SimpleMediaItem>, onDeleteItemsRequested: (List<com.example.whatsappcleaner.data.local.SimpleMediaItem>) -> Unit) {
    AiFeatureDetailScreen(feature = AiFeature.MEME_CLEANER, stats = listOf(Triple("Meme candidates", memeCount.toString(), "Detected by classifier")), items = items, onBack = onBack, onActionClick = {}, onDeleteItemsRequested = onDeleteItemsRequested)
}

@Composable
fun BlurryPhotosFeatureScreen(imageCount: Int, onBack: () -> Unit, items: List<com.example.whatsappcleaner.data.local.SimpleMediaItem>, onDeleteItemsRequested: (List<com.example.whatsappcleaner.data.local.SimpleMediaItem>) -> Unit) {
    AiFeatureDetailScreen(feature = AiFeature.BLURRY_PHOTOS, stats = listOf(Triple("Blurry photos", imageCount.toString(), "Low-sharpness images")), items = items, onBack = onBack, onActionClick = {}, onDeleteItemsRequested = onDeleteItemsRequested)
}

@Composable
fun ScreenshotsCleanerFeatureScreen(screenshotCount: Int, onBack: () -> Unit, items: List<com.example.whatsappcleaner.data.local.SimpleMediaItem>, onDeleteItemsRequested: (List<com.example.whatsappcleaner.data.local.SimpleMediaItem>) -> Unit) {
    AiFeatureDetailScreen(feature = AiFeature.SCREENSHOTS_CLEANER, stats = listOf(Triple("Screenshots", screenshotCount.toString(), "Temporary captures")), items = items, onBack = onBack, onActionClick = {}, onDeleteItemsRequested = onDeleteItemsRequested)
}

@Composable
fun SpamMediaDetectorFeatureScreen(spamCount: Int, onBack: () -> Unit, items: List<com.example.whatsappcleaner.data.local.SimpleMediaItem>, onDeleteItemsRequested: (List<com.example.whatsappcleaner.data.local.SimpleMediaItem>) -> Unit) {
    AiFeatureDetailScreen(feature = AiFeature.SPAM_MEDIA_DETECTOR, stats = listOf(Triple("Potential spam", spamCount.toString(), "Likely junk media")), items = items, onBack = onBack, onActionClick = {}, onDeleteItemsRequested = onDeleteItemsRequested)
}
