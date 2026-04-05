package com.example.whatsappcleaner.ui.settings

enum class AppThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class ReminderFrequencyOption(val label: String, val intervalMinutes: Long) {
    EVERY_15_MIN("Every 15 min", 15),
    EVERY_30_MIN("Every 30 min", 30),
    HOURLY("Hourly", 60),
    EVERY_2_HOURS("Every 2 hours", 120),
    EVERY_6_HOURS("Every 6 hours", 360),
    EVERY_12_HOURS("Every 12 hours", 720),
    DAILY("Daily (24h)", 1440)
}

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val languageLabel: String = "English (coming soon)",
    val dailyReminderEnabled: Boolean = true,
    val smartAlertEnabled: Boolean = true,
    val autoCleanFrequency: ReminderFrequencyOption = ReminderFrequencyOption.DAILY,
    val fileSizeFilterMb: Int = 50,
    val showOnlyLargeFiles: Boolean = false,
    val includeScreenshots: Boolean = true,
    val includeMemes: Boolean = true,
    val includeDuplicates: Boolean = true
)
