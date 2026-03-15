package com.example.whatsappcleaner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.ui.home.HomeUiState
import com.example.whatsappcleaner.ui.home.MediaFilter
import com.example.whatsappcleaner.ui.home.MediaViewerScreen
import com.example.whatsappcleaner.ui.home.MemeAnalyzerScreen
import com.example.whatsappcleaner.ui.home.PhoneRealityReportScreen
import com.example.whatsappcleaner.ui.home.SimpleHomeScreen
import com.example.whatsappcleaner.ui.home.SmartCleanScreen
import com.example.whatsappcleaner.ui.home.SuggestionType

private object Routes {
    const val Home = "home"
    const val SmartClean = "smart_clean"
    const val PhoneReality = "phone_reality"
    const val MemeAnalyzer = "meme_analyzer"
    const val MediaViewer = "media_viewer"
}

@Composable
fun WhatsCleanAppRoot(
    state: HomeUiState,
    onRefreshClick: () -> Unit,
    onFilterChange: (MediaFilter) -> Unit,
    onSuggestionChange: (SuggestionType) -> Unit,
    onFrequencyChange: (ReminderFreq) -> Unit,
    onTimeChange: (ReminderTime) -> Unit,
    onRemindersToggle: (Boolean) -> Unit,
    onOpenInSystem: (SimpleMediaItem) -> Unit,
    onOpenSystemStorage: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.permissionGranted) {
        PermissionGate(onRequestPermission = onRequestPermission, modifier = modifier)
        return
    }

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Home, modifier = modifier) {
        composable(Routes.Home) {
            SimpleHomeScreen(
                items = state.filteredItems,
                onRefreshClick = onRefreshClick,
                summaryInfo = state.summaryInfo,
                isLoading = state.isLoading,
                currentFilter = state.currentFilter,
                onFilterChange = onFilterChange,
                remindersEnabled = state.remindersEnabled,
                onRemindersToggle = onRemindersToggle,
                memeCount = state.memeCount,
                spamCount = state.spamCount,
                junkCount = state.junkCount,
                duplicateCount = state.duplicateCount,
                onNavigateToSmartClean = { navController.navigate(Routes.SmartClean) },
                onNavigateToPhoneReality = { navController.navigate(Routes.PhoneReality) },
                onNavigateToMemeAnalyzer = { navController.navigate(Routes.MemeAnalyzer) },
                onNavigateToMediaViewer = { navController.navigate(Routes.MediaViewer) },
                onOpenInSystem = onOpenInSystem,
                onOpenSystemStorage = onOpenSystemStorage,
                selectedFrequency = state.selectedFrequency,
                onFrequencyChange = onFrequencyChange,
                selectedTime = state.selectedTime,
                allTimeOptions = state.timeOptions,
                onTimeChange = onTimeChange,
                largeTodayCount = state.largeTodayCount,
                largeTodaySizeText = state.largeTodaySizeText,
                screenshotTodayCount = state.screenshotTodayCount,
                screenshotTodaySizeText = state.screenshotTodaySizeText,
                activeSuggestion = state.activeSuggestion,
                onSuggestionChange = onSuggestionChange
            )
        }

        composable(Routes.SmartClean) {
            SmartCleanScreen(
                duplicateItems = state.duplicateItems,
                spamItems = state.spamItems,
                largeFileItems = state.largeFileItems,
                sentFiles = state.sentFileItems,
                onOpenInSystem = onOpenInSystem,
                onNext = { navController.navigate(Routes.PhoneReality) }
            )
        }

        composable(Routes.PhoneReality) {
            PhoneRealityReportScreen(
                report = state.report,
                imagesCount = state.allItems.count { it.mimeType?.startsWith("image") == true },
                videosCount = state.allItems.count { it.mimeType?.startsWith("video") == true },
                onNext = { navController.navigate(Routes.MemeAnalyzer) }
            )
        }

        composable(Routes.MemeAnalyzer) {
            MemeAnalyzerScreen(
                memes = state.memeItems,
                onOpenInSystem = onOpenInSystem,
                onNext = { navController.navigate(Routes.MediaViewer) }
            )
        }

        composable(Routes.MediaViewer) {
            MediaViewerScreen(
                allItems = state.allItems,
                spamItems = state.spamItems,
                duplicateItems = state.duplicateItems,
                onOpenInSystem = onOpenInSystem
            )
        }
    }
}

@Composable
private fun PermissionGate(onRequestPermission: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Storage permission needed", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Allow image/video access so the app can scan gallery media using scoped storage APIs.",
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        Button(onClick = onRequestPermission) { Text("Grant access") }
    }
}
