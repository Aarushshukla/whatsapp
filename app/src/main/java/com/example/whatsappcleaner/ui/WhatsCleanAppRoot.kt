package com.example.whatsappcleaner.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.whatsappcleaner.data.local.UserPrefs
import com.example.whatsappcleaner.ui.home.HomeViewModel
import com.example.whatsappcleaner.ui.home.SimpleHomeScreen
import com.example.whatsappcleaner.ui.intro.OnboardingScreen
import com.example.whatsappcleaner.ui.intro.SplashScreen

enum class AppState { SPLASH, ONBOARDING, HOME }

@Composable
fun WhatsCleanAppRoot(viewModel: HomeViewModel) {
    val context = LocalContext.current
    var appState by remember { mutableStateOf(AppState.SPLASH) }

    // Observe the data from our updated ViewModel
    val state by viewModel.uiState.collectAsState()

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
            SimpleHomeScreen(
                todayItems = state.todayItems,
                olderItems = state.olderItems,
                onRefreshClick = { viewModel.refreshMedia() },
                summaryInfo = state.summaryInfo,
                currentFilter = state.currentFilter,
                onFilterChange = { viewModel.setFilter(it) },
                largeTodayCount = state.largeTodayCount,
                largeTodaySizeText = state.largeTodaySizeText,
                screenshotTodayCount = state.screenshotTodayCount,
                screenshotTodaySizeText = state.screenshotTodaySizeText,
                activeSuggestion = state.activeSuggestion,
                onSuggestionChange = { viewModel.setSuggestion(it) },
                remindersEnabled = state.remindersEnabled,
                selectedFrequency = state.selectedFrequency,
                onFrequencyChange = { viewModel.setFrequency(it) },
                selectedTime = state.selectedTime,
                allTimeOptions = state.timeOptions,
                onTimeChange = { viewModel.setTime(it) },
                onRemindersToggle = { viewModel.toggleReminders(it) },
                onOpenInSystem = { item ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(item.uri, item.mimeType ?: "*/*")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) { }
                },
                onOpenSystemStorage = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                }
            )
        }
    }
}