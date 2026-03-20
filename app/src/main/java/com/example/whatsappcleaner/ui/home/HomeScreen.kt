@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.example.whatsappcleaner.ui.theme.BrandNavy
import com.example.whatsappcleaner.ui.theme.DividerColor
import com.example.whatsappcleaner.ui.theme.PrimaryBackground
import com.example.whatsappcleaner.ui.theme.SurfaceMuted
import com.example.whatsappcleaner.ui.theme.SurfaceWhite
import com.example.whatsappcleaner.ui.theme.TextMain
import com.example.whatsappcleaner.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
    onNavigateToJunk: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSpam: () -> Unit,
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
    val snackbarHostState = remember { SnackbarHostState() }
    var selected by rememberSaveable { mutableStateOf(setOf<String>()) }
    var pendingDelete by remember { mutableStateOf<SimpleMediaItem?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet { AppDrawer { destination ->
                scope.launch { drawerState.close() }
                when (destination) {
                    "home" -> Unit
                    "smart_clean" -> onNavigateToSmartClean()
                    "analytics_screen" -> onNavigateToAnalytics()
                    else -> onNavigateToPhoneReality()
                }
            } }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WhatsApp Cleaner", color = BrandNavy)
                            Text("Premium storage care", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = BrandNavy)
                        }
                    }
                )
            },
            containerColor = PrimaryBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    PremiumHeaderCard(
                        summaryInfo = summaryInfo,
                        remindersEnabled = remindersEnabled,
                        onRemindersToggle = onRemindersToggle,
                        largeTodayCount = largeTodayCount,
                        largeTodaySizeText = largeTodaySizeText,
                        screenshotTodayCount = screenshotTodayCount,
                        screenshotTodaySizeText = screenshotTodaySizeText,
                        selectedFrequency = selectedFrequency,
                        selectedTime = selectedTime
                    )
                }
                item {
                    GradientHeroButton(
                        text = "⚡ Smart Clean Now",
                        onClick = onNavigateToSmartClean,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    InsightGrid(
                        memeCount = memeCount,
                        spamCount = spamCount,
                        junkCount = junkCount,
                        duplicateCount = duplicateCount,
                        onNavigateToSmartClean = onNavigateToSmartClean,
                        onNavigateToPhoneReality = onNavigateToPhoneReality,
                        onNavigateToMemeAnalyzer = onNavigateToMemeAnalyzer,
                        onNavigateToJunk = onNavigateToJunk,
                        onNavigateToAnalytics = onNavigateToAnalytics,
                        onNavigateToSpam = onNavigateToSpam
                    )
                }
                item {
                    SuggestionStrip(
                        activeSuggestion = activeSuggestion,
                        largeTodayCount = largeTodayCount,
                        screenshotTodayCount = screenshotTodayCount,
                        onSuggestionChange = onSuggestionChange
                    )
                }
                item {
                    Text("📂 Browse media", style = MaterialTheme.typography.titleLarge, color = TextMain)
                }
                item {
                    FilterTabs(currentFilter = currentFilter, onFilterChange = onFilterChange)
                }
                if (isLoading) {
                    items(4) { LoadingCard() }
                } else if (items.isEmpty()) {
                    item {
                        LegitCard {
                            FriendlyState(
                                icon = Icons.Default.CheckCircle,
                                title = "🎉 Nothing to clean!",
                                message = "Grant a quick review later or refresh to rescan your photos and videos."
                            )
                        }
                    }
                } else {
                    items(items, key = { it.uri.toString() }) { item ->
                        val isSelected = selected.contains(item.uri.toString())
                        MediaSwipeRow(
                            item = item,
                            selected = isSelected,
                            onClick = {
                                selected = if (isSelected) selected - item.uri.toString() else selected + item.uri.toString()
                            },
                            onDelete = { pendingDelete = item },
                            onKeep = {
                                scope.launch { snackbarHostState.showSnackbar("Kept ${item.name}") }
                            },
                            onOpen = { onOpenInSystem(item) }
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionButton(
                            text = "Refresh",
                            icon = Icons.Default.Refresh,
                            onClick = onRefreshClick,
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            text = "Storage",
                            icon = Icons.Default.Storage,
                            onClick = onOpenSystemStorage,
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            text = "Review (${selected.size})",
                            icon = Icons.Default.Visibility,
                            onClick = {
                                val first = items.firstOrNull { it.uri.toString() in selected }
                                if (first != null) onOpenInSystem(first) else onNavigateToMediaViewer()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = selected.isNotEmpty() || items.isNotEmpty()
                        )
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this file?", color = TextMain) },
            text = { Text("Are you sure you want to delete ${item.name}? We’ll open the file in the system viewer so you can remove it safely.", color = TextSecondary) },
            confirmButton = {
                LegitButton(
                    text = "Open delete flow",
                    onClick = {
                        pendingDelete = null
                        onOpenInSystem(item)
                        scope.launch { snackbarHostState.showSnackbar("Opened ${item.name} for safe deletion") }
                    }
                )
            },
            dismissButton = {
                LegitButton(text = "Cancel", onClick = { pendingDelete = null })
            },
            containerColor = SurfaceWhite,
            tonalElevation = 0.dp
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3B82F6),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF3B82F6).copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.85f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp, pressedElevation = 2.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = modifier.height(56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun PremiumHeaderCard(
    summaryInfo: String,
    remindersEnabled: Boolean,
    onRemindersToggle: (Boolean) -> Unit,
    largeTodayCount: Int,
    largeTodaySizeText: String,
    screenshotTodayCount: Int,
    screenshotTodaySizeText: String,
    selectedFrequency: ReminderFreq,
    selectedTime: ReminderTime
) {
    val totalTarget = (largeTodayCount + screenshotTodayCount).coerceAtLeast(1)
    val resolved = screenshotTodayCount.coerceAtMost(totalTarget)
    val ringProgress = resolved.toFloat() / totalTarget.toFloat()
    val brush = Brush.horizontalGradient(listOf(AccentBlue.copy(alpha = 0.22f), AccentPurple.copy(alpha = 0.18f)))

    LegitCard {
        Column(
            modifier = Modifier
                .background(brush)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("🔥 Streak + storage pulse", color = TextMain, style = MaterialTheme.typography.titleLarge)
                    Text(summaryInfo, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Large today: $largeTodayCount • $largeTodaySizeText", color = TextMain, style = MaterialTheme.typography.bodyMedium)
                    Text("Screenshots today: $screenshotTodayCount • $screenshotTodaySizeText", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
                StorageRing(progress = ringProgress, label = "${(ringProgress * 100).toInt()}%", subtitle = "storage health")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MiniMetricCard("🔥 6 day", "clean streak")
                MiniMetricCard(selectedFrequency.label, "reminder cadence")
                MiniMetricCard(selectedTime.label, "next reminder")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Daily reminders", color = TextMain, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = onRemindersToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentGreen)
                )
            }
        }
    }
}

@Composable
private fun MiniMetricCard(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceMuted.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(title, color = TextMain, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterTabs(currentFilter: MediaFilter, onFilterChange: (MediaFilter) -> Unit) {
    val filters = listOf(
        MediaFilter.ALL to "All",
        MediaFilter.IMAGES to "Images",
        MediaFilter.VIDEOS to "Videos",
        MediaFilter.MEMES to "Memes",
        MediaFilter.DUPLICATES to "Duplicates"
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        filters.forEach { (filter, title) ->
            FilterChip(
                selected = currentFilter == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(title) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentBlue.copy(alpha = 0.16f),
                    selectedLabelColor = TextMain,
                    containerColor = SurfaceWhite,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = currentFilter == filter,
                    borderColor = DividerColor,
                    selectedBorderColor = AccentBlue
                )
            )
        }
    }
}

@Composable
private fun InsightGrid(
    memeCount: Int,
    spamCount: Int,
    junkCount: Int,
    duplicateCount: Int,
    onNavigateToSmartClean: () -> Unit,
    onNavigateToPhoneReality: () -> Unit,
    onNavigateToMemeAnalyzer: () -> Unit,
    onNavigateToJunk: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSpam: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("📦 Insight cards", style = MaterialTheme.typography.titleLarge, color = TextMain)
        DashboardCardRow(
            DashboardCardModel("Smart Clean", "$junkCount junk • $duplicateCount duplicates", Icons.Default.AutoDelete, onNavigateToSmartClean),
            DashboardCardModel("Phone Reality", "Storage truth, streaks, usage", Icons.Default.Analytics, onNavigateToPhoneReality)
        )
        DashboardCardRow(
            DashboardCardModel("Meme Detector", "$memeCount memes detected", Icons.Default.Mood, onNavigateToMemeAnalyzer),
            DashboardCardModel("Junk Files", "$junkCount review-worthy files", Icons.Default.CleaningServices, onNavigateToJunk)
        )
        DashboardCardRow(
            DashboardCardModel("Storage Analytics", "Visual usage and trends", Icons.Default.AutoAwesome, onNavigateToAnalytics),
            DashboardCardModel("Spam Media", "$spamCount possible spam items", Icons.Default.Shield, onNavigateToSpam)
        )
    }
}

private data class DashboardCardModel(val title: String, val subtitle: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
private fun DashboardCardRow(first: DashboardCardModel, second: DashboardCardModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        DashboardCard(first, Modifier.weight(1f))
        DashboardCard(second, Modifier.weight(1f))
    }
}

@Composable
private fun DashboardCard(model: DashboardCardModel, modifier: Modifier = Modifier) {
    LegitCard(modifier = modifier.clickable(onClick = model.onClick)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentBlue.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(model.icon, contentDescription = null, tint = AccentBlue)
            }
            Text(model.title, color = TextMain, style = MaterialTheme.typography.titleMedium)
            Text(model.subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
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
            Text("Quick wins", style = MaterialTheme.typography.titleMedium, color = TextMain)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SuggestionChip("Everything", activeSuggestion == SuggestionType.NONE) { onSuggestionChange(SuggestionType.NONE) }
                SuggestionChip("Large today ($largeTodayCount)", activeSuggestion == SuggestionType.LARGE_TODAY) { onSuggestionChange(SuggestionType.LARGE_TODAY) }
                SuggestionChip("Screenshots ($screenshotTodayCount)", activeSuggestion == SuggestionType.SCREENSHOTS_TODAY) { onSuggestionChange(SuggestionType.SCREENSHOTS_TODAY) }
            }
        }
    }
}

@Composable
private fun SuggestionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) AccentGreen.copy(alpha = 0.18f) else SurfaceMuted)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, color = if (selected) AccentGreen else TextSecondary, style = MaterialTheme.typography.labelLarge)
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
        positionalThreshold = { it * 0.35f },
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
                .background(if (selected) AccentBlue.copy(alpha = 0.12f) else SurfaceWhite)
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) AccentBlue.copy(alpha = 0.18f) else SurfaceMuted),
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
                    tint = if (selected) AccentBlue else TextSecondary
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, maxLines = 1, color = TextMain, style = MaterialTheme.typography.titleSmall)
                Text(item.mimeType ?: "Unknown media", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Text(formatSize(item.sizeKb.toLong() * 1024L), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
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
                .height(96.dp)
                .clip(RoundedCornerShape(22.dp))
                .shimmerEffect()
        )
        HorizontalDivider(color = DividerColor.copy(alpha = 0.4f))
    }
}
