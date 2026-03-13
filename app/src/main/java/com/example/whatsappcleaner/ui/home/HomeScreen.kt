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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Coil Imports for real thumbnails
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.VideoFrameDecoder

import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize

import com.example.whatsappcleaner.ui.theme.*
import com.example.whatsappcleaner.ui.components.*
import com.example.whatsappcleaner.ui.navigation.AppDrawer

enum class MediaFilter { ALL, IMAGES, VIDEOS, OTHER }
enum class SuggestionType { NONE, LARGE_TODAY, SCREENSHOTS_TODAY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHomeScreen(
    todayItems: List<SimpleMediaItem>,
    olderItems: List<SimpleMediaItem>,
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

    val allDisplayedItems = todayItems + olderItems

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { AppDrawer { scope.launch { drawerState.close() } } }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Gallery Overview", style = MaterialTheme.typography.titleLarge, color = BrandNavy) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = BrandNavy)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PrimaryBackground)
                )
            },
            containerColor = PrimaryBackground
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize()) {

                LegitCard {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = AccentBlue)
                            Spacer(Modifier.width(8.dp))
                            Text("Storage Status", style = MaterialTheme.typography.titleMedium, color = BrandNavy)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(summaryInfo, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                        Spacer(Modifier.height(16.dp))
                        Divider(color = Color.LightGray.copy(alpha = 0.2f))
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

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip("All", MediaFilter.ALL, currentFilter, onFilterChange)
                    FilterChip("Images", MediaFilter.IMAGES, currentFilter, onFilterChange)
                    FilterChip("Videos", MediaFilter.VIDEOS, currentFilter, onFilterChange)
                }

                Spacer(Modifier.height(16.dp))

                if (todayItems.isEmpty() && olderItems.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, "Empty", tint = AccentGreen, modifier = Modifier.size(72.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("All Clean!", style = MaterialTheme.typography.headlineSmall, color = BrandNavy)
                            Spacer(Modifier.height(8.dp))
                            Text("No files found.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(Modifier.weight(1f)) {
                        // --- TODAY BIFURCATION HEADER ---
                        if (todayItems.isNotEmpty()) {
                            item {
                                Text("Added Today", style = MaterialTheme.typography.titleMedium, color = AccentBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            items(todayItems) { item ->
                                val isSelected = selected.contains(item.uri.toString())
                                MediaRow(item, isSelected) { selected = if (isSelected) selected - item.uri.toString() else selected + item.uri.toString() }
                            }
                        }

                        // --- OLDER BIFURCATION HEADER ---
                        if (olderItems.isNotEmpty()) {
                            item {
                                Text("Existing Media", style = MaterialTheme.typography.titleMedium, color = TextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                            }
                            items(olderItems) { item ->
                                val isSelected = selected.contains(item.uri.toString())
                                MediaRow(item, isSelected) { selected = if (isSelected) selected - item.uri.toString() else selected + item.uri.toString() }
                            }
                        }
                    }
                }

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
        val selectedItemsList = allDisplayedItems.filter { selected.contains(it.uri.toString()) }
        ReviewDialog(
            items = selectedItemsList,
            onDismiss = { showReviewDialog = false },
            onOpenNext = { index -> if (index in selectedItemsList.indices) onOpenInSystem(selectedItemsList[index]) },
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
    val context = LocalContext.current

    val isVideo = item.mimeType?.startsWith("video", ignoreCase = true) == true ||
            item.name.endsWith(".mp4", ignoreCase = true)

    val isImage = item.mimeType?.startsWith("image", ignoreCase = true) == true ||
            item.name.endsWith(".jpg", ignoreCase = true) ||
            item.name.endsWith(".jpeg", ignoreCase = true) ||
            item.name.endsWith(".png", ignoreCase = true)

    val imageRequest = ImageRequest.Builder(context)
        .data(item.uri)
        .crossfade(true)
        .apply {
            if (isVideo) {
                decoderFactory(VideoFrameDecoder.Factory())
            }
        }
        .build()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AccentBlue.copy(alpha = 0.1f) else SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            if (isImage || isVideo) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "Media Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (isVideo) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            } else {
                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = TextSecondary)
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AccentBlue.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White)
                }
            }
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