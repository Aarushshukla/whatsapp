package com.example.whatsappcleaner.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.billing.SubscriptionPlan
import com.example.whatsappcleaner.data.billing.SubscriptionState
import com.example.whatsappcleaner.BuildConfig

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
    onTerms: () -> Unit,
    onFaq: () -> Unit,
    onContactSupport: () -> Unit,
    onReportIssue: () -> Unit,
    onRateApp: () -> Unit,
    onShareApp: () -> Unit,
    onInviteFriends: () -> Unit,
    onDebugCrashTest: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", color = colors.onBackground, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { BrandHeader(versionLabel = versionLabel, tagline = tagline) }
            item {
                SettingsSection(title = "Appearance") {
                    ChoiceRow(
                        "Theme",
                        settings.themeMode.label,
                        Icons.Default.DarkMode,
                        AppThemeMode.entries.map { themeMode -> themeMode.label to { onThemeSelected(themeMode) } }
                    )
                    ChoiceRow("Language", settings.languageLabel, Icons.Default.Language, listOf("English (coming soon)" to onFaq))
                }
            }
            item {
                SettingsSection(title = "Notifications") {
                    SettingsRow(
                        title = "Daily reminder",
                        subtitle = "Get a gentle nudge to review clutter.",
                        icon = Icons.Default.Notifications,
                        trailing = {
                            Switch(
                                checked = settings.dailyReminderEnabled,
                                onCheckedChange = onDailyReminderToggle,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = colors.primary,
                                    checkedThumbColor = colors.onPrimary
                                )
                            )
                        },
                        onClick = { onDailyReminderToggle(!settings.dailyReminderEnabled) }
                    )
                    SettingsRow(
                        title = "Smart alert",
                        subtitle = "Notify when junk or duplicates spike.",
                        icon = Icons.Default.CheckCircle,
                        trailing = {
                            Switch(
                                checked = settings.smartAlertEnabled,
                                onCheckedChange = onSmartAlertToggle,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = colors.primary,
                                    checkedThumbColor = colors.onPrimary
                                )
                            )
                        },
                        onClick = { onSmartAlertToggle(!settings.smartAlertEnabled) }
                    )
                    ChoiceRow(
                        "Reminder frequency",
                        settings.autoCleanFrequency.label,
                        Icons.Default.Tune,
                        ReminderFrequencyOption.entries.map { frequencyOption ->
                            frequencyOption.label to { onAutoCleanFrequencySelected(frequencyOption) }
                        }
                    )
                }
            }
            item {
                SettingsSection(title = "Cleaning preferences") {
                    ChoiceRow(
                        "File size filter",
                        ">= ${settings.fileSizeFilterMb} MB",
                        Icons.Default.Tune,
                        listOf(25, 50, 100, 250).map { value -> "$value MB" to { onFileSizeFilterSelected(value) } }
                    )
                    ToggleRow("Show only large files", "Focus on heavier items first.", Icons.Default.Tune, settings.showOnlyLargeFiles, onShowOnlyLargeToggle)
                    ToggleRow("Include screenshots", "Surface camera roll screenshots.", Icons.Default.Tune, settings.includeScreenshots, onIncludeScreenshotsToggle)
                    ToggleRow("Include memes", "Premium meme detection candidates.", Icons.Default.Tune, settings.includeMemes, onIncludeMemesToggle)
                    ToggleRow("Include duplicates", "Review exact duplicate candidates.", Icons.Default.Tune, settings.includeDuplicates, onIncludeDuplicatesToggle)
                }
            }
            item {
                SettingsSection(title = "Subscription") {
                    // TODO: RE-ENABLE SUBSCRIPTION LATER
                    /*
                    ActionRow(
                        "Upgrade to Pro",
                        if (subscriptionState.isProUser) "Current plan: ${subscriptionState.currentPlan.displayName}" else "Unlock Smart Clean, duplicate review, meme detection, and bulk delete",
                        Icons.Default.Upgrade,
                        onUpgradeToPro
                    )
                    ActionRow("Restore purchase", "Refresh your previous Play purchases", Icons.Default.Restore, onRestorePurchase)
                    ActionRow(
                        "Manage subscription",
                        if (subscriptionState.currentPlan == SubscriptionPlan.LIFETIME) "Lifetime unlocks do not renew" else "Open Play subscription center",
                        Icons.Default.Subscriptions,
                        onManageSubscription
                    )
                    */
                    ActionRow("All features unlocked", "Subscription actions are temporarily disabled", Icons.Default.CheckCircle, {})
                }
            }
            item {
                SettingsSection(title = "Privacy & support") {
                    ActionRow("Privacy Policy", "Review our privacy commitments", Icons.Default.Policy, onPrivacyPolicy)
                    ActionRow("Terms & Conditions", "Read usage terms and responsibilities", Icons.Default.Gavel, onTerms)
                    ActionRow("Local scanning", "Scanning happens on-device where supported", Icons.Default.PrivacyTip, onPrivacyPolicy)
                    ActionRow("Permission explanation", "Media access is used only to analyze files", Icons.Default.Shield, onPrivacyPolicy)
                    ActionRow("Data usage note", "Analytics and billing are optional services", Icons.Default.Gavel, onPrivacyPolicy)
                    HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))
                    ActionRow("FAQ", "Open common answers and tips", Icons.Default.Info, onFaq)
                    ActionRow("Contact support", "Email the support team", Icons.Default.Email, onContactSupport)
                    ActionRow("Report issue", "Send a bug report", Icons.Default.BugReport, onReportIssue)
                }
            }
            item {
                SettingsSection(title = "Growth") {
                    ActionRow("Rate app", "Leave a Play Store review", Icons.Default.Star, onRateApp)
                    ActionRow("Share app", "Share app with friends", Icons.Default.Share, onShareApp)
                    ActionRow("Invite friends", "Send a quick invite message", Icons.Default.Upgrade, onInviteFriends)
                }
            }
            if (BuildConfig.DEBUG) {
                item {
                    SettingsSection(title = "Debug") {
                        Button(onClick = onDebugCrashTest) {
                            Text("Test Crash")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandHeader(versionLabel: String, tagline: String) {
    val colors = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primaryContainer.copy(alpha = 0.6f))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Cleanly AI", color = colors.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(tagline, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Text(versionLabel, color = colors.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = colors.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    trailing: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.985f else 1f, animationSpec = tween(140), label = "settings_row_scale")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(colors.primaryContainer, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colors.primary)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = colors.onSurface, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        trailing()
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    SettingsRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = colors.primary, checkedThumbColor = colors.onPrimary)
            )
        },
        onClick = { onCheckedChange(!checked) }
    )
}

@Composable
private fun ActionRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    SettingsRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant) },
        onClick = onClick
    )
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
