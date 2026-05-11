package com.example.whatsappcleaner

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatsappcleaner.ads.AdManager
import com.example.whatsappcleaner.data.analytics.AnalyticsHelper
import com.example.whatsappcleaner.data.analytics.trackEvent
import com.example.whatsappcleaner.data.billing.SubscriptionRepository
import com.example.whatsappcleaner.ui.WhatsCleanAppRoot
import com.example.whatsappcleaner.ui.home.DeleteExecution
import com.example.whatsappcleaner.ui.home.HomeViewModel
import com.example.whatsappcleaner.ui.settings.AppThemeMode
import com.example.whatsappcleaner.ui.theme.WhatsCleanTheme
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val viewModel: HomeViewModel by viewModels()
    private val subscriptionRepository by lazy(LazyThreadSafetyMode.NONE) { SubscriptionRepository.get(this) }
    private val adManager by lazy(LazyThreadSafetyMode.NONE) { AdManager(this) }
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var hasShownExitAdThisSession = false
    private val deleteLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            try {
                Log.d("DELETE_FLOW", "Step 4: Result code = ${result.resultCode}")
                Log.d(TAG, "Delete request finished. resultCode=${result.resultCode}")
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    Log.d("DELETE_FLOW", "Step 4.1: User confirmed (Allow pressed)")
                    viewModel.confirmDeleteSuccess()
                } else {
                    Log.d("DELETE_FLOW", "Step 4.2: User cancelled delete request")
                }
            } catch (error: Exception) {
                Log.e(TAG, "Failed while handling delete launcher result.", error)
                FirebaseCrashlytics.getInstance().recordException(error)
                viewModel.onMediaDeleteFailed()
                showDeleteError("Unable to process delete result.")
            }
        }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            object : ActivityResultCallback<Map<String, Boolean>> {
                override fun onActivityResult(permissions: Map<String, Boolean>) {
                    val granted = permissions.isNotEmpty() && permissions.values.all { isGranted -> isGranted }
                    Log.d(TAG, "Permission result: $permissions")
                    viewModel.updatePermissionStatus(granted)
                    if (granted) {
                        viewModel.refreshMedia()
                    } else {
                        Log.w(TAG, "Media permissions denied. Showing fallback UI.")
                    }
                }
            }
        )
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        Log.d("TEST", "Firebase initialized")
        val analytics = Firebase.analytics
        val app = FirebaseApp.getInstance()
        Log.d("TEST", "FirebaseApp: ${app.name}")
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        Log.d("TEST", "APP STARTED")
        Firebase.analytics.logEvent("test_event", null)
        Log.d(TAG, "Firebase analytics ready: $analytics")
        Log.d(TAG, "onCreate called.")
        // TODO: RE-ENABLE SUBSCRIPTION LATER
        /*
        runCatching { subscriptionRepository.start(this) }
            .onFailure { error -> Log.e(TAG, "Unable to initialize subscriptions during onCreate.", error) }
        */
        ensureMediaAccessForSignedInUser()
        adManager.initialize()
        setContent {
            MainActivityContent(versionLabel = safeVersionLabel())
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called. Refreshing purchases and permission state.")
        // TODO: RE-ENABLE SUBSCRIPTION LATER
        /*
        runCatching {
            subscriptionRepository.start(this)
            subscriptionRepository.refreshPurchases()
        }
            .onFailure { error -> Log.e(TAG, "Unable to refresh purchases on resume.", error) }
        */
        val hasPermission = syncPermissionState()
        if (hasPermission) {
            viewModel.refreshMedia(showLoading = false)
        }
    }

    private fun ensureMediaAccessForSignedInUser() {
        val hasPermission = syncPermissionState()
        if (hasPermission) {
            viewModel.refreshMedia()
        } else {
            requestStoragePermissions()
        }
    }

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    private fun syncPermissionState(): Boolean {
        val permissions = requiredPermissions()
        val granted = permissions.isNotEmpty() && permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        viewModel.updatePermissionStatus(granted)
        return granted
    }

    private fun requestStoragePermissions() {
        val permissions = requiredPermissions()
        if (permissions.isEmpty()) {
            Log.w(TAG, "No storage permissions are required on this device configuration.")
            viewModel.updatePermissionStatus(true)
            viewModel.refreshMedia()
            return
        }
        val alreadyGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (alreadyGranted) {
            Log.d(TAG, "Storage permissions already granted.")
            viewModel.updatePermissionStatus(true)
            viewModel.refreshMedia()
            return
        }
        Log.d(TAG, "Requesting media permissions: ${permissions.joinToString()}")
        requestPermissionLauncher.launch(permissions)
    }

    private fun openFileInSystem(uri: Uri?) {
        if (uri == null || uri == Uri.EMPTY) {
            Log.w(TAG, "Skipping openFileInSystem because the uri was null or empty.")
            openSystemStorage()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { startActivity(Intent.createChooser(intent, "Open media")) }
            .onFailure { error ->
                Log.e(TAG, "Unable to open media file in system app.", error)
                openSystemStorage()
            }
    }

    private fun openSystemStorage() {
        val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
        runCatching { startActivity(intent) }
            .onFailure { error ->
                Log.e(TAG, "Unable to open internal storage settings.", error)
                runCatching { startActivity(Intent(Settings.ACTION_SETTINGS)) }
                    .onFailure { fallbackError -> Log.e(TAG, "Unable to open Android settings.", fallbackError) }
            }
    }

    private fun launchDeleteRequest(uris: List<Uri>) {
        Log.d("DELETE_FLOW", "Step 3: Entered launchDeleteRequest")
        Log.d("DELETE_DEBUG", "Entering launchDeleteRequest with uriCount=${uris.size}")
        Log.d("DELETE_FLOW", "Step 2: URIs size = ${uris.size}")
        uris.forEach { uri -> Log.d("DELETE_FLOW", "URI = $uri") }
        Log.d("DELETE_DEBUG", "deleteMedia called. sdk=${Build.VERSION.SDK_INT}, uriCount=${uris.size}")
        uris.forEachIndexed { index, uri ->
            Log.d("DELETE_DEBUG", "URI[$index]=$uri")
        }
        if (uris.isEmpty()) {
            Log.e("DELETE_DEBUG", "Empty list")
            viewModel.onMediaDeleteCancelled()
            showDeleteError("No files selected for deletion.")
            return
        }
        val validUris = uris.distinct()
        Log.d("DELETE_FLOW", "Filtered valid URIs count = ${validUris.size}")
        if (validUris.isEmpty()) {
            Log.w("DELETE_DEBUG", "No valid MediaStore URIs available for delete request.")
            viewModel.onMediaDeleteCancelled()
            showDeleteError("Some files cannot be deleted due to system restrictions")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Log.d("DELETE_FLOW", "Step 3: Launching system delete request")
                Log.d("DELETE_DEBUG", "MediaStore.createDeleteRequest flow for ${validUris.size} URIs")
                val request = MediaStore.createDeleteRequest(contentResolver, validUris)
                val intentSenderRequest = IntentSenderRequest.Builder(request.intentSender).build()
                Log.d("DELETE_DEBUG", "About to call deleteLauncher.launch() with uriCount=${validUris.size}")
                deleteLauncher.launch(intentSenderRequest)
                Log.d("DELETE_FLOW", "Step 3.1: deleteLauncher launched")
                Log.d("DELETE_DEBUG", "deleteLauncher.launch() executed for MediaStore delete request")
            } catch (error: Exception) {
                Log.e("DELETE_DEBUG", "Unable to launch MediaStore delete request", error)
                FirebaseCrashlytics.getInstance().recordException(error)
                viewModel.onMediaDeleteFailed()
                showDeleteError("Unable to delete files right now.")
            }
            return
        }

        try {
            var deletedCount = 0
            validUris.forEach { uri ->
                deletedCount += contentResolver.delete(uri, null, null)
            }
            Log.d("DELETE_DEBUG", "contentResolver.delete flow completed. deletedCount=$deletedCount")
            if (deletedCount > 0) {
                viewModel.confirmDeleteSuccess()
            } else {
                viewModel.onMediaDeleteCancelled()
                showDeleteError("Some files cannot be deleted due to system restrictions")
            }
        } catch (error: Exception) {
            Log.e("DELETE_DEBUG", "Unable to delete media via contentResolver.delete", error)
            FirebaseCrashlytics.getInstance().recordException(error)
            viewModel.onMediaDeleteFailed()
            showDeleteError("Unable to delete files right now.")
        }
    }

    private fun isValidMediaStoreUri(uri: Uri): Boolean {
        if (uri == Uri.EMPTY) return false
        val normalized = uri.toString()
        if (normalized.startsWith("content://com.whatsapp")) return false
        if (normalized.startsWith("file://")) return false
        return normalized.startsWith("content://media/")
    }

    private fun showDeleteError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun shareText(text: String?) {
        val safeText = text?.takeIf { candidateText -> candidateText.isNotBlank() }
            ?: "Clean smarter. Free space instantly with ChatSweep."
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, safeText)
        }
        runCatching { startActivity(Intent.createChooser(intent, "Share ChatSweep")) }
            .onFailure { error -> Log.e(TAG, "Unable to share text.", error) }
    }

    private fun rateApp() {
        val appUri = Uri.parse("market://details?id=$packageName")
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, appUri)
        runCatching { startActivity(intent) }
            .onFailure { error ->
                Log.e(TAG, "Unable to open Play Store rating page.", error)
                runCatching { startActivity(Intent(Intent.ACTION_VIEW, webUri)) }
                    .onFailure { fallbackError -> Log.e(TAG, "Unable to open web Play Store page.", fallbackError) }
            }
    }

    private fun openManageSubscription() {
        // TODO: RE-ENABLE SUBSCRIPTION LATER
        /*
        val webUri = Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, webUri)) }
            .onFailure { error ->
                Log.e(TAG, "Unable to open subscription management page.", error)
                rateApp()
            }
        */
        rateApp()
    }

    private fun openUrl(url: String) {
        val safeUrl = url.takeIf { candidateUrl -> candidateUrl.isNotBlank() } ?: return
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl))) }
            .onFailure { error ->
                Log.e(TAG, "Unable to open url: $safeUrl", error)
                openSystemStorage()
            }
    }

    private fun sendEmail(address: String, subject: String) {
        val safeAddress = address.takeIf { candidateAddress -> candidateAddress.isNotBlank() } ?: return
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$safeAddress")
            putExtra(Intent.EXTRA_SUBJECT, subject.ifBlank { "ChatSweep support" })
        }
        try {
            startActivity(Intent.createChooser(intent, "Contact ChatSweep"))
        } catch (error: ActivityNotFoundException) {
            Log.e(TAG, "No email app available.", error)
            shareText("Reach us at $safeAddress")
        }
    }


    private fun showExitAdOncePerSession() {
        if (hasShownExitAdThisSession) {
            finish()
            return
        }
        hasShownExitAdThisSession = true
        adManager.showInterstitial(this) {
            finish()
        }
    }

    private fun showInterstitialIfReady(onDone: () -> Unit = {}) {
        adManager.showInterstitial(this) {
            onDone()
        }
    }

    private fun showRewardedForDeepClean() {
        if (viewModel.uiState.value.aiScanSummary.isRunning) return
        adManager.showRewarded(
            activity = this,
            onRewarded = {
                viewModel.unlockDeepCleanCredit()
                viewModel.runAiScan(isDeepClean = true)
            },
            onDismissed = { }
        )
    }

    private fun safeVersionLabel(): String {
        val versionName = BuildConfig.VERSION_NAME.takeIf { candidateName -> candidateName.isNotBlank() } ?: "1.0"
        val versionCode = BuildConfig.VERSION_CODE.takeIf { candidateCode -> candidateCode > 0 } ?: 1
        return "v$versionName ($versionCode)"
    }

    @Composable
    private fun MainActivityContent(versionLabel: String) {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val mediaItems by viewModel.items.collectAsStateWithLifecycle()
        val darkTheme = when (state.settings.themeMode) {
            AppThemeMode.DARK -> true
            AppThemeMode.LIGHT -> false
            AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
        val privacyPolicyUrl = "https://www.google.com/search?q=Cleanly+AI+privacy+policy"
        val faqUrl = "https://www.google.com/search?q=Cleanly+AI+FAQ"
        val latestShowInterstitialIfReady = rememberUpdatedState(newValue = { showInterstitialIfReady() })

        LaunchedEffect(state.shouldShowInterstitialForAiScan) {
            if (state.shouldShowInterstitialForAiScan && !state.aiScanSummary.isRunning) {
                latestShowInterstitialIfReady.value.invoke()
                viewModel.consumeAiScanInterstitialRequest()
            }
        }

        LaunchedEffect(state.shouldShowInterstitialForDelete) {
            if (state.shouldShowInterstitialForDelete && !state.isDeleteInProgress) {
                latestShowInterstitialIfReady.value.invoke()
                viewModel.consumeDeleteInterstitialRequest()
            }
        }

        LaunchedEffect(state.deleteRequestId, mediaItems.size) {
            if (state.pendingDeleteUris.isEmpty()) {
                Log.d(TAG, "Delete request id changed to ${state.deleteRequestId}, but pendingDeleteUris is empty.")
            }
        }
        LaunchedEffect(Unit) {
            ensureMediaAccessForSignedInUser()
        }

        WhatsCleanTheme(darkTheme = darkTheme) {
            WhatsCleanAppRoot(
                state = state,
                onRefreshClick = {
                    AnalyticsHelper.logScanStarted()
                    trackEvent(this@MainActivity, "scan_started")
                    viewModel.refreshMedia()
                },
                onAiScanClick = {
                    AnalyticsHelper.logScanStarted()
                    trackEvent(this@MainActivity, "scan_started")
                    viewModel.runAiScan()
                },
                onFilterChange = viewModel::setFilter,
                onSuggestionChange = viewModel::setSuggestion,
                onFrequencyChange = viewModel::setFrequency,
                onTimeChange = viewModel::setTime,
                onRemindersToggle = viewModel::toggleReminders,
                onOpenInSystem = { item -> openFileInSystem(item.uri) },
                onOpenSystemStorage = {
                    viewModel.onStorageScreenOpened()
                    openSystemStorage()
                },
                onRequestPermission = ::requestStoragePermissions,
                onSettingsOpened = viewModel::onSettingsOpened,
                onThemeSelected = viewModel::setThemeMode,
                onSmartAlertsToggle = viewModel::setSmartAlerts,
                onAutoCleanFrequencySelected = viewModel::setAutoCleanFrequency,
                onFileSizeFilterSelected = viewModel::setFileSizeFilter,
                onShowOnlyLargeToggle = viewModel::setShowOnlyLargeFiles,
                onIncludeScreenshotsToggle = viewModel::setIncludeScreenshots,
                onIncludeMemesToggle = viewModel::setIncludeMemes,
                onIncludeDuplicatesToggle = viewModel::setIncludeDuplicates,
                onUpgradeToPro = { viewModel.notePaywallViewed("settings_upgrade") },
                onRestorePurchase = viewModel::restorePurchases,
                onManageSubscription = ::openManageSubscription,
                onPurchasePlan = { product, source ->
                    // TODO: RE-ENABLE SUBSCRIPTION LATER
                    /*
                    viewModel.notePaywallViewed(source)
                    Log.d(TAG, "Paywall CTA clicked for product=${product.productId}, source=$source")
                    runCatching { subscriptionRepository.launchPurchase(this@MainActivity, product, source) }
                        .onFailure { error -> Log.e(TAG, "Unable to launch purchase flow.", error) }
                    */
                },
                onShareText = ::shareText,
                onShareResult = { shareText(viewModel.shareResultText()) },
                onInviteFriends = { shareText(viewModel.shareInviteText()) },
                onRateApp = ::rateApp,
                onPrivacyPolicy = { openUrl(privacyPolicyUrl) },
                onFaq = { openUrl(faqUrl) },
                onContactSupport = { sendEmail("support@cleanlyai.app", "ChatSweep support") },
                onReportIssue = { sendEmail("support@cleanlyai.app", "ChatSweep bug report") },
                onPremiumFeatureRequested = viewModel::onPremiumFeatureRequested,
                onDeleteClicked = { origin ->
                    AnalyticsHelper.logDelete(count = 0)
                    trackEvent(this@MainActivity, "delete_clicked")
                    viewModel.onDeleteClicked(origin)
                },
                onSmartCleanClicked = {
                    Log.d("TEST", "SMART CLEAN CLICKED")
                    Firebase.analytics.logEvent("smart_clean_clicked", null)
                    AnalyticsHelper.logSmartClean()
                    trackEvent(this@MainActivity, "smart_clean_clicked")
                },
                onAiToolOpened = { feature ->
                    Log.d("AI_TOOLS", "Opened AI tool: ${feature.name}")
                    AnalyticsHelper.logAITool(feature.name)
                    trackEvent(this@MainActivity, "ai_tool_opened")
                },
                onDeleteMediaRequest = { items, origin ->
                    Log.d("DELETE_FLOW", "Step 1: Delete button clicked")
                    AnalyticsHelper.logDelete(items.size)
                    trackEvent(this@MainActivity, "delete_clicked")
                    Log.d("DELETE_DEBUG", "Delete button click from $origin with ${items.size} selected items")
                    val rawUris = items.map { item -> item.uri }
                    rawUris.forEachIndexed { index, uri -> Log.d("DELETE_DEBUG", "Raw URI[$index]=$uri") }

                    when (val execution = viewModel.requestMediaDeletion(items, origin, Build.VERSION.SDK_INT)) {
                        is DeleteExecution.NeedsUserApproval -> {
                            val validUris = execution.uris.distinct()
                            if (validUris.isEmpty()) {
                                showDeleteError("Some files cannot be deleted due to system restrictions")
                                viewModel.onMediaDeleteCancelled()
                            } else {
                                Log.d("DELETE_DEBUG", "Valid MediaStore URI list size=${validUris.size}")
                                validUris.forEachIndexed { index, uri -> Log.d("DELETE_DEBUG", "Valid delete URI[$index]=$uri") }
                                Log.d("DELETE_FLOW", "Step 2.5: Calling launchDeleteRequest")
                                launchDeleteRequest(validUris)
                            }
                        }
                        is DeleteExecution.StartedInBackground -> {
                            val validUris = execution.uris.distinct()
                            if (validUris.isNotEmpty()) {
                                Log.d("DELETE_FLOW", "Step 2.5: Calling launchDeleteRequest")
                                launchDeleteRequest(validUris)
                            }
                        }
                        DeleteExecution.Ignored -> Unit
                    }
                },
                onUndoDelete = viewModel::undoLastDelete,
                onDeleteSnackbarConsumed = viewModel::clearDeleteSnackbar,
                onReviewClicked = viewModel::onReviewClicked,
                onCleanupRecorded = viewModel::recordCleanupResult,
                onDeepCleanWatchAd = ::showRewardedForDeepClean,
                onExitRequested = ::showExitAdOncePerSession,
                onDebugCrashTest = {
                    Log.d("CRASH", "Test crash triggered")
                    throw RuntimeException("Test Crash - Firebase Check")
                },
                versionLabel = versionLabel
            )
        }
    }
}
