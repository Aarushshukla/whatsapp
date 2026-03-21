package com.example.whatsappcleaner.ui.settings

enum class AppThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class ReminderFrequencyOption(val label: String, val days: Int) {
    DAILY("Daily", 1),
    EVERY_3_DAYS("Every 3 days", 3),
    WEEKLY("Weekly", 7)
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
