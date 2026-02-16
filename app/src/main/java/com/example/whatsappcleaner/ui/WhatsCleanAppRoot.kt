package com.example.whatsappcleaner.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.whatsappcleaner.data.MediaLoader
import com.example.whatsappcleaner.data.SimpleMediaItem
import com.example.whatsappcleaner.data.UserPrefs
import com.example.whatsappcleaner.data.formatSize
// --- IMPORT THE NEW RENAMED CLASSES ---
import com.example.whatsappcleaner.ReminderFreq
import com.example.whatsappcleaner.ReminderTime
// --------------------------------------
import com.example.whatsappcleaner.ui.home.MediaFilter
import com.example.whatsappcleaner.ui.home.SimpleHomeScreen
import com.example.whatsappcleaner.ui.home.SuggestionType
import com.example.whatsappcleaner.workers.ReminderWorker
import java.util.concurrent.TimeUnit

@Composable
fun WhatsCleanAppRoot() {
    val context = LocalContext.current
    var items by remember { mutableStateOf<List<SimpleMediaItem>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(false) }

    // Stats
    var todayCount by remember { mutableStateOf(0) }
    var todayBytes by remember { mutableStateOf(0L) }
    var weekCount by remember { mutableStateOf(0) }
    var weekBytes by remember { mutableStateOf(0L) }

    // Smart Suggestions
    var largeTodayCount by remember { mutableStateOf(0) }
    var largeTodayBytes by remember { mutableStateOf(0L) }
    var screenshotTodayCount by remember { mutableStateOf(0) }
    var screenshotTodayBytes by remember { mutableStateOf(0L) }

    var filter by remember { mutableStateOf(MediaFilter.ALL) }
    var activeSuggestion by remember { mutableStateOf(SuggestionType.NONE) }

    var streak by remember { mutableStateOf(0) }
    var remindersEnabled by remember { mutableStateOf(true) }

    // --- UPDATED TO USE NEW CLASS NAMES ---
    val frequencyOptions = remember {
        listOf(
            ReminderFreq("Every day", 1),
            ReminderFreq("Every 3 days", 3),
            ReminderFreq("Every week", 7)
        )
    }

    val timeOptions = remember {
        (0..23).map { hour ->
            val label = String.format("%02d:00", hour)
            ReminderTime(label = label, hour = hour, minute = 0)
        }
    }

    var selectedFrequency by remember { mutableStateOf(frequencyOptions.first()) }
    var selectedTime by remember { mutableStateOf(timeOptions.first()) }

    // --- LOGIC FUNCTIONS ---

    fun reload() {
        if (!hasPermission) return
        val loader = MediaLoader(context)

        // 1. Load Today's Data
        val todayItems = loader.loadTodayWhatsAppMedia()
        items = todayItems
        todayCount = todayItems.size
        todayBytes = todayItems.sumOf { it.sizeKb.toLong() * 1024L }

        // 2. Calculate Large Files (> 50MB)
        val largeThresholdBytes = 50L * 1024L * 1024L
        val large = todayItems.filter { it.sizeKb.toLong() * 1024L >= largeThresholdBytes }
        largeTodayCount = large.size
        largeTodayBytes = large.sumOf { it.sizeKb.toLong() * 1024L }

        // 3. Calculate Screenshots
        val screenshots = todayItems.filter { it.name.startsWith("Screenshot", ignoreCase = true) }
        screenshotTodayCount = screenshots.size
        screenshotTodayBytes = screenshots.sumOf { it.sizeKb.toLong() * 1024L }

        // 4. Load Week Data (for stats only)
        val now = System.currentTimeMillis()
        val weekStart = now - 7L * 86_400_000L
        val weekItems = loader.loadWhatsAppMediaInRange(weekStart, now)
        weekCount = weekItems.size
        weekBytes = weekItems.sumOf { it.sizeKb.toLong() * 1024L }

        activeSuggestion = SuggestionType.NONE
    }

    fun scheduleReminderWithFrequency(freqDays: Int) {
        if (!remindersEnabled) return
        val interval = freqDays.coerceAtLeast(1).toLong()

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            interval, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "whats_clean_daily_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun onRemindersToggle(enabled: Boolean) {
        remindersEnabled = enabled
        val prefs = UserPrefs.get(context)
        prefs.setRemindersEnabled(enabled)

        val wm = WorkManager.getInstance(context)
        if (enabled) {
            scheduleReminderWithFrequency(selectedFrequency.days)
        } else {
            wm.cancelUniqueWork("whats_clean_daily_reminder")
        }
    }

    // --- EFFECTS ---

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            hasPermission = results.values.any { it }
        }

    // Initial Load
    LaunchedEffect(Unit) {
        val prefs = UserPrefs.get(context)
        remindersEnabled = prefs.isRemindersEnabled()

        val savedDays = prefs.getReminderFrequencyDays()
        selectedFrequency = frequencyOptions.firstOrNull { it.days == savedDays } ?: frequencyOptions.first()

        val h = prefs.getReminderTimeHour()
        val m = prefs.getReminderTimeMinute()
        selectedTime = timeOptions.firstOrNull { it.hour == h && it.minute == m } ?: timeOptions.first()

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

    // Reload when permission granted
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            reload()
            streak = UserPrefs.get(context).getStreak()
        }
    }

    // Schedule reminder on change
    LaunchedEffect(hasPermission, remindersEnabled, selectedFrequency) {
        if (hasPermission && remindersEnabled) {
            scheduleReminderWithFrequency(selectedFrequency.days)
        }
    }

    // --- UI PREPARATION ---

    val summaryInfo = if (hasPermission) {
        val base = "Today: $todayCount items • ${formatSize(todayBytes)}    |    Last 7 days: $weekCount items • ${formatSize(weekBytes)}"
        if (streak > 0) "$base    •   Clean streak: ${streak}d" else base
    } else {
        "Please grant storage permission"
    }

    // Filtering Logic
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

    // --- SCREEN CONTENT ---

    SimpleHomeScreen(
        items = suggestionFiltered,
        onRefreshClick = {
            val prefs = UserPrefs.get(context)
            streak = prefs.recordCleanup()
            reload()
        },
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
            scheduleReminderWithFrequency(option.days)
        },
        selectedTime = selectedTime,
        allTimeOptions = timeOptions,
        onTimeChange = { option ->
            selectedTime = option
            val prefs = UserPrefs.get(context)
            prefs.setReminderTime(option.hour, option.minute)
            scheduleReminderWithFrequency(selectedFrequency.days)
        },
        onRemindersToggle = { onRemindersToggle(it) },
        onOpenInSystem = { item ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(item.uri, item.mimeType ?: "*/*")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("OpenExternal", "No app can open this URI: ${item.uri}", e)
            }
        },
        onOpenSystemStorage = {
            val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                context.startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
    )
}