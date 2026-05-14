package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.SimpleMediaItem
import com.example.whatsappcleaner.data.formatSize
import com.example.whatsappcleaner.ui.FrequencyOption

enum class MediaFilter { ALL, IMAGES, VIDEOS, OTHER }
enum class SuggestionType { NONE, LARGE_TODAY, SCREENSHOTS_TODAY }

@Composable
fun SimpleHomeScreen(
    items: List<SimpleMediaItem>,
    onRefreshClick: () -> Unit,
    permissionInfo: String,
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
    selectedFrequency: FrequencyOption,
    onFrequencyChange: (FrequencyOption) -> Unit,
    reminderHour: Int,
    reminderMinute: Int,
    onTimeChange: (Int, Int) -> Unit,
    onRemindersToggle: (Boolean) -> Unit,
    onOpenInSystem: (SimpleMediaItem) -> Unit,
    onOpenSystemStorage: () -> Unit
) {
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "ChatSweep",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Independent cleaner for chat media files. Not affiliated with WhatsApp.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (summaryInfo.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = summaryInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(6.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cleanup reminders",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Get periodic reminders to keep media storage healthy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = onRemindersToggle
                    )
                }

                if (remindersEnabled) {
                    Spacer(Modifier.height(10.dp))

                    ReminderDropdownRow(
                        label = "Frequency",
                        value = selectedFrequency.label,
                        options = listOf("Daily", "Weekly"),
                        onSelectedIndex = { index ->
                            val option = if (index == 0) {
                                FrequencyOption("Daily", 1)
                            } else {
                                FrequencyOption("Weekly", 7)
                            }
                            onFrequencyChange(option)
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Reminder time: ${String.format("%02d:%02d", reminderHour, reminderMinute)}"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip("All", MediaFilter.ALL, currentFilter, onFilterChange)
            FilterChip("Images", MediaFilter.IMAGES, currentFilter, onFilterChange)
            FilterChip("Videos", MediaFilter.VIDEOS, currentFilter, onFilterChange)
            FilterChip("Other", MediaFilter.OTHER, currentFilter, onFilterChange)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (largeTodayCount > 0) {
                SuggestionChip(
                    label = "Large today: $largeTodayCount • $largeTodaySizeText",
                    value = SuggestionType.LARGE_TODAY,
                    current = activeSuggestion,
                    onChange = onSuggestionChange
                )
            }
            if (screenshotTodayCount > 0) {
                SuggestionChip(
                    label = "Screenshots: $screenshotTodayCount • $screenshotTodaySizeText",
                    value = SuggestionType.SCREENSHOTS_TODAY,
                    current = activeSuggestion,
                    onChange = onSuggestionChange
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = permissionInfo,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(items) { item ->
                val isSelected = selected.contains(item.uri.toString())
                MediaRow(
                    item = item,
                    selected = isSelected,
                    onClick = {
                        selected = if (isSelected) {
                            selected - item.uri.toString()
                        } else {
                            selected + item.uri.toString()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onRefreshClick) {
                Text("Refresh")
            }
            Button(
                onClick = { showDialog = true },
                enabled = selected.isNotEmpty()
            ) {
                Text("Review (${selected.size})")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onOpenSystemStorage) {
                Text("System storage")
            }
        }
    }

    if (showDialog) {
        val selectedItems = items.filter { selected.contains(it.uri.toString()) }
        ReviewDialog(
            items = selectedItems,
            onDismiss = { showDialog = false },
            onOpenNext = { index ->
                if (index in selectedItems.indices) {
                    onOpenInSystem(selectedItems[index])
                }
            },
            onRefresh = {
                showDialog = false
                onRefreshClick()
                selected = emptySet()
            }
        )
    }

    if (showTimePicker) {
        ReminderTimePickerDialog(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                showTimePicker = false
                onTimeChange(hour, minute)
            }
        )
    }
}

@Composable
private fun ReminderDropdownRow(
    label: String,
    value: String,
    options: List<String>,
    onSelectedIndex: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Box {
            Surface(
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = value, style = MaterialTheme.typography.bodySmall)
                    Text(text = "▼", style = MaterialTheme.typography.bodySmall)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = {
                            expanded = false
                            onSelectedIndex(index)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reminder time") },
        text = {
            TimeInput(
                state = pickerState,
                colors = TimePickerDefaults.colors()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pickerState.hour, pickerState.minute) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MediaRow(
    item: SimpleMediaItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = if (selected) 4.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (selected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        Color.White
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${formatSize(item.sizeKb.toLong() * 1024L)}\n${item.path}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ReviewDialog(
    items: List<SimpleMediaItem>,
    onDismiss: () -> Unit,
    onOpenNext: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review in system app") },
        text = {
            Column {
                Text("Selected files (${items.size}):")
                Spacer(Modifier.height(8.dp))
                items.forEachIndexed { i, item ->
                    val prefix = if (i == currentIndex) "▶ " else "• "
                    Text("$prefix${item.name}", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                if (items.isNotEmpty()) {
                    Text(
                        text = "Tap \"Open next\" to open each item in Gallery/Files and delete it there. When done, tap \"I’m done – Refresh\".",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (items.isNotEmpty()) {
                        onOpenNext(currentIndex)
                        if (currentIndex < items.lastIndex) {
                            currentIndex++
                        }
                    }
                },
                enabled = items.isNotEmpty()
            ) {
                Text(
                    if (items.isNotEmpty())
                        "Open next (${currentIndex + 1}/${items.size})"
                    else
                        "Open next"
                )
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onRefresh) {
                    Text("I’m done – Refresh")
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun FilterChip(
    label: String,
    value: MediaFilter,
    current: MediaFilter,
    onChange: (MediaFilter) -> Unit
) {
    val selected = value == current
    Surface(
        tonalElevation = if (selected) 4.dp else 0.dp,
        color = if (selected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .clickable { onChange(value) }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    value: SuggestionType,
    current: SuggestionType,
    onChange: (SuggestionType) -> Unit
) {
    val selected = value == current
    Surface(
        tonalElevation = if (selected) 4.dp else 0.dp,
        color = if (selected)
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .clickable {
                onChange(if (selected) SuggestionType.NONE else value)
            }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.secondary else Color.Unspecified,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
