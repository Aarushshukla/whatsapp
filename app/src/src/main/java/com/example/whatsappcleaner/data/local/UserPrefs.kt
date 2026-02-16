package com.example.whatsappcleaner.data

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

class UserPrefs private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("whats_clean_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile private var INSTANCE: UserPrefs? = null

        fun get(context: Context): UserPrefs =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPrefs(context.applicationContext).also { INSTANCE = it }
            }

        private const val KEY_LAST_CLEANUP = "last_cleanup_time"
        private const val KEY_STREAK = "cleanup_streak"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_REMINDER_FREQUENCY_DAYS = "reminder_freq_days"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
    }

    fun getLastCleanupTime(): Long =
        prefs.getLong(KEY_LAST_CLEANUP, 0L)

    fun getStreak(): Int =
        prefs.getInt(KEY_STREAK, 0)

    fun isRemindersEnabled(): Boolean =
        prefs.getBoolean(KEY_REMINDERS_ENABLED, true) // default ON

    fun setRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply()
    }

    fun getReminderFrequencyDays(): Int =
        prefs.getInt(KEY_REMINDER_FREQUENCY_DAYS, 1) // 1 = daily

    fun setReminderFrequencyDays(days: Int) {
        prefs.edit().putInt(KEY_REMINDER_FREQUENCY_DAYS, days).apply()
    }

    fun getReminderTimeHour(): Int =
        prefs.getInt(KEY_REMINDER_HOUR, 22) // default 22:00

    fun getReminderTimeMinute(): Int =
        prefs.getInt(KEY_REMINDER_MINUTE, 0)

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
    }

    /**
     * Call when user finishes a cleanup (e.g., taps "I’m done – Refresh").
     * Returns updated streak value.
     */
    fun recordCleanup(now: Long = System.currentTimeMillis()): Int {
        val last = getLastCleanupTime()
        val prevStreak = getStreak()

        val daysDiff = if (last == 0L) {
            0L
        } else {
            TimeUnit.MILLISECONDS.toDays(now) - TimeUnit.MILLISECONDS.toDays(last)
        }

        val newStreak = when (daysDiff) {
            0L -> prevStreak.coerceAtLeast(1)
            1L -> prevStreak + 1
            else -> 1
        }

        prefs.edit()
            .putLong(KEY_LAST_CLEANUP, now)
            .putInt(KEY_STREAK, newStreak)
            .apply()

        return newStreak
    }
}
