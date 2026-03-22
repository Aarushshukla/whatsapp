package com.example.whatsappcleaner.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.whatsappcleaner.ui.settings.AppThemeMode
import com.example.whatsappcleaner.ui.settings.ReminderFrequencyOption

class UserPrefs private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("whats_clean_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile private var INSTANCE: UserPrefs? = null
        fun get(context: Context): UserPrefs =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPrefs(context.applicationContext).also { INSTANCE = it }
            }
        private const val KEY_SEEN_ONBOARDING = "seen_onboarding"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE = "language_label"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_SMART_ALERTS_ENABLED = "smart_alerts_enabled"
        private const val KEY_AUTO_CLEAN_DAYS = "auto_clean_days"
        private const val KEY_FILE_SIZE_MB = "file_size_mb"
        private const val KEY_SHOW_ONLY_LARGE = "show_only_large"
        private const val KEY_INCLUDE_SCREENSHOTS = "include_screenshots"
        private const val KEY_INCLUDE_MEMES = "include_memes"
        private const val KEY_INCLUDE_DUPLICATES = "include_duplicates"
        private const val KEY_FREE_PREMIUM_ATTEMPTS = "free_premium_attempts"
    }

    fun hasSeenOnboarding(): Boolean = prefs.getBoolean(KEY_SEEN_ONBOARDING, false)
    fun setOnboardingSeen() = prefs.edit().putBoolean(KEY_SEEN_ONBOARDING, true).apply()

    fun getStreak(): Int = prefs.getInt("streak", 0)
    fun recordCleanup(): Int {
        val current = getStreak() + 1
        prefs.edit().putInt("streak", current).apply()
        return current
    }

    fun isRemindersEnabled(): Boolean = prefs.getBoolean(KEY_REMINDERS_ENABLED, true)
    fun setRemindersEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply()

    fun getReminderFrequencyDays(): Int = prefs.getInt("reminder_freq", 1)
    fun setReminderFrequencyDays(days: Int) = prefs.edit().putInt("reminder_freq", days).apply()

    fun getReminderTimeHour(): Int = prefs.getInt("reminder_hour", 9)
    fun getReminderTimeMinute(): Int = prefs.getInt("reminder_min", 0)
    fun setReminderTime(h: Int, m: Int) = prefs.edit().putInt("reminder_hour", h).putInt("reminder_min", m).apply()

    fun getThemeMode(): AppThemeMode = AppThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
    fun setThemeMode(mode: AppThemeMode) = prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()

    fun getLanguageLabel(): String = prefs.getString(KEY_LANGUAGE, "English (coming soon)") ?: "English (coming soon)"
    fun setLanguageLabel(label: String) = prefs.edit().putString(KEY_LANGUAGE, label).apply()

    fun isSmartAlertEnabled(): Boolean = prefs.getBoolean(KEY_SMART_ALERTS_ENABLED, true)
    fun setSmartAlertEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SMART_ALERTS_ENABLED, enabled).apply()

    fun getAutoCleanFrequency(): ReminderFrequencyOption {
        val days = prefs.getInt(KEY_AUTO_CLEAN_DAYS, ReminderFrequencyOption.DAILY.days)
        return ReminderFrequencyOption.entries.firstOrNull { it.days == days } ?: ReminderFrequencyOption.DAILY
    }
    fun setAutoCleanFrequency(option: ReminderFrequencyOption) = prefs.edit().putInt(KEY_AUTO_CLEAN_DAYS, option.days).apply()

    fun getFileSizeFilterMb(): Int = prefs.getInt(KEY_FILE_SIZE_MB, 50)
    fun setFileSizeFilterMb(value: Int) = prefs.edit().putInt(KEY_FILE_SIZE_MB, value).apply()

    fun showOnlyLargeFiles(): Boolean = prefs.getBoolean(KEY_SHOW_ONLY_LARGE, false)
    fun setShowOnlyLargeFiles(enabled: Boolean) = prefs.edit().putBoolean(KEY_SHOW_ONLY_LARGE, enabled).apply()

    fun includeScreenshots(): Boolean = prefs.getBoolean(KEY_INCLUDE_SCREENSHOTS, true)
    fun setIncludeScreenshots(enabled: Boolean) = prefs.edit().putBoolean(KEY_INCLUDE_SCREENSHOTS, enabled).apply()

    fun includeMemes(): Boolean = prefs.getBoolean(KEY_INCLUDE_MEMES, true)
    fun setIncludeMemes(enabled: Boolean) = prefs.edit().putBoolean(KEY_INCLUDE_MEMES, enabled).apply()

    fun includeDuplicates(): Boolean = prefs.getBoolean(KEY_INCLUDE_DUPLICATES, true)
    fun setIncludeDuplicates(enabled: Boolean) = prefs.edit().putBoolean(KEY_INCLUDE_DUPLICATES, enabled).apply()

    fun getFreePremiumAttempts(): Int = prefs.getInt(KEY_FREE_PREMIUM_ATTEMPTS, 0)
    fun incrementFreePremiumAttempts(): Int {
        val next = getFreePremiumAttempts() + 1
        prefs.edit().putInt(KEY_FREE_PREMIUM_ATTEMPTS, next).apply()
        return next
    }

    fun resetFreePremiumAttempts() = prefs.edit().putInt(KEY_FREE_PREMIUM_ATTEMPTS, 0).apply()
}
