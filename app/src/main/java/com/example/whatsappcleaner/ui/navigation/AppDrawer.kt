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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
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
    ModalDrawerSheet(
        drawerContainerColor = SurfaceWhite,
        drawerContentColor = BrandNavy
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(AccentBlue.copy(alpha = 0.18f))
                .padding(24.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Box(Modifier.size(50.dp).background(AccentBlue, RoundedCornerShape(12.dp)))
                Spacer(Modifier.height(12.dp))
                Text("Cleaner Pro", style = MaterialTheme.typography.headlineSmall, color = BrandNavy)
                Text("Dark dashboard edition", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        Spacer(Modifier.height(16.dp))
        DrawerItem("Dashboard", Icons.Default.Dashboard) { onItemClick("home") }
        DrawerItem("Smart Clean", Icons.Default.CleaningServices) { onItemClick("smart_clean") }
        DrawerItem("Analytics", Icons.Default.Analytics) { onItemClick("analytics_screen") }
        Divider(Modifier.padding(vertical = 16.dp, horizontal = 24.dp))
        DrawerItem("Settings", Icons.Default.Settings) { onItemClick("home") }
        DrawerItem("Help Center", Icons.Default.Help) { onItemClick("phone_reality") }
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
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
