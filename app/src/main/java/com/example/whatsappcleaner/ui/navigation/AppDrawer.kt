package com.example.whatsappcleaner.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawer(onItemClick: (String) -> Unit) {
    ModalDrawerSheet(drawerContainerColor = Color.White) {
        Column(Modifier.fillMaxWidth().background(Color.White).padding(20.dp)) {
            Text("ChatSweep", style = MaterialTheme.typography.headlineSmall)
            Text("Private offline media cleaner", style = MaterialTheme.typography.bodySmall)
        }
        Section("MAIN"); DrawerItem("Dashboard", Icons.Default.Dashboard) { onItemClick("home") }; DrawerItem("Smart Review", Icons.Default.AutoAwesome) { onItemClick("smart_review") }; DrawerItem("Scan Again", Icons.Default.Refresh) { onItemClick("scan_again") }; DrawerItem("Categories", Icons.Default.Category) { onItemClick("categories") }
        Section("STORAGE"); DrawerItem("Media Overview", Icons.Default.PermMedia) { onItemClick("media_overview") }; DrawerItem("Photos", Icons.Default.Photo) { onItemClick("media_overview") }; DrawerItem("Videos", Icons.Default.VideoFile) { onItemClick("media_overview") }; DrawerItem("Audio", Icons.Default.GraphicEq) { onItemClick("media_overview") }; DrawerItem("Documents", Icons.Default.Description) { onItemClick("media_overview") }; DrawerItem("Statuses", Icons.Default.Schedule) { onItemClick("media_overview") }; DrawerItem("Stickers", Icons.Default.EmojiEmotions) { onItemClick("media_overview") }
        Section("TOOLS"); DrawerItem("Duplicate Finder", Icons.Default.ContentCopy) { onItemClick("duplicate_finder") }; DrawerItem("Large Files", Icons.Default.Folder) { onItemClick("large_files") }; DrawerItem("Old Media", Icons.Default.History) { onItemClick("old_media") }; DrawerItem("Blurry Images", Icons.Default.BlurOn) { onItemClick("blurry_images") }; DrawerItem("Scan History", Icons.Default.Timeline) { onItemClick("scan_history") }; DrawerItem("Last Cleanup Receipt", Icons.Default.ReceiptLong) { onItemClick("cleanup_receipt") }; DrawerItem("Storage Overview", Icons.Default.Storage) { onItemClick("storage_overview") }
        Section("LEGAL"); DrawerItem("Privacy Policy", Icons.Default.Policy) { onItemClick("privacy_policy") }; DrawerItem("Terms & Conditions", Icons.Default.Gavel) { onItemClick("terms") }; DrawerItem("About ChatSweep", Icons.Default.Info) { onItemClick("about") }
        Section("SETTINGS"); DrawerItem("Settings", Icons.Default.Settings) { onItemClick("settings") }; DrawerItem("Help & Feedback", Icons.Default.Help) { onItemClick("help_feedback") }
    }
}
@Composable private fun Section(title: String) { Spacer(Modifier.height(10.dp)); Text(title, modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelLarge, color = Color(0xFF2F6FED)) }
@Composable private fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) { NavigationDrawerItem(label = { Text(label) }, icon = { Icon(icon, null) }, selected = false, onClick = onClick) }
