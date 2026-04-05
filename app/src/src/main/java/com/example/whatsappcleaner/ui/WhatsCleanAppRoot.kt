package com.example.whatsappcleaner.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.whatsappcleaner.data.MediaLoader
import com.example.whatsappcleaner.data.SimpleMediaItem
import com.example.whatsappcleaner.data.UserPrefs
import com.example.whatsappcleaner.data.formatSize
import com.example.whatsappcleaner.reminder.ReminderScheduler
import com.example.whatsappcleaner.ui.home.MediaFilter
import com.example.whatsappcleaner.ui.home.SimpleHomeScreen
import com.example.whatsappcleaner.ui.home.SuggestionType

data class FrequencyOption(val label: String, val days: Int)

@Composable
fun WhatsCleanAppRoot() {
    val context = LocalContext.current
    var items by remember { mutableStateOf<List<SimpleMediaItem>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(false) }
    var permissionAsked by remember { mutableStateOf(false) }

    var todayCount by remember { mutableStateOf(0) }
    var todayBytes by remember { mutableStateOf(0L) }
    var weekCount by remember { mutableStateOf(0) }
    var weekBytes by remember { mutableStateOf(0L) }

    var largeTodayCount by remember { mutableStateOf(0) }
    var largeTodayBytes by remember { mutableStateOf(0L) }
    var screenshotTodayCount by remember { mutableStateOf(0) }
    var screenshotTodayBytes by remember { mutableStateOf(0L) }

    var filter by remember { mutableStateOf(MediaFilter.ALL) }
    var activeSuggestion by remember { mutableStateOf(SuggestionType.NONE) }

    var streak by remember { mutableStateOf(0) }
    var remindersEnabled by remember { mutableStateOf(true) }

    val frequencyOptions = remember {
        listOf(
            FrequencyOption("Daily", 1),
            FrequencyOption("Weekly", 7)
        )
    }

    var selectedFrequency by remember { mutableStateOf(frequencyOptions.first()) }
    var reminderHour by remember { mutableStateOf(22) }
    var reminderMinute by remember { mutableStateOf(0) }

    fun reload() {
        if (!hasPermission) return
        val loader = MediaLoader(context)

        val todayItems = loader.loadTodayWhatsAppMedia()
        items = todayItems
        todayCount = todayItems.size
        todayBytes = todayItems.sumOf { it.sizeKb.toLong() * 1024L }

        val largeThresholdBytes = 50L * 1024L * 1024L
        val large = todayItems.filter { it.sizeKb.toLong() * 1024L >= largeThresholdBytes }
        largeTodayCount = large.size
        largeTodayBytes = large.sumOf { it.sizeKb.toLong() * 1024L }

        val screenshots = todayItems.filter { it.name.startsWith("Screenshot", ignoreCase = true) }
        screenshotTodayCount = screenshots.size
        screenshotTodayBytes = screenshots.sumOf { it.sizeKb.toLong() * 1024L }

        val now = System.currentTimeMillis()
        val weekStart = now - 7L * 86_400_000L
        val weekItems = loader.loadWhatsAppMediaInRange(weekStart, now)
        weekCount = weekItems.size
        weekBytes = weekItems.sumOf { it.sizeKb.toLong() * 1024L }

        activeSuggestion = SuggestionType.NONE
    }

    fun applyReminderScheduling() {
        if (!hasPermission || !remindersEnabled) return
        ReminderScheduler.schedulePeriodicReminder(
            context = context,
            frequencyDays = selectedFrequency.days,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute
        )
    }

    fun onRemindersToggle(enabled: Boolean) {
        remindersEnabled = enabled
        val prefs = UserPrefs.get(context)
        prefs.setRemindersEnabled(enabled)

        if (enabled) {
            applyReminderScheduling()
        } else {
            ReminderScheduler.cancelPeriodicReminder(context)
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            hasPermission = results.values.any { it }
            permissionAsked = true
        }

    LaunchedEffect(Unit) {
        val prefs = UserPrefs.get(context)
        remindersEnabled = prefs.isRemindersEnabled()

        val savedDays = prefs.getReminderFrequencyDays()
        selectedFrequency =
            frequencyOptions.firstOrNull { it.days == savedDays } ?: frequencyOptions.first()

        reminderHour = prefs.getReminderTimeHour()
        reminderMinute = prefs.getReminderTimeMinute()

        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(perms)
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            reload()
            streak = UserPrefs.get(context).getStreak()
        }
    }

    LaunchedEffect(hasPermission, remindersEnabled, selectedFrequency, reminderHour, reminderMinute) {
        if (hasPermission && remindersEnabled) {
            applyReminderScheduling()
        }
    }

    val permissionInfo = when {
        hasPermission && items.isEmpty() ->
            "Permission granted, but no media found for today."
        hasPermission && items.isNotEmpty() ->
            "Permission granted. Showing ${items.size} media items from today."
        permissionAsked && !hasPermission ->
            "Permission denied. Please allow media access in Android settings."
        else ->
            "Requesting permission..."
    }

    val summaryInfo = if (hasPermission) {
        val base = "Today: $todayCount items • ${formatSize(todayBytes)}    |    Last 7 days: $weekCount items • ${formatSize(weekBytes)}"
        if (streak > 0) "$base    •   Clean streak: ${streak}d" else base
    } else {
        ""
    }

    val baseFiltered = when (filter) {
        MediaFilter.ALL -> items
        MediaFilter.IMAGES -> items.filter { it.mimeType?.startsWith("image/") == true }
        MediaFilter.VIDEOS -> items.filter { it.mimeType?.startsWith("video/") == true }
        MediaFilter.OTHER -> items.filter { item ->
            val m = item.mimeType
            m == null || (!m.startsWith("image/") && !m.startsWith("video/"))
        }
    }

    val suggestionFiltered = when (activeSuggestion) {
        SuggestionType.NONE -> baseFiltered
        SuggestionType.LARGE_TODAY -> {
            val threshold = 50L * 1024L * 1024L
            baseFiltered.filter { it.sizeKb.toLong() * 1024L >= threshold }
        }
        SuggestionType.SCREENSHOTS_TODAY -> {
            baseFiltered.filter { it.name.startsWith("Screenshot", ignoreCase = true) }
        }
    }

    SimpleHomeScreen(
        items = suggestionFiltered,
        onRefreshClick = {
            val prefs = UserPrefs.get(context)
            streak = prefs.recordCleanup()
            reload()
        },
        permissionInfo = permissionInfo,
        summaryInfo = summaryInfo,
        currentFilter = filter,
        onFilterChange = { filter = it; activeSuggestion = SuggestionType.NONE },
        largeTodayCount = largeTodayCount,
        largeTodaySizeText = formatSize(largeTodayBytes),
        screenshotTodayCount = screenshotTodayCount,
        screenshotTodaySizeText = formatSize(screenshotTodayBytes),
        activeSuggestion = activeSuggestion,
        onSuggestionChange = { activeSuggestion = it },
        remindersEnabled = remindersEnabled,
        selectedFrequency = selectedFrequency,
        onFrequencyChange = { option ->
            selectedFrequency = option
            val prefs = UserPrefs.get(context)
            prefs.setReminderFrequencyDays(option.days)
            applyReminderScheduling()
        },
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        onTimeChange = { hour, minute ->
            reminderHour = hour
            reminderMinute = minute
            val prefs = UserPrefs.get(context)
            prefs.setReminderTime(hour, minute)
            applyReminderScheduling()
        },
        onRemindersToggle = { onRemindersToggle(it) },
        onOpenInSystem = { item ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = item.uri
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("OpenExternal", "No app can open this URI: ${item.uri}", e)
            }
        },
        onOpenSystemStorage = {
            val intent = Intent(StorageManager.ACTION_MANAGE_STORAGE)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("OpenExternal", "Cannot open system storage manager", e)
            }
        }
    )
}
