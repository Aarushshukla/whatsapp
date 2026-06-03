@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.whatsappcleaner.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import com.example.whatsappcleaner.data.billing.BillingProduct
import com.example.whatsappcleaner.data.local.CategoryFilters
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatCount
import com.example.whatsappcleaner.data.local.formatPercent
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.components.FriendlyState
import com.example.whatsappcleaner.ui.home.AiFeature
import com.example.whatsappcleaner.ui.home.AnalyticsScreen
import com.example.whatsappcleaner.ui.home.BlurryPhotosFeatureScreen
import com.example.whatsappcleaner.ui.home.CategorySummaryUi
import com.example.whatsappcleaner.ui.home.DuplicateDetectorFeatureScreen
import com.example.whatsappcleaner.ui.home.FeaturesScreen
import com.example.whatsappcleaner.ui.home.LargeFilesFinderFeatureScreen
import com.example.whatsappcleaner.ui.home.MemeCleanerFeatureScreen
import com.example.whatsappcleaner.ui.home.OldMediaCleanerFeatureScreen
import com.example.whatsappcleaner.ui.home.ScreenshotsCleanerFeatureScreen
import com.example.whatsappcleaner.ui.home.SmartSuggestionsFeatureScreen
import com.example.whatsappcleaner.ui.home.SpamMediaDetectorFeatureScreen
import com.example.whatsappcleaner.ui.home.WhatsAppMediaCleanerFeatureScreen
import com.example.whatsappcleaner.ui.home.HomeUiState
import com.example.whatsappcleaner.ui.home.JunkFilesScreen
import com.example.whatsappcleaner.ui.home.MediaFilter
import com.example.whatsappcleaner.ui.home.MediaViewerScreen
import com.example.whatsappcleaner.ui.home.MemeAnalyzerScreen
import com.example.whatsappcleaner.ui.home.PolishedSmartCleanScreen
import com.example.whatsappcleaner.ui.home.PremiumFeature
import com.example.whatsappcleaner.ui.home.ScanUiState
import com.example.whatsappcleaner.ui.home.SimpleHomeScreen
import com.example.whatsappcleaner.ui.home.MediaOverviewScreen
import com.example.whatsappcleaner.ui.home.DashboardMediaBucket
import com.example.whatsappcleaner.ui.home.CategorySummaryUi
import com.example.whatsappcleaner.ui.home.DashboardSubScreen
import com.example.whatsappcleaner.ui.home.SimpleStatRow
import com.example.whatsappcleaner.ui.home.PermissionIntroScreen
import com.example.whatsappcleaner.ui.home.CheckSuccessScreen
import com.example.whatsappcleaner.ui.home.CleanupReminderScreen
import com.example.whatsappcleaner.ui.home.ScanIntroScreen
import com.example.whatsappcleaner.ui.home.ScanProgressScreen
import com.example.whatsappcleaner.ui.home.SpamMediaScreen
import com.example.whatsappcleaner.ui.home.SuggestionType
import com.example.whatsappcleaner.ui.paywall.PaywallScreen
import com.example.whatsappcleaner.ui.settings.AppThemeMode
import com.example.whatsappcleaner.ui.settings.ReminderFrequencyOption
import com.example.whatsappcleaner.ui.settings.SettingsScreen


private sealed interface AppScreenState {
    data object Resolving : AppScreenState
    data object PermissionIntro : AppScreenState
    data object PermissionGreat : AppScreenState
    data object FirstScanIntro : AppScreenState
    data object ScanProgress : AppScreenState
    data object ScanFinished : AppScreenState
    data object Dashboard : AppScreenState
    data object Empty : AppScreenState
    data object Error : AppScreenState
}

private object Routes {
    const val Home = "home"
    const val SmartClean = "smart_clean"
    const val PhoneReality = "phone_reality"
    const val MemeScreen = "meme_screen"
    const val JunkScreen = "junk_screen"
    const val Analytics = "analytics_screen"
    const val Spam = "spam_screen"
    const val MediaViewer = "media_viewer"
    const val Features = "features"
    const val WhatsAppCleaner = "whatsapp_cleaner"
    const val ScreenshotsCleaner = "screenshots_cleaner"
    const val Paywall = "paywall"
    const val Settings = "settings"
    const val PrivacyPolicy = "privacy_policy"
    const val Terms = "terms"
    const val About = "about"
    const val AiSmartSuggestions = "ai_smart_suggestions"
    const val AiDuplicateDetector = "ai_duplicate_detector"
    const val AiLargeFilesFinder = "ai_large_files_finder"
    const val AiOldMediaCleaner = "ai_old_media_cleaner"
    const val AiWhatsappMediaCleaner = "ai_whatsapp_media_cleaner"
    const val AiMemeCleaner = "ai_meme_cleaner"
    const val AiBlurryPhotos = "ai_blurry_photos"
    const val AiScreenshotsCleaner = "ai_screenshots_cleaner"
    const val AiSpamDetector = "ai_spam_detector"
    const val SmartReview = "smart_review"
    const val SmartReviewList = "smart_review_list"
    const val Categories = "categories"
    const val MediaOverview = "media_overview"
    const val Photos = "photos"
    const val Videos = "videos"
    const val Audio = "audio"
    const val Documents = "documents"
    const val Statuses = "statuses"
    const val Stickers = "stickers"
    const val DuplicateFinder = "duplicate_finder"
    const val DuplicateReview = "duplicate_review"
    const val LargeFiles = "large_files"
    const val LargeFilesReview = "large_files_review"
    const val OldMedia = "old_media"
    const val OldMediaReview = "old_media_review"
    const val StatusCleaner = "status_cleaner"
    const val MemesStickers = "memes_stickers"
    const val BlurryImages = "blurry_images"
    const val BlurryImagesReview = "blurry_images_review"
    const val ScanHistory = "scan_history"
    const val LastCleanupReceipt = "last_cleanup_receipt"
    const val StorageOverview = "storage_overview"
    const val HelpFeedback = "help_feedback"
    const val CleanupReminder = "cleanup_reminder"
}

@Composable
fun WhatsCleanAppRoot(
    state: HomeUiState,
    scanUiState: ScanUiState,
    onRefreshClick: () -> Unit,
    onAiScanClick: () -> Unit,
    onFilterChange: (MediaFilter) -> Unit,
    onSuggestionChange: (SuggestionType) -> Unit,
    onFrequencyChange: (ReminderFreq) -> Unit,
    onTimeChange: (ReminderTime) -> Unit,
    onRemindersToggle: (Boolean) -> Unit,
    onReminderEnableRequested: () -> Unit,
    onCleanupReminderIntervalSelected: (Long) -> Unit,
    onSaveCleanupReminder: () -> Unit,
    onCancelCleanupReminder: () -> Unit,
    onOpenInSystem: (SimpleMediaItem) -> Unit,
    onOpenSystemStorage: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
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
    onSmartCleanClicked: () -> Unit,
    onAiToolOpened: (AiFeature) -> Unit,
    onDeleteMediaRequest: (List<SimpleMediaItem>, String) -> Unit,
    onUndoDelete: () -> Unit,
    onDeleteSnackbarConsumed: () -> Unit,
    onReviewClicked: () -> Unit,
    onCleanupRecorded: (Long) -> Unit,
    onDeepCleanWatchAd: () -> Unit,
    onExitRequested: () -> Unit,
    onDebugCrashTest: () -> Unit,
    versionLabel: String,
    modifier: Modifier = Modifier
) {
        val context = LocalContext.current
    val prefs = remember { com.example.whatsappcleaner.data.local.UserPrefs.get(context) }
    var firstScanFinishedShown by rememberSaveable { mutableStateOf(false) }
    var permissionJustGranted by rememberSaveable { mutableStateOf(false) }
    var previousPermissionGranted by rememberSaveable { mutableStateOf(state.permissionGranted) }
    var hasSeenPermissionSuccess by rememberSaveable { mutableStateOf(prefs.hasSeenPermissionSuccess()) }
    var hasCompletedFirstScan by rememberSaveable { mutableStateOf(prefs.isFirstScanCompleted()) }
    var hasCompletedOnboarding by rememberSaveable { mutableStateOf(prefs.hasCompletedOnboarding()) }
    var appScreenState by remember { mutableStateOf<AppScreenState>(AppScreenState.Resolving) }
    var scanLaunchInFlight by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.permissionGranted, permissionJustGranted, hasSeenPermissionSuccess, hasCompletedFirstScan, hasCompletedOnboarding, scanUiState, firstScanFinishedShown) {
        appScreenState = when {
            !state.permissionGranted -> AppScreenState.PermissionIntro
            permissionJustGranted -> AppScreenState.PermissionGreat
            scanLaunchInFlight || scanUiState is ScanUiState.Loading -> AppScreenState.ScanProgress
            scanUiState is ScanUiState.Error -> AppScreenState.Error
            scanUiState is ScanUiState.Empty && !hasCompletedOnboarding -> AppScreenState.Empty
            scanUiState is ScanUiState.Success && !hasCompletedFirstScan && !hasCompletedOnboarding && !firstScanFinishedShown -> AppScreenState.ScanFinished
            !hasCompletedFirstScan && !hasCompletedOnboarding -> AppScreenState.FirstScanIntro
            state.filteredItems.isEmpty() -> AppScreenState.Empty
            else -> AppScreenState.Dashboard
        }
    }

    LaunchedEffect(state.permissionGranted) {
        val nowGranted = state.permissionGranted
        if (!previousPermissionGranted && nowGranted && !hasSeenPermissionSuccess) {
            permissionJustGranted = true
        }
        if (!nowGranted) {
            permissionJustGranted = false
        }
        previousPermissionGranted = nowGranted
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (appScreenState) {
            AppScreenState.Resolving -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Surface
            }
            AppScreenState.PermissionIntro -> {
                PermissionIntroScreen(
                    onAllow = onRequestPermission,
                    onTryAgain = onRequestPermission,
                    onOpenSettings = onOpenAppSettings,
                    message = if (scanUiState is ScanUiState.Error) "Storage access is needed to scan chat media." else null
                )
                return@Surface
            }
            AppScreenState.PermissionGreat -> {
                AnimatedContent(targetState = "great", transitionSpec = {
                    (slideInHorizontally(animationSpec = tween(300)) + fadeIn(animationSpec = tween(280))) togetherWith
                        (slideOutHorizontally(animationSpec = tween(300)) + fadeOut(animationSpec = tween(220)))
                }, label = "permission_great_transition") {
                    CheckSuccessScreen("Great", "Storage access is ready.", "CONTINUE") {
                        permissionJustGranted = false
                        prefs.setSeenPermissionSuccess(true)
                        hasSeenPermissionSuccess = true
                        hasCompletedFirstScan = prefs.isFirstScanCompleted()
                        hasCompletedOnboarding = prefs.hasCompletedOnboarding()
                    }
                }
                return@Surface
            }
            AppScreenState.FirstScanIntro -> {
                ScanIntroScreen(
                    onScan = {
                        if (!(scanLaunchInFlight || scanUiState is ScanUiState.Loading)) {
                            scanLaunchInFlight = true
                            appScreenState = AppScreenState.ScanProgress
                            onRefreshClick()
                        }
                    },
                    scanning = scanUiState is ScanUiState.Loading || scanLaunchInFlight
                )
                return@Surface
            }
            AppScreenState.ScanProgress -> {
                scanLaunchInFlight = false
                ScanProgressScreen(scanUiState)
                return@Surface
            }
            AppScreenState.ScanFinished -> {
                scanLaunchInFlight = false
                val msg = if (state.totalSize > 0L) "You can review ${com.example.whatsappcleaner.data.local.formatSize(state.totalSize)}" else "Your results are ready"
                CheckSuccessScreen("Your scan is finished!", msg, "CONTINUE") {
                    prefs.setFirstScanCompleted(true)
                    prefs.setCompletedOnboarding(true)
                    hasCompletedFirstScan = true
                    hasCompletedOnboarding = true
                    firstScanFinishedShown = true
                }
                return@Surface
            }
            AppScreenState.Empty, AppScreenState.Error, AppScreenState.Dashboard -> Unit
        }
    }
    if (appScreenState != AppScreenState.Dashboard && appScreenState != AppScreenState.Empty && appScreenState != AppScreenState.Error) return
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    BackHandler(enabled = currentRoute == Routes.Home) {
        onExitRequested()
    }

    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.Home,
        modifier = modifier
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
                    if (true) {
                        onSmartCleanClicked()
                        navController.navigateSingleTop(Routes.SmartClean)
                    } else navController.navigateSingleTop(Routes.Paywall)
                },
                onNavigateToPhoneReality = { navController.navigateSingleTop(Routes.PhoneReality) },
                onNavigateToMemeAnalyzer = {
                    if (true) {
                        navController.navigateSingleTop(Routes.MemeScreen)
                    } else navController.navigateSingleTop(Routes.Paywall)
                },
                onNavigateToMediaViewer = {
                    onReviewClicked()
                    navController.navigateSingleTop(Routes.MediaViewer)
                },
                onNavigateToJunk = { navController.navigateSingleTop(Routes.JunkScreen) },
                onNavigateToAnalytics = {
                    if (true) {
                        navController.navigateSingleTop(Routes.Analytics)
                    } else navController.navigateSingleTop(Routes.Paywall)
                },
                onNavigateToSpam = { navController.navigateSingleTop(Routes.Spam) },
                onNavigateToSettings = {
                    onSettingsOpened()
                    navController.navigateSingleTop(Routes.Settings)
                },
                onNavigateToDuplicates = {
                    if (true) {
                        navController.navigateSingleTop(Routes.DuplicateFinder)
                    } else navController.navigateSingleTop(Routes.Paywall)
                },
                onBulkDeleteClick = {
                    if (true) {
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
                isDeleteInProgress = state.isDeleteInProgress,
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
                oldFilesCount = state.report.oldFiles,
                smartSuggestionSummary = state.smartSuggestionSummary,
                smartSuggestedItems = state.smartSuggestedItems,
                suggestionReasonsByUri = state.suggestionReasonsByUri,
                scanUiState = scanUiState,
                onNavigateToFeatures = { navController.navigateSingleTop(Routes.Features) },
                onAiFeatureClick = { feature ->
                    if (true) {
                        onAiToolOpened(feature)
                        navController.navigateSingleTop(
                            when (feature) {
                                AiFeature.SMART_SUGGESTIONS -> Routes.AiSmartSuggestions
                                AiFeature.DUPLICATE_DETECTOR -> Routes.AiDuplicateDetector
                                AiFeature.LARGE_FILES_FINDER -> Routes.AiLargeFilesFinder
                                AiFeature.OLD_MEDIA_CLEANER -> Routes.AiOldMediaCleaner
                                AiFeature.WHATSAPP_MEDIA_CLEANER -> Routes.AiWhatsappMediaCleaner
                                AiFeature.MEME_CLEANER -> Routes.AiMemeCleaner
                                AiFeature.BLURRY_PHOTOS -> Routes.AiBlurryPhotos
                                AiFeature.SCREENSHOTS_CLEANER -> Routes.AiScreenshotsCleaner
                                AiFeature.SPAM_MEDIA_DETECTOR -> Routes.AiSpamDetector
                            }
                        )
                    } else {
                        navController.navigateSingleTop(Routes.Paywall)
                    }
                },
                onNavigateToPrivacyPolicy = { navController.navigateSingleTop(Routes.PrivacyPolicy) },
                onNavigateToTerms = { navController.navigateSingleTop(Routes.Terms) },
                onNavigateToAbout = { navController.navigateSingleTop(Routes.About) },
                onNavigateToHelpFeedback = { navController.navigateSingleTop(Routes.HelpFeedback) },
                onNavigateToScanHistory = { navController.navigateSingleTop(Routes.ScanHistory) },
                onNavigateToCleanupReceipt = { navController.navigateSingleTop(Routes.LastCleanupReceipt) },
                onNavigateToStorageOverview = { navController.navigateSingleTop(Routes.StorageOverview) },
                onNavigateToSmartReview = { navController.navigateSingleTop(Routes.SmartReview) },
                onNavigateToMediaOverview = { navController.navigateSingleTop(Routes.MediaOverview) },
                onNavigateToCategories = { navController.navigateSingleTop(Routes.Categories) },
                onNavigateToPhotos = { navController.navigateSingleTop(Routes.Photos) },
                onNavigateToVideos = { navController.navigateSingleTop(Routes.Videos) },
                onNavigateToAudio = { navController.navigateSingleTop(Routes.Audio) },
                onNavigateToDocuments = { navController.navigateSingleTop(Routes.Documents) },
                onNavigateToStatuses = { navController.navigateSingleTop(Routes.Statuses) },
                onNavigateToStickers = { navController.navigateSingleTop(Routes.Stickers) },
                onNavigateToDuplicateFinder = { navController.navigateSingleTop(Routes.DuplicateFinder) },
                onNavigateToLargeFiles = { navController.navigateSingleTop(Routes.LargeFiles) },
                onNavigateToOldMedia = { navController.navigateSingleTop(Routes.OldMedia) },
                onNavigateToStatusCleaner = { navController.navigateSingleTop(Routes.StatusCleaner) },
                onNavigateToMemesStickers = { navController.navigateSingleTop(Routes.MemesStickers) },
                onNavigateToBlurryImages = { navController.navigateSingleTop(Routes.BlurryImages) },
                onNavigateToCleanupReminder = { navController.navigateSingleTop(Routes.CleanupReminder) }
            )
        }

        composable(Routes.Features) {
            FeaturesScreen(
                onBack = { navController.popBackStack() },
                onFeatureClick = { feature ->
                    if (true) {
                        navController.navigateSingleTop(
                            when (feature) {
                                AiFeature.SMART_SUGGESTIONS -> Routes.AiSmartSuggestions
                                AiFeature.DUPLICATE_DETECTOR -> Routes.AiDuplicateDetector
                                AiFeature.LARGE_FILES_FINDER -> Routes.AiLargeFilesFinder
                                AiFeature.OLD_MEDIA_CLEANER -> Routes.AiOldMediaCleaner
                                AiFeature.WHATSAPP_MEDIA_CLEANER -> Routes.AiWhatsappMediaCleaner
                                AiFeature.MEME_CLEANER -> Routes.AiMemeCleaner
                                AiFeature.BLURRY_PHOTOS -> Routes.AiBlurryPhotos
                                AiFeature.SCREENSHOTS_CLEANER -> Routes.AiScreenshotsCleaner
                                AiFeature.SPAM_MEDIA_DETECTOR -> Routes.AiSpamDetector
                            }
                        )
                    } else {
                        navController.navigateSingleTop(Routes.Paywall)
                    }
                },
                aiScanSummary = state.aiScanSummary,
                deepCleanCredits = state.deepCleanCredits,
                onAiScanClick = onAiScanClick,
                onDeepCleanWatchAd = onDeepCleanWatchAd
            )
        }

        composable(Routes.SmartClean) {
            PolishedSmartCleanScreen(
                allItems = state.allItems,
                duplicateItems = state.duplicateItems,
                spamItems = state.spamItems,
                largeFileItems = state.largeFileItems,
                sentFiles = state.sentFileItems,
                onOpenInSystem = onOpenInSystem,
                onDeleteItemsRequested = { items -> onDeleteMediaRequest(items, "smart_clean") },
                onBack = { navController.popBackStack() },
                onShareResult = onShareResult,
                onCleanupRecorded = onCleanupRecorded
            )
        }

        composable(Routes.PhoneReality) {
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

        composable(Routes.MemeScreen) {
            MemeAnalyzerScreen(
                memes = state.memeItems,
                onOpenInSystem = onOpenInSystem,
                onNext = { navController.popBackStack() }
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

        composable(Routes.AiSmartSuggestions) {
            SmartSuggestionsFeatureScreen(
                totalSuggested = state.smartSuggestionSummary.totalSuggestedFiles,
                totalSpaceToFree = state.smartSuggestionSummary.totalSpaceToFree,
                items = state.smartSuggestedItems,
                onDeleteItemsRequested = { items -> onDeleteMediaRequest(items, "ai_smart_suggestions") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AiDuplicateDetector) {
            DuplicateDetectorFeatureScreen(
                duplicateCount = state.duplicateGroups.flatten().size,
                items = state.duplicateGroups.flatten(),
                onDeleteItemsRequested = { items -> onDeleteMediaRequest(items, "ai_duplicates") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AiLargeFilesFinder) {
            LargeFilesFinderFeatureScreen(
                count = state.largeFileItems.size,
                totalBytes = state.largeFileItems.sumOf { mediaItem -> mediaItem.size },
                items = state.largeFileItems.sortedByDescending { it.size },
                onDeleteItemsRequested = { items -> onDeleteMediaRequest(items, "ai_large") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AiOldMediaCleaner) {
            OldMediaCleanerFeatureScreen(
                count = state.oldFileItems.size,
                items = state.oldFileItems,
                onDeleteItemsRequested = { items -> onDeleteMediaRequest(items, "ai_old") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AiWhatsappMediaCleaner) {
            WhatsAppMediaCleanerFeatureScreen(
                sentCount = state.whatsappJunkItems.size,
                items = state.whatsappJunkItems,
                onDeleteItemsRequested = { items -> onDeleteMediaRequest(items, "ai_whatsapp_junk") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AiMemeCleaner) {
            MemeCleanerFeatureScreen(
                memeCount = state.memeCount,
                items = state.memeItems,
                onDeleteItemsRequested = { items -> onDeleteMediaRequest(items, "ai_memes") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AiBlurryPhotos) {
            BlurryPhotosFeatureScreen(
                imageCount = state.blurryImageItems.size,
                items = state.blurryImageItems,
                onDeleteItemsRequested = { items -> onDeleteMediaRequest(items, "ai_blurry") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AiScreenshotsCleaner) {
            ScreenshotsCleanerFeatureScreen(
                screenshotCount = state.screenshotTodayCount,
                items = state.allItems.filter { it.name.startsWith("Screenshot", true) },
                onDeleteItemsRequested = { items -> onDeleteMediaRequest(items, "ai_screenshots") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AiSpamDetector) {
            SpamMediaDetectorFeatureScreen(
                spamCount = state.spamCount,
                items = state.spamItems,
                onDeleteItemsRequested = { items -> onDeleteMediaRequest(items, "ai_spam") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SmartReview) {
            val suggestions = remember(state.allItems, state.duplicateItems, state.largeFileItems, state.oldFileItems, state.blurryImageItems, state.smartSuggestedItems) {
                (state.duplicateItems + state.largeFileItems + state.oldFileItems + state.blurryImageItems + state.smartSuggestedItems).distinctBy { it.uri.toString() }
            }
            standardSummaryScreen(
                title = "Smart Review",
                subtitle = if (suggestions.isEmpty()) "Run a scan to find cleanup opportunities." else "Real cleanup suggestions from your latest scan",
                lines = listOf(
                    "Suggested cleanup size" to formatSize(suggestions.sumOf { it.size }),
                    "Suggested file count" to formatCount(suggestions.size),
                    "Duplicates" to formatCount(state.duplicateItems.size),
                    "Large files" to formatCount(state.largeFileItems.size),
                    "Old media" to formatCount(state.oldFileItems.size),
                    "Blurry images" to formatCount(state.blurryImageItems.size)
                ),
                hasData = suggestions.isNotEmpty(),
                emptyTitle = "No smart suggestions yet",
                emptySubtitle = "Run a scan to find cleanup opportunities.",
                actionLabel = if (suggestions.isEmpty()) "SCAN AGAIN" else "REVIEW SUGGESTED FILES",
                onBack = { navController.popBackStack() },
                onAction = { if (suggestions.isEmpty()) onRefreshClick() else navController.navigateSingleTop(Routes.SmartReviewList) }
            )
        }
        composable(Routes.SmartReviewList) {
            val suggestions = (state.duplicateItems + state.largeFileItems + state.oldFileItems + state.blurryImageItems + state.smartSuggestedItems).distinctBy { it.uri.toString() }
            reviewGridScreen("Suggested Files", suggestions, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "smart_review") }, onOpenPreview = onOpenInSystem)
        }

        composable(Routes.MediaOverview) {
            val buckets = remember(state.allItems, state.categorySummaries, state.totalSize) {
                buildMediaBuckets(state.allItems, state.categorySummaries, state.totalSize)
            }
            MediaOverviewScreen(items = buckets.filter { it.count > 0 }, onBack = { navController.popBackStack() }, onOpen = {
                navController.navigateSingleTop(
                    when (it) {
                        "Photos" -> Routes.Photos
                        "Videos" -> Routes.Videos
                        "Audio" -> Routes.Audio
                        "Documents" -> Routes.Documents
                        "Statuses" -> Routes.Statuses
                        "Stickers" -> Routes.Stickers
                        else -> Routes.MediaViewer
                    }
                )
            })
        }
        composable(Routes.Categories) {
            DashboardSubScreen("Categories", "Review by cleanup groups", { navController.popBackStack() }) {
                listOf(
                    "Duplicates" to Routes.DuplicateFinder,
                    "Large Videos" to Routes.LargeFiles,
                    "Old Media" to Routes.OldMedia,
                    "Statuses" to Routes.Statuses,
                    "Memes & Stickers" to Routes.MemesStickers,
                    "Blurry Images" to Routes.BlurryImages
                ).forEach { (title, route) ->
                    Button(onClick = { navController.navigateSingleTop(route) }, modifier = Modifier.fillMaxWidth()) { Text(title) }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
        composable(Routes.Photos) { reviewGridScreen("Photos", state.allItems.filter { classifyMediaCategory(it) == "Photos" }, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "photos") }, onOpenPreview = onOpenInSystem) }
        composable(Routes.Videos) { reviewGridScreen("Videos", state.allItems.filter { classifyMediaCategory(it) == "Videos" }, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "videos") }, onOpenPreview = onOpenInSystem) }
        composable(Routes.Audio) { reviewGridScreen("Audio", state.allItems.filter { classifyMediaCategory(it) == "Audio" }, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "audio") }, onOpenPreview = onOpenInSystem) }
        composable(Routes.Documents) { reviewGridScreen("Documents", state.allItems.filter { classifyMediaCategory(it) == "Documents" }, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "documents") }, onOpenPreview = onOpenInSystem) }
        composable(Routes.Statuses) { reviewGridScreen("Statuses", state.sentFileItems, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "statuses") }, onOpenPreview = onOpenInSystem) }
        composable(Routes.StatusCleaner) { reviewGridScreen("Statuses", state.sentFileItems, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "statuses") }, onOpenPreview = onOpenInSystem) }
        composable(Routes.Stickers) { reviewGridScreen("Stickers", state.memeItems, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "stickers") }, onOpenPreview = onOpenInSystem) }
        composable(Routes.DuplicateFinder) {
            val cached = state.cachedCategorySummary("Duplicates")
            val hasFiles = state.allItems.isNotEmpty()
            standardSummaryScreen(
                title = "Duplicate Finder",
                subtitle = if (state.duplicateItems.isEmpty()) "No duplicates found" else "Duplicate candidates from current scan",
                lines = listOf(
                    "Duplicate groups" to formatCount(state.duplicateGroups.size),
                    "Duplicate files" to formatCount(state.duplicateItems.size),
                    "Cleanup potential" to formatSize(state.duplicateItems.sumOf { it.size })
                ),
                cachedLines = cached?.let { listOf("Cached duplicate files" to formatCount(it.count), "Cached cleanup potential" to formatSize(it.sizeBytes)) }.orEmpty(),
                hasData = state.duplicateItems.isNotEmpty(),
                hasFileList = hasFiles,
                emptyTitle = "No duplicates found",
                emptySubtitle = "Run a scan to find duplicate chat media.",
                actionLabel = "REVIEW DUPLICATES",
                onBack = { navController.popBackStack() },
                onAction = { if (hasFiles) navController.navigateSingleTop(Routes.DuplicateReview) else onRefreshClick() }
            )
        }
        composable(Routes.DuplicateReview) { reviewGridScreen("Review Duplicates", state.duplicateItems, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "duplicates") }, onOpenPreview = onOpenInSystem) }
        composable(Routes.LargeFiles) {
            val cached = state.cachedCategorySummary("Large Files")
            val hasFiles = state.allItems.isNotEmpty()
            standardSummaryScreen(
                title = "Large Files",
                subtitle = "Files over 20 MB, sorted largest first",
                lines = listOf(
                    "Large file count" to formatCount(state.largeFileItems.size),
                    "Total large files size" to formatSize(state.largeFileItems.sumOf { it.size }),
                    "Sort chips" to "Largest • Newest • Oldest"
                ),
                cachedLines = cached?.let { listOf("Cached large file count" to formatCount(it.count), "Cached large files size" to formatSize(it.sizeBytes)) }.orEmpty(),
                hasData = state.largeFileItems.isNotEmpty(),
                hasFileList = hasFiles,
                emptyTitle = "No large files found",
                emptySubtitle = "Run a scan to find large media.",
                actionLabel = "REVIEW LARGE FILES",
                onBack = { navController.popBackStack() },
                onAction = { if (hasFiles) navController.navigateSingleTop(Routes.LargeFilesReview) else onRefreshClick() }
            )
        }
        composable(Routes.LargeFilesReview) { reviewGridScreen("Review Large Files", state.largeFileItems.sortedByDescending { it.size }, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "large_files") }, onOpenPreview = onOpenInSystem) }
        composable(Routes.OldMedia) {
            val cached = state.cachedCategorySummary("Old Media")
            val hasFiles = state.allItems.isNotEmpty()
            standardSummaryScreen(
                title = "Old Media",
                subtitle = "Older than 30 days by modified or added date",
                lines = listOf(
                    "Old media count" to formatCount(state.oldFileItems.size),
                    "Old media size" to formatSize(state.oldFileItems.sumOf { it.size }),
                    "30+ days" to formatCount(state.oldFileItems.count { ageDays(it) >= 30L }),
                    "90+ days" to formatCount(state.oldFileItems.count { ageDays(it) >= 90L }),
                    "180+ days" to formatCount(state.oldFileItems.count { ageDays(it) >= 180L })
                ),
                cachedLines = cached?.let { listOf("Cached old media count" to formatCount(it.count), "Cached old media size" to formatSize(it.sizeBytes)) }.orEmpty(),
                hasData = state.oldFileItems.isNotEmpty(),
                hasFileList = hasFiles,
                emptyTitle = "No old media found",
                emptySubtitle = "Files without dates are kept for careful review instead.",
                actionLabel = "REVIEW OLD MEDIA",
                onBack = { navController.popBackStack() },
                onAction = { if (hasFiles) navController.navigateSingleTop(Routes.OldMediaReview) else onRefreshClick() }
            )
        }
        composable(Routes.OldMediaReview) { reviewGridScreen("Review Old Media", state.oldFileItems, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "old_media") }, onOpenPreview = onOpenInSystem) }
        composable(Routes.MemesStickers) { reviewGridScreen("Memes & Stickers", state.memeItems, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "memes_stickers") }, onOpenPreview = onOpenInSystem) }
        composable(Routes.BlurryImages) {
            val cached = state.cachedCategorySummary("Blurry / Low-quality Images") ?: state.cachedCategorySummary("Blurry Images")
            val hasFiles = state.allItems.isNotEmpty()
            standardSummaryScreen(
                title = "Blurry Images",
                subtitle = if (state.blurryImageItems.isEmpty()) "No blurry images found" else "Low-quality image candidates from current scan",
                lines = listOf(
                    "Blurry image count" to formatCount(state.blurryImageItems.size),
                    "Total blurry size" to formatSize(state.blurryImageItems.sumOf { it.size })
                ),
                cachedLines = cached?.let { listOf("Cached blurry image count" to formatCount(it.count), "Cached blurry image size" to formatSize(it.sizeBytes)) }.orEmpty(),
                hasData = state.blurryImageItems.isNotEmpty(),
                hasFileList = hasFiles,
                emptyTitle = "No blurry images found",
                emptySubtitle = "Run a scan to find blurry or low-quality images.",
                actionLabel = "REVIEW BLURRY IMAGES",
                onBack = { navController.popBackStack() },
                onAction = { if (hasFiles) navController.navigateSingleTop(Routes.BlurryImagesReview) else onRefreshClick() }
            )
        }
        composable(Routes.BlurryImagesReview) { reviewGridScreen("Review Blurry Images", state.blurryImageItems, onBack = { navController.popBackStack() }, onDeleteSelected = { onDeleteMediaRequest(it, "blurry_images") }, onOpenPreview = onOpenInSystem) }
        composable(Routes.ScanHistory) {
            DashboardSubScreen("Scan History", "Previous scans and trends", { navController.popBackStack() }) {
                if (state.totalSize > 0L) {
                    SimpleStatRow("Last scan", state.summaryInfo)
                    SimpleStatRow("Scanned size", com.example.whatsappcleaner.data.local.formatSize(state.totalSize))
                    SimpleStatRow("Potential cleanup", com.example.whatsappcleaner.data.local.formatSize(state.smartSuggestionSummary.totalSpaceToFree))
                } else {
                    Text("No scan history yet")
                    Text("Run your first scan to see storage trends.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        composable(Routes.CleanupReminder) {
            LaunchedEffect(Unit) { com.example.whatsappcleaner.data.analytics.trackEvent(context, "reminder_opened") }
            CleanupReminderScreen(
                reminderEnabled = state.remindersEnabled,
                selectedIntervalMinutes = state.cleanupReminderIntervalMinutes,
                permissionDenied = state.reminderPermissionDenied,
                onIntervalSelected = onCleanupReminderIntervalSelected,
                onEnableToggle = { enabled ->
                    if (enabled) onReminderEnableRequested() else onCancelCleanupReminder()
                },
                onSave = onSaveCleanupReminder,
                onOpenSettings = onOpenAppSettings
            )
        }
        composable(Routes.LastCleanupReceipt) {
            DashboardSubScreen("Last Cleanup Receipt", "Recent cleanup summary", { navController.popBackStack() }) {
                SimpleStatRow("Cleaned amount", com.example.whatsappcleaner.data.local.formatSize(state.lastCleanupBytes))
                SimpleStatRow("Deleted files", "See delete confirmations")
                if (state.lastCleanupBytes == 0L) {
                    Text(
                        "Review and select files to see cleanup suggestions here",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        composable(Routes.StorageOverview) {
            DashboardSubScreen("Storage Overview", "Chat media footprint", { navController.popBackStack() }) {
                SimpleStatRow("Total chat media", com.example.whatsappcleaner.data.local.formatSize(state.totalSize))
                SimpleStatRow("Potential cleanup", com.example.whatsappcleaner.data.local.formatSize(state.smartSuggestionSummary.totalSpaceToFree))
                SimpleStatRow("File count", state.totalFiles.toString())
            }
        }
        composable(Routes.HelpFeedback) {
            standardSummaryScreen(
                title = "Help & Feedback",
                subtitle = "Frequently asked questions",
                lines = listOf(
                    "Scanning" to "Runs locally on your device",
                    "Permissions" to "Needed to read media metadata",
                    "Uploads" to "No cloud upload for scans",
                    "Deletion" to "You confirm before delete"
                ),
                hasData = true,
                emptyTitle = "",
                emptySubtitle = "",
                actionLabel = "OPEN FAQ",
                onBack = { navController.popBackStack() },
                onAction = onFaq
            )
        }

        composable(Routes.Paywall) {
            // TODO: RE-ENABLE SUBSCRIPTION LATER
            /*
            PaywallScreen(
                subscriptionState = state.subscriptionState,
                source = state.paywallSource,
                exceededFreeLimit = state.hasExceededFreeLimit,
                onBack = { navController.popBackStack() },
                onPurchaseClick = { product -> onPurchasePlan(product, state.paywallSource) },
                onRestoreClick = { onRestorePurchase("paywall") },
                onContinueFreeClick = { navController.popBackStack() }
            )
            */
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("All features are unlocked.", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { navController.popBackStack() }) {
                    Text("Continue")
                }
            }
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
                onPrivacyPolicy = { navController.navigateSingleTop(Routes.PrivacyPolicy) },
                onTerms = { navController.navigateSingleTop(Routes.Terms) },
                onFaq = onFaq,
                onContactSupport = onContactSupport,
                onReportIssue = onReportIssue,
                onRateApp = onRateApp,
                onShareApp = { onShareText("Clean chat media storage with ChatSweep.") },
                onInviteFriends = onInviteFriends,
                onDebugCrashTest = onDebugCrashTest
            )
        }

        composable(Routes.PrivacyPolicy) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.Terms) {
            TermsAndConditionsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.About) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}


@Composable
private fun reviewGridScreen(title: String, items: List<SimpleMediaItem>, onBack: () -> Unit, onDeleteSelected: (List<SimpleMediaItem>) -> Unit, onOpenPreview: (SimpleMediaItem) -> Unit) {
    val selectedUris = remember { mutableStateListOf<String>() }
    val totalSelected = items.filter { selectedUris.contains(it.uri.toString()) }
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }) }, bottomBar = {
        if (totalSelected.isNotEmpty()) BottomAppBar { Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("${totalSelected.size} selected • ${com.example.whatsappcleaner.data.local.formatSize(totalSelected.sumOf { it.size })}"); Button(onClick = { onDeleteSelected(totalSelected) }) { Text("DELETE SELECTED") } } }
    }) { pv ->
        Column(Modifier.padding(pv).padding(12.dp)) {
            Text("${com.example.whatsappcleaner.data.local.formatSize(items.sumOf { it.size })} • ${items.size} files", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    selectedUris.clear(); selectedUris.addAll(items.filter { it.reviewSafetyLabel() == "SAFE" && !it.isProtectedForAutoSelection() }.map { it.uri.toString() })
                }, modifier = Modifier.weight(1f)) { Text("SELECT SAFE") }
                OutlinedButton(onClick = { selectedUris.clear() }, modifier = Modifier.weight(1f)) { Text("DESELECT") }
            }
            Spacer(Modifier.height(10.dp))
            if (items.isEmpty()) {
                Text("No files found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Run Scan Again to review individual files.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyVerticalGrid(columns = GridCells.Adaptive(150.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.uri.toString() }) { item ->
                    val sel = selectedUris.contains(item.uri.toString())
                    Column(Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onOpenPreview(item) }.padding(8.dp)) {
                        if (item.mimeType?.startsWith("image") == true || item.mimeType?.startsWith("video") == true) {
                            AsyncImage(model = item.uri, contentDescription = item.name, modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = sel, onCheckedChange = { if (it) selectedUris.add(item.uri.toString()) else selectedUris.remove(item.uri.toString()) }); Text(com.example.whatsappcleaner.data.local.formatSize(item.size), fontSize = 12.sp) }
                        Text(item.reviewSafetyLabel(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(item.name, maxLines = 1, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
}

private fun SimpleMediaItem.reviewSafetyLabel(): String = if (isProtectedForAutoSelection()) "PROTECTED" else if (path.contains("status", true) || path.contains("sticker", true) || name.contains("meme", true)) "SAFE" else "REVIEW"
private fun SimpleMediaItem.isProtectedForAutoSelection(): Boolean = (System.currentTimeMillis() - addedMillis) < (3L * 24L * 60L * 60L * 1000L)

@Composable
private fun mediaTypeScreen(
    title: String,
    subtitle: String,
    items: List<SimpleMediaItem>,
    actionLabel: String,
    onBack: () -> Unit,
    onAction: () -> Unit
) {
    DashboardSubScreen(title, subtitle, onBack = onBack) {
        SimpleStatRow("Count", items.size.toString())
        SimpleStatRow("Total size", com.example.whatsappcleaner.data.local.formatSize(items.sumOf { it.size }))
        if (items.isEmpty()) {
            Text("No ${title.lowercase()} found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) { Text(actionLabel) }
    }
}

@Composable
private fun AnimatedNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
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
        },
        builder = builder
    )
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
    }
}



private fun ageDays(item: SimpleMediaItem): Long = CategoryFilters.oldMediaAgeDays(item)

private fun buildMediaBuckets(items: List<SimpleMediaItem>, cachedSummaries: List<CategorySummaryUi>, totalSize: Long): List<DashboardMediaBucket> {
    if (items.isEmpty()) {
        return cachedSummaries.map { cached ->
            DashboardMediaBucket(cached.title, cached.count, cached.sizeBytes, formatPercent(cached.sizeBytes, totalSize).removeSuffix("%").toFloatOrNull()?.toInt() ?: 0)
        }
    }
    val grouped = items.groupBy { classifyMediaCategory(it) }
    val total = items.sumOf { it.size }.coerceAtLeast(0L)
    return listOf("Photos", "Videos", "Audio", "Documents", "Statuses", "Stickers", "Other").map { name ->
        val categoryItems = grouped[name].orEmpty()
        val bytes = categoryItems.sumOf { it.size }
        DashboardMediaBucket(name, categoryItems.size, bytes, formatPercent(bytes, total).removeSuffix("%").toFloatOrNull()?.toInt() ?: 0)
    }
}

private fun classifyMediaCategory(item: SimpleMediaItem): String {
    val mime = item.mimeType.orEmpty().lowercase()
    val ext = item.name.substringAfterLast('.', "").lowercase()
    val marker = "${item.bucketName.orEmpty()} ${item.name} ${item.path}".lowercase()
    return when {
        "status" in marker || "statuses" in marker -> "Statuses"
        "sticker" in marker || "stickers" in marker || (ext == "webp" && ("whatsapp" in marker || "sticker" in marker)) -> "Stickers"
        mime.startsWith("image/") -> "Photos"
        mime.startsWith("video/") -> "Videos"
        mime.startsWith("audio/") -> "Audio"
        ext in setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "zip", "rar", "7z", "apk") -> "Documents"
        mime.contains("pdf") || mime.contains("msword") || mime.contains("spreadsheet") || mime.contains("presentation") || mime.startsWith("text/") || mime.contains("zip") || mime.contains("rar") -> "Documents"
        else -> "Other"
    }
}

private fun isStatus(item: SimpleMediaItem): Boolean {
    val text = "${item.bucketName.orEmpty()} ${item.name} ${item.path}".lowercase()
    return "status" in text
}

private fun isSticker(item: SimpleMediaItem): Boolean {
    val mime = item.mimeType.orEmpty().lowercase()
    val text = "${item.bucketName.orEmpty()} ${item.name} ${item.path}".lowercase()
    return mime.contains("webp") || "sticker" in text
}

private fun isPhoto(item: SimpleMediaItem): Boolean = item.mimeType.orEmpty().lowercase().startsWith("image/") && !isSticker(item)
private fun isVideo(item: SimpleMediaItem): Boolean = item.mimeType.orEmpty().lowercase().startsWith("video/")
private fun isAudio(item: SimpleMediaItem): Boolean = item.mimeType.orEmpty().lowercase().startsWith("audio/")
private fun isDocument(item: SimpleMediaItem): Boolean {
    val mime = item.mimeType.orEmpty().lowercase()
    val name = item.name.lowercase()
    return mime.contains("pdf") || mime.contains("msword") || mime.contains("spreadsheet") || mime.contains("presentation") ||
        mime.contains("text") || mime.contains("zip") || name.endsWith(".pdf") || name.endsWith(".doc") || name.endsWith(".docx") ||
        name.endsWith(".txt") || name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".ppt") || name.endsWith(".pptx") || name.endsWith(".zip")
}

@Composable
private fun PermissionGate(
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            message = "ChatSweep needs media access to scan chat photos, videos, audio, and documents on your device. Files are scanned locally. Nothing is uploaded."
        )
        Text(
            "If access is denied, tap Retry. If Android stops showing the prompt, open Settings and allow media access.",
            modifier = Modifier.padding(vertical = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Button(onClick = onRequestPermission) { Text("Retry") }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onOpenAppSettings) { Text("Open Settings") }
    }
}


@Composable
private fun HomeUiState.cachedCategorySummary(vararg titles: String): CategorySummaryUi? =
    categorySummaries.firstOrNull { category -> titles.any { title -> category.title.equals(title, ignoreCase = true) } }

@Composable
private fun standardSummaryScreen(
    title: String,
    subtitle: String,
    lines: List<Pair<String, String>>,
    cachedLines: List<Pair<String, String>> = emptyList(),
    hasData: Boolean,
    hasFileList: Boolean = true,
    emptyTitle: String,
    emptySubtitle: String,
    actionLabel: String,
    onBack: () -> Unit,
    onAction: () -> Unit
) {
    DashboardSubScreen(title, subtitle, onBack) {
        when {
            hasData -> lines.forEach { (key, value) -> SimpleStatRow(key, value) }
            !hasFileList && cachedLines.isNotEmpty() -> {
                cachedLines.forEach { (key, value) -> SimpleStatRow(key, value) }
                Spacer(Modifier.height(8.dp))
                Text("Run Scan Again to review individual files.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                Text(emptyTitle, color = MaterialTheme.colorScheme.onSurface)
                Text(emptySubtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) { Text(if (!hasFileList && cachedLines.isNotEmpty()) "SCAN AGAIN" else actionLabel) }
    }
}
