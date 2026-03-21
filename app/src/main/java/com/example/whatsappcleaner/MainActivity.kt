package com.example.whatsappcleaner

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import com.example.whatsappcleaner.data.billing.SubscriptionRepository
import com.example.whatsappcleaner.ui.WhatsCleanAppRoot
import com.example.whatsappcleaner.ui.home.HomeViewModel
import com.example.whatsappcleaner.ui.settings.AppThemeMode
import com.example.whatsappcleaner.ui.theme.WhatsCleanTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()
    private val subscriptionRepository by lazy { SubscriptionRepository.get(this) }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.all { it.value }
            viewModel.updatePermissionStatus(granted)
            if (granted) {
                viewModel.refreshMedia()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscriptionRepository.start()
        syncPermissionState()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = when (state.settings.themeMode) {
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
                        subscriptionRepository.launchPurchase(this, product, source)
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
                    versionLabel = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        subscriptionRepository.refreshPurchases()
        syncPermissionState()
    }

    private fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun syncPermissionState() {
        val granted = requiredPermissions().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        viewModel.updatePermissionStatus(granted)
    }

    private fun requestStoragePermissions() {
        requestPermissionLauncher.launch(requiredPermissions())
    }

    private fun openFileInSystem(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { startActivity(Intent.createChooser(intent, "Open media")) }
            .onFailure { openSystemStorage() }
    }

    private fun openSystemStorage() {
        val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
        runCatching { startActivity(intent) }
            .onFailure { startActivity(Intent(Settings.ACTION_SETTINGS)) }
    }

    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Share Cleanly AI"))
    }

    private fun rateApp() {
        val appUri = Uri.parse("market://details?id=$packageName")
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, appUri)
        runCatching { startActivity(intent) }
            .onFailure { startActivity(Intent(Intent.ACTION_VIEW, webUri)) }
    }

    private fun openManageSubscription() {
        val webUri = Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, webUri)) }
            .onFailure { rateApp() }
    }

    private fun openUrl(url: String) {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            .onFailure { openSystemStorage() }
    }

    private fun sendEmail(address: String, subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$address")
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        try {
            startActivity(Intent.createChooser(intent, "Contact Cleanly AI"))
        } catch (_: ActivityNotFoundException) {
            shareText("Reach us at $address")
        }
    }
}
