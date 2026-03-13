package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.airbnb.lottie.compose.*
import com.example.whatsappcleaner.R
// --- FIXED IMPORTS ---
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
// --------------------
import com.example.whatsappcleaner.ui.theme.*
import com.example.whatsappcleaner.ui.components.*
import com.example.whatsappcleaner.ui.navigation.AppDrawer

enum class MediaFilter { ALL, IMAGES, VIDEOS, OTHER }
enum class SuggestionType { NONE, LARGE_TODAY, SCREENSHOTS_TODAY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHomeScreen(
    items: List<SimpleMediaItem>,
    onRefreshClick: () -> Unit,
    summaryInfo: String,
    currentFilter: MediaFilter,
    onFilterChange: (MediaFilter) -> Unit,
    largeTodayCount: Int,
    largeTodaySizeText: String,
    screenshotTodayCount: Int,
    screenshotTodaySizeText: String,
    activeSuggestion: SuggestionType,
    onSuggestionChange: (SuggestionType) -> Unit,
    remindersEnabled: Boolean,
    selectedFrequency: ReminderFreq,
    onFrequencyChange: (ReminderFreq) -> Unit,
    selectedTime: ReminderTime,
    allTimeOptions: List<ReminderTime>,
    onTimeChange: (ReminderTime) -> Unit,
    onRemindersToggle: (Boolean) -> Unit,
    onOpenInSystem: (SimpleMediaItem) -> Unit,
    onOpenSystemStorage: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showReviewDialog by remember { mutableStateOf(false) }

    // Lottie Animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_anim))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { AppDrawer { scope.launch { drawerState.close() } } }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Dashboard", style = MaterialTheme.typography.titleLarge, color = BrandNavy) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu", tint = BrandNavy)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PrimaryBackground)
                )
            },
            containerColor = PrimaryBackground
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize()
            ) {
                // 1. Stats Card
                LegitCard {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PieChart, null, tint = AccentBlue)
                            Spacer(Modifier.width(8.dp))
                            Text("Storage Overview", style = MaterialTheme.typography.titleMedium, color = BrandNavy)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(summaryInfo.replace("|", "\n"), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Spacer(Modifier.height(16.dp))
                        Divider(color = Color.LightGray.copy(0.2f))
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Daily Reminders", style = MaterialTheme.typography.bodyMedium, color = TextMain)
                            Switch(
                                checked = remindersEnabled,
                                onCheckedChange = onRemindersToggle,
                                colors = SwitchDefaults.colors(checkedThumbColor = SurfaceWhite, checkedTrackColor = AccentGreen)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // 2. Filters
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip("All", MediaFilter.ALL, currentFilter, onFilterChange)
                    FilterChip("Images", MediaFilter.IMAGES, currentFilter, onFilterChange)
                    FilterChip("Videos", MediaFilter.VIDEOS, currentFilter, onFilterChange)
                }
                Spacer(Modifier.height(16.dp))

                // 3. List
                if (items.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (composition != null) {
                            LottieAnimation(composition, { progress }, modifier = Modifier.size(200.dp))
                        } else {
                            FriendlyState(Icons.Default.CheckCircle, "All Clean", "No files found.")
                        }
                    }
                } else {
                    LazyColumn(Modifier.weight(1f)) {
                        items(items) { item ->
                            val isSelected = selected.contains(item.uri.toString())
                            MediaRow(item, isSelected) {
                                selected = if (isSelected) selected - item.uri.toString() else selected + item.uri.toString()
                            }
                        }
                    }
                }

                // 4. Buttons
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegitButton("Refresh", onRefreshClick, Modifier.weight(1f))
                    LegitButton("Review (${selected.size})", { showReviewDialog = true }, Modifier.weight(1f), isDestructive = true, enabled = selected.isNotEmpty())
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showReviewDialog) {
        val selectedItems = items.filter { selected.contains(it.uri.toString()) }
        ReviewDialog(
            selectedItems,
            onDismiss = { showReviewDialog = false },
            onOpenNext = { index -> if (index in selectedItems.indices) onOpenInSystem(selectedItems[index]) },
            onRefresh = { showReviewDialog = false; onRefreshClick(); selected = emptySet() }
        )
    }
}

@Composable
fun FilterChip(label: String, value: MediaFilter, current: MediaFilter, onChange: (MediaFilter) -> Unit) {
    val selected = value == current
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) BrandNavy else SurfaceWhite,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
        modifier = Modifier.clickable { onChange(value) }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (selected) Color.White else TextMain,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun MediaRow(item: SimpleMediaItem, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth().padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AccentBlue.copy(0.1f) else SurfaceWhite)
            .clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(if (selected) AccentBlue else Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            if(selected) Icon(Icons.Default.Check, null, tint = Color.White)
            else Icon(Icons.Default.InsertDriveFile, null, tint = TextSecondary)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, color = TextMain)
            Text(formatSize(item.sizeKb.toLong() * 1024L), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
fun ReviewDialog(items: List<SimpleMediaItem>, onDismiss: () -> Unit, onOpenNext: (Int) -> Unit, onRefresh: () -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = { Text("Review Files", color = BrandNavy) },
        text = {
            Column {
                Text("Selected (${items.size}):", color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                if (items.isNotEmpty() && currentIndex < items.size) {
                    Text("▶ ${items[currentIndex].name}", style = MaterialTheme.typography.bodyMedium, color = AccentBlue)
                }
                Text("Tap 'Open Next' to view/delete in system gallery.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (items.isNotEmpty()) { onOpenNext(currentIndex); if (currentIndex < items.lastIndex) currentIndex++ } }
            ) { Text("Open Next", color = AccentBlue) }
        },
        dismissButton = {
            TextButton(onClick = onRefresh) { Text("Done", color = TextSecondary) }
        }
    )
}
