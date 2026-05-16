package com.example.whatsappcleaner.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale

@Composable
fun CleanupReminderScreen(
    reminderEnabled: Boolean,
    selectedIntervalMinutes: Long,
    permissionDenied: Boolean,
    onIntervalSelected: (Long) -> Unit,
    onEnableToggle: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancelReminder: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val options = listOf(15L, 30L, 45L, 60L, 120L, 240L, 360L, 720L, 1440L, 10080L)
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF6F8FC)).verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Cleanup Reminder", style = MaterialTheme.typography.headlineSmall)
        Text("Get a gentle reminder to review chat media before it fills your storage.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        val reminderCardScale by animateFloatAsState(targetValue = if (reminderEnabled) 1f else 0.985f, animationSpec = tween(220), label = "reminder_card_scale")
        Card(modifier = Modifier.scale(reminderCardScale), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Text(" Enable reminder", modifier = Modifier.padding(start = 8.dp))
                    }
                    Switch(checked = reminderEnabled, onCheckedChange = onEnableToggle)
                }
                Spacer(Modifier.height(8.dp))
                val reminderTextColor by animateColorAsState(if (reminderEnabled) Color(0xFF1B8E3E) else MaterialTheme.colorScheme.onSurfaceVariant, tween(220), label = "reminder_text")
                Text(if (reminderEnabled) "Reminder active: every ${labelFor(selectedIntervalMinutes)}" else "No reminder set", color = reminderTextColor)
            }
        }

        Spacer(Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { minutes ->
                    val selected = minutes == selectedIntervalMinutes
                    val chipScale by animateFloatAsState(targetValue = if (selected) 1f else 0.96f, animationSpec = tween(180), label = "chip_scale_$minutes")
                    FilterChip(
                        modifier = Modifier.scale(chipScale),
                        selected = selected,
                        onClick = { onIntervalSelected(minutes) },
                        label = { Text(labelFor(minutes)) },
                        leadingIcon = if (selected) ({ Text("✓") }) else null,
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFE9F7EF))
                    )
                }
            }
        }

        if (permissionDenied) {
            Spacer(Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Notifications are blocked. Enable notification permission in Settings to receive reminders.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onOpenSettings) { Text("Open Settings") }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Save Reminder") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onCancelReminder, modifier = Modifier.fillMaxWidth()) { Text("Cancel Reminder") }
    }
}

private fun labelFor(minutes: Long): String = when (minutes) {
    60L -> "1 hour"
    120L -> "2 hours"
    240L -> "4 hours"
    360L -> "6 hours"
    720L -> "12 hours"
    1440L -> "24 hours"
    10080L -> "Weekly"
    else -> "$minutes min"
}
