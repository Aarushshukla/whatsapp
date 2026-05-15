package com.example.whatsappcleaner.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class DrawerEntry(val route: String, val label: String, val icon: ImageVector)

private val mainEntries = listOf(
    DrawerEntry("home", "Dashboard", Icons.Default.Dashboard),
    DrawerEntry("smart_review", "Smart Review", Icons.Default.AutoAwesome),
    DrawerEntry("scan_again", "Scan Again", Icons.Default.Refresh),
    DrawerEntry("categories", "Categories", Icons.Default.Category)
)

private val storageEntries = listOf(
    DrawerEntry("media_overview", "Media Overview", Icons.Default.PermMedia),
    DrawerEntry("photos", "Photos", Icons.Default.Photo),
    DrawerEntry("videos", "Videos", Icons.Default.VideoFile),
    DrawerEntry("audio", "Audio", Icons.Default.GraphicEq),
    DrawerEntry("documents", "Documents", Icons.Default.Description),
    DrawerEntry("statuses", "Statuses", Icons.Default.Schedule),
    DrawerEntry("stickers", "Stickers", Icons.Default.EmojiEmotions)
)

private val toolEntries = listOf(
    DrawerEntry("duplicate_finder", "Duplicate Finder", Icons.Default.ContentCopy),
    DrawerEntry("large_files", "Large Files", Icons.Default.Folder),
    DrawerEntry("old_media", "Old Media", Icons.Default.History),
    DrawerEntry("blurry_images", "Blurry Images", Icons.Default.BlurOn),
    DrawerEntry("scan_history", "Scan History", Icons.Default.Timeline),
    DrawerEntry("cleanup_receipt", "Last Cleanup Receipt", Icons.Default.ReceiptLong),
    DrawerEntry("storage_overview", "Storage Overview", Icons.Default.Storage)
)

private val legalEntries = listOf(
    DrawerEntry("privacy_policy", "Privacy Policy", Icons.Default.Policy),
    DrawerEntry("terms", "Terms & Conditions", Icons.Default.Gavel),
    DrawerEntry("about", "About ChatSweep", Icons.Default.Info)
)

private val settingsEntries = listOf(
    DrawerEntry("settings", "Settings", Icons.Default.Settings),
    DrawerEntry("help_feedback", "Help & Feedback", Icons.Default.Help)
)

@Composable
fun AppDrawer(
    visibleRoutes: Set<String>,
    onItemClick: (String) -> Unit
) {
    ModalDrawerSheet(drawerContainerColor = Color.White) {
        Column(Modifier.fillMaxWidth().background(Color.White).padding(20.dp)) {
            Text("ChatSweep", style = MaterialTheme.typography.headlineSmall)
            Text("Private offline media cleaner", style = MaterialTheme.typography.bodySmall)
        }
        DrawerSection("MAIN", mainEntries, visibleRoutes, onItemClick)
        DrawerSection("STORAGE", storageEntries, visibleRoutes, onItemClick)
        DrawerSection("TOOLS", toolEntries, visibleRoutes, onItemClick)
        DrawerSection("LEGAL", legalEntries, visibleRoutes, onItemClick)
        DrawerSection("SETTINGS", settingsEntries, visibleRoutes, onItemClick)
    }
}

@Composable
private fun DrawerSection(
    title: String,
    entries: List<DrawerEntry>,
    visibleRoutes: Set<String>,
    onItemClick: (String) -> Unit
) {
    val visible = entries.filter { it.route in visibleRoutes }
    if (visible.isEmpty()) return
    Section(title)
    visible.forEach { item ->
        DrawerItem(item.label, item.icon) { onItemClick(item.route) }
    }
}

@Composable private fun Section(title: String) { Spacer(Modifier.height(10.dp)); Text(title, modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelLarge, color = Color(0xFF2F6FED)) }
@Composable private fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) { NavigationDrawerItem(label = { Text(label) }, icon = { androidx.compose.material3.Icon(icon, null) }, selected = false, onClick = onClick) }
