package com.example.whatsappcleaner.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.ui.theme.*

@Composable
fun AppDrawer(
    onItemClick: (String) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = SurfaceWhite,
        drawerContentColor = BrandNavy
    ) {
        // Professional Header
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp).background(BrandNavy).padding(24.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Box(Modifier.size(50.dp).background(AccentBlue, RoundedCornerShape(12.dp)))
                Spacer(Modifier.height(12.dp))
                Text("Cleaner Pro", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Text("v1.0.2 Premium", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Menu Items
        DrawerItem("Dashboard", Icons.Default.Dashboard) { onItemClick("home") }
        DrawerItem("Deep Clean", Icons.Default.CleaningServices) { onItemClick("scan") }
        Divider(Modifier.padding(vertical = 16.dp, horizontal = 24.dp))
        DrawerItem("Settings", Icons.Default.Settings) { onItemClick("settings") }
        DrawerItem("Help Center", Icons.Default.Help) { onItemClick("help") }
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label, style = MaterialTheme.typography.titleMedium) },
        icon = { Icon(icon, null, tint = BrandNavy) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
    )
}