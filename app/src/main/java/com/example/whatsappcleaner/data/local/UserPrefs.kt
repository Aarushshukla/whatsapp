package com.example.whatsappcleaner.data.local

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
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
        private const val KEY_AUTO_CLEAN_INTERVAL_MIN = "auto_clean_interval_min"
        private const val KEY_AUTO_CLEAN_DAYS = "auto_clean_days" // legacy
        private const val KEY_FILE_SIZE_MB = "file_size_mb"
        private const val KEY_SHOW_ONLY_LARGE = "show_only_large"
        private const val KEY_INCLUDE_SCREENSHOTS = "include_screenshots"
        private const val KEY_INCLUDE_MEMES = "include_memes"
        private const val KEY_INCLUDE_DUPLICATES = "include_duplicates"
        private const val KEY_FREE_PREMIUM_ATTEMPTS = "free_premium_attempts"
        private const val KEY_SCAN_HISTORY = "scan_history_v1"
        private const val KEY_LAST_SCAN_COMPLETED_AT = "last_scan_completed_at"
        private const val KEY_CACHED_SCAN_SUMMARY = "cached_scan_summary_v1"

    private const val KEY_FIRST_SCAN_COMPLETED = "first_scan_completed"
    private const val KEY_PERMISSION_SUCCESS_SEEN = "permission_success_seen"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    data class ScanHistoryRecord(
        val scanDateMillis: Long,
        val totalMediaSizeBytes: Long,
        val imageSizeBytes: Long = 0L,
        val videoSizeBytes: Long = 0L,
        val duplicateSizeBytes: Long = 0L
    )

    fun hasSeenOnboarding(): Boolean = prefs.getBoolean(KEY_SEEN_ONBOARDING, false)
    fun setOnboardingSeen() = prefs.edit().putBoolean(KEY_SEEN_ONBOARDING, true).apply()

    fun getStreak(): Int = prefs.getInt("streak", 0)

    fun recordCleanupDay(): Int {
        val todayEpochDay = System.currentTimeMillis() / 86_400_000L
        val previousEpochDay = prefs.getLong("last_cleanup_epoch_day", -1L)
        val updatedStreak = when {
            previousEpochDay == todayEpochDay -> getStreak()
            previousEpochDay == todayEpochDay - 1L -> getStreak() + 1
            else -> 1
        }
        prefs.edit()
            .putInt("streak", updatedStreak)
            .putLong("last_cleanup_epoch_day", todayEpochDay)
            .apply()
        return updatedStreak
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
        val legacyDays = prefs.getInt(KEY_AUTO_CLEAN_DAYS, -1)
        val intervalMinutes = prefs.getLong(
            KEY_AUTO_CLEAN_INTERVAL_MIN,
            if (legacyDays > 0) legacyDays * 24L * 60L else ReminderFrequencyOption.DAILY.intervalMinutes
        )
        return ReminderFrequencyOption.entries.firstOrNull { it.intervalMinutes == intervalMinutes }
            ?: ReminderFrequencyOption.DAILY
    }

    fun setAutoCleanFrequency(option: ReminderFrequencyOption) = prefs.edit()
        .putLong(KEY_AUTO_CLEAN_INTERVAL_MIN, option.intervalMinutes)
        .remove(KEY_AUTO_CLEAN_DAYS)
        .apply()

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

    fun isFirstScanCompleted(): Boolean = prefs.getBoolean(KEY_FIRST_SCAN_COMPLETED, false)
    fun setFirstScanCompleted(completed: Boolean) = prefs.edit().putBoolean(KEY_FIRST_SCAN_COMPLETED, completed).apply()
    fun hasSeenPermissionSuccess(): Boolean = prefs.getBoolean(KEY_PERMISSION_SUCCESS_SEEN, false)
    fun setSeenPermissionSuccess(seen: Boolean) = prefs.edit().putBoolean(KEY_PERMISSION_SUCCESS_SEEN, seen).apply()
    fun hasCompletedOnboarding(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    fun setCompletedOnboarding(completed: Boolean) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()

    fun appendScanHistory(record: ScanHistoryRecord) {
        val existing = getScanHistory().toMutableList().apply { add(record) }
        val trimmed = existing.sortedBy { it.scanDateMillis }.takeLast(12)
        val array = JSONArray()
        trimmed.forEach { item ->
            array.put(JSONObject().apply {
                put("scanDateMillis", item.scanDateMillis)
                put("totalMediaSizeBytes", item.totalMediaSizeBytes)
                put("imageSizeBytes", item.imageSizeBytes)
                put("videoSizeBytes", item.videoSizeBytes)
                put("duplicateSizeBytes", item.duplicateSizeBytes)
            })
        }
        prefs.edit().putString(KEY_SCAN_HISTORY, array.toString()).apply()
    }

    fun getScanHistory(): List<ScanHistoryRecord> {
        val raw = prefs.getString(KEY_SCAN_HISTORY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(
                        ScanHistoryRecord(
                            scanDateMillis = item.optLong("scanDateMillis", 0L),
                            totalMediaSizeBytes = item.optLong("totalMediaSizeBytes", 0L),
                            imageSizeBytes = item.optLong("imageSizeBytes", 0L),
                            videoSizeBytes = item.optLong("videoSizeBytes", 0L),
                            duplicateSizeBytes = item.optLong("duplicateSizeBytes", 0L)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    data class CachedScanSummary(
        val totalSizeBytes: Long,
        val potentialCleanupBytes: Long,
        val fileCount: Int,
        val lastScanCompletedAt: Long,
        val categories: List<CachedCategorySummary>
    )

    data class CachedCategorySummary(
        val title: String,
        val count: Int,
        val sizeBytes: Long,
        val percent: Float = 0f
    )

    fun setLastScanCompletedAt(timestamp: Long) = prefs.edit().putLong(KEY_LAST_SCAN_COMPLETED_AT, timestamp).apply()
    fun getLastScanCompletedAt(): Long = prefs.getLong(KEY_LAST_SCAN_COMPLETED_AT, 0L)

    fun saveCachedScanSummary(summary: CachedScanSummary) {
        val root = JSONObject().apply {
            put("totalSizeBytes", summary.totalSizeBytes)
            put("potentialCleanupBytes", summary.potentialCleanupBytes)
            put("fileCount", summary.fileCount)
            put("lastScanCompletedAt", summary.lastScanCompletedAt)
            put("categories", JSONArray().apply {
                summary.categories.forEach { category ->
                    put(JSONObject().apply {
                        put("title", category.title)
                        put("count", category.count)
                        put("sizeBytes", category.sizeBytes)
                        put("percent", category.percent)
                    })
                }
            })
        }
        prefs.edit().putString(KEY_CACHED_SCAN_SUMMARY, root.toString()).apply()
    }

    fun getCachedScanSummary(): CachedScanSummary? {
        val raw = prefs.getString(KEY_CACHED_SCAN_SUMMARY, null) ?: return null
        return runCatching {
            val root = JSONObject(raw)
            val categoriesJson = root.optJSONArray("categories") ?: JSONArray()
            val categories = buildList {
                for (i in 0 until categoriesJson.length()) {
                    val item = categoriesJson.optJSONObject(i) ?: continue
                    add(
                        CachedCategorySummary(
                            title = item.optString("title"),
                            count = item.optInt("count"),
                            sizeBytes = item.optLong("sizeBytes"),
                            percent = item.optDouble("percent", 0.0).toFloat()
                        )
                    )
                }
            }
            CachedScanSummary(
                totalSizeBytes = root.optLong("totalSizeBytes"),
                potentialCleanupBytes = root.optLong("potentialCleanupBytes"),
                fileCount = root.optInt("fileCount"),
                lastScanCompletedAt = root.optLong("lastScanCompletedAt"),
                categories = categories
            )
        }.getOrNull()
    }
}
