package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.components.FriendlyState
import com.example.whatsappcleaner.ui.components.LegitButton
import com.example.whatsappcleaner.ui.components.LegitCard
import com.example.whatsappcleaner.ui.navigation.AppDrawer
import com.example.whatsappcleaner.ui.theme.AccentBlue
import com.example.whatsappcleaner.ui.theme.AccentGreen
import com.example.whatsappcleaner.ui.theme.BrandNavy
import com.example.whatsappcleaner.ui.theme.PrimaryBackground
import com.example.whatsappcleaner.ui.theme.SurfaceWhite
import com.example.whatsappcleaner.ui.theme.TextMain
import com.example.whatsappcleaner.ui.theme.TextSecondary
import kotlinx.coroutines.launch

enum class MediaFilter { ALL, IMAGES, VIDEOS, OTHER }
enum class SuggestionType { NONE, LARGE_TODAY, SCREENSHOTS_TODAY }

@OptIn(ExperimentalMaterial3Api::class)
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
    onNavigateToSmartClean: () -> Unit,
    onNavigateToPhoneReality: () -> Unit,
    onNavigateToMemeAnalyzer: () -> Unit,
    onNavigateToMediaViewer: () -> Unit,
    onOpenInSystem: (SimpleMediaItem) -> Unit,
    onOpenSystemStorage: () -> Unit,
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
    onSuggestionChange: (SuggestionType) -> Unit
) {
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { ModalDrawerSheet { AppDrawer { scope.launch { drawerState.close() } } } }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Dashboard", color = BrandNavy) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = BrandNavy)
                        }
                    }
                )
            },
            containerColor = PrimaryBackground
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                LegitCard {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = AccentBlue)
                            Spacer(Modifier.size(8.dp))
                            Text("Storage Overview", color = BrandNavy, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(summaryInfo, color = TextSecondary)
                        Text("Large today: $largeTodayCount ($largeTodaySizeText)", color = TextSecondary)
                        Text("Screenshots today: $screenshotTodayCount ($screenshotTodaySizeText)", color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Daily Reminders", color = TextMain)
                            Switch(
                                checked = remindersEnabled,
                                onCheckedChange = onRemindersToggle,
                                colors = SwitchDefaults.colors(checkedTrackColor = AccentGreen)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                DashboardCards(
                    memeCount = memeCount,
                    spamCount = spamCount,
                    junkCount = junkCount,
                    duplicateCount = duplicateCount,
                    onNavigateToSmartClean = onNavigateToSmartClean,
                    onNavigateToPhoneReality = onNavigateToPhoneReality,
                    onNavigateToMemeAnalyzer = onNavigateToMemeAnalyzer,
                    onNavigateToMediaViewer = onNavigateToMediaViewer
                )
                Spacer(Modifier.height(10.dp))
                FilterTabs(currentFilter = currentFilter, onFilterChange = onFilterChange)
                Spacer(Modifier.height(8.dp))
                if (isLoading) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Loading media…", color = TextSecondary)
                    }
                } else if (items.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        FriendlyState(Icons.Default.CheckCircle, "All clean", "No files found for this tab.")
                    }
                } else {
                    LazyColumn(Modifier.weight(1f)) {
                        items(items) { item ->
                            val isSelected = selected.contains(item.uri.toString())
                            MediaRow(item = item, selected = isSelected) {
                                selected = if (isSelected) selected - item.uri.toString() else selected + item.uri.toString()
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegitButton("Refresh", onRefreshClick, Modifier.weight(1f))
                    LegitButton("Open storage", onOpenSystemStorage, Modifier.weight(1f))
                    LegitButton(
                        text = "Review (${selected.size})",
                        onClick = {
                            val first = items.firstOrNull { it.uri.toString() in selected }
                            first?.let(onOpenInSystem)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selected.isNotEmpty()
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun FilterTabs(currentFilter: MediaFilter, onFilterChange: (MediaFilter) -> Unit) {
    val filters = listOf(MediaFilter.ALL, MediaFilter.IMAGES, MediaFilter.VIDEOS)
    ScrollableTabRow(selectedTabIndex = filters.indexOf(currentFilter), edgePadding = 0.dp) {
        filters.forEach { filter ->
            Tab(selected = currentFilter == filter, onClick = { onFilterChange(filter) }, text = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) })
        }
    }
}

@Composable
private fun DashboardCards(
    memeCount: Int,
    spamCount: Int,
    junkCount: Int,
    duplicateCount: Int,
    onNavigateToSmartClean: () -> Unit,
    onNavigateToPhoneReality: () -> Unit,
    onNavigateToMemeAnalyzer: () -> Unit,
    onNavigateToMediaViewer: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DashboardCard("Smart Clean", "$junkCount junk • $duplicateCount duplicates", Icons.Default.AutoDelete, onNavigateToSmartClean)
        DashboardCard("Phone Reality", "Storage breakdown and heatmap", Icons.Default.Analytics, onNavigateToPhoneReality)
        DashboardCard("Meme Analyzer", "$memeCount memes detected", Icons.Default.Image, onNavigateToMemeAnalyzer)
        DashboardCard("Media Viewer", "$spamCount possible spam files", Icons.Default.VideoLibrary, onNavigateToMediaViewer)
    }
}

@Composable
private fun DashboardCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    LegitCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = AccentBlue)
            Spacer(Modifier.size(10.dp))
            Column {
                Text(title, color = BrandNavy, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun MediaRow(item: SimpleMediaItem, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AccentBlue.copy(alpha = 0.12f) else SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) Icons.Default.Check else Icons.Default.InsertDriveFile,
            contentDescription = null,
            tint = if (selected) AccentBlue else TextSecondary
        )
        Spacer(Modifier.size(10.dp))
        Column {
            Text(item.name, maxLines = 1, color = TextMain)
            Text(formatSize(item.sizeKb.toLong() * 1024L), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
