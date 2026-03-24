package com.example.whatsappcleaner.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import com.example.whatsappcleaner.data.billing.BillingProduct
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.ui.components.FriendlyState
import com.example.whatsappcleaner.ui.home.AnalyticsScreen
import com.example.whatsappcleaner.ui.home.HomeUiState
import com.example.whatsappcleaner.ui.home.JunkFilesScreen
import com.example.whatsappcleaner.ui.home.MediaFilter
import com.example.whatsappcleaner.ui.home.MediaViewerScreen
import com.example.whatsappcleaner.ui.home.PolishedMemeScreen
import com.example.whatsappcleaner.ui.home.PolishedPhoneRealityScreen
import com.example.whatsappcleaner.ui.home.PolishedSmartCleanScreen
import com.example.whatsappcleaner.ui.home.PremiumFeature
import com.example.whatsappcleaner.ui.home.SimpleHomeScreen
import com.example.whatsappcleaner.ui.home.SpamMediaScreen
import com.example.whatsappcleaner.ui.home.SuggestionType
import com.example.whatsappcleaner.ui.paywall.PaywallScreen
import com.example.whatsappcleaner.ui.settings.AppThemeMode
import com.example.whatsappcleaner.ui.settings.ReminderFrequencyOption
import com.example.whatsappcleaner.ui.settings.SettingsScreen

private object Routes {
    const val Home = "home"
    const val SmartClean = "smart_clean"
    const val PhoneReality = "phone_reality"
    const val MemeScreen = "meme_screen"
    const val JunkScreen = "junk_screen"
    const val Analytics = "analytics_screen"
    const val Spam = "spam_screen"
    const val MediaViewer = "media_viewer"
    const val Paywall = "paywall"
    const val Settings = "settings"
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
    onSettingsOpened: () -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit,
    onSmartAlertsToggle: (Boolean) -> Unit,
    onAutoCleanFrequencySelected: (ReminderFrequencyOption) -> Unit,
    onFileSizeFilterSelected: (Int) -> Unit,
    onShowOnlyLargeToggle: (Boolean) -> Unit,
    onIncludeScreenshotsToggle: (Boolean) -> Unit,
    onIncludeMemesToggle: (Boolean) -> Unit,
    onIncludeDuplicatesToggle: (Boolean) -> Unit,
    onUpgradeToPro: () -> Unit,
    onRestorePurchase: (String) -> Unit,
    onManageSubscription: () -> Unit,
    onPurchasePlan: (BillingProduct, String) -> Unit,
    onShareText: (String) -> Unit,
    onShareResult: () -> Unit,
    onInviteFriends: () -> Unit,
    onRateApp: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onFaq: () -> Unit,
    onContactSupport: () -> Unit,
    onReportIssue: () -> Unit,
    onPremiumFeatureRequested: (PremiumFeature) -> Boolean,
    onDeleteClicked: (String) -> Unit,
    onDeleteMediaRequest: (List<SimpleMediaItem>, String) -> Unit,
    onUndoDelete: () -> Unit,
    onDeleteSnackbarConsumed: () -> Unit,
    onReviewClicked: () -> Unit,
    onCleanupRecorded: (Long) -> Unit,
    versionLabel: String,
    modifier: Modifier = Modifier
) {
    if (!state.permissionGranted) {
        PermissionGate(onRequestPermission = onRequestPermission, modifier = modifier)
        return
    }
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(450)
            ) + fadeIn(animationSpec = tween(450))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(450)
            ) + fadeOut(animationSpec = tween(450))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(450)
            ) + fadeIn(animationSpec = tween(450))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(450)
            ) + fadeOut(animationSpec = tween(450))
        }
    ) {
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
                isProUser = state.isProUser,
                onNavigateToSmartClean = {
                    if (onPremiumFeatureRequested(PremiumFeature.SMART_CLEAN_ADVANCED)) {
                        navController.navigateSingleTop(Routes.SmartClean)
                    } else navController.navigateSingleTop(Routes.Paywall)
                },
                onNavigateToPhoneReality = { navController.navigateSingleTop(Routes.PhoneReality) },
                onNavigateToMemeAnalyzer = {
                    if (onPremiumFeatureRequested(PremiumFeature.MEME_DETECTION)) {
                        navController.navigateSingleTop(Routes.MemeScreen)
                    } else navController.navigateSingleTop(Routes.Paywall)
                },
                onNavigateToMediaViewer = {
                    onReviewClicked()
                    navController.navigateSingleTop(Routes.MediaViewer)
                },
                onNavigateToJunk = { navController.navigateSingleTop(Routes.JunkScreen) },
                onNavigateToAnalytics = {
                    if (onPremiumFeatureRequested(PremiumFeature.ADVANCED_ANALYTICS)) {
                        navController.navigateSingleTop(Routes.Analytics)
                    } else navController.navigateSingleTop(Routes.Paywall)
                },
                onNavigateToSpam = { navController.navigateSingleTop(Routes.Spam) },
                onNavigateToSettings = {
                    onSettingsOpened()
                    navController.navigateSingleTop(Routes.Settings)
                },
                onNavigateToDuplicates = {
                    if (onPremiumFeatureRequested(PremiumFeature.DUPLICATE_DETECTION)) {
                        navController.navigateSingleTop(Routes.SmartClean)
                    } else navController.navigateSingleTop(Routes.Paywall)
                },
                onBulkDeleteClick = {
                    if (onPremiumFeatureRequested(PremiumFeature.BULK_DELETE)) {
                        onDeleteClicked("bulk_delete")
                    } else navController.navigateSingleTop(Routes.Paywall)
                },
                onUpgradeToPro = {
                    onUpgradeToPro()
                    navController.navigateSingleTop(Routes.Paywall)
                },
                onDeleteConfirmed = { onDeleteClicked("home_delete") },
                onDeleteItemsRequested = { items -> onDeleteMediaRequest(items, "home_delete") },
                onOpenInSystem = onOpenInSystem,
                onOpenSystemStorage = onOpenSystemStorage,
                pendingDeleteUris = state.pendingDeleteUris.map { it.toString() }.toSet(),
                deleteSnackbarMessage = state.deleteSnackbarMessage,
                onUndoDelete = onUndoDelete,
                onDeleteSnackbarConsumed = onDeleteSnackbarConsumed,
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
                onSuggestionChange = onSuggestionChange,
                totalFiles = state.totalFiles,
                totalSize = state.totalSize,
                oldFilesCount = state.report.oldFiles
            )
        }

        composable(Routes.SmartClean) {
            PolishedSmartCleanScreen(
                duplicateItems = state.duplicateItems,
                spamItems = state.spamItems,
                largeFileItems = state.largeFileItems,
                sentFiles = state.sentFileItems,
                onOpenInSystem = onOpenInSystem,
                onBack = { navController.popBackStack() },
                onShareResult = onShareResult,
                onCleanupRecorded = onCleanupRecorded
            )
        }

        composable(Routes.PhoneReality) {
            PolishedPhoneRealityScreen(
                report = state.report,
                imageCount = state.allItems.count { mediaItem -> mediaItem.mimeType?.startsWith("image") == true },
                videoCount = state.allItems.count { mediaItem -> mediaItem.mimeType?.startsWith("video") == true },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MemeScreen) {
            PolishedMemeScreen(
                memes = state.memeItems,
                onOpenInSystem = onOpenInSystem,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.JunkScreen) {
            JunkFilesScreen(
                items = state.largeFileItems + state.sentFileItems,
                onOpenInSystem = onOpenInSystem,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Analytics) {
            AnalyticsScreen(
                report = state.report,
                imageCount = state.allItems.count { mediaItem -> mediaItem.mimeType?.startsWith("image") == true },
                videoCount = state.allItems.count { mediaItem -> mediaItem.mimeType?.startsWith("video") == true },
                memeCount = state.memeCount,
                duplicateCount = state.duplicateCount,
                spamCount = state.spamCount,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Spam) {
            SpamMediaScreen(
                items = state.spamItems,
                onOpenInSystem = onOpenInSystem,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MediaViewer) {
            MediaViewerScreen(
                allItems = state.allItems,
                spamItems = state.spamItems,
                duplicateItems = state.duplicateItems,
                onOpenInSystem = onOpenInSystem,
                onDeleteItemsRequested = { items -> onDeleteMediaRequest(items, "media_viewer_swipe") },
                pendingDeleteUris = state.pendingDeleteUris.map { it.toString() }.toSet(),
                deleteSnackbarMessage = state.deleteSnackbarMessage,
                onUndoDelete = onUndoDelete,
                onDeleteSnackbarConsumed = onDeleteSnackbarConsumed,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Paywall) {
            PaywallScreen(
                subscriptionState = state.subscriptionState,
                source = state.paywallSource,
                exceededFreeLimit = state.hasExceededFreeLimit,
                onBack = { navController.popBackStack() },
                onPurchaseClick = { product -> onPurchasePlan(product, state.paywallSource) },
                onRestoreClick = { onRestorePurchase("paywall") }
            )
        }

        composable(Routes.Settings) {
            SettingsScreen(
                settings = state.settings,
                subscriptionState = state.subscriptionState,
                versionLabel = versionLabel,
                tagline = "Clean smarter. Free space instantly.",
                onBack = { navController.popBackStack() },
                onThemeSelected = onThemeSelected,
                onDailyReminderToggle = onRemindersToggle,
                onSmartAlertToggle = onSmartAlertsToggle,
                onAutoCleanFrequencySelected = onAutoCleanFrequencySelected,
                onFileSizeFilterSelected = onFileSizeFilterSelected,
                onShowOnlyLargeToggle = onShowOnlyLargeToggle,
                onIncludeScreenshotsToggle = onIncludeScreenshotsToggle,
                onIncludeMemesToggle = onIncludeMemesToggle,
                onIncludeDuplicatesToggle = onIncludeDuplicatesToggle,
                onUpgradeToPro = {
                    onUpgradeToPro()
                    navController.navigateSingleTop(Routes.Paywall)
                },
                onRestorePurchase = { onRestorePurchase("settings") },
                onManageSubscription = onManageSubscription,
                onPrivacyPolicy = onPrivacyPolicy,
                onFaq = onFaq,
                onContactSupport = onContactSupport,
                onReportIssue = onReportIssue,
                onRateApp = onRateApp,
                onShareApp = { onShareText("Clean smarter. Free space instantly with Cleanly AI.") },
                onInviteFriends = onInviteFriends
            )
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

@Composable
private fun PermissionGate(onRequestPermission: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FriendlyState(
            icon = Icons.Default.Lock,
            title = "Media permission required",
            message = "Allow photo and video access so the dashboard can show WhatsApp images, videos, memes, and cleanup suggestions."
        )
        Text(
            "If you previously denied access, grant it from the next system prompt and then tap refresh.",
            modifier = Modifier.padding(vertical = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Button(onClick = onRequestPermission) { Text("Grant media access") }
    }
}
