package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.navigation.AppDrawer
import kotlinx.coroutines.launch

private val AppBg = Color(0xFFF7F9FC)
private val CardBg = Color(0xFFFFFFFF)
private val MainText = Color(0xFF20242A)
private val SecondaryText = Color(0xFF6B7280)
private val PrimaryBlue = Color(0xFF2F6FED)
private val Border = Color(0xFFE5E7EB)

@Composable
fun SimpleHomeScreen(items: List<SimpleMediaItem>, onRefreshClick: () -> Unit, summaryInfo: String, isLoading: Boolean, currentFilter: MediaFilter, onFilterChange: (MediaFilter) -> Unit, remindersEnabled: Boolean, onRemindersToggle: (Boolean) -> Unit, memeCount: Int, spamCount: Int, junkCount: Int, duplicateCount: Int, isProUser: Boolean, onNavigateToSmartClean: () -> Unit, onNavigateToPhoneReality: () -> Unit, onNavigateToMemeAnalyzer: () -> Unit, onNavigateToMediaViewer: () -> Unit, onNavigateToJunk: () -> Unit, onNavigateToAnalytics: () -> Unit, onNavigateToSpam: () -> Unit, onNavigateToSettings: () -> Unit, onNavigateToDuplicates: () -> Unit, onBulkDeleteClick: () -> Unit, onUpgradeToPro: () -> Unit, onDeleteConfirmed: () -> Unit, onDeleteItemsRequested: (List<SimpleMediaItem>) -> Unit, onOpenInSystem: (SimpleMediaItem) -> Unit, onOpenSystemStorage: () -> Unit, pendingDeleteUris: Set<String>, isDeleteInProgress: Boolean, deleteSnackbarMessage: String?, onUndoDelete: () -> Unit, onDeleteSnackbarConsumed: () -> Unit, selectedFrequency: ReminderFreq, onFrequencyChange: (ReminderFreq) -> Unit, selectedTime: ReminderTime, allTimeOptions: List<ReminderTime>, onTimeChange: (ReminderTime) -> Unit, largeTodayCount: Int, largeTodaySizeText: String, screenshotTodayCount: Int, screenshotTodaySizeText: String, activeSuggestion: SuggestionType, onSuggestionChange: (SuggestionType) -> Unit, totalFiles: Int, totalSize: Long, oldFilesCount: Int, smartSuggestionSummary: SmartSuggestionSummary, smartSuggestedItems: List<SimpleMediaItem>, suggestionReasonsByUri: Map<String, List<String>>, scanUiState: ScanUiState, onNavigateToFeatures: () -> Unit, onAiFeatureClick: (AiFeature) -> Unit, onNavigateToPrivacyPolicy: () -> Unit, onNavigateToTerms: () -> Unit, onNavigateToAbout: () -> Unit, onNavigateToHelpFeedback: () -> Unit, onNavigateToScanHistory: () -> Unit, onNavigateToCleanupReceipt: () -> Unit, onNavigateToStorageOverview: () -> Unit, onNavigateToSmartReview: () -> Unit, onNavigateToMediaOverview: () -> Unit, onNavigateToCategories: () -> Unit, onNavigateToPhotos: () -> Unit, onNavigateToVideos: () -> Unit, onNavigateToAudio: () -> Unit, onNavigateToDocuments: () -> Unit, onNavigateToStatuses: () -> Unit, onNavigateToStickers: () -> Unit, onNavigateToDuplicateFinder: () -> Unit, onNavigateToLargeFiles: () -> Unit, onNavigateToOldMedia: () -> Unit, onNavigateToStatusCleaner: () -> Unit, onNavigateToMemesStickers: () -> Unit, onNavigateToBlurryImages: () -> Unit) {
    val drawerState = androidx.compose.material3.rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val hasScanSummary = totalFiles > 0 || totalSize > 0L
    val potentialCleanupSize = smartSuggestionSummary.totalSpaceToFree

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer { route ->
                scope.launch { drawerState.close() }
                when (route) {
                    "home" -> Unit
                    "smart_review" -> onNavigateToSmartReview()
                    "scan_again" -> onRefreshClick()
                    "duplicate_finder" -> onNavigateToDuplicateFinder()
                    "large_files" -> onNavigateToLargeFiles()
                    "old_media" -> onNavigateToOldMedia()
                    "blurry_images" -> onNavigateToBlurryImages()
                    "scan_history" -> onNavigateToScanHistory()
                    "last_cleanup_receipt" -> onNavigateToCleanupReceipt()
                    "storage_overview" -> onNavigateToStorageOverview()
                    "media_overview" -> onNavigateToMediaOverview()
                    "photos" -> onNavigateToPhotos()
                    "videos" -> onNavigateToVideos()
                    "audio" -> onNavigateToAudio()
                    "documents" -> onNavigateToDocuments()
                    "statuses" -> onNavigateToStatuses()
                    "stickers" -> onNavigateToStickers()
                    "categories" -> onNavigateToCategories()
                    "status_cleaner" -> onNavigateToStatusCleaner();
                    "memes_stickers" -> onNavigateToMemesStickers()
                    "help_feedback" -> onNavigateToHelpFeedback()
                    "settings" -> onNavigateToSettings()
                    "privacy_policy" -> onNavigateToPrivacyPolicy()
                    "terms" -> onNavigateToTerms()
                    "about" -> onNavigateToAbout()
                    else -> Unit
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Open menu")
                    }
                    Text("ChatSweep", fontWeight = FontWeight.Bold, color = MainText)
                }
                IconButton(onClick = onRefreshClick) {
                    Icon(Icons.Default.Refresh, contentDescription = "Scan again", tint = PrimaryBlue)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, Border),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Main Storage", fontWeight = FontWeight.SemiBold, color = MainText)
                    InfoRow("Total chat media size", formatSize(totalSize))
                    InfoRow("Potential cleanup size", formatSize(potentialCleanupSize))
                    InfoRow("Last scan time", if (hasScanSummary) summaryInfo else "No scan yet")
                    InfoRow("File count", "$totalFiles")

                    SegmentedCategoryBar(totalSize = totalSize, potentialCleanupSize = potentialCleanupSize)

                    Button(
                        onClick = if (hasScanSummary) onNavigateToSmartReview else onRefreshClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (hasScanSummary) "SMART REVIEW" else "START SMART SCAN")
                    }

                    OutlinedButton(
                        onClick = onRefreshClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SCAN AGAIN")
                    }

                    Text(
                        "No cloud upload. Nothing is deleted automatically.",
                        color = SecondaryText,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            FeatureGrid(
                duplicateCount = duplicateCount,
                totalFiles = totalFiles,
                totalSize = totalSize,
                memeCount = memeCount,
                onSmartReview = onNavigateToSmartReview,
                onMedia = onNavigateToMediaOverview,
                onDuplicates = onNavigateToDuplicateFinder,
                onLargeVideos = onNavigateToLargeFiles,
                onStatuses = onNavigateToStatusCleaner,
                onMemes = onNavigateToMemesStickers,
                onBlurry = onNavigateToBlurryImages,
                onScanHistory = onNavigateToScanHistory
            )
        }
    }
}

@Composable
private fun FeatureGrid(
    duplicateCount: Int,
    totalFiles: Int,
    totalSize: Long,
    memeCount: Int,
    onSmartReview: () -> Unit,
    onMedia: () -> Unit,
    onDuplicates: () -> Unit,
    onLargeVideos: () -> Unit,
    onStatuses: () -> Unit,
    onMemes: () -> Unit,
    onBlurry: () -> Unit,
    onScanHistory: () -> Unit
) {
    val cards = listOf(
        Triple("Smart Review", Icons.Default.AutoAwesome, onSmartReview),
        Triple("Media", Icons.Default.Collections, onMedia),
        Triple("Duplicates", Icons.Default.ContentCopy, onDuplicates),
        Triple("Large Videos", Icons.Default.VideoLibrary, onLargeVideos),
        Triple("Statuses", Icons.Default.HourglassBottom, onStatuses),
        Triple("Memes & Stickers", Icons.Default.StickyNote2, onMemes),
        Triple("Blurry Images", Icons.Default.ImageSearch, onBlurry),
        Triple("Scan History", Icons.Default.History, onScanHistory)
    )
    LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), userScrollEnabled = false, modifier = Modifier.height(440.dp)) {
        items(cards.size) { index ->
            val (title, icon, click) = cards[index]
            DashboardFeatureCard(title, icon, when (title) {
                "Duplicates" -> "$duplicateCount files"
                "Media" -> "${formatSize(totalSize)} • $totalFiles files"
                "Memes & Stickers" -> "$memeCount items"
                else -> "Open"
            }, click)
        }
    }
}

@Composable
private fun DashboardFeatureCard(title: String, icon: ImageVector, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = CardBg), border = BorderStroke(1.dp, Border), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SecondaryText)
            }
            Text(title, color = MainText, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = SecondaryText)
        Text(value, color = MainText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SegmentedCategoryBar(totalSize: Long, potentialCleanupSize: Long) {
    val used = totalSize.coerceAtLeast(1L).toFloat()
    val cleanup = potentialCleanupSize.coerceAtLeast(0L).toFloat() / used
    val keep = (1f - cleanup).coerceIn(0.05f, 1f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(Border, RoundedCornerShape(99.dp))
    ) {
        Row(
            modifier = Modifier
                .weight(keep)
                .fillMaxSize()
                .background(Color(0xFF7AA3F4))
        ) {}
        Row(
            modifier = Modifier
                .weight(cleanup.coerceIn(0.05f, 0.95f))
                .fillMaxSize()
                .background(PrimaryBlue)
        ) {}
    }
}
