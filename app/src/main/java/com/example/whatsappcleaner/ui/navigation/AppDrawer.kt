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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
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
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp).background(AccentBlue.copy(alpha = 0.18f)).padding(24.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Box(Modifier.size(50.dp).background(AccentBlue, RoundedCornerShape(12.dp)))
                Spacer(Modifier.height(12.dp))
                Text("ChatSweep", style = MaterialTheme.typography.headlineSmall, color = BrandNavy)
                Text("Clean chat media storage", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        Spacer(Modifier.height(12.dp))
        DrawerItem("Dashboard", Icons.Default.Dashboard) { onItemClick("home") }
        DrawerItem("Scan Again", Icons.Default.Refresh) { onItemClick("scan_again") }
        DrawerItem("Smart Review", Icons.Default.CleaningServices) { onItemClick("smart_review") }
        DrawerItem("Categories", Icons.Default.Category) { onItemClick("categories") }
        DrawerItem("Storage Overview", Icons.Default.Storage) { onItemClick("storage_overview") }
        HorizontalDivider(Modifier.padding(vertical = 14.dp, horizontal = 24.dp))
        DrawerItem("Privacy Policy", Icons.Default.Policy) { onItemClick("privacy_policy") }
        DrawerItem("Terms", Icons.Default.Description) { onItemClick("terms") }
        DrawerItem("About", Icons.Default.Info) { onItemClick("about") }
        DrawerItem("Settings", Icons.Default.Settings) { onItemClick("settings") }
    }
}

@Composable
private fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label, style = MaterialTheme.typography.titleMedium) },
        icon = { androidx.compose.material3.Icon(icon, null, tint = BrandNavy) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
    )
}
