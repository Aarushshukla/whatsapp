package com.example.whatsappcleaner.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.billing.SubscriptionState
import com.example.whatsappcleaner.ui.theme.AccentBlue
import com.example.whatsappcleaner.ui.theme.AccentGreen
import com.example.whatsappcleaner.ui.theme.PrimaryBackground
import com.example.whatsappcleaner.ui.theme.SurfaceWhite
import com.example.whatsappcleaner.ui.theme.TextMain
import com.example.whatsappcleaner.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsUiState,
    subscriptionState: SubscriptionState,
    versionLabel: String,
    tagline: String,
    onBack: () -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDailyReminderToggle: (Boolean) -> Unit,
    onSmartAlertToggle: (Boolean) -> Unit,
    onAutoCleanFrequencySelected: (ReminderFrequencyOption) -> Unit,
    onFileSizeFilterSelected: (Int) -> Unit,
    onShowOnlyLargeToggle: (Boolean) -> Unit,
    onIncludeScreenshotsToggle: (Boolean) -> Unit,
    onIncludeMemesToggle: (Boolean) -> Unit,
    onIncludeDuplicatesToggle: (Boolean) -> Unit,
    onUpgradeToPro: () -> Unit,
    onRestorePurchase: () -> Unit,
    onManageSubscription: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onFaq: () -> Unit,
    onContactSupport: () -> Unit,
    onReportIssue: () -> Unit,
    onRateApp: () -> Unit,
    onShareApp: () -> Unit,
    onInviteFriends: () -> Unit
) {
    Scaffold(
        containerColor = PrimaryBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextMain)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                BrandHeader(versionLabel = versionLabel, tagline = tagline)
            }
            item {
                SettingsSection(title = "General") {
                    ChoiceRow("Theme", settings.themeMode.label, Icons.Default.DarkMode, AppThemeMode.entries.map { themeMode -> themeMode.label to { onThemeSelected(themeMode) } })
                    ChoiceRow("Language", settings.languageLabel, Icons.Default.Language, listOf("English (coming soon)" to onFaq))
                }
            }
            item {
                SettingsSection(title = "Notifications") {
                    ToggleRow("Daily reminder", "Get a gentle nudge to review clutter.", Icons.Default.Notifications, settings.dailyReminderEnabled, onDailyReminderToggle)
                    ToggleRow("Smart alert", "Notify when junk or duplicates spike.", Icons.Default.CheckCircle, settings.smartAlertEnabled, onSmartAlertToggle)
                    if (subscriptionState.isProUser) {
                        ChoiceRow("Auto-clean reminder", settings.autoCleanFrequency.label, Icons.Default.Tune, ReminderFrequencyOption.entries.map { frequencyOption -> frequencyOption.label to { onAutoCleanFrequencySelected(frequencyOption) } })
                    } else {
                        ActionRow("Auto-clean reminder", "Pro only: unlock advanced cleanup automation", Icons.Default.Tune, onUpgradeToPro)
                    }
                }
            }
            item {
                SettingsSection(title = "Cleaning Preferences") {
                    ChoiceRow("File size filter", ">= ${settings.fileSizeFilterMb} MB", Icons.Default.Tune, listOf(25, 50, 100, 250).map { value -> "$value MB" to { onFileSizeFilterSelected(value) } })
                    ToggleRow("Show only large files", "Focus on heavier items first.", Icons.Default.Tune, settings.showOnlyLargeFiles, onShowOnlyLargeToggle)
                    ToggleRow("Include screenshots", "Surface camera roll screenshots.", Icons.Default.Tune, settings.includeScreenshots, onIncludeScreenshotsToggle)
                    ToggleRow("Include memes", "Premium meme detection candidates.", Icons.Default.Tune, settings.includeMemes, onIncludeMemesToggle)
                    ToggleRow("Include duplicates", "Review exact duplicate candidates.", Icons.Default.Tune, settings.includeDuplicates, onIncludeDuplicatesToggle)
                }
            }
            item {
                SettingsSection(title = "Subscription") {
                    ActionRow("Upgrade to Pro", "Unlock premium cleanup flows", Icons.Default.Upgrade, onUpgradeToPro)
                    ActionRow("Restore purchase", "Refresh your previous purchases", Icons.Default.Restore, onRestorePurchase)
                    ActionRow("Manage subscription", "Open the Play subscription center", Icons.Default.Subscriptions, onManageSubscription)
                    ActionRow("Current plan", subscriptionState.currentPlan.displayName, Icons.Default.Star, onClick = onUpgradeToPro)
                }
            }
            item {
                SettingsSection(title = "Privacy & Security") {
                    ActionRow("Privacy Policy", "Review our privacy commitments", Icons.Default.Policy, onPrivacyPolicy)
                    ActionRow("Local scanning", "Scanning happens locally on device where supported", Icons.Default.PrivacyTip, onPrivacyPolicy)
                    ActionRow("Permission explanation", "Media access is used only to analyze your files", Icons.Default.Shield, onPrivacyPolicy)
                    ActionRow("Data usage note", "Analytics and billing are optional platform services", Icons.Default.Gavel, onPrivacyPolicy)
                }
            }
            item {
                SettingsSection(title = "Help & Support") {
                    ActionRow("FAQ", "Open common answers and tips", Icons.Default.Info, onFaq)
                    ActionRow("Contact support", "Email the Cleanly AI support team", Icons.Default.Email, onContactSupport)
                    ActionRow("Report issue", "Send a bug report", Icons.Default.BugReport, onReportIssue)
                }
            }
            item {
                SettingsSection(title = "Growth") {
                    ActionRow("Rate app", "Leave a Play Store review", Icons.Default.Star, onRateApp)
                    ActionRow("Share app", "Share Cleanly AI with friends", Icons.Default.Share, onShareApp)
                    ActionRow("Invite friends", "Send a quick invite message", Icons.Default.Upgrade, onInviteFriends)
                }
            }
            item {
                SettingsSection(title = "About") {
                    ActionRow("App version", versionLabel, Icons.Default.Info, onClick = onShareApp)
                    ActionRow("App name", "Cleanly AI", Icons.Default.Star, onClick = onRateApp)
                    ActionRow("Tagline", tagline, Icons.Default.CheckCircle, onClick = onShareApp)
                }
            }
        }
    }
}

@Composable
private fun BrandHeader(versionLabel: String, tagline: String) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Cleanly AI", color = TextMain, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(tagline, color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
            Text(versionLabel, color = AccentBlue, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable Column.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = TextMain, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.background(AccentBlue.copy(alpha = 0.14f), RoundedCornerShape(14.dp)).padding(10.dp)) {
            Icon(icon, contentDescription = null, tint = AccentBlue)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextMain, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ActionRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.background(AccentBlue.copy(alpha = 0.14f), RoundedCornerShape(14.dp)).padding(10.dp)) {
            Icon(icon, contentDescription = null, tint = AccentBlue)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextMain, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}

@Composable
private fun ChoiceRow(title: String, subtitle: String, icon: ImageVector, options: List<Pair<String, () -> Unit>>) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ActionRow(title = title, subtitle = subtitle, icon = icon, onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (label, action) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        action()
                    }
                )
            }
        }
    }
}
