package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class CategoryCardModel(val icon: ImageVector,val title: String,val description: String,val fileCount: Int,val storageBytes: Long,val safety: String,val route: () -> Unit)
private val AppBg = Color(0xFFF7F9FC)
private val CardBg = Color(0xFFFFFFFF)
private val MainText = Color(0xFF20242A)
private val SecondaryText = Color(0xFF6B7280)
private val PrimaryBlue = Color(0xFF2F6FED)
private val SuccessGreen = Color(0xFF20A64A)
private val Border = Color(0xFFE5E7EB)

@Composable
fun SimpleHomeScreen(
    items: List<SimpleMediaItem>, onRefreshClick: () -> Unit, summaryInfo: String, isLoading: Boolean, currentFilter: MediaFilter, onFilterChange: (MediaFilter) -> Unit, remindersEnabled: Boolean, onRemindersToggle: (Boolean) -> Unit, memeCount: Int, spamCount: Int, junkCount: Int, duplicateCount: Int, isProUser: Boolean, onNavigateToSmartClean: () -> Unit, onNavigateToPhoneReality: () -> Unit, onNavigateToMemeAnalyzer: () -> Unit, onNavigateToMediaViewer: () -> Unit, onNavigateToJunk: () -> Unit, onNavigateToAnalytics: () -> Unit, onNavigateToSpam: () -> Unit, onNavigateToSettings: () -> Unit, onNavigateToDuplicates: () -> Unit, onBulkDeleteClick: () -> Unit, onUpgradeToPro: () -> Unit, onDeleteConfirmed: () -> Unit, onDeleteItemsRequested: (List<SimpleMediaItem>) -> Unit, onOpenInSystem: (SimpleMediaItem) -> Unit, onOpenSystemStorage: () -> Unit, pendingDeleteUris: Set<String>, isDeleteInProgress: Boolean, deleteSnackbarMessage: String?, onUndoDelete: () -> Unit, onDeleteSnackbarConsumed: () -> Unit, selectedFrequency: ReminderFreq, onFrequencyChange: (ReminderFreq) -> Unit, selectedTime: ReminderTime, allTimeOptions: List<ReminderTime>, onTimeChange: (ReminderTime) -> Unit, largeTodayCount: Int, largeTodaySizeText: String, screenshotTodayCount: Int, screenshotTodaySizeText: String, activeSuggestion: SuggestionType, onSuggestionChange: (SuggestionType) -> Unit, totalFiles: Int, totalSize: Long, oldFilesCount: Int, smartSuggestionSummary: SmartSuggestionSummary, smartSuggestedItems: List<SimpleMediaItem>, suggestionReasonsByUri: Map<String, List<String>>, scanUiState: ScanUiState, onNavigateToFeatures: () -> Unit, onAiFeatureClick: (AiFeature) -> Unit, onNavigateToPrivacyPolicy: () -> Unit, onNavigateToTerms: () -> Unit, onNavigateToAbout: () -> Unit
) {
    val drawerState = androidx.compose.material3.rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val categoryCards = listOf(
        CategoryCardModel(Icons.Default.AutoAwesome, "Smart Review", "Best first cleanup suggestions", smartSuggestedItems.size, smartSuggestionSummary.totalSpaceToFree, "Recommended", onNavigateToSmartClean),
        CategoryCardModel(Icons.Default.ContentCopy, "Duplicate Media", "Repeated photos and videos", duplicateCount, smartSuggestionSummary.totalSpaceToFree / 3, "Safe", onNavigateToDuplicates),
        CategoryCardModel(Icons.Default.SdStorage, "Large Videos", "Big files with high impact", largeTodayCount, smartSuggestionSummary.totalSpaceToFree / 3, "Review", onNavigateToMediaViewer),
        CategoryCardModel(Icons.Default.Schedule, "Old Media", "Media not opened in a while", oldFilesCount, totalSize / 5, "Review", onNavigateToSmartClean),
        CategoryCardModel(Icons.Default.ViewAgenda, "Statuses", "Old temporary status files", 0, 0, "Review", onNavigateToMediaViewer),
        CategoryCardModel(Icons.Default.ViewAgenda, "Memes & Stickers", "Forwarded and low-value media", memeCount, totalSize / 8, "Review", onNavigateToMemeAnalyzer),
        CategoryCardModel(Icons.Default.ImageSearch, "Blurry Images", "Low-quality images to review", 0, 0, "Manual", onNavigateToSmartClean),
        CategoryCardModel(Icons.Default.Storage, "Review Carefully", "Files that need manual decision", junkCount + spamCount, totalSize / 12, "Caution", onNavigateToJunk)
    )
    val visibleCards = categoryCards.filter { it.fileCount > 0 || it.storageBytes > 0L }
    val totalScanned = totalSize.coerceAtLeast(0L)
    val lastScanLabel = remember(summaryInfo) { if (summaryInfo.contains("Found")) SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date()) else "Never" }

    androidx.compose.material3.ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        AppDrawer { route ->
            scope.launch { drawerState.close() }
            when (route) {
                "home" -> Unit; "scan_again" -> onRefreshClick(); "smart_review" -> onNavigateToSmartClean(); "categories" -> onNavigateToFeatures(); "storage_overview" -> onNavigateToAnalytics(); "duplicate_finder" -> onNavigateToDuplicates(); "large_files" -> onNavigateToMediaViewer(); "old_media" -> onNavigateToSmartClean(); "blurry_images" -> onNavigateToSmartClean(); "scan_history" -> onNavigateToFeatures(); "cleanup_receipt" -> onNavigateToFeatures(); "privacy_policy" -> onNavigateToPrivacyPolicy(); "terms" -> onNavigateToTerms(); "about" -> onNavigateToAbout(); "settings" -> onNavigateToSettings(); "help_feedback" -> onNavigateToSettings()
            }
        }
    }) {
        LazyColumn(modifier = Modifier.fillMaxSize().background(AppBg), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Menu", tint = MainText) }; Text("ChatSweep", color = MainText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                IconButton(onClick = onRefreshClick) { Icon(Icons.Default.Refresh, "Scan Again", tint = PrimaryBlue) }
            } }
            item { Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Chat media", color = MainText, fontWeight = FontWeight.SemiBold)
                Text("Total scanned: ${formatSize(totalScanned)}", color = MainText)
                Text("Potential cleanup: ${formatSize(smartSuggestionSummary.totalSpaceToFree)}", color = SuccessGreen)
                Text("Last scan: $lastScanLabel", color = SecondaryText)
                Text("Files: $totalFiles", color = SecondaryText)
                SegmentedBar(visibleCards.map { it.storageBytes }, totalScanned)
                Button(onClick = onNavigateToSmartClean, modifier = Modifier.fillMaxWidth()) { Text(if (totalFiles > 0) "SMART REVIEW" else "START SMART SCAN") }
                Text("Scan Again", color = PrimaryBlue, modifier = Modifier.clickable { onRefreshClick() })
            } } }
            items(visibleCards) { card ->
                val pct = if (totalScanned > 0L) ((card.storageBytes.toDouble() / totalScanned) * 100).toInt().coerceIn(0, 100) else 0
                Card(modifier = Modifier.fillMaxWidth().clickable(enabled = card.fileCount > 0) { card.route() }, colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(PrimaryBlue.copy(.12f), CircleShape), contentAlignment = Alignment.Center) { Icon(card.icon, null, tint = PrimaryBlue) }
                        Column(Modifier.weight(1f).padding(start = 10.dp)) { Text(card.title, color = MainText); Text(card.description, color = SecondaryText, style = MaterialTheme.typography.bodySmall); Text("${card.fileCount} files • ${formatSize(card.storageBytes)} • $pct%", color = SecondaryText, style = MaterialTheme.typography.labelMedium) }
                        Text(if (card.fileCount > 0) card.safety else "Clean", color = if (card.fileCount > 0) PrimaryBlue else SuccessGreen); Icon(Icons.Default.ChevronRight, null, tint = SecondaryText)
                    }
                }
            }
            item { Text("No cloud upload. Nothing is deleted automatically.", color = SecondaryText, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun SegmentedBar(values: List<Long>, total: Long) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(color = Border, shape = RoundedCornerShape(99.dp))
    ) {
        values.take(7).forEachIndexed { idx, v ->
            val w = if (total > 0L) (v.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
            Box(
                Modifier
                    .weight(if (w == 0f) 0.0001f else w)
                    .fillMaxSize()
                    .background(
                        color = listOf(
                            PrimaryBlue,
                            Color(0xFF4F8EFF),
                            Color(0xFF6BA2FF),
                            Color(0xFF8DB8FF),
                            Color(0xFFA6C9FF),
                            Color(0xFFC0DAFF),
                            Color(0xFFD9EBFF)
                        ).getOrElse(idx) { PrimaryBlue }
                    )
            )
        }
    }
}
