package com.example.whatsappcleaner

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.app.RecoverableSecurityException
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatsappcleaner.data.billing.SubscriptionRepository
import com.example.whatsappcleaner.ui.WhatsCleanAppRoot
import com.example.whatsappcleaner.ui.home.HomeViewModel
import com.example.whatsappcleaner.ui.settings.AppThemeMode
import com.example.whatsappcleaner.ui.theme.WhatsCleanTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val viewModel: HomeViewModel by viewModels()
    private val subscriptionRepository by lazy(LazyThreadSafetyMode.NONE) { SubscriptionRepository.get(this) }
    private lateinit var deleteLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var pendingDeleteFromRecoverableFlow: Boolean = false

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

    private fun setupDeleteLauncher() {
        Log.d(TAG, "Initializing deleteLauncher with StartIntentSenderForResult contract.")
        deleteLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            try {
                val approved = result.resultCode == android.app.Activity.RESULT_OK
                if (approved) {
                    Log.d("DELETE_DEBUG", "Delete success")
                } else {
                    Log.e("DELETE_DEBUG", "Delete cancelled")
                }
                Log.d(TAG, "Delete request finished. resultCode=${result.resultCode}, approved=$approved")
                val pendingUris = viewModel.uiState.value.pendingDeleteUris
                Log.d(
                    TAG,
                    "Delete launcher callback. resultCode=${result.resultCode}, sdk=${Build.VERSION.SDK_INT}, recoverableFlow=$pendingDeleteFromRecoverableFlow, pending=${pendingUris.size}"
                )
                pendingUris.forEachIndexed { index, uri ->
                    Log.d("DELETE_DEBUG", "Delete result URI[$index]=$uri, resultCode=${result.resultCode}")
                }
                if (approved && pendingDeleteFromRecoverableFlow) {
                    val retryDeletedCount = pendingUris.distinct().count { uri ->
                        Log.d("DELETE_DEBUG", "Retry delete on approved URI: $uri")
                        runCatching { contentResolver.delete(uri, null, null) > 0 }
                            .onFailure { error ->
                                Log.e(TAG, "Retry delete failed after RecoverableSecurityException approval. uri=$uri", error)
                            }
                            .getOrDefault(false)
                    }
                    Log.d(TAG, "Recoverable retry delete result. deletedCount=$retryDeletedCount")
                }
                val deletedUris = if (approved) pendingUris.filterNot(::uriExists) else emptyList()
                val notDeletedUris = if (approved) pendingUris.filter(::uriExists) else pendingUris
                pendingDeleteFromRecoverableFlow = false

                if (approved) {
                    Log.d(TAG, "Verified deletion results: deleted=${deletedUris.size}, notDeleted=${notDeletedUris.size}")
                    if (notDeletedUris.isNotEmpty()) {
                        Log.w(TAG, "Some URIs were not removed after approval: ${notDeletedUris.joinToString()}")
                    }
                } else {
                    Log.d(TAG, "Delete request was cancelled or denied by user.")
                }

                if (approved) {
                    viewModel.onMediaDeleteResult(success = deletedUris.isNotEmpty(), deletedCount = deletedUris.size)
                    Log.d(TAG, "Delete request approved. Scheduling silent background sync.")
                    viewModel.refreshMedia(forceRefresh = true, showLoading = false)
                } else {
                    viewModel.onMediaDeleteCancelled()
                }
            } catch (error: Exception) {
                Log.e(TAG, "Failed while handling delete launcher result.", error)
                viewModel.onMediaDeleteResult(success = false)
                showDeleteError("Unable to process delete result.")
            }
        }
        Log.d(TAG, "deleteLauncher initialized.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called.")
        setupDeleteLauncher()
        runCatching { subscriptionRepository.start(this) }
            .onFailure { error -> Log.e(TAG, "Unable to initialize subscriptions during onCreate.", error) }
        syncPermissionState()
        setContent {
            MainActivityContent(
                activity = this,
                versionLabel = safeVersionLabel()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called. Refreshing purchases and permission state.")
        runCatching {
            subscriptionRepository.start(this)
            subscriptionRepository.refreshPurchases()
        }
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
        val granted = permissions.isNotEmpty() && permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
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

    fun deleteMedia(uris: List<Uri>) {
        Log.d("DELETE_DEBUG", "deleteMedia called. sdk=${Build.VERSION.SDK_INT}, uriCount=${uris.size}")
        uris.forEachIndexed { index, uri ->
            Log.d("DELETE_DEBUG", "URI[$index]=$uri")
        }
        if (uris.isEmpty()) {
            Log.e("DELETE_DEBUG", "Empty list")
            viewModel.onMediaDeleteResult(success = false)
            showDeleteError("No files selected for deletion.")
            return
        }
        val validUris = uris
            .mapNotNull { uri ->
                Log.d("DELETE_DEBUG", "Checking URI before delete request: $uri")
                uri.takeIf { candidate ->
                    val isValid = isValidMediaStoreUri(candidate)
                    if (!isValid) {
                        Log.w("DELETE_DEBUG", "Skipping invalid delete URI: $candidate")
                    }
                    isValid
                }
            }
            .distinct()
        if (validUris.isEmpty()) {
            Log.w("DELETE_DEBUG", "No valid MediaStore URIs available for delete request.")
            viewModel.onMediaDeleteResult(success = false)
            showDeleteError("This file cannot be deleted due to system restrictions")
            return
        }

        val sdkInt = Build.VERSION.SDK_INT
        when {
            sdkInt >= Build.VERSION_CODES.R -> {
                try {
                    Log.d("DELETE_DEBUG", "Android 11+ flow: MediaStore.createDeleteRequest for ${validUris.size} URIs")
                    val request = MediaStore.createDeleteRequest(contentResolver, validUris)
                    val intentSenderRequest = IntentSenderRequest.Builder(request.intentSender).build()
                    if (!::deleteLauncher.isInitialized) {
                        Log.e("DELETE_DEBUG", "deleteLauncher is not initialized; cannot launch request")
                        viewModel.onMediaDeleteResult(success = false)
                        showDeleteError("Delete action is unavailable right now.")
                        return
                    }
                    pendingDeleteFromRecoverableFlow = false
                    deleteLauncher.launch(intentSenderRequest)
                    Log.d("DELETE_DEBUG", "deleteLauncher.launch() executed for Android 11+ delete request")
                } catch (error: Exception) {
                    Log.e("DELETE_DEBUG", "Unable to launch MediaStore delete request", error)
                    viewModel.onMediaDeleteResult(success = false)
                    showDeleteError("Unable to delete files right now.")
                }
            }
            sdkInt == Build.VERSION_CODES.Q -> {
                deleteOnAndroid10(validUris)
            }
            else -> {
                deleteOnAndroid9AndBelow(validUris)
            }
        }
    }

    private fun deleteOnAndroid10(validUris: List<Uri>) {
        Log.d(TAG, "Android 10 flow started for ${validUris.size} URIs")
        var deletedCount = 0
        validUris.forEach { uri ->
            try {
                Log.d("DELETE_DEBUG", "Deleting URI on Android 10 flow: $uri")
                val rows = contentResolver.delete(uri, null, null)
                Log.d(TAG, "Android 10 delete result. uri=$uri, rows=$rows")
                if (rows > 0) deletedCount++
            } catch (recoverable: RecoverableSecurityException) {
                Log.w(TAG, "RecoverableSecurityException while deleting uri=$uri. Launching user action.")
                if (!::deleteLauncher.isInitialized) {
                    Log.e(TAG, "deleteLauncher not initialized for RecoverableSecurityException flow")
                    viewModel.onMediaDeleteResult(success = false)
                    showDeleteError("Delete action is unavailable right now.")
                    return
                }
                pendingDeleteFromRecoverableFlow = true
                runCatching {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(recoverable.userAction.actionIntent.intentSender).build()
                    deleteLauncher.launch(intentSenderRequest)
                }.onFailure { error ->
                    Log.e(TAG, "Unable to launch RecoverableSecurityException intent sender for uri=$uri", error)
                    pendingDeleteFromRecoverableFlow = false
                    viewModel.onMediaDeleteResult(success = false)
                    showDeleteError("Unable to request permission to delete file.")
                }
                return
            } catch (error: Exception) {
                Log.e(TAG, "Android 10 delete failed. uri=$uri", error)
            }
        }
        finalizeDirectDeleteResult(deletedCount, validUris.size, "Android 10 direct delete")
    }

    private fun deleteOnAndroid9AndBelow(validUris: List<Uri>) {
        Log.d(TAG, "Android 9 and below flow started for ${validUris.size} URIs")
        val deletedCount = validUris.count { uri ->
            Log.d("DELETE_DEBUG", "Deleting URI on Android 9- flow: $uri")
            runCatching {
                val rows = contentResolver.delete(uri, null, null)
                Log.d(TAG, "Android 9- delete result. uri=$uri, rows=$rows")
                rows > 0
            }.onFailure { error ->
                Log.e(TAG, "Android 9- delete failed. uri=$uri", error)
            }.getOrDefault(false)
        }
        finalizeDirectDeleteResult(deletedCount, validUris.size, "Android 9- direct delete")
    }

    private fun finalizeDirectDeleteResult(deletedCount: Int, totalCount: Int, source: String) {
        Log.d(TAG, "$source completed. deleted=$deletedCount/$totalCount")
        if (deletedCount <= 0) {
            Log.w(TAG, "$source failed. No files deleted.")
            viewModel.onMediaDeleteResult(success = false)
            showDeleteError("Failed to delete file(s).")
            return
        }
        viewModel.onMediaDeleteResult(success = true, deletedCount = deletedCount)
        viewModel.refreshMedia()
        if (deletedCount < totalCount) {
            showDeleteError("Some files could not be deleted.")
        }
    }

    private fun isValidMediaStoreUri(uri: Uri): Boolean {
        if (uri == Uri.EMPTY) return false
        val normalized = uri.toString()
        if (normalized.startsWith("content://com.whatsapp")) return false
        if (normalized.startsWith("file://")) return false
        return normalized.startsWith("content://media/external/")
    }

    private fun showDeleteError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun uriExists(uri: Uri): Boolean {
        return runCatching {
            contentResolver.openAssetFileDescriptor(uri, "r")?.use { true } ?: false
        }.getOrElse {
            false
        }
    }

    private fun shareText(text: String?) {
        val safeText = text?.takeIf { candidateText -> candidateText.isNotBlank() }
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
        val versionName = BuildConfig.VERSION_NAME.takeIf { candidateName -> candidateName.isNotBlank() } ?: "1.0"
        val versionCode = BuildConfig.VERSION_CODE.takeIf { candidateCode -> candidateCode > 0 } ?: 1
        return "v$versionName ($versionCode)"
    }

    @Composable
    private fun MainActivityContent(activity: MainActivity, versionLabel: String) {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val mediaItems by viewModel.items.collectAsStateWithLifecycle()
        val darkTheme = when (state.settings.themeMode) {
            AppThemeMode.DARK -> true
            AppThemeMode.LIGHT -> false
            AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
        val privacyPolicyUrl = "https://www.google.com/search?q=Cleanly+AI+privacy+policy"
        val faqUrl = "https://www.google.com/search?q=Cleanly+AI+FAQ"

        androidx.compose.runtime.LaunchedEffect(state.deleteRequestId, mediaItems.size) {
            if (state.pendingDeleteUris.isEmpty()) {
                Log.d(TAG, "Delete request id changed to ${state.deleteRequestId}, but pendingDeleteUris is empty.")
            }
        }

        WhatsCleanTheme(darkTheme = darkTheme) {
            WhatsCleanAppRoot(
                state = state,
                onRefreshClick = viewModel::refreshMedia,
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
                    viewModel.notePaywallViewed(source)
                    Log.d(TAG, "Paywall CTA clicked for product=${product.productId}, source=$source")
                    runCatching { subscriptionRepository.launchPurchase(this@MainActivity, product, source) }
                        .onFailure { error -> Log.e(TAG, "Unable to launch purchase flow.", error) }
                },
                onShareText = ::shareText,
                onShareResult = { shareText(viewModel.shareResultText()) },
                onInviteFriends = { shareText(viewModel.shareInviteText()) },
                onRateApp = ::rateApp,
                onPrivacyPolicy = { openUrl(privacyPolicyUrl) },
                onFaq = { openUrl(faqUrl) },
                onContactSupport = { sendEmail("support@cleanlyai.app", "Cleanly AI support") },
                onReportIssue = { sendEmail("support@cleanlyai.app", "Cleanly AI bug report") },
                onPremiumFeatureRequested = viewModel::onPremiumFeatureRequested,
                onDeleteClicked = viewModel::onDeleteClicked,
                onDeleteMediaRequest = { items, origin ->
                    Log.d("DELETE_DEBUG", "Delete button click from $origin with ${items.size} selected items")
                    val rawUris = items.map { item -> item.uri }
                    rawUris.forEachIndexed { index, uri -> Log.d("DELETE_DEBUG", "Raw URI[$index]=$uri") }

                    val validItems = items.filter { item -> isValidMediaStoreUri(item.uri) }
                    val validUris = validItems.map { item -> item.uri }.distinct()
                    Log.d("DELETE_DEBUG", "Valid MediaStore URI list size=${validUris.size}")
                    validUris.forEachIndexed { index, uri -> Log.d("DELETE_DEBUG", "Valid delete URI[$index]=$uri") }

                    if (validUris.isEmpty()) {
                        showDeleteError("This file cannot be deleted due to system restrictions")
                    } else {
                        viewModel.requestMediaDeletion(validItems, origin)
                        activity.deleteMedia(validUris)
                    }
                },
                onUndoDelete = viewModel::undoLastDelete,
                onDeleteSnackbarConsumed = viewModel::clearDeleteSnackbar,
                onReviewClicked = viewModel::onReviewClicked,
                onCleanupRecorded = viewModel::recordCleanupResult,
                versionLabel = versionLabel
            )
        }
    }

}
