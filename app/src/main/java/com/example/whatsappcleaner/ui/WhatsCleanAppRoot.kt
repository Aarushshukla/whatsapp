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
import androidx.work.*
import com.example.whatsappcleaner.data.local.UserPrefs
import com.example.whatsappcleaner.data.local.MediaLoader
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
// --- FIXED IMPORTS ---
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
// --------------------
import com.example.whatsappcleaner.ui.home.*
import com.example.whatsappcleaner.ui.intro.*
import com.example.whatsappcleaner.workers.ReminderWorker
import java.util.concurrent.TimeUnit

enum class AppState { SPLASH, ONBOARDING, HOME }

@Composable
fun WhatsCleanAppRoot() {
    val context = LocalContext.current
    var appState by remember { mutableStateOf(AppState.SPLASH) }

    LaunchedEffect(Unit) {
        if (UserPrefs.get(context).hasSeenOnboarding()) { }
    }

    when (appState) {
        AppState.SPLASH -> {
            SplashScreen {
                if (UserPrefs.get(context).hasSeenOnboarding()) {
                    appState = AppState.HOME
                } else {
                    appState = AppState.ONBOARDING
                }
            }
        }
        AppState.ONBOARDING -> {
            OnboardingScreen {
                UserPrefs.get(context).setOnboardingSeen()
                appState = AppState.HOME
            }
        }
        AppState.HOME -> {
            var items by remember { mutableStateOf<List<SimpleMediaItem>>(emptyList()) }
            var hasPermission by remember { mutableStateOf(false) }
            var filter by remember { mutableStateOf(MediaFilter.ALL) }

            var largeTodayCount by remember { mutableStateOf(0) }
            var largeTodayBytes by remember { mutableStateOf(0L) }
            var screenshotTodayCount by remember { mutableStateOf(0) }
            var screenshotTodayBytes by remember { mutableStateOf(0L) }

            var activeSuggestion by remember { mutableStateOf(SuggestionType.NONE) }
            var remindersEnabled by remember { mutableStateOf(true) }

            val frequencyOptions = remember { listOf(ReminderFreq("Every day", 1)) }
            var selectedFrequency by remember { mutableStateOf(frequencyOptions.first()) }
            val timeOptions = remember { listOf(ReminderTime("09:00", 9, 0)) }
            var selectedTime by remember { mutableStateOf(timeOptions.first()) }

            fun reload() {
                if (!hasPermission) return
                val loader = MediaLoader(context)
                val todayItems = loader.loadTodayWhatsAppMedia()
                items = todayItems

                val large = todayItems.filter { it.sizeKb.toLong() * 1024L >= 50L * 1024L * 1024L }
                largeTodayCount = large.size
                largeTodayBytes = large.sumOf { it.sizeKb.toLong() * 1024L }

                val screens = todayItems.filter { it.name.startsWith("Screenshot", true) }
                screenshotTodayCount = screens.size
                screenshotTodayBytes = screens.sumOf { it.sizeKb.toLong() * 1024L }
            }

            fun onRemindersToggle(enabled: Boolean) {
                remindersEnabled = enabled
                UserPrefs.get(context).setRemindersEnabled(enabled)
            }

            val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                hasPermission = it.values.any { granted -> granted }
            }

            LaunchedEffect(Unit) {
                remindersEnabled = UserPrefs.get(context).isRemindersEnabled()
                if (Build.VERSION.SDK_INT >= 33) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO))
                } else {
                    permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                }
            }

            LaunchedEffect(hasPermission) { if (hasPermission) reload() }

            val summaryInfo = if (hasPermission) "Found ${items.size} files" else "Permission required"

            val finalItems = when (filter) {
                MediaFilter.ALL -> items
                MediaFilter.IMAGES -> items.filter { it.mimeType?.startsWith("image/") == true }
                MediaFilter.VIDEOS -> items.filter { it.mimeType?.startsWith("video/") == true }
                MediaFilter.OTHER -> items.filter { it.mimeType?.startsWith("image/") != true && it.mimeType?.startsWith("video/") != true }
            }

            SimpleHomeScreen(
                finalItems,
                onRefreshClick = { reload() },
                summaryInfo = summaryInfo,
                currentFilter = filter,
                onFilterChange = { filter = it },
                largeTodayCount = largeTodayCount,
                largeTodaySizeText = formatSize(largeTodayBytes),
                screenshotTodayCount = screenshotTodayCount,
                screenshotTodaySizeText = formatSize(screenshotTodayBytes),
                activeSuggestion = activeSuggestion,
                onSuggestionChange = { activeSuggestion = it },
                remindersEnabled = remindersEnabled,
                selectedFrequency = selectedFrequency,
                onFrequencyChange = { selectedFrequency = it },
                selectedTime = selectedTime,
                allTimeOptions = timeOptions,
                onTimeChange = { selectedTime = it },
                onRemindersToggle = { onRemindersToggle(it) },
                onOpenInSystem = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(it.uri, it.mimeType ?: "*/*")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    try { context.startActivity(intent) } catch (e: Exception) { }
                },
                onOpenSystemStorage = {
                    try { context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)) } catch (e: Exception) { }
                }
            )
        }
    }
}
