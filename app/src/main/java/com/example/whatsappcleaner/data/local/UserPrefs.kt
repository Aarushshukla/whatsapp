package com.example.whatsappcleaner.data.local

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

class UserPrefs private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("whats_clean_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile private var INSTANCE: UserPrefs? = null
        fun get(context: Context): UserPrefs =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPrefs(context.applicationContext).also { INSTANCE = it }
            }
        private const val KEY_SEEN_ONBOARDING = "seen_onboarding"
    }

    fun hasSeenOnboarding(): Boolean = prefs.getBoolean(KEY_SEEN_ONBOARDING, false)
    fun setOnboardingSeen() = prefs.edit().putBoolean(KEY_SEEN_ONBOARDING, true).apply()

    fun getStreak(): Int = prefs.getInt("streak", 0)
    fun recordCleanup(): Int {
        val current = getStreak() + 1
        prefs.edit().putInt("streak", current).apply()
        return current
    }

    fun isRemindersEnabled(): Boolean = prefs.getBoolean("reminders_enabled", true)
    fun setRemindersEnabled(enabled: Boolean) = prefs.edit().putBoolean("reminders_enabled", enabled).apply()

    fun getReminderFrequencyDays(): Int = prefs.getInt("reminder_freq", 1)
    fun setReminderFrequencyDays(days: Int) = prefs.edit().putInt("reminder_freq", days).apply()

    fun getReminderTimeHour(): Int = prefs.getInt("reminder_hour", 9)
    fun getReminderTimeMinute(): Int = prefs.getInt("reminder_min", 0)
    fun setReminderTime(h: Int, m: Int) = prefs.edit().putInt("reminder_hour", h).putInt("reminder_min", m).apply()
}