package com.example.whatsappcleaner.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.ui.theme.AccentBlue
import com.example.whatsappcleaner.ui.theme.BrandNavy
import com.example.whatsappcleaner.ui.theme.SurfaceWhite
import com.example.whatsappcleaner.ui.theme.TextSecondary

@Composable
fun AppDrawer(onItemClick: (String) -> Unit) {
    ModalDrawerSheet(drawerContainerColor = SurfaceWhite, drawerContentColor = BrandNavy) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(AccentBlue.copy(alpha = 0.18f)).padding(24.dp), contentAlignment = Alignment.BottomStart) {
            Column { Box(Modifier.size(50.dp).background(AccentBlue, RoundedCornerShape(12.dp))); Spacer(Modifier.height(12.dp)); Text("ChatSweep", style = MaterialTheme.typography.headlineSmall, color = BrandNavy); Text("Clean chat media storage", style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
        }
        Spacer(Modifier.height(12.dp))
        val primary = listOf(
            Triple("Dashboard","home",Icons.Default.Dashboard), Triple("Smart Review","smart_review",Icons.Default.CleaningServices), Triple("Scan Again","scan_again",Icons.Default.Refresh),
            Triple("Categories","categories",Icons.Default.Category), Triple("Photos","photos",Icons.Default.Image), Triple("Videos","videos",Icons.Default.VideoLibrary), Triple("Audio","audio",Icons.Default.GraphicEq), Triple("Documents","documents",Icons.Default.Description),
            Triple("Statuses","statuses",Icons.Default.ViewAgenda), Triple("Stickers","stickers",Icons.Default.EmojiEmotions), Triple("Duplicate Finder","duplicate_finder",Icons.Default.ContentCopy), Triple("Large Files","large_files",Icons.Default.FolderOpen),
            Triple("Old Media","old_media",Icons.Default.Schedule), Triple("Blurry Images","blurry_images",Icons.Default.ImageSearch), Triple("Scan History","scan_history",Icons.Default.History), Triple("Last Cleanup Receipt","last_cleanup_receipt",Icons.Default.ReceiptLong),
            Triple("Storage Overview","storage_overview",Icons.Default.Storage)
        )
        primary.forEach { (label, route, icon) -> DrawerItem(label, icon) { onItemClick(route) } }
        HorizontalDivider(Modifier.padding(vertical = 14.dp, horizontal = 24.dp))
        listOf(
            Triple("Privacy Policy","privacy_policy",Icons.Default.Policy), Triple("Terms & Conditions","terms",Icons.Default.Gavel), Triple("About ChatSweep","about",Icons.Default.Info),
            Triple("Settings","settings",Icons.Default.Settings), Triple("Help & Feedback","help_feedback",Icons.Default.Help)
        ).forEach { (label, route, icon) -> DrawerItem(label, icon) { onItemClick(route) } }
    }
}

@Composable
private fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(label = { Text(label, style = MaterialTheme.typography.titleMedium) }, icon = { androidx.compose.material3.Icon(icon, null, tint = BrandNavy) }, selected = false, onClick = onClick, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent))
}
