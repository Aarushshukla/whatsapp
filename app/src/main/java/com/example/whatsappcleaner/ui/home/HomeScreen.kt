package com.example.whatsappcleaner.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.navigation.AppDrawer
import kotlinx.coroutines.launch

private data class CategoryCardModel(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val fileCount: Int,
    val storageBytes: Long,
    val safety: String,
    val onClick: () -> Unit
)
private val AppBg = Color(0xFFF7F9FC)
private val CardBg = Color(0xFFFFFFFF)
private val MainText = Color(0xFF20242A)
private val SecondaryText = Color(0xFF6B7280)

@OptIn(ExperimentalLayoutApi::class)
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
    scanUiState: ScanUiState,
    onNavigateToFeatures: () -> Unit,
    onAiFeatureClick: (AiFeature) -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(deleteSnackbarMessage) {
        deleteSnackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onDeleteSnackbarConsumed()
        }
    }

    val scanProgress = (scanUiState as? ScanUiState.Loading)?.progress ?: if (totalSize > 0L) 0.72f else 0f
    val scanStageText = when (scanUiState) {
        is ScanUiState.Loading -> scanUiState.stage
        is ScanUiState.Success -> scanUiState.result
        ScanUiState.Empty -> "No files found."
        is ScanUiState.Error -> "Scan failed: ${scanUiState.message}"
        ScanUiState.Idle -> "Ready to scan"
    }

    val categoryCards = listOf(
        CategoryCardModel(Icons.Default.AutoAwesome, "Smart Recommendation", "Best first cleanup suggestions", smartSuggestedItems.size, smartSuggestionSummary.totalSpaceToFree, "Safest first", onNavigateToSmartClean),
        CategoryCardModel(Icons.Default.ContentCopy, "Duplicate Media", "Repeated photos and videos", duplicateCount, smartSuggestionSummary.totalSpaceToFree, "Safe review", onNavigateToDuplicates),
        CategoryCardModel(Icons.Default.SdStorage, "Large Videos", "Big files with high impact", largeTodayCount, smartSuggestionSummary.totalSpaceToFree, "Manual select", onNavigateToMediaViewer),
        CategoryCardModel(Icons.Default.Schedule, "Old Media", "Media that has not been opened in a while", oldFilesCount, totalSize / 5, "Age-based", onNavigateToSmartClean),
        CategoryCardModel(Icons.Default.ViewAgenda, "Status Files", "Temporary status media to review", 0, 0, "Review", onNavigateToMediaViewer),
        CategoryCardModel(Icons.Default.Collections, "Memes & Stickers", "Low-value shareables", memeCount, totalSize / 8, "Review first", onNavigateToMemeAnalyzer),
        CategoryCardModel(Icons.Default.ImageSearch, "Blurry Images", "Likely low-quality photos", 0, 0, "AI assist", onNavigateToSmartClean),
        CategoryCardModel(Icons.Default.Storage, "Review Carefully", "Potentially important files", junkCount, totalSize / 12, "Needs attention", onNavigateToJunk)
    )

    val visibleCards = categoryCards.filter { it.fileCount > 0 || it.storageBytes > 0L }
    val animatedBytes by animateFloatAsState(
        targetValue = smartSuggestionSummary.totalSpaceToFree.toFloat(),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "space_countup"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer { route ->
                scope.launch { drawerState.close() }
                when (route) {
                    "home" -> Unit
                    "scan_again" -> onRefreshClick()
                    "smart_review" -> onNavigateToSmartClean()
                    "categories" -> onNavigateToFeatures()
                    "storage_overview" -> onNavigateToAnalytics()
                    "privacy_policy" -> onNavigateToPrivacyPolicy()
                    "terms" -> onNavigateToTerms()
                    "about" -> onNavigateToAbout()
                    "settings" -> onNavigateToSettings()
                }
            }
        }
    ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("ChatSweep", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MainText)
                Row {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MainText) }
                    IconButton(onClick = onRefreshClick, enabled = scanUiState !is ScanUiState.Loading) { Icon(Icons.Default.Storage, contentDescription = "Scan Again", tint = MainText) }
                }
            }
        }
        item {
            Text("You can free up ${formatSize(animatedBytes.toLong())}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MainText)
            Text("Review files before deleting. Nothing is deleted automatically.", color = SecondaryText)
        }

        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = CardBg), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(136.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator(
                            progress = { scanProgress },
                            strokeWidth = 12.dp,
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Storage, null)
                            Text(if (totalSize > 0L) formatSize(totalSize) else "Ready to scan", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(scanStageText, color = MainText)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onRefreshClick, modifier = Modifier.fillMaxWidth(), enabled = scanUiState !is ScanUiState.Loading) {
                        Text(if (isLoading) "Scanning..." else "Scan Again")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("No auto-delete • Review before deleting • Offline scan", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                }
            }
        }

        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("Chat media size", if (totalSize > 0L) formatSize(totalSize) else "—")
                StatChip("Potential cleanup", formatSize(smartSuggestionSummary.totalSpaceToFree))
                StatChip("Last scan", if (totalSize > 0L) "Just now" else "Never")
                StatChip("Files found", totalFiles.toString())
            }
        }

        item { Text("Scan Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MainText) }

        if (visibleCards.isEmpty()) {
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Text("Everything looks clean for now. Run another scan anytime.", modifier = Modifier.padding(16.dp), color = SecondaryText)
                }
            }
        }

        itemsIndexed(visibleCards) { index, card ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(320, delayMillis = index * 50)) +
                    slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(320, delayMillis = index * 50))
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().clickable { card.onClick.invoke() }
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(42.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(card.icon, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            val pct = if (totalSize > 0L) ((card.storageBytes.toDouble() / totalSize.toDouble()) * 100.0).toInt().coerceIn(0, 100) else 0
                            Text(card.title, fontWeight = FontWeight.SemiBold, color = MainText)
                            Text(card.description, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                            Text("${card.fileCount} files • ${formatSize(card.storageBytes)} • $pct%", style = MaterialTheme.typography.labelMedium, color = SecondaryText)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(card.safety, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SecondaryText)
                        }
                    }
                }
            }
        }

        item { SnackbarHost(snackbarHostState) }
    }
    }
}

@Composable
private fun StatChip(title: String, value: String) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = SecondaryText)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MainText)
        }
    }
}
