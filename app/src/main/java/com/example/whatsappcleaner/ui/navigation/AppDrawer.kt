package com.example.whatsappcleaner.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawer(
    selectedRoute: String?,
    lastScanSummary: String? = null,
    onItemClick: (String) -> Unit
) {
    ModalDrawerSheet(drawerContainerColor = Color.White) {
        Column(Modifier.fillMaxWidth().background(Color.White).padding(20.dp)) {
            Text("ChatSweep", style = MaterialTheme.typography.headlineSmall)
            Text("Private offline media cleaner", style = MaterialTheme.typography.bodySmall)
            if (!lastScanSummary.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(lastScanSummary, style = MaterialTheme.typography.labelMedium, color = Color(0xFF607D8B))
            }
        }
        Section("STORAGE")
        DrawerItem("Media", Icons.Default.PermMedia, selectedRoute == "media_overview") { onItemClick("media_overview") }
        DrawerItem("Photos", Icons.Default.Photo, selectedRoute == "photos") { onItemClick("photos") }
        DrawerItem("Videos", Icons.Default.VideoFile, selectedRoute == "videos") { onItemClick("videos") }
        DrawerItem("Audio", Icons.Default.GraphicEq, selectedRoute == "audio") { onItemClick("audio") }
        DrawerItem("Documents", Icons.Default.Description, selectedRoute == "documents") { onItemClick("documents") }
        DrawerItem("Statuses", Icons.Default.Schedule, selectedRoute == "statuses") { onItemClick("statuses") }
        DrawerItem("Stickers", Icons.Default.EmojiEmotions, selectedRoute == "stickers") { onItemClick("stickers") }
        Section("CLEANING TOOLS")
        DrawerItem("Smart Review", Icons.Default.AutoAwesome, selectedRoute == "smart_review") { onItemClick("smart_review") }
        DrawerItem("Duplicate Finder", Icons.Default.ContentCopy, selectedRoute == "duplicate_finder") { onItemClick("duplicate_finder") }
        DrawerItem("Large Files", Icons.Default.Folder, selectedRoute == "large_files") { onItemClick("large_files") }
        DrawerItem("Old Media", Icons.Default.History, selectedRoute == "old_media") { onItemClick("old_media") }
        DrawerItem("Memes & Stickers", Icons.Default.Mood, selectedRoute == "memes_stickers") { onItemClick("memes_stickers") }
        DrawerItem("Blurry Images", Icons.Default.BlurOn, selectedRoute == "blurry_images") { onItemClick("blurry_images") }
        DrawerItem("Scan Again", Icons.Default.Refresh, false) { onItemClick("scan_again") }
        Section("REMINDERS")
        DrawerItem("Cleanup Reminder", Icons.Default.Notifications, selectedRoute == "cleanup_reminder") { onItemClick("cleanup_reminder") }
        Section("APP")
        DrawerItem("Scan History", Icons.Default.Timeline, selectedRoute == "scan_history") { onItemClick("scan_history") }
        DrawerItem("Last Cleanup Receipt", Icons.Default.ReceiptLong, selectedRoute == "last_cleanup_receipt") { onItemClick("last_cleanup_receipt") }
        DrawerItem("Storage Overview", Icons.Default.Storage, selectedRoute == "storage_overview") { onItemClick("storage_overview") }
        DrawerItem("Settings", Icons.Default.Settings, selectedRoute == "settings") { onItemClick("settings") }
        DrawerItem("Help & Feedback", Icons.Default.Help, selectedRoute == "help_feedback") { onItemClick("help_feedback") }
        DrawerItem("Privacy Policy", Icons.Default.Policy, selectedRoute == "privacy_policy") { onItemClick("privacy_policy") }
        DrawerItem("Terms & Conditions", Icons.Default.Gavel, selectedRoute == "terms") { onItemClick("terms") }
        DrawerItem("About ChatSweep", Icons.Default.Info, selectedRoute == "about") { onItemClick("about") }
    }
}
@Composable private fun Section(title: String) { Spacer(Modifier.height(10.dp)); Text(title, modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2F6FED)) }
@Composable private fun DrawerItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        modifier = Modifier.heightIn(min = 52.dp),
        label = { Row(Modifier.fillMaxWidth()) { Text(label); Spacer(Modifier.width(8.dp)) } },
        icon = { Icon(icon, null) },
        selected = selected,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFFE9F2FF),
            unselectedContainerColor = Color.Transparent
        ),
        onClick = onClick
    )
}
