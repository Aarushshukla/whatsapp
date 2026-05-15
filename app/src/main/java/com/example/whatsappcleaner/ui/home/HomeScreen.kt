package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
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
fun SimpleHomeScreen(items: List<SimpleMediaItem>, onRefreshClick: () -> Unit, summaryInfo: String, isLoading: Boolean, currentFilter: MediaFilter, onFilterChange: (MediaFilter) -> Unit, remindersEnabled: Boolean, onRemindersToggle: (Boolean) -> Unit, memeCount: Int, spamCount: Int, junkCount: Int, duplicateCount: Int, isProUser: Boolean, onNavigateToSmartClean: () -> Unit, onNavigateToPhoneReality: () -> Unit, onNavigateToMemeAnalyzer: () -> Unit, onNavigateToMediaViewer: () -> Unit, onNavigateToJunk: () -> Unit, onNavigateToAnalytics: () -> Unit, onNavigateToSpam: () -> Unit, onNavigateToSettings: () -> Unit, onNavigateToDuplicates: () -> Unit, onBulkDeleteClick: () -> Unit, onUpgradeToPro: () -> Unit, onDeleteConfirmed: () -> Unit, onDeleteItemsRequested: (List<SimpleMediaItem>) -> Unit, onOpenInSystem: (SimpleMediaItem) -> Unit, onOpenSystemStorage: () -> Unit, pendingDeleteUris: Set<String>, isDeleteInProgress: Boolean, deleteSnackbarMessage: String?, onUndoDelete: () -> Unit, onDeleteSnackbarConsumed: () -> Unit, selectedFrequency: ReminderFreq, onFrequencyChange: (ReminderFreq) -> Unit, selectedTime: ReminderTime, allTimeOptions: List<ReminderTime>, onTimeChange: (ReminderTime) -> Unit, largeTodayCount: Int, largeTodaySizeText: String, screenshotTodayCount: Int, screenshotTodaySizeText: String, activeSuggestion: SuggestionType, onSuggestionChange: (SuggestionType) -> Unit, totalFiles: Int, totalSize: Long, oldFilesCount: Int, smartSuggestionSummary: SmartSuggestionSummary, smartSuggestedItems: List<SimpleMediaItem>, suggestionReasonsByUri: Map<String, List<String>>, scanUiState: ScanUiState, onNavigateToFeatures: () -> Unit, onAiFeatureClick: (AiFeature) -> Unit, onNavigateToPrivacyPolicy: () -> Unit, onNavigateToTerms: () -> Unit, onNavigateToAbout: () -> Unit) {
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
                    "smart_review" -> onNavigateToSmartClean()
                    "scan_again" -> onRefreshClick()
                    "settings" -> onNavigateToSettings()
                    "privacy_policy" -> onNavigateToPrivacyPolicy()
                    "terms" -> onNavigateToTerms()
                    "about" -> onNavigateToAbout()
                    else -> onNavigateToPhoneReality()
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
                        onClick = if (hasScanSummary) onNavigateToSmartClean else onRefreshClick,
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
