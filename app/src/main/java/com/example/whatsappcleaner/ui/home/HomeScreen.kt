@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.whatsappcleaner.ui.home

import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.statusBarsPadding
import coil.compose.AsyncImage
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.components.FriendlyState
import com.example.whatsappcleaner.ui.components.GradientHeroButton
import com.example.whatsappcleaner.ui.components.LegitButton
import com.example.whatsappcleaner.ui.components.LegitCard
import com.example.whatsappcleaner.ui.components.StorageRing
import com.example.whatsappcleaner.ui.components.shimmerEffect
import com.example.whatsappcleaner.ui.navigation.AppDrawer
import com.example.whatsappcleaner.ui.theme.AccentBlue
import com.example.whatsappcleaner.ui.theme.AccentError
import com.example.whatsappcleaner.ui.theme.AccentGreen
import com.example.whatsappcleaner.ui.theme.AccentPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun SimpleMediaItem.safeDisplayName(): String = name.ifBlank { "Media file" }
private const val HOME_SCREEN_TAG = "HomeScreen"

private fun SimpleMediaItem.safeMimeLabel(): String = mimeType.orEmpty().ifBlank { "Unknown media" }

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SimpleHomeScreen(
    items: List<SimpleMediaItem>,
    onRefreshClick: () -> Unit,
    summaryInfo: String,
    isLoading: Boolean,
    currentFilter: MediaFilter,
    onFilterChange: (MediaFilter) -> Unit,
    remindersEnabled: Boolean,
    onRemindersToggle: (Boolean) -> Unit,
    memeCount: Int,
    spamCount: Int,
    junkCount: Int,
    duplicateCount: Int,
    isProUser: Boolean,
    onNavigateToSmartClean: () -> Unit,
    onNavigateToPhoneReality: () -> Unit,
    onNavigateToMemeAnalyzer: () -> Unit,
    onNavigateToMediaViewer: () -> Unit,
    onNavigateToJunk: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSpam: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDuplicates: () -> Unit,
    onBulkDeleteClick: () -> Unit,
    onUpgradeToPro: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDeleteItemsRequested: (List<SimpleMediaItem>) -> Unit,
    onOpenInSystem: (SimpleMediaItem) -> Unit,
    onOpenSystemStorage: () -> Unit,
    pendingDeleteUris: Set<String>,
    isDeleteInProgress: Boolean,
    deleteSnackbarMessage: String?,
    onUndoDelete: () -> Unit,
    onDeleteSnackbarConsumed: () -> Unit,
    selectedFrequency: ReminderFreq,
    onFrequencyChange: (ReminderFreq) -> Unit,
    selectedTime: ReminderTime,
    allTimeOptions: List<ReminderTime>,
    onTimeChange: (ReminderTime) -> Unit,
    largeTodayCount: Int,
    largeTodaySizeText: String,
    screenshotTodayCount: Int,
    screenshotTodaySizeText: String,
    activeSuggestion: SuggestionType,
    onSuggestionChange: (SuggestionType) -> Unit,
    totalFiles: Int,
    totalSize: Long,
    oldFilesCount: Int,
    smartSuggestionSummary: SmartSuggestionSummary,
    smartSuggestedItems: List<SimpleMediaItem>,
    suggestionReasonsByUri: Map<String, List<String>>,
    onNavigateToFeatures: () -> Unit,
    onAiFeatureClick: (AiFeature) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedItems = remember { mutableStateMapOf<Long, Boolean>() }
    val selectedCount by remember { derivedStateOf { selectedItems.count { it.value } } }
    val selectedIdSet by remember {
        derivedStateOf {
            selectedItems.asSequence()
                .filter { it.value }
                .map { it.key }
                .toSet()
        }
    }
    val allItemsSelected by remember(items.size, selectedCount) {
        derivedStateOf { items.isNotEmpty() && selectedCount == items.size }
    }

    fun toggleSelection(item: SimpleMediaItem) {
        val isSelected = selectedItems[item.id] == true
        if (isSelected) {
            selectedItems.remove(item.id)
        } else {
            selectedItems[item.id] = true
        }
    }
    var pendingDeleteItems by remember { mutableStateOf<List<SimpleMediaItem>>(emptyList()) }
    var previewItem by remember { mutableStateOf<SimpleMediaItem?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val gridState = rememberLazyGridState()

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(1800)
            successMessage = null
        }
    }
    LaunchedEffect(items) {
        val validIds = items.map { mediaItem -> mediaItem.id }.toSet()
        val staleIds = selectedItems.keys.filterNot { selectedId -> selectedId in validIds }
        staleIds.forEach { staleId -> selectedItems.remove(staleId) }
    }
    LaunchedEffect(smartSuggestedItems) {
        val suggestionIds = smartSuggestedItems.map { mediaItem -> mediaItem.id }.toSet()
        if (suggestionIds.isNotEmpty()) {
            selectedItems.clear()
            suggestionIds.forEach { selectedId -> selectedItems[selectedId] = true }
        }
    }
    LaunchedEffect(deleteSnackbarMessage) {
        deleteSnackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message)
            onDeleteSnackbarConsumed()
        }
    }
    LaunchedEffect(pendingDeleteUris) {
        if (pendingDeleteUris.isEmpty()) {
            pendingDeleteItems = emptyList()
        }
    }

    val statFs = remember { runCatching { StatFs(Environment.getDataDirectory().path) }.getOrNull() }
    val totalDeviceBytes = statFs?.totalBytes ?: 0L
    val freeBytes = statFs?.availableBytes ?: 0L
    val usedBytes = (totalDeviceBytes - freeBytes).coerceAtLeast(0L)
    val storageProgress = if (totalDeviceBytes == 0L) 0f else usedBytes.toFloat() / totalDeviceBytes.toFloat()
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(90)
        contentVisible = true
    }
    if (isLoading && items.isEmpty()) {
        HomeLoadingScreen()
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("WhatsApp Cleaner", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Smart cleanup dashboard",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                windowInsets = WindowInsets.systemBars,
                modifier = Modifier.statusBarsPadding()
            )
        },
        snackbarHost = { PremiumSnackbarHost(snackbarHostState = snackbarHostState) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            state = gridState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(360)) + slideInVertically(initialOffsetY = { it / 3 })
                ) {
                    PremiumHeader(
                        title = "WhatsApp Cleaner",
                        subtitle = "Clear junk faster with AI-ranked cleanup suggestions",
                        remindersEnabled = remindersEnabled,
                        onRemindersToggle = onRemindersToggle
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SmartCleanButton(
                    cleanableSize = formatSize(smartSuggestionSummary.totalSpaceToFree),
                    cleanableCount = smartSuggestionSummary.totalSuggestedFiles,
                    onClick = onNavigateToSmartClean
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                AnimatedSuccessBanner(message = successMessage)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(480)) + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    PremiumStorageCard(
                        totalFiles = totalFiles,
                        totalSize = totalSize,
                        largeTodayCount = largeTodayCount,
                        largeTodaySizeText = largeTodaySizeText,
                        screenshotTodayCount = screenshotTodayCount,
                        screenshotTodaySizeText = screenshotTodaySizeText,
                        oldFilesCount = oldFilesCount,
                        cleanableSize = formatSize(smartSuggestionSummary.totalSpaceToFree),
                        progress = storageProgress,
                        usedSpace = formatSize(usedBytes),
                        freeSpace = formatSize(freeBytes),
                        onClick = onNavigateToAnalytics
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                QuickActionsRow(
                    selectedCount = selectedCount,
                    onDeleteClick = {
                        if (isDeleteInProgress) return@QuickActionsRow
                        val itemsToDelete = items.filter { mediaItem -> mediaItem.id in selectedIdSet }
                        if (itemsToDelete.isNotEmpty()) {
                            pendingDeleteItems = itemsToDelete
                        } else {
                            onBulkDeleteClick()
                        }
                    },
                    onSelectAllClick = {
                        if (allItemsSelected) {
                            selectedItems.clear()
                        } else {
                            selectedItems.clear()
                            items.forEach { selectedItem -> selectedItems[selectedItem.id] = true }
                        }
                    },
                    onFilterClick = {
                        val nextFilter = when (currentFilter) {
                            MediaFilter.ALL -> MediaFilter.IMAGES
                            MediaFilter.IMAGES -> MediaFilter.VIDEOS
                            MediaFilter.VIDEOS -> MediaFilter.MEMES
                            MediaFilter.MEMES -> MediaFilter.ALL
                            MediaFilter.DUPLICATES -> MediaFilter.ALL
                            MediaFilter.OTHER -> MediaFilter.ALL
                        }
                        onFilterChange(nextFilter)
                    },
                    onAiToolsClick = { onNavigateToFeatures() }
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                FilterTabs(currentFilter = currentFilter, onFilterChange = onFilterChange)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionTitle(
                    title = "Loaded files",
                    subtitle = "${items.size} files ready to review and clean"
                )
            }

            if (isLoading) {
                repeat(6) {
                    item { MediaGridShimmer() }
                }
            } else if (items.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LegitCard {
                        FriendlyState(
                            icon = Icons.Default.CheckCircle,
                            title = "You're all caught up",
                            message = "No obvious cleanup items found right now. New files and suggestions will appear after your next scan."
                        )
                    }
                }
            } else {
                gridItems(
                    items,
                    key = { mediaItem -> mediaItem.id },
                    span = { GridItemSpan(maxLineSpan) }
                ) { item ->
                    val isSelected = selectedItems[item.id] == true
                    PremiumMediaRow(
                        item = item,
                        selected = isSelected,
                        suggestionReason = suggestionReasonsByUri[item.uri.toString()]?.joinToString(" • "),
                        onImageClick = { previewItem = item },
                        onToggleSelection = { toggleSelection(item) },
                        onOpen = { onOpenInSystem(item) },
                        onKeep = {
                            val itemName = item.safeDisplayName()
                            successMessage = "Kept ${itemName.take(18)}"
                            scope.launch { snackbarHostState.showSnackbar("Kept $itemName") }
                        },
                        onDelete = {
                            if (!isDeleteInProgress) {
                                pendingDeleteItems = listOf(item)
                            }
                        }
                    )
                }
            }
        }
    }

    previewItem?.let { item ->
        ImagePreviewScreen(uri = item.uri, onBack = { previewItem = null })
    }

    if (pendingDeleteItems.isNotEmpty()) {
        val deleteCount = pendingDeleteItems.size
        AlertDialog(
            onDismissRequest = { pendingDeleteItems = emptyList() },
            title = { Text("Delete Files", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Text(
                    "Are you sure you want to delete $deleteCount file(s)?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                LegitButton(
                    text = "Delete",
                    enabled = !isDeleteInProgress,
                    onClick = {
                        val itemsToDelete = pendingDeleteItems
                        pendingDeleteItems = emptyList()
                        itemsToDelete.forEach { itemToDelete -> selectedItems.remove(itemToDelete.id) }
                        onDeleteConfirmed()
                        onDeleteItemsRequested(itemsToDelete)
                    }
                )
            },
            dismissButton = {
                LegitButton(text = "Cancel", onClick = { pendingDeleteItems = emptyList() })
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        )
    }
}

@Composable
private fun HomeLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Preparing cleaner...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF1F2937)
            )
        }
    }
}

@Composable
private fun SmartSuggestionsCard(
    summary: SmartSuggestionSummary,
    autoSelectedCount: Int
) {
    LegitCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentPurple)
                Text("Smart Suggestions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(
                "Potential cleanup: ${summary.totalSuggestedFiles} files • ${formatSize(summary.totalSpaceToFree)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Duplicates: ${summary.duplicateFiles} in ${summary.duplicateGroups} groups • Large: ${summary.largeFiles} • Old: ${summary.oldFiles}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Auto-selected: $autoSelectedCount recommended files",
                style = MaterialTheme.typography.labelLarge,
                color = AccentGreen
            )
        }
    }
}



@Composable
private fun PremiumHeader(
    title: String,
    subtitle: String,
    remindersEnabled: Boolean,
    onRemindersToggle: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daily reminders", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Switch(checked = remindersEnabled, onCheckedChange = onRemindersToggle)
            }
        }
    }
}

@Composable
private fun SmartCleanButton(
    cleanableSize: String,
    cleanableCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            GradientHeroButton(
                text = "Smart Clean",
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                icon = Icons.Default.AutoAwesome
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recommended now",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "$cleanableSize • $cleanableCount files",
                    style = MaterialTheme.typography.titleSmall,
                    color = AccentGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun MainActionsCard(
    remindersEnabled: Boolean,
    onRemindersToggle: (Boolean) -> Unit,
    onRefreshClick: () -> Unit,
    onExploreFeatures: () -> Unit
) {
    LegitCard {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daily reminders", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = onRemindersToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = AccentBlue,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickPill(
                    icon = Icons.Default.Refresh,
                    label = "Refresh",
                    onClick = onRefreshClick
                )
                QuickPill(
                    icon = Icons.Default.AutoAwesome,
                    label = "Explore Features",
                    onClick = onExploreFeatures
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    summaryInfo: String,
    remindersEnabled: Boolean,
    onRemindersToggle: (Boolean) -> Unit,
    selectedFrequency: ReminderFreq,
    selectedTime: ReminderTime,
    isProUser: Boolean,
    onMenuClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onStorageClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onUpgradeToPro: () -> Unit
) {
    LegitCard {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Cleanly AI", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text("Clean smarter. Free space instantly.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(summaryInfo, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    QuickPill(icon = Icons.Default.Refresh, label = "Refresh", onClick = onRefreshClick)
                }
                item {
                    QuickPill(icon = Icons.Default.Storage, label = "Storage", onClick = onStorageClick)
                }
                item {
                    QuickPill(icon = Icons.Default.Visibility, label = "Insights", onClick = onInsightsClick)
                }
                item {
                    QuickPill(icon = Icons.Default.Settings, label = "Settings", onClick = onSettingsClick)
                }
                item {
                    // TODO: RE-ENABLE SUBSCRIPTION LATER
                    /*
                    QuickPill(icon = Icons.Default.Star, label = if (isProUser) "Pro active" else "Go Pro", onClick = onUpgradeToPro)
                    */
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniMetricCard(
                    title = selectedFrequency.label,
                    subtitle = "Reminder cadence",
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.weight(1f)
                )
                MiniMetricCard(
                    title = selectedTime.label,
                    subtitle = "Next reminder",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily reminders",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = onRemindersToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = AccentBlue,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun QuickPill(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun PremiumStorageCard(
    totalFiles: Int,
    totalSize: Long,
    largeTodayCount: Int,
    largeTodaySizeText: String,
    screenshotTodayCount: Int,
    screenshotTodaySizeText: String,
    oldFilesCount: Int,
    cleanableSize: String,
    progress: Float,
    usedSpace: String,
    freeSpace: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Storage overview", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Used $usedSpace • Free $freeSpace", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(AccentGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                    Text(
                        "Cleanable now: $cleanableSize",
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "$largeTodayCount large files (${largeTodaySizeText.ifBlank { "review" }}) • $screenshotTodayCount screenshots (${screenshotTodaySizeText.ifBlank { "review" }})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "$totalFiles files • ${formatSize(totalSize)} • $oldFilesCount older files",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StorageRing(
                progress = progress,
                label = "${(progress * 100).toInt()}%",
                subtitle = "reviewable"
            )
        }
    }
}

@Composable
private fun AnimatedSuccessBanner(message: String?) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(animationSpec = tween(360)) + scaleIn(animationSpec = tween(360), initialScale = 0.82f),
        exit = fadeOut(animationSpec = tween(280)) + scaleOut(animationSpec = tween(280), targetScale = 0.9f)
    ) {
        if (message != null) {
            LegitCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AccentGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen)
                    Text(
                        text = message,
                        color = AccentGreen,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private data class InsightCardModel(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

@Composable
private fun InsightCard(model: InsightCardModel) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(220),
        label = "insight_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = model.onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(model.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(model.icon, contentDescription = null, tint = model.accent)
            }
            Text(model.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(model.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickActionsRow(
    selectedCount: Int,
    onDeleteClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onFilterClick: () -> Unit,
    onAiToolsClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Quick actions", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionChip(Icons.Default.DeleteOutline, "Delete ($selectedCount)", selectedCount > 0, onDeleteClick)
            QuickActionChip(Icons.Default.SelectAll, "Select all", true, onSelectAllClick)
            QuickActionChip(Icons.Default.Sort, "Sort / filter", true, onFilterClick)
            QuickActionButton(
                icon = Icons.Default.AutoAwesome,
                title = "AI tools",
                onClick = { onAiToolsClick() }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    QuickActionChip(
        icon = icon,
        label = title,
        enabled = true,
        onClick = onClick
    )
}

@Composable
private fun QuickActionChip(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(180),
        label = "action_button_scale"
    )
    FilterChip(
        selected = false,
        onClick = onClick,
        enabled = enabled,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(999.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = AccentBlue,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = false,
            borderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        interactionSource = interactionSource
    )
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterTabs(currentFilter: MediaFilter, onFilterChange: (MediaFilter) -> Unit) {
    val filters = listOf(
        MediaFilter.ALL to Pair("All", Icons.Default.FolderOpen),
        MediaFilter.IMAGES to Pair("Images", Icons.Default.Image),
        MediaFilter.VIDEOS to Pair("Videos", Icons.Default.VideoLibrary),
        MediaFilter.MEMES to Pair("Memes", Icons.Default.Mood)
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(filters) { (filter, descriptor) ->
            FilterChip(
                selected = currentFilter == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(descriptor.first) },
                leadingIcon = {
                    Icon(
                        imageVector = descriptor.second,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentBlue.copy(alpha = 0.18f),
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                    selectedLeadingIconColor = AccentBlue,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = currentFilter == filter,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = AccentBlue
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionStrip(
    activeSuggestion: SuggestionType,
    largeTodayCount: Int,
    screenshotTodayCount: Int,
    onSuggestionChange: (SuggestionType) -> Unit
) {
    LegitCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Quick wins", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SuggestionChip(
                    label = "Everything",
                    selected = activeSuggestion == SuggestionType.NONE,
                    icon = Icons.Default.FolderOpen
                ) { onSuggestionChange(SuggestionType.NONE) }
                SuggestionChip(
                    label = "Large today ($largeTodayCount)",
                    selected = activeSuggestion == SuggestionType.LARGE_TODAY,
                    icon = Icons.Default.CleaningServices
                ) { onSuggestionChange(SuggestionType.LARGE_TODAY) }
                SuggestionChip(
                    label = "Screenshots ($screenshotTodayCount)",
                    selected = activeSuggestion == SuggestionType.SCREENSHOTS_TODAY,
                    icon = Icons.Default.Image
                ) { onSuggestionChange(SuggestionType.SCREENSHOTS_TODAY) }
            }
        }
    }
}

@Composable
private fun SuggestionChip(label: String, selected: Boolean, icon: ImageVector, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(180),
        label = "suggestion_chip_scale"
    )
    Row(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) AccentGreen.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Text(label, color = if (selected) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun PremiumSnackbarHost(snackbarHostState: SnackbarHostState) {
    SnackbarHost(hostState = snackbarHostState) { data: SnackbarData ->
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(280)) + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut(animationSpec = tween(220)) + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Snackbar(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
                action = {
                    data.visuals.actionLabel?.let { label ->
                        TextButton(onClick = { data.performAction() }) {
                            Text(label.uppercase(), color = AccentBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            ) {
                Text(data.visuals.message, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun PremiumMediaRow(
    item: SimpleMediaItem,
    selected: Boolean,
    suggestionReason: String?,
    onImageClick: () -> Unit,
    onToggleSelection: () -> Unit,
    onOpen: () -> Unit,
    onKeep: () -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(220),
        label = "media_scale"
    )
    val selectedBorder by animateColorAsState(
        targetValue = if (selected) AccentBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        animationSpec = tween(260),
        label = "selected_border"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.97f,
        animationSpec = tween(240),
        label = "media_alpha"
    )
    val imageUri = remember(item.uri) { item.uri.takeIf { uri -> uri != Uri.EMPTY } }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (selected) 10.dp else 4.dp, RoundedCornerShape(22.dp))
            .scale(scale)
            .alpha(contentAlpha)
            .border(width = 1.6.dp, color = selectedBorder, shape = RoundedCornerShape(22.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onImageClick,
                onLongClick = onToggleSelection
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x55000000))
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            selected -> Icons.Default.Check
                            item.mimeType?.startsWith("image") == true -> Icons.Default.Image
                            item.mimeType?.startsWith("video") == true -> Icons.Default.VideoLibrary
                            else -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = if (selected) AccentGreen else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        if (selected) "Selected" else formatSize(item.sizeKb.toLong() * 1024L),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    item.safeDisplayName(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${item.safeMimeLabel()} • ${formatSize(item.sizeKb.toLong() * 1024L)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!suggestionReason.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentPurple.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(14.dp))
                        Text(
                            "Suggested: $suggestionReason",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentPurple,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniActionButton(
                    icon = Icons.Default.FolderOpen,
                    label = "Open",
                    accent = AccentBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onOpen
                )
                MiniActionButton(
                    icon = Icons.Default.CheckCircle,
                    label = "Keep",
                    accent = AccentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onKeep
                )
                MiniActionButton(
                    icon = Icons.Default.DeleteOutline,
                    label = "Delete",
                    accent = AccentError,
                    modifier = Modifier.weight(1f),
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
fun ImagePreviewScreen(uri: Uri, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .zIndex(2f)
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun MiniActionButton(
    icon: ImageVector,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(160),
        label = "mini_action_scale"
    )
    Row(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.12f))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = accent, maxLines = 1)
    }
}

@Composable
private fun MediaGridShimmer() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .shimmerEffect()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(10.dp))
                .shimmerEffect()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(14.dp)
                .clip(RoundedCornerShape(10.dp))
                .shimmerEffect()
        )
    }
}

@Composable
private fun MiniMetricCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun MediaSwipeRow(
    item: SimpleMediaItem,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onKeep: () -> Unit,
    onOpen: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { distance -> distance * 0.35f },
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onKeep()
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }

                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val isDelete = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            val alignment = if (isDelete) Alignment.CenterEnd else Alignment.CenterStart
            val color = if (isDelete) AccentError.copy(alpha = 0.18f) else AccentGreen.copy(alpha = 0.18f)
            val icon = if (isDelete) Icons.Default.DeleteOutline else Icons.Default.CheckCircle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(color)
                    .padding(horizontal = 18.dp),
                contentAlignment = alignment
            ) {
                Icon(icon, contentDescription = null, tint = if (isDelete) AccentError else AccentGreen)
            }
        },
        content = {
            MediaRow(item = item, selected = selected, onClick = onClick, onOpen = onOpen)
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun MediaRow(item: SimpleMediaItem, selected: Boolean, onClick: () -> Unit, onOpen: () -> Unit) {
    LegitCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (selected) AccentBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) AccentBlue.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        selected -> Icons.Default.Check
                        item.mimeType?.startsWith("image") == true -> Icons.Default.Image
                        item.mimeType?.startsWith("video") == true -> Icons.Default.VideoLibrary
                        else -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = if (selected) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.safeDisplayName(), maxLines = 1, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                Text(item.safeMimeLabel(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(formatSize(item.sizeKb.toLong() * 1024L), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            LegitButton(text = "Open", onClick = onOpen, modifier = Modifier.height(42.dp))
        }
    }
}

@Composable
private fun LoadingCard() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .clip(RoundedCornerShape(20.dp))
                .shimmerEffect()
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }
}
