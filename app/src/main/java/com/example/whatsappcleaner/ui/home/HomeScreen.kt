package com.example.whatsappcleaner.ui.home

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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.launch

private data class CategoryCardModel(
    val icon: ImageVector,
    val title: String,
    val benefit: String,
    val fileCount: Int,
    val storageBytes: Long,
    val safety: String,
    val onClick: (() -> Unit)?
)

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
    storageHeatmapByMonth: List<Pair<String, Long>>,
    monthlyGrowthForecast: String?,
    scanUiState: ScanUiState,
    onNavigateToFeatures: () -> Unit,
    onAiFeatureClick: (AiFeature) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(deleteSnackbarMessage) {
        deleteSnackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onDeleteSnackbarConsumed()
        }
    }

    val scanProgress = (scanUiState as? ScanUiState.Loading)?.progress ?: if (totalSize > 0L) 0.72f else 0f
    val animatedProgress = animateFloatAsState(targetValue = scanProgress, label = "scanProgress")
    val animatedSizeNumber = animateIntAsState(targetValue = (totalSize / (1024 * 1024)).toInt(), label = "sizeCountUp")
    val scanStageText = when (scanUiState) {
        is ScanUiState.Loading -> scanUiState.stage
        is ScanUiState.Success -> scanUiState.result
        ScanUiState.Empty -> "No files found."
        is ScanUiState.Error -> "Scan failed: ${scanUiState.message}"
        ScanUiState.Idle -> "Ready to scan"
    }

    val categoryCards = listOf(
        CategoryCardModel(Icons.Default.ContentCopy, "Duplicates", "Remove repeated media quickly", duplicateCount, smartSuggestionSummary.totalSpaceToFree, "Safe review", onNavigateToDuplicates),
        CategoryCardModel(Icons.Default.SdStorage, "Large Files", "Find biggest space hogs", largeTodayCount, smartSuggestionSummary.totalSpaceToFree, "Manual select", onNavigateToSmartClean),
        CategoryCardModel(Icons.Default.Schedule, "Old Media", "Clear forgotten files", oldFilesCount, totalSize / 5, "Age-based", onNavigateToSmartClean),
        CategoryCardModel(Icons.Default.ViewAgenda, "Statuses", "Trim expired status media", 0, 0, "Read-only scan", null),
        CategoryCardModel(Icons.Default.Collections, "Memes & Stickers", "Clean low-value forwards", memeCount, totalSize / 8, "Review first", onNavigateToMemeAnalyzer),
        CategoryCardModel(Icons.Default.ImageSearch, "Blurry Images", "Spot low-quality shots", 0, 0, "AI assist", null)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("ChatSweep", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Private offline media cleaner", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(136.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator(
                            progress = { animatedProgress.value },
                            strokeWidth = 12.dp,
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Storage, null)
                            Text(if (totalSize > 0L) "${animatedSizeNumber.value} MB" else "Ready to scan", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(scanStageText)
                    if (scanUiState is ScanUiState.Success) {
                        Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onRefreshClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = scanUiState !is ScanUiState.Loading
                    ) {
                        Text(if (isLoading) "Scanning..." else "Scan Chat Media")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("No auto-delete • Review before deleting • Offline scan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        if (items.isNotEmpty()) {
            item {
                Text("Storage Heatmap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                storageHeatmapByMonth.take(2).forEach { row ->
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Text(row.first, modifier = Modifier.padding(12.dp))
                    }
                }
                monthlyGrowthForecast?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }

        if (scanUiState == ScanUiState.Empty) {
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("No files found", fontWeight = FontWeight.SemiBold)
                        Text("Your chat media looks clean.")
                        Button(onClick = onRefreshClick) { Text("Scan again") }
                    }
                }
            }
        }
        if (scanUiState is ScanUiState.Error) {
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Scan failed", fontWeight = FontWeight.SemiBold)
                        Text(scanUiState.message)
                        Button(onClick = onRefreshClick) { Text("Try again") }
                    }
                }
            }
        }

        item { Text("Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }

        items(categoryCards.size) { index ->
            val card = categoryCards[index]
            AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (card.onClick != null) card.onClick.invoke()
                        else scope.launch { snackbarHostState.showSnackbar("${card.title} is coming soon.") }
                    }
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(card.icon, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(card.title, fontWeight = FontWeight.SemiBold)
                        Text(card.benefit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${card.fileCount} files • ${formatSize(card.storageBytes)}", style = MaterialTheme.typography.labelMedium)
                    }
                    Text(card.safety, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            )}
        }

        item { SnackbarHost(snackbarHostState) }
    }
}

@Composable
private fun StatChip(title: String, value: String) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}
