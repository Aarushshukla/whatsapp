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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.navigation.AppDrawer
import kotlinx.coroutines.launch

private val AppBg = Color(0xFFF4F7FB)
private val CardBg = Color(0xFFFFFFFF)
private val MainText = Color(0xFF20242A)
private val SecondaryText = Color(0xFF6B7280)
private val PrimaryBlue = Color(0xFF2F6FED)
private val Border = Color(0xFFE5E7EB)
private val SegmentColors = listOf(Color(0xFF5B8DEF), Color(0xFF7F9CF5), Color(0xFF67D5B5), Color(0xFFFFB86B), Color(0xFF9B8AFB), Color(0xFFCBD5E1))

@Composable
fun SimpleHomeScreen(items: List<SimpleMediaItem>, onRefreshClick: () -> Unit, summaryInfo: String, isLoading: Boolean, currentFilter: MediaFilter, onFilterChange: (MediaFilter) -> Unit, remindersEnabled: Boolean, onRemindersToggle: (Boolean) -> Unit, memeCount: Int, spamCount: Int, junkCount: Int, duplicateCount: Int, isProUser: Boolean, onNavigateToSmartClean: () -> Unit, onNavigateToPhoneReality: () -> Unit, onNavigateToMemeAnalyzer: () -> Unit, onNavigateToMediaViewer: () -> Unit, onNavigateToJunk: () -> Unit, onNavigateToAnalytics: () -> Unit, onNavigateToSpam: () -> Unit, onNavigateToSettings: () -> Unit, onNavigateToDuplicates: () -> Unit, onBulkDeleteClick: () -> Unit, onUpgradeToPro: () -> Unit, onDeleteConfirmed: () -> Unit, onDeleteItemsRequested: (List<SimpleMediaItem>) -> Unit, onOpenInSystem: (SimpleMediaItem) -> Unit, onOpenSystemStorage: () -> Unit, pendingDeleteUris: Set<String>, isDeleteInProgress: Boolean, deleteSnackbarMessage: String?, onUndoDelete: () -> Unit, onDeleteSnackbarConsumed: () -> Unit, selectedFrequency: ReminderFreq, onFrequencyChange: (ReminderFreq) -> Unit, selectedTime: ReminderTime, allTimeOptions: List<ReminderTime>, onTimeChange: (ReminderTime) -> Unit, largeTodayCount: Int, largeTodaySizeText: String, screenshotTodayCount: Int, screenshotTodaySizeText: String, activeSuggestion: SuggestionType, onSuggestionChange: (SuggestionType) -> Unit, totalFiles: Int, totalSize: Long, oldFilesCount: Int, smartSuggestionSummary: SmartSuggestionSummary, smartSuggestedItems: List<SimpleMediaItem>, suggestionReasonsByUri: Map<String, List<String>>, scanUiState: ScanUiState, onNavigateToFeatures: () -> Unit, onAiFeatureClick: (AiFeature) -> Unit, onNavigateToPrivacyPolicy: () -> Unit, onNavigateToTerms: () -> Unit, onNavigateToAbout: () -> Unit, onNavigateToHelpFeedback: () -> Unit, onNavigateToScanHistory: () -> Unit, onNavigateToCleanupReceipt: () -> Unit, onNavigateToStorageOverview: () -> Unit, onNavigateToSmartReview: () -> Unit, onNavigateToMediaOverview: () -> Unit, onNavigateToCategories: () -> Unit, onNavigateToPhotos: () -> Unit, onNavigateToVideos: () -> Unit, onNavigateToAudio: () -> Unit, onNavigateToDocuments: () -> Unit, onNavigateToStatuses: () -> Unit, onNavigateToStickers: () -> Unit, onNavigateToDuplicateFinder: () -> Unit, onNavigateToLargeFiles: () -> Unit, onNavigateToOldMedia: () -> Unit, onNavigateToStatusCleaner: () -> Unit, onNavigateToMemesStickers: () -> Unit, onNavigateToBlurryImages: () -> Unit, onNavigateToCleanupReminder: () -> Unit) {
    val drawerState = androidx.compose.material3.rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val hasScanSummary = totalFiles > 0 || totalSize > 0L
    val potentialCleanupSize = smartSuggestionSummary.totalSpaceToFree

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        AppDrawer(selectedRoute = "home", lastScanSummary = if (hasScanSummary) summaryInfo else null) { route ->
            scope.launch { drawerState.close() }
            when (route) {
                "smart_review" -> onNavigateToSmartReview(); "scan_again" -> onRefreshClick(); "duplicate_finder" -> onNavigateToDuplicateFinder(); "large_files" -> onNavigateToLargeFiles(); "old_media" -> onNavigateToOldMedia(); "blurry_images" -> onNavigateToBlurryImages(); "scan_history" -> onNavigateToScanHistory(); "last_cleanup_receipt" -> onNavigateToCleanupReceipt(); "storage_overview" -> onNavigateToStorageOverview(); "media_overview" -> onNavigateToMediaOverview(); "photos" -> onNavigateToPhotos(); "videos" -> onNavigateToVideos(); "audio" -> onNavigateToAudio(); "documents" -> onNavigateToDocuments(); "statuses" -> onNavigateToStatuses(); "stickers" -> onNavigateToStickers(); "memes_stickers" -> onNavigateToMemesStickers(); "cleanup_reminder" -> onNavigateToCleanupReminder(); "help_feedback" -> onNavigateToHelpFeedback(); "settings" -> onNavigateToSettings(); "privacy_policy" -> onNavigateToPrivacyPolicy(); "terms" -> onNavigateToTerms(); "about" -> onNavigateToAbout(); else -> Unit
            }
        }
    }) {
        Column(modifier = Modifier.fillMaxSize().background(AppBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = "Open menu") }
                    Text("ChatSweep", fontWeight = FontWeight.Bold, color = MainText)
                }
                IconButton(onClick = onRefreshClick) { Icon(Icons.Default.Refresh, contentDescription = "Scan again", tint = PrimaryBlue) }
            }

            MainStorageCard(hasScanSummary, totalSize, totalFiles, summaryInfo, potentialCleanupSize, duplicateCount, largeTodaySizeText, onNavigateToSmartReview, onRefreshClick)
            FeatureGrid(onNavigateToMediaOverview, onNavigateToDuplicateFinder, onNavigateToLargeFiles, onNavigateToStatusCleaner)
            ReviewToolsSection(onNavigateToMemesStickers, onNavigateToBlurryImages, onNavigateToOldMedia, onNavigateToStorageOverview)
            Text("No cloud upload. Nothing is deleted automatically.", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun MainStorageCard(hasScanSummary: Boolean, totalSize: Long, totalFiles: Int, summaryInfo: String, potentialCleanupSize: Long, duplicateCount: Int, largeVideosText: String, onSmartReview: () -> Unit, onScanAgain: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), border = BorderStroke(1.dp, Border), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Chat media", fontWeight = FontWeight.SemiBold, color = MainText)
            Text(if (hasScanSummary) formatSize(totalSize) else "No scan yet", style = MaterialTheme.typography.headlineMedium, color = MainText, fontWeight = FontWeight.Bold)
            Text(if (hasScanSummary && potentialCleanupSize > 0) "Review up to ${formatSize(potentialCleanupSize)}" else "No scan yet", color = SecondaryText)
            InfoRow("File count", if (hasScanSummary) "$totalFiles" else "—")
            InfoRow("Last scan time", if (hasScanSummary) summaryInfo else "No scan yet")

            SegmentedCategoryBar(hasScanSummary)
            LegendRows(totalSize, potentialCleanupSize, duplicateCount, largeVideosText)

            Button(onClick = if (hasScanSummary) onSmartReview else onScanAgain, modifier = Modifier.fillMaxWidth()) { Text(if (hasScanSummary) "SMART REVIEW" else "START SMART SCAN") }
            OutlinedButton(onClick = onScanAgain, modifier = Modifier.fillMaxWidth()) { Text("SCAN AGAIN") }
        }
    }
}

@Composable
private fun FeatureGrid(onMedia: () -> Unit, onDuplicates: () -> Unit, onLargeFiles: () -> Unit, onStatuses: () -> Unit) {
    val cards = listOf(
        Triple("Media", Icons.Default.Collections, onMedia),
        Triple("Duplicates", Icons.Default.ContentCopy, onDuplicates),
        Triple("Large Files", Icons.Default.Folder, onLargeFiles),
        Triple("Statuses", Icons.Default.HourglassBottom, onStatuses)
    )
    LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), userScrollEnabled = false, modifier = Modifier.height(190.dp)) {
        items(cards.size) { idx ->
            val (title, icon, action) = cards[idx]
            DashboardFeatureCard(title, icon, "Open", action)
        }
    }
}

@Composable
private fun ReviewToolsSection(onMemes: () -> Unit, onBlurry: () -> Unit, onOldMedia: () -> Unit, onStorage: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), border = BorderStroke(1.dp, Border), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Review tools", color = MainText, fontWeight = FontWeight.SemiBold)
            ReviewRow("Memes & Stickers", Icons.Default.StickyNote2, onMemes)
            ReviewRow("Blurry Images", Icons.Default.ImageSearch, onBlurry)
            ReviewRow("Old Media", Icons.Default.VideoLibrary, onOldMedia)
            ReviewRow("Storage Overview", Icons.Default.Storage, onStorage)
        }
    }
}

@Composable
private fun ReviewRow(title: String, icon: ImageVector, onClick: () -> Unit) = DashboardFeatureCard(title, icon, "Open", onClick)

@Composable
private fun DashboardFeatureCard(title: String, icon: ImageVector, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = CardBg), border = BorderStroke(1.dp, Border), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue)
                Column { Text(title, color = MainText, fontWeight = FontWeight.SemiBold); Text(subtitle, color = SecondaryText, style = MaterialTheme.typography.bodySmall) }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SecondaryText)
        }
    }
}

@Composable
private fun SegmentedCategoryBar(hasScanSummary: Boolean) {
    val labels = listOf("Duplicates", "Large videos", "Statuses", "Memes & Stickers", "Blurry images", "Review carefully")
    Row(Modifier.fillMaxWidth().height(12.dp).background(Border, RoundedCornerShape(100.dp))) {
        labels.forEachIndexed { idx, _ ->
            Row(Modifier.weight(1f).fillMaxSize().background(if (hasScanSummary) SegmentColors[idx] else Color(0xFFDDE3EE))) {}
        }
    }
}

@Composable
private fun LegendRows(totalSize: Long, potentialCleanupSize: Long, duplicateCount: Int, largeVideosText: String) {
    val dupSize = if (duplicateCount > 0) potentialCleanupSize / 3 else 0L
    val largeSize = potentialCleanupSize / 3
    val statusesSize = potentialCleanupSize / 6
    val reviewSize = potentialCleanupSize - dupSize - largeSize - statusesSize
    val denom = totalSize.coerceAtLeast(1L).toFloat()
    InfoRow("Duplicates", "${formatSize(dupSize)} / ${(dupSize / denom * 100).toInt()}%")
    InfoRow("Large videos", if (largeVideosText.isNotBlank()) "$largeVideosText" else "${formatSize(largeSize)} / ${(largeSize / denom * 100).toInt()}%")
    InfoRow("Statuses", "${formatSize(statusesSize)} / ${(statusesSize / denom * 100).toInt()}%")
    InfoRow("Review carefully", "${formatSize(reviewSize.coerceAtLeast(0L))} / ${(reviewSize.coerceAtLeast(0L) / denom * 100).toInt()}%")
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = SecondaryText)
        Text(value, color = MainText, fontWeight = FontWeight.Medium)
    }
}
