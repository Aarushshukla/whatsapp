package com.example.whatsappcleaner.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawer(onItemClick: (String) -> Unit) {
    ModalDrawerSheet {
        Column(Modifier.fillMaxWidth().background(androidx.compose.ui.graphics.Color(0xFFEFF4FF)).padding(20.dp)) {
            Text("ChatSweep", style = MaterialTheme.typography.headlineSmall)
            Text("Private offline media cleaner", style = MaterialTheme.typography.bodySmall)
        }
        Section("Main"); DrawerItem("Dashboard", Icons.Default.Dashboard) { onItemClick("home") }; DrawerItem("Smart Review", Icons.Default.CleaningServices) { onItemClick("smart_review") }; DrawerItem("Scan Again", Icons.Default.Refresh) { onItemClick("scan_again") }; DrawerItem("Categories", Icons.Default.Category) { onItemClick("categories") }
        Section("Storage"); DrawerItem("Photos", Icons.Default.Photo) { onItemClick("storage_overview") }; DrawerItem("Videos", Icons.Default.VideoFile) { onItemClick("storage_overview") }; DrawerItem("Statuses", Icons.Default.Storage) { onItemClick("storage_overview") }; DrawerItem("Stickers", Icons.Default.Storage) { onItemClick("storage_overview") }
        Section("Tools"); DrawerItem("Duplicate Finder", Icons.Default.CleaningServices) { onItemClick("duplicate_finder") }; DrawerItem("Large Files", Icons.Default.Storage) { onItemClick("large_files") }; DrawerItem("Old Media", Icons.Default.History) { onItemClick("old_media") }; DrawerItem("Blurry Images", Icons.Default.Photo) { onItemClick("blurry_images") }; DrawerItem("Scan History", Icons.Default.History) { onItemClick("scan_history") }; DrawerItem("Last Cleanup Receipt", Icons.Default.ReceiptLong) { onItemClick("cleanup_receipt") }; DrawerItem("Storage Overview", Icons.Default.Storage) { onItemClick("storage_overview") }
        Section("Legal"); DrawerItem("Privacy Policy", Icons.Default.Policy) { onItemClick("privacy_policy") }; DrawerItem("Terms & Conditions", Icons.Default.Gavel) { onItemClick("terms") }; DrawerItem("About ChatSweep", Icons.Default.Info) { onItemClick("about") }
        Section("Settings"); DrawerItem("Settings", Icons.Default.Settings) { onItemClick("settings") }; DrawerItem("Help & Feedback", Icons.Default.Feedback) { onItemClick("help_feedback") }
    }
}
@Composable private fun Section(title: String) { Spacer(Modifier.height(10.dp)); Text(title, modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelLarge) }
@Composable private fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) { NavigationDrawerItem(label = { Text(label) }, icon = { androidx.compose.material3.Icon(icon, null) }, selected = false, onClick = onClick) }
