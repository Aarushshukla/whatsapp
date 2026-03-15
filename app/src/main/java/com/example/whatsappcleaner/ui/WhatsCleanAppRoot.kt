package com.example.whatsappcleaner.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.whatsappcleaner.ui.home.HomeUiState
import com.example.whatsappcleaner.ui.home.PhoneRealityReportScreen
import com.example.whatsappcleaner.ui.home.SimpleHomeScreen
import com.example.whatsappcleaner.ui.home.SmartCleanScreen

@Composable
fun WhatsCleanAppRoot(
    state: HomeUiState,
    onRefreshClick: () -> Unit,
    onFilterChange: (com.example.whatsappcleaner.ui.home.MediaFilter) -> Unit,
    onSuggestionChange: (com.example.whatsappcleaner.ui.home.SuggestionType) -> Unit,
    onFrequencyChange: (com.example.whatsappcleaner.data.ReminderFreq) -> Unit,
    onTimeChange: (com.example.whatsappcleaner.data.ReminderTime) -> Unit,
    onRemindersToggle: (Boolean) -> Unit,
    onOpenInSystem: (com.example.whatsappcleaner.data.local.SimpleMediaItem) -> Unit,
    onOpenSystemStorage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var route by remember { mutableStateOf("home") }

    when (route) {
        "smart_clean" -> SmartCleanScreen(
            duplicateItems = state.duplicateItems,
            spamItems = state.spamItems,
            largeFileItems = state.largeFileItems,
            sentFiles = state.sentFileItems,
            modifier = modifier
        )

        "phone_reality" -> PhoneRealityReportScreen(
            report = state.report,
            imagesCount = state.allItems.count { it.mimeType?.startsWith("image") == true },
            videosCount = state.allItems.count { it.mimeType?.startsWith("video") == true },
            modifier = modifier
        )

        else -> SimpleHomeScreen(
            items = state.filteredItems,
            onRefreshClick = onRefreshClick,
            summaryInfo = state.summaryInfo,
            currentFilter = state.currentFilter,
            onFilterChange = onFilterChange,
            largeTodayCount = state.largeTodayCount,
            largeTodaySizeText = state.largeTodaySizeText,
            screenshotTodayCount = state.screenshotTodayCount,
            screenshotTodaySizeText = state.screenshotTodaySizeText,
            activeSuggestion = state.activeSuggestion,
            onSuggestionChange = onSuggestionChange,
            remindersEnabled = state.remindersEnabled,
            selectedFrequency = state.selectedFrequency,
            onFrequencyChange = onFrequencyChange,
            selectedTime = state.selectedTime,
            allTimeOptions = state.timeOptions,
            onTimeChange = onTimeChange,
            onRemindersToggle = onRemindersToggle,
            memeCount = state.memeCount,
            spamCount = state.spamCount,
            junkCount = state.junkCount,
            duplicateCount = state.duplicateCount,
            onNavigateToSmartClean = { route = "smart_clean" },
            onNavigateToPhoneReality = { route = "phone_reality" },
            onOpenInSystem = onOpenInSystem,
            onOpenSystemStorage = onOpenSystemStorage
        )
    }
}
