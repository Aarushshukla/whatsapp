package com.example.whatsappcleaner

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatsappcleaner.data.billing.SubscriptionRepository
import com.example.whatsappcleaner.ui.WhatsCleanAppRoot
import com.example.whatsappcleaner.ui.home.HomeUiState
import com.example.whatsappcleaner.ui.home.HomeViewModel
import com.example.whatsappcleaner.ui.settings.AppThemeMode
import com.example.whatsappcleaner.ui.theme.WhatsCleanTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val viewModel: HomeViewModel by viewModels()
    private val subscriptionRepository by lazy(LazyThreadSafetyMode.NONE) { SubscriptionRepository.get(this) }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            object : ActivityResultCallback<Map<String, Boolean>> {
                override fun onActivityResult(permissions: Map<String, Boolean>) {
                    val granted = permissions.isNotEmpty() && permissions.values.all { it }
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
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called.")
        runCatching { subscriptionRepository.start() }
            .onFailure { error -> Log.e(TAG, "Unable to initialize subscriptions during onCreate.", error) }
        syncPermissionState()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle(initialValue = HomeUiState())
            val themeMode = state.settings.themeMode
            val darkTheme = when (themeMode) {
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            WhatsCleanTheme(darkTheme = darkTheme) {
                WhatsCleanAppRoot(
                    state = state,
                    onRefreshClick = { viewModel.refreshMedia() },
                    onFilterChange = { viewModel.setFilter(it) },
                    onSuggestionChange = { viewModel.setSuggestion(it) },
                    onFrequencyChange = { viewModel.setFrequency(it) },
                    onTimeChange = { viewModel.setTime(it) },
                    onRemindersToggle = { viewModel.toggleReminders(it) },
                    onOpenInSystem = { item -> openFileInSystem(item.uri) },
                    onOpenSystemStorage = {
                        viewModel.onStorageScreenOpened()
                        openSystemStorage()
                    },
                    onRequestPermission = { requestStoragePermissions() },
                    onSettingsOpened = { viewModel.onSettingsOpened() },
                    onThemeSelected = { viewModel.setThemeMode(it) },
                    onSmartAlertsToggle = { viewModel.setSmartAlerts(it) },
                    onAutoCleanFrequencySelected = { viewModel.setAutoCleanFrequency(it) },
                    onFileSizeFilterSelected = { viewModel.setFileSizeFilter(it) },
                    onShowOnlyLargeToggle = { viewModel.setShowOnlyLargeFiles(it) },
                    onIncludeScreenshotsToggle = { viewModel.setIncludeScreenshots(it) },
                    onIncludeMemesToggle = { viewModel.setIncludeMemes(it) },
                    onIncludeDuplicatesToggle = { viewModel.setIncludeDuplicates(it) },
                    onUpgradeToPro = { viewModel.notePaywallViewed("settings_upgrade") },
                    onRestorePurchase = { source -> viewModel.restorePurchases(source) },
                    onManageSubscription = { openManageSubscription() },
                    onPurchasePlan = { product, source ->
                        viewModel.notePaywallViewed(source)
                        runCatching { subscriptionRepository.launchPurchase(this, product, source) }
                            .onFailure { error -> Log.e(TAG, "Unable to launch purchase flow.", error) }
                    },
                    onShareText = { text -> shareText(text) },
                    onShareResult = { shareText(viewModel.shareResultText()) },
                    onInviteFriends = { shareText(viewModel.shareInviteText()) },
                    onRateApp = { rateApp() },
                    onPrivacyPolicy = { openUrl("https://www.google.com/search?q=Cleanly+AI+privacy+policy") },
                    onFaq = { openUrl("https://www.google.com/search?q=Cleanly+AI+FAQ") },
                    onContactSupport = { sendEmail("support@cleanlyai.app", "Cleanly AI support") },
                    onReportIssue = { sendEmail("support@cleanlyai.app", "Cleanly AI bug report") },
                    onPremiumFeatureRequested = { viewModel.onPremiumFeatureRequested(it) },
                    onDeleteClicked = { viewModel.onDeleteClicked(it) },
                    onReviewClicked = { viewModel.onReviewClicked() },
                    onCleanupRecorded = { bytes -> viewModel.recordCleanupResult(bytes) },
                    versionLabel = safeVersionLabel()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called. Refreshing purchases and permission state.")
        runCatching { subscriptionRepository.refreshPurchases() }
            .onFailure { error -> Log.e(TAG, "Unable to refresh purchases on resume.", error) }
        syncPermissionState()
    }

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    private fun syncPermissionState() {
        val permissions = requiredPermissions()
        val granted = permissions.isNotEmpty() && permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        viewModel.updatePermissionStatus(granted)
    }

    private fun requestStoragePermissions() {
        val permissions = requiredPermissions()
        if (permissions.isEmpty()) {
            Log.w(TAG, "No storage permissions are required on this device configuration.")
            viewModel.updatePermissionStatus(true)
            viewModel.refreshMedia()
            return
        }
        val alreadyGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
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

    private fun shareText(text: String?) {
        val safeText = text?.takeIf { it.isNotBlank() }
            ?: "Clean smarter. Free space instantly with Cleanly AI."
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, safeText)
        }
        runCatching { startActivity(Intent.createChooser(intent, "Share Cleanly AI")) }
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
        val webUri = Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, webUri)) }
            .onFailure { error ->
                Log.e(TAG, "Unable to open subscription management page.", error)
                rateApp()
            }
    }

    private fun openUrl(url: String) {
        val safeUrl = url.takeIf { it.isNotBlank() } ?: return
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl))) }
            .onFailure { error ->
                Log.e(TAG, "Unable to open url: $safeUrl", error)
                openSystemStorage()
            }
    }

    private fun sendEmail(address: String, subject: String) {
        val safeAddress = address.takeIf { it.isNotBlank() } ?: return
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$safeAddress")
            putExtra(Intent.EXTRA_SUBJECT, subject.ifBlank { "Cleanly AI support" })
        }
        try {
            startActivity(Intent.createChooser(intent, "Contact Cleanly AI"))
        } catch (error: ActivityNotFoundException) {
            Log.e(TAG, "No email app available.", error)
            shareText("Reach us at $safeAddress")
        }
    }

    private fun safeVersionLabel(): String {
        val versionName = BuildConfig.VERSION_NAME.takeIf { it.isNotBlank() } ?: "1.0"
        val versionCode = BuildConfig.VERSION_CODE.takeIf { it > 0 } ?: 1
        return "v$versionName ($versionCode)"
    }
}
