package com.example.whatsappcleaner.ui.home

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsappcleaner.ai.ImageCategory
import com.example.whatsappcleaner.ai.MemeClassifier
import com.example.whatsappcleaner.ai.PhoneRealityAnalyzer
import com.example.whatsappcleaner.ai.SpamMediaAnalyzer
import com.example.whatsappcleaner.ai.StorageReport
import com.example.whatsappcleaner.ai.SmartJunkAnalyzer
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import com.example.whatsappcleaner.data.analytics.AppAnalytics
import com.example.whatsappcleaner.data.billing.SubscriptionRepository
import com.example.whatsappcleaner.data.billing.SubscriptionState
import com.example.whatsappcleaner.data.local.MediaLoader
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.UserPrefs
import com.example.whatsappcleaner.data.local.formatSize
import kotlin.math.abs
import com.example.whatsappcleaner.reminder.ReminderScheduler
import com.example.whatsappcleaner.ui.settings.AppThemeMode
import com.example.whatsappcleaner.ui.settings.ReminderFrequencyOption
import com.example.whatsappcleaner.ui.settings.SettingsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.annotation.MainThread
import kotlinx.coroutines.delay

enum class PremiumFeature(val analyticsKey: String, val paywallSource: String) {
    SMART_CLEAN_ADVANCED("smart_clean_clicked", "smart_clean_advanced"),
    DUPLICATE_DETECTION("duplicate_detection", "duplicate_detection"),
    MEME_DETECTION("meme_detection", "meme_detection"),
    BULK_DELETE("bulk_delete", "bulk_delete"),
    AUTO_CLEAN("auto_clean", "auto_clean"),
    ADVANCED_ANALYTICS("advanced_analytics", "advanced_analytics"),
    AI_TOOLS("ai_tools", "ai_tools")
}

data class HomeUiState(
    val allItems: List<SimpleMediaItem> = emptyList(),
    val filteredItems: List<SimpleMediaItem> = emptyList(),
    val summaryInfo: String = "Loading...",
    val permissionGranted: Boolean = false,
    val isLoading: Boolean = true,
    val currentFilter: MediaFilter = MediaFilter.ALL,
    val activeSuggestion: SuggestionType = SuggestionType.NONE,
    val largeTodayCount: Int = 0,
    val largeTodaySizeText: String = "",
    val screenshotTodayCount: Int = 0,
    val screenshotTodaySizeText: String = "",
    val remindersEnabled: Boolean = true,
    val selectedFrequency: ReminderFreq = ReminderFreq("Every day", 1),
    val selectedTime: ReminderTime = ReminderTime("09:00", 9, 0),
    val timeOptions: List<ReminderTime> = generateTimeOptions(),
    val memeCount: Int = 0,
    val memeItems: List<SimpleMediaItem> = emptyList(),
    val junkCount: Int = 0,
    val duplicateCount: Int = 0,
    val spamCount: Int = 0,
    val totalFiles: Int = 0,
    val totalSize: Long = 0L,
    val duplicateItems: List<SimpleMediaItem> = emptyList(),
    val spamItems: List<SimpleMediaItem> = emptyList(),
    val largeFileItems: List<SimpleMediaItem> = emptyList(),
    val sentFileItems: List<SimpleMediaItem> = emptyList(),
    val smartSuggestionSummary: SmartSuggestionSummary = SmartSuggestionSummary(),
    val smartSuggestedItems: List<SimpleMediaItem> = emptyList(),
    val suggestionReasonsByUri: Map<String, List<String>> = emptyMap(),
    val report: StorageReport = StorageReport(0, 0, 0, 0, 0, 0),
    val settings: SettingsUiState = SettingsUiState(),
    val subscriptionState: SubscriptionState = SubscriptionState(),
    val paywallSource: String = "dashboard",
    val hasExceededFreeLimit: Boolean = false,
    val lastCleanupBytes: Long = 0L,
    val pendingDeleteIds: Set<Long> = emptySet(),
    val pendingDeleteUris: List<Uri> = emptyList(),
    val pendingDeleteItems: List<SimpleMediaItem> = emptyList(),
    val deleteRequestId: Long = 0L,
    val isDeleteInProgress: Boolean = false,
    val deleteSnackbarMessage: String? = null,
    val lastDeletedItems: List<SimpleMediaItem> = emptyList(),
    val duplicateGroups: List<List<SimpleMediaItem>> = emptyList(),
    val oldFileItems: List<SimpleMediaItem> = emptyList(),
    val whatsappJunkItems: List<SimpleMediaItem> = emptyList(),
    val blurryImageItems: List<SimpleMediaItem> = emptyList(),
    val aiScanSummary: AiScanSummary = AiScanSummary(),
    val shouldShowInterstitialForAiScan: Boolean = false,
    val deepCleanCredits: Int = 0,
    val shouldShowInterstitialForDelete: Boolean = false
) {
    // TODO: RE-ENABLE SUBSCRIPTION LATER
    /*
    val isProUser: Boolean get() = subscriptionState.isProUser
    */
    val isProUser: Boolean get() = true
    val isDeleting: Boolean get() = isDeleteInProgress
}

data class SmartSuggestionSummary(
    val duplicateFiles: Int = 0,
    val duplicateGroups: Int = 0,
    val largeFiles: Int = 0,
    val oldFiles: Int = 0,
    val totalSuggestedFiles: Int = 0,
    val totalSpaceToFree: Long = 0L
)

data class AiScanSummary(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val status: String = "Idle",
    val cleanableBytes: Long = 0L,
    val junkCount: Int = 0
)

sealed class DeleteExecution {
    data class NeedsUserApproval(val uris: List<Uri>) : DeleteExecution()
    data class StartedInBackground(val uris: List<Uri>) : DeleteExecution()
    data object Ignored : DeleteExecution()
}

fun generateTimeOptions(): List<ReminderTime> = (0..23).map { hour ->
    ReminderTime("%02d:00".format(hour), hour, 0)
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val INITIAL_LOAD_LIMIT = 100
        private const val LARGE_FILE_THRESHOLD_BYTES = 10L * 1024L * 1024L
        private const val OLD_FILE_AGE_DAYS = 30L
    }

    private val mediaLoader = MediaLoader(application)
    private val appContext = application.applicationContext
    private val smartJunkAnalyzer = SmartJunkAnalyzer()
    private val spamMediaAnalyzer = SpamMediaAnalyzer()
    private val phoneRealityAnalyzer = PhoneRealityAnalyzer()
    private val prefs = UserPrefs.get(application)
    private val analytics = AppAnalytics.get(application)
    private val subscriptionRepository = SubscriptionRepository.get(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val _items = MutableStateFlow<List<SimpleMediaItem>>(emptyList())
    val items: StateFlow<List<SimpleMediaItem>> = _items.asStateFlow()
    private var hasLoadedInitialCache = false
    private var refreshInProgress = false

    private data class MediaComputation(
        val allItems: List<SimpleMediaItem>,
        val filteredItems: List<SimpleMediaItem>,
        val summaryInfo: String,
        val largeTodayCount: Int,
        val largeTodaySizeText: String,
        val screenshotTodayCount: Int,
        val screenshotTodaySizeText: String,
        val memeCount: Int,
        val memeItems: List<SimpleMediaItem>,
        val junkCount: Int,
        val duplicateCount: Int,
        val spamCount: Int,
        val totalFiles: Int,
        val totalSize: Long,
        val duplicateItems: List<SimpleMediaItem>,
        val spamItems: List<SimpleMediaItem>,
        val largeFileItems: List<SimpleMediaItem>,
        val sentFileItems: List<SimpleMediaItem>,
        val smartSuggestionSummary: SmartSuggestionSummary,
        val smartSuggestedItems: List<SimpleMediaItem>,
        val suggestionReasonsByUri: Map<String, List<String>>,
        val report: StorageReport,
        val duplicateGroups: List<List<SimpleMediaItem>>,
        val oldFileItems: List<SimpleMediaItem>,
        val whatsappJunkItems: List<SimpleMediaItem>
    )

    init {
        loadPreferences()
        // TODO: RE-ENABLE SUBSCRIPTION LATER
        /*
        subscriptionRepository.start()
        subscriptionRepository.refreshPurchases()
        viewModelScope.launch {
            subscriptionRepository.state.collectLatest { subscriptionState ->
                if (subscriptionState.isProUser) {
                    prefs.resetFreePremiumAttempts()
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        subscriptionState = subscriptionState,
                        hasExceededFreeLimit = if (subscriptionState.isProUser) false else prefs.getFreePremiumAttempts() >= 2
                    )
                }
            }
        }
        */
    }

    fun updatePermissionStatus(granted: Boolean) {
        Log.d(TAG, "Permission state updated: granted=$granted")
        _uiState.update { currentState -> currentState.copy(permissionGranted = granted) }
        if (!granted) _uiState.update { currentState ->
            currentState.copy(summaryInfo = "Permission needed to scan.", isLoading = false)
        }
    }

    fun refreshMedia(forceRefresh: Boolean = false, showLoading: Boolean = true) {
        if (!_uiState.value.permissionGranted) return
        viewModelScope.launch(Dispatchers.IO) {
            if (refreshInProgress) {
                Log.d(TAG, "Skipping refresh request because a scan is already in progress.")
                return@launch
            }
            if (hasLoadedInitialCache && !forceRefresh && _uiState.value.allItems.isNotEmpty()) {
                Log.d(TAG, "Using cached media list; skipping full rescan.")
                withContext(Dispatchers.Main) {
                    _uiState.update { current ->
                        current.copy(
                            filteredItems = filterList(current.allItems, current.currentFilter, current.activeSuggestion, current.settings),
                            summaryInfo = "Found ${current.allItems.size} files (${formatSize(current.totalSize)})",
                            isLoading = false
                        )
                    }
                }
                return@launch
            }
            refreshInProgress = true
            Log.d(TAG, "Refreshing media library.")
            val loadStartedAt = System.currentTimeMillis()
            if (showLoading) {
                withContext(Dispatchers.Main) {
                    _uiState.update { currentState -> currentState.copy(summaryInfo = "Scanning files...", isLoading = true) }
                }
            }
            try {
                val images = mediaLoader.loadAllDeviceMedia("image", limit = INITIAL_LOAD_LIMIT)
                val videos = mediaLoader.loadAllDeviceMedia("video", limit = INITIAL_LOAD_LIMIT)
                val initialItems = (images + videos).sortedByDescending { mediaItem -> mediaItem.addedMillis }
                if (showLoading) {
                    ensureMinimumLoadingDuration(loadStartedAt)
                }
                applyLoadedMediaState(initialItems)
                hasLoadedInitialCache = true

                val fullImages = mediaLoader.loadAllDeviceMedia("image")
                val fullVideos = mediaLoader.loadAllDeviceMedia("video")
                val allItems = (fullImages + fullVideos).sortedByDescending { mediaItem -> mediaItem.addedMillis }

                if (allItems.size != initialItems.size) {
                    applyLoadedMediaState(allItems)
                }
                hasLoadedInitialCache = true
            } catch (error: SecurityException) {
                Log.e(TAG, "Media scan failed due to permission issue.", error)
                withContext(Dispatchers.Main) {
                    _uiState.update { currentState ->
                        currentState.copy(summaryInfo = "Scan failed: permission denied", isLoading = false)
                    }
                }
            } catch (error: Exception) {
                Log.e(TAG, "Media scan failed.", error)
                withContext(Dispatchers.Main) {
                    _uiState.update { currentState ->
                        currentState.copy(summaryInfo = "Scan failed: ${error.message ?: "Unknown error"}", isLoading = false)
                    }
                }
            } finally {
                refreshInProgress = false
            }
        }
    }

    private suspend fun ensureMinimumLoadingDuration(startedAtMillis: Long) {
        val minimumDurationMillis = 800L
        val elapsed = System.currentTimeMillis() - startedAtMillis
        if (elapsed < minimumDurationMillis) {
            delay(minimumDurationMillis - elapsed)
        }
    }

    private suspend fun applyLoadedMediaState(allItems: List<SimpleMediaItem>) {
        val memes = withContext(Dispatchers.IO) {
            val classifier = MemeClassifier(getApplication())
            try {
                val memeItems = ArrayList<SimpleMediaItem>(allItems.size / 4)
                allItems.forEach { item ->
                    if (isMeme(item, classifier)) memeItems.add(item)
                }
                memeItems
            } finally {
                classifier.close()
            }
        }

        val computedState = withContext(Dispatchers.Default) {
            val junkBreakdown = smartJunkAnalyzer.buildBreakdown(allItems)
            val junkItems = smartJunkAnalyzer.findJunk(allItems)
            val spamItems = spamMediaAnalyzer.findSpamMedia(allItems)
            val baseReport = phoneRealityAnalyzer.generateReport(allItems)
            val totalSize = allItems.sumOf { mediaItem -> mediaItem.sizeKb.toLong() * 1024L }
            val now = System.currentTimeMillis()
            val oldFileCutoff = now - (OLD_FILE_AGE_DAYS * 24L * 60L * 60L * 1000L)
            val duplicateGroups = allItems
                .groupBy { mediaItem -> "${mediaItem.name.lowercase()}_${mediaItem.size}" }
                .values
                .filter { groupedItems -> groupedItems.size > 1 }
            val duplicateSuggestedItems = duplicateGroups.flatten()
            val largeSuggestedItems = allItems
                .filter { mediaItem -> mediaItem.size > LARGE_FILE_THRESHOLD_BYTES }
                .sortedByDescending { mediaItem -> mediaItem.size }
            val oldSuggestedItems = allItems.filter { mediaItem -> mediaItem.addedMillis < oldFileCutoff }
            val whatsappJunkItems = allItems.filter { mediaItem ->
                mediaItem.mimeType?.startsWith("image") == true &&
                    mediaItem.size < 200L * 1024L &&
                    (mediaItem.uri.toString().contains("whatsapp", ignoreCase = true) || mediaItem.name.contains("IMG-", ignoreCase = true))
            }

            val suggestedReasonMap = mutableMapOf<String, MutableSet<String>>()
            duplicateSuggestedItems.forEach { item ->
                suggestedReasonMap.getOrPut(item.uri.toString()) { linkedSetOf() }
                    .add("Duplicate name")
            }
            largeSuggestedItems.forEach { item ->
                suggestedReasonMap.getOrPut(item.uri.toString()) { linkedSetOf() }
                    .add("Large file (>10MB)")
            }
            oldSuggestedItems.forEach { item ->
                suggestedReasonMap.getOrPut(item.uri.toString()) { linkedSetOf() }
                    .add("Old file (>30 days)")
            }
            val smartSuggestedItems = allItems.filter { item -> suggestedReasonMap.containsKey(item.uri.toString()) }
            val smartSuggestionSummary = SmartSuggestionSummary(
                duplicateFiles = duplicateSuggestedItems.size,
                duplicateGroups = duplicateGroups.size,
                largeFiles = largeSuggestedItems.size,
                oldFiles = oldSuggestedItems.size,
                totalSuggestedFiles = smartSuggestedItems.size,
                totalSpaceToFree = smartSuggestedItems.sumOf { item -> item.size }
            )
            val summary = if (allItems.isEmpty()) {
                Log.d(TAG, "No media items found on device.")
                "No media found."
            } else {
                Log.d(TAG, "Loaded ${allItems.size} media items.")
                "Found ${allItems.size} files (${formatSize(totalSize)})"
            }
            val today = System.currentTimeMillis() - 86400000
            val largeItems = allItems.filter { mediaItem -> mediaItem.addedMillis > today && mediaItem.sizeKb > 5000 }
            val screenshots = allItems.filter { mediaItem -> mediaItem.addedMillis > today && mediaItem.name.startsWith("Screenshot", true) }
            val filteredItems = filterList(allItems, _uiState.value.currentFilter, _uiState.value.activeSuggestion, _uiState.value.settings)

            MediaComputation(
                allItems = allItems,
                filteredItems = filteredItems,
                summaryInfo = summary,
                largeTodayCount = largeItems.size,
                largeTodaySizeText = formatSize(largeItems.sumOf { i -> i.sizeKb.toLong() * 1024 }),
                screenshotTodayCount = screenshots.size,
                screenshotTodaySizeText = formatSize(screenshots.sumOf { i -> i.sizeKb.toLong() * 1024 }),
                memeCount = memes.size,
                memeItems = memes,
                junkCount = junkItems.size,
                duplicateCount = junkBreakdown.duplicates.size,
                spamCount = spamItems.size,
                totalFiles = allItems.size,
                totalSize = totalSize,
                duplicateItems = junkBreakdown.duplicates,
                spamItems = spamItems,
                largeFileItems = junkBreakdown.largeFiles,
                sentFileItems = junkBreakdown.sentFiles,
                smartSuggestionSummary = smartSuggestionSummary,
                smartSuggestedItems = smartSuggestedItems,
                suggestionReasonsByUri = suggestedReasonMap.mapValues { entry -> entry.value.toList() },
                report = baseReport.copy(
                    memeCount = memes.size,
                    duplicateCount = junkBreakdown.duplicates.size,
                    spamCount = spamItems.size
                ),
                duplicateGroups = duplicateGroups,
                oldFileItems = oldSuggestedItems.sortedBy { mediaItem -> mediaItem.addedMillis },
                whatsappJunkItems = whatsappJunkItems
            )
        }

        applyComputedState(computedState)
    }

    @MainThread
    private suspend fun applyComputedState(computed: MediaComputation) {
        withContext(Dispatchers.Main) {
            _uiState.update { current ->
                current.copy(
                    allItems = computed.allItems,
                    filteredItems = computed.filteredItems,
                    summaryInfo = computed.summaryInfo,
                    isLoading = false,
                    largeTodayCount = computed.largeTodayCount,
                    largeTodaySizeText = computed.largeTodaySizeText,
                    screenshotTodayCount = computed.screenshotTodayCount,
                    screenshotTodaySizeText = computed.screenshotTodaySizeText,
                    memeCount = computed.memeCount,
                    memeItems = computed.memeItems,
                    junkCount = computed.junkCount,
                    duplicateCount = computed.duplicateCount,
                    spamCount = computed.spamCount,
                    totalFiles = computed.totalFiles,
                    totalSize = computed.totalSize,
                    duplicateItems = computed.duplicateItems,
                    spamItems = computed.spamItems,
                    largeFileItems = computed.largeFileItems,
                    sentFileItems = computed.sentFileItems,
                    smartSuggestionSummary = computed.smartSuggestionSummary,
                    smartSuggestedItems = computed.smartSuggestedItems,
                    suggestionReasonsByUri = computed.suggestionReasonsByUri,
                    report = computed.report,
                    duplicateGroups = computed.duplicateGroups,
                    oldFileItems = computed.oldFileItems,
                    whatsappJunkItems = computed.whatsappJunkItems
                )
            }
            _items.value = computed.allItems
        }
    }

    private suspend fun isMeme(item: SimpleMediaItem, classifier: MemeClassifier): Boolean {
        val likelyByName = item.name.contains("meme", ignoreCase = true) || item.path.contains("meme", ignoreCase = true)
        if (item.mimeType?.startsWith("image") != true) return likelyByName
        val classification = classifier.classify(item.uri)
        return classification.category == ImageCategory.MEME || likelyByName
    }

    fun runAiScan(isDeepClean: Boolean = false) {
        val currentState = _uiState.value
        val allItems = currentState.allItems
        if (allItems.isEmpty()) return
        if (isDeepClean && currentState.deepCleanCredits <= 0) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(aiScanSummary = AiScanSummary(isRunning = true, progress = 0.1f, status = "Checking duplicates...")) }
            val duplicateItems = allItems
                .groupBy { "${it.name.lowercase()}_${it.size}" }
                .values
                .filter { it.size > 1 }
                .flatten()

            _uiState.update { it.copy(aiScanSummary = it.aiScanSummary.copy(progress = 0.35f, status = "Finding large files...")) }
            val largeItems = allItems.filter { it.size > LARGE_FILE_THRESHOLD_BYTES }.sortedByDescending { it.size }

            _uiState.update { it.copy(aiScanSummary = it.aiScanSummary.copy(progress = 0.55f, status = "Checking old media...")) }
            val oldCutoff = System.currentTimeMillis() - (OLD_FILE_AGE_DAYS * 24L * 60L * 60L * 1000L)
            val oldItems = allItems.filter { it.addedMillis < oldCutoff }

            _uiState.update { it.copy(aiScanSummary = it.aiScanSummary.copy(progress = 0.75f, status = "Detecting blurry and junk images...")) }
            val junkItems = allItems.filter { mediaItem ->
                mediaItem.mimeType?.startsWith("image") == true &&
                    mediaItem.size < 200L * 1024L &&
                    mediaItem.uri.toString().contains("whatsapp", ignoreCase = true)
            }
            val blurryItems = detectBlurryImages(allItems, if (isDeepClean) 200 else 80)

            val union = (duplicateItems + largeItems + oldItems + junkItems + blurryItems).distinctBy { it.uri.toString() }
            _uiState.update {
                it.copy(
                    duplicateGroups = duplicateItems.groupBy { item -> "${item.name.lowercase()}_${item.size}" }.values.toList(),
                    largeFileItems = largeItems,
                    oldFileItems = oldItems.sortedBy { item -> item.addedMillis },
                    whatsappJunkItems = junkItems,
                    blurryImageItems = blurryItems,
                    aiScanSummary = AiScanSummary(
                        isRunning = false,
                        progress = 1f,
                        status = if (isDeepClean) "Deep clean complete" else "Scan complete",
                        cleanableBytes = union.sumOf { item -> item.size },
                        junkCount = union.size
                    ),
                    shouldShowInterstitialForAiScan = true,
                    deepCleanCredits = if (isDeepClean) (it.deepCleanCredits - 1).coerceAtLeast(0) else it.deepCleanCredits
                )
            }
        }
    }

    fun unlockDeepCleanCredit() {
        _uiState.update { current ->
            current.copy(deepCleanCredits = current.deepCleanCredits + 1)
        }
    }

    fun consumeAiScanInterstitialRequest() {
        _uiState.update { current -> current.copy(shouldShowInterstitialForAiScan = false) }
    }

    fun consumeDeleteInterstitialRequest() {
        _uiState.update { current -> current.copy(shouldShowInterstitialForDelete = false) }
    }

    private fun detectBlurryImages(items: List<SimpleMediaItem>, limit: Int): List<SimpleMediaItem> =
        items.asSequence()
            .filter { it.mimeType?.startsWith("image") == true }
            .take(limit)
            .filter { isLikelyBlurry(it.uri) }
            .toList()

    private fun isLikelyBlurry(uri: Uri): Boolean = runCatching {
        val options = BitmapFactory.Options().apply { inSampleSize = 8 }
        appContext.contentResolver.openInputStream(uri)?.use { input ->
            val bitmap = BitmapFactory.decodeStream(input, null, options) ?: return false
            if (bitmap.width < 8 || bitmap.height < 8) return true
            var diffSum = 0L
            var count = 0
            for (y in 1 until bitmap.height step 2) {
                for (x in 1 until bitmap.width step 2) {
                    val pixel = bitmap.getPixel(x, y)
                    val left = bitmap.getPixel(x - 1, y)
                    val up = bitmap.getPixel(x, y - 1)
                    val lum = ((pixel shr 16) and 0xFF) + ((pixel shr 8) and 0xFF) + (pixel and 0xFF)
                    val lumL = ((left shr 16) and 0xFF) + ((left shr 8) and 0xFF) + (left and 0xFF)
                    val lumU = ((up shr 16) and 0xFF) + ((up shr 8) and 0xFF) + (up and 0xFF)
                    diffSum += abs(lum - lumL) + abs(lum - lumU)
                    count++
                }
            }
            bitmap.recycle()
            (if (count == 0) 0.0 else diffSum.toDouble() / count.toDouble()) < 35.0
        } ?: false
    }.getOrDefault(false)

    fun setFilter(filter: MediaFilter) {
        _uiState.update { currentState ->
            currentState.copy(
                currentFilter = filter,
                filteredItems = filterList(currentState.allItems, filter, currentState.activeSuggestion, currentState.settings)
            )
        }
    }

    fun setSuggestion(suggestion: SuggestionType) {
        _uiState.update { currentState ->
            currentState.copy(
                activeSuggestion = suggestion,
                filteredItems = filterList(currentState.allItems, currentState.currentFilter, suggestion, currentState.settings)
            )
        }
    }

    private fun filterList(
        items: List<SimpleMediaItem>,
        filter: MediaFilter,
        suggestion: SuggestionType,
        settings: SettingsUiState
    ): List<SimpleMediaItem> {
        var result = when (filter) {
            MediaFilter.IMAGES -> items.filter { mediaItem -> mediaItem.mimeType?.startsWith("image") == true }
            MediaFilter.VIDEOS -> items.filter { mediaItem -> mediaItem.mimeType?.startsWith("video") == true }
            MediaFilter.MEMES -> items.filter { mediaItem ->
                mediaItem.name.contains("meme", ignoreCase = true) || mediaItem.path.contains("meme", ignoreCase = true)
            }
            MediaFilter.DUPLICATES -> items
                .groupBy { mediaItem -> "${mediaItem.name.lowercase()}_${mediaItem.sizeKb}" }
                .values
                .filter { groupedItems -> groupedItems.size > 1 }
                .flatten()
            MediaFilter.OTHER -> items.filter { mediaItem ->
                mediaItem.mimeType?.startsWith("image") != true && mediaItem.mimeType?.startsWith("video") != true
            }
            MediaFilter.ALL -> items
        }
        val duplicateKeys = items
            .groupBy { mediaItem -> "${mediaItem.name.lowercase()}_${mediaItem.sizeKb}" }
            .filterValues { groupedItems -> groupedItems.size > 1 }
            .keys

        result = result.filter { item ->
            val passesSize = item.sizeKb >= settings.fileSizeFilterMb * 1024 || !settings.showOnlyLargeFiles
            val passesScreenshots = settings.includeScreenshots || !item.name.startsWith("Screenshot", true)
            val passesMemes = settings.includeMemes || !(item.name.contains("meme", ignoreCase = true) || item.path.contains("meme", ignoreCase = true))
            val isDuplicateCandidate = duplicateKeys.contains("${item.name.lowercase()}_${item.sizeKb}")
            val passesDuplicates = settings.includeDuplicates || !isDuplicateCandidate
            passesSize && passesScreenshots && passesMemes && passesDuplicates
        }
        val today = System.currentTimeMillis() - 86400000
        result = when (suggestion) {
            SuggestionType.LARGE_TODAY -> result.filter { mediaItem -> mediaItem.addedMillis > today && mediaItem.sizeKb > 5000 }
            SuggestionType.SCREENSHOTS_TODAY -> result.filter { mediaItem ->
                mediaItem.addedMillis > today && mediaItem.name.startsWith("Screenshot", true)
            }
            SuggestionType.NONE -> result
        }
        return result
    }

    fun toggleReminders(enabled: Boolean) {
        prefs.setRemindersEnabled(enabled)
        if (enabled) {
            ReminderScheduler.schedulePeriodicReminder(
                getApplication(),
                _uiState.value.settings.autoCleanFrequency.intervalMinutes
            )
        } else {
            ReminderScheduler.cancelPeriodicReminder(getApplication())
        }
        _uiState.update { currentState ->
            currentState.copy(remindersEnabled = enabled, settings = currentState.settings.copy(dailyReminderEnabled = enabled))
        }
    }

    fun setFrequency(option: ReminderFreq) {
        prefs.setReminderFrequencyDays(option.days)
        _uiState.update { currentState -> currentState.copy(selectedFrequency = option) }
    }

    fun setTime(option: ReminderTime) {
        prefs.setReminderTime(option.hour, option.minute)
        _uiState.update { currentState -> currentState.copy(selectedTime = option) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.setThemeMode(mode)
        _uiState.update { currentState -> currentState.copy(settings = currentState.settings.copy(themeMode = mode)) }
    }

    fun setSmartAlerts(enabled: Boolean) {
        prefs.setSmartAlertEnabled(enabled)
        _uiState.update { currentState ->
            currentState.copy(settings = currentState.settings.copy(smartAlertEnabled = enabled))
        }
    }

    fun setAutoCleanFrequency(option: ReminderFrequencyOption) {
        prefs.setAutoCleanFrequency(option)
        if (_uiState.value.settings.dailyReminderEnabled) {
            ReminderScheduler.schedulePeriodicReminder(getApplication(), option.intervalMinutes)
        }
        _uiState.update { currentState ->
            currentState.copy(settings = currentState.settings.copy(autoCleanFrequency = option))
        }
    }

    fun setFileSizeFilter(valueMb: Int) {
        prefs.setFileSizeFilterMb(valueMb)
        _uiState.update { currentState ->
            val updatedSettings = currentState.settings.copy(fileSizeFilterMb = valueMb)
            currentState.copy(
                settings = updatedSettings,
                filteredItems = filterList(currentState.allItems, currentState.currentFilter, currentState.activeSuggestion, updatedSettings)
            )
        }
    }

    fun setShowOnlyLargeFiles(enabled: Boolean) {
        prefs.setShowOnlyLargeFiles(enabled)
        _uiState.update { currentState ->
            val updatedSettings = currentState.settings.copy(showOnlyLargeFiles = enabled)
            currentState.copy(
                settings = updatedSettings,
                filteredItems = filterList(currentState.allItems, currentState.currentFilter, currentState.activeSuggestion, updatedSettings)
            )
        }
    }

    fun setIncludeScreenshots(enabled: Boolean) {
        prefs.setIncludeScreenshots(enabled)
        _uiState.update { currentState ->
            val updatedSettings = currentState.settings.copy(includeScreenshots = enabled)
            currentState.copy(
                settings = updatedSettings,
                filteredItems = filterList(currentState.allItems, currentState.currentFilter, currentState.activeSuggestion, updatedSettings)
            )
        }
    }

    fun setIncludeMemes(enabled: Boolean) {
        prefs.setIncludeMemes(enabled)
        _uiState.update { currentState ->
            val updatedSettings = currentState.settings.copy(includeMemes = enabled)
            currentState.copy(
                settings = updatedSettings,
                filteredItems = filterList(currentState.allItems, currentState.currentFilter, currentState.activeSuggestion, updatedSettings)
            )
        }
    }

    fun setIncludeDuplicates(enabled: Boolean) {
        prefs.setIncludeDuplicates(enabled)
        _uiState.update { currentState ->
            val updatedSettings = currentState.settings.copy(includeDuplicates = enabled)
            currentState.copy(
                settings = updatedSettings,
                filteredItems = filterList(currentState.allItems, currentState.currentFilter, currentState.activeSuggestion, updatedSettings)
            )
        }
    }

    fun onPremiumFeatureRequested(feature: PremiumFeature): Boolean {
        // TODO: RE-ENABLE SUBSCRIPTION LATER
        /*
        if (feature == PremiumFeature.SMART_CLEAN_ADVANCED) analytics.trackSmartCleanClicked()
        val isPro = _uiState.value.isProUser
        if (isPro) return true
        val attempts = prefs.incrementFreePremiumAttempts()
        _uiState.update { currentState ->
            currentState.copy(
                paywallSource = feature.paywallSource,
                hasExceededFreeLimit = attempts >= 2
            )
        }
        analytics.trackPaywallViewed(feature.paywallSource)
        return false
        */
        if (true) return true
        return false
    }

    fun notePaywallViewed(source: String) {
        // TODO: RE-ENABLE SUBSCRIPTION LATER
        /*
        _uiState.update { currentState ->
            currentState.copy(
                paywallSource = source,
                hasExceededFreeLimit = !currentState.isProUser && prefs.getFreePremiumAttempts() >= 2
            )
        }
        analytics.trackPaywallViewed(source)
        */
    }

    fun restorePurchases(source: String) {
        // TODO: RE-ENABLE SUBSCRIPTION LATER
        /*
        subscriptionRepository.restorePurchases(source)
        */
    }

    fun recordCleanupResult(bytes: Long) {
        val streak = prefs.recordCleanupDay()
        _uiState.update { currentState ->
            currentState.copy(lastCleanupBytes = bytes, deleteSnackbarMessage = "Great job! ${formatSize(bytes)} removed • ${streak}-day streak")
        }
    }

    fun shareResultText(): String {
        val freed = formatSize(_uiState.value.lastCleanupBytes)
        analytics.trackShareResultClicked("cleanup_result")
        return "I cleaned my phone and freed ${if (freed == "0 B") "space" else freed} using Cleanly AI."
    }

    fun shareInviteText(): String {
        analytics.trackShareResultClicked("invite_friends")
        return "Clean smarter. Free space instantly with Cleanly AI."
    }

    fun onDeleteClicked(origin: String) {
        analytics.trackDeleteClicked(origin)
    }

    fun requestMediaDeletion(items: List<SimpleMediaItem>, origin: String, sdkInt: Int): DeleteExecution {
        Log.d(TAG, "requestMediaDeletion called from $origin with ${items.size} items.")
        if (_uiState.value.isDeleteInProgress) {
            _uiState.update { currentState ->
                currentState.copy(deleteSnackbarMessage = "Delete already in progress")
            }
            return DeleteExecution.Ignored
        }
        val distinctItems = items.distinctBy { item -> item.uri.toString() }
        if (distinctItems.size != items.size) {
            Log.w(TAG, "Detected duplicate delete targets. original=${items.size}, distinct=${distinctItems.size}")
        }
        val validItems = distinctItems.filter { item ->
            val uriValue = item.uri.toString()
            val isMediaUri = uriValue.startsWith("content://media/")
            if (!isMediaUri) {
                Log.w(TAG, "Skipping non-MediaStore URI during delete request: $uriValue")
                return@filter false
            }
            val uriId = runCatching { android.content.ContentUris.parseId(item.uri) }.getOrNull()
            if (uriId == null || uriId <= 0L) {
                Log.w(TAG, "Skipping delete target with invalid URI id. uri=$uriValue, itemId=${item.id}")
                return@filter false
            }
            if (item.id > 0L && item.id != uriId) {
                Log.w(TAG, "Skipping delete target with mismatched ID. uriId=$uriId, itemId=${item.id}, uri=$uriValue")
                return@filter false
            }
            true
        }
        Log.d("DELETE_FLOW", "Filtered valid URIs count = ${validItems.size}")
        if (validItems.isEmpty()) {
            Log.w(TAG, "Skipping deletion request from $origin because there were no valid items.")
            _uiState.update { currentState ->
                currentState.copy(deleteSnackbarMessage = "No valid files selected")
            }
            return DeleteExecution.Ignored
        }
        Log.d(TAG, "Prepared ${validItems.size} valid items for deletion from $origin.")
        analytics.trackDeleteClicked(origin)
        val uris = validItems.map { item -> item.uri }.distinct()
        val ids = validItems.mapNotNull { item ->
            runCatching { android.content.ContentUris.parseId(item.uri) }.getOrNull()
        }.toSet()
        setPendingDelete(ids = ids, uris = uris, items = validItems)
        return if (sdkInt >= Build.VERSION_CODES.R) {
            Log.d(TAG, "Requesting MediaStore delete for ${validItems.size} items from $origin.")
            DeleteExecution.NeedsUserApproval(uris)
        } else {
            Log.d(TAG, "Requesting direct ContentResolver delete for ${validItems.size} items from $origin.")
            DeleteExecution.StartedInBackground(uris)
        }
    }

    fun setPendingDelete(ids: Set<Long>, uris: List<Uri>, items: List<SimpleMediaItem>) {
        if (ids.isEmpty() || uris.isEmpty() || items.isEmpty()) {
            _uiState.update { currentState ->
                currentState.copy(
                    pendingDeleteUris = emptyList(),
                    pendingDeleteIds = emptySet(),
                    pendingDeleteItems = emptyList(),
                    isDeleteInProgress = false
                )
            }
            return
        }

        val normalizedUris = uris
            .distinct()
            .filter { uri -> uri.toString().startsWith("content://media/") }

        if (normalizedUris.isEmpty()) {
            _uiState.update { currentState ->
                currentState.copy(
                    pendingDeleteUris = emptyList(),
                    pendingDeleteIds = emptySet(),
                    pendingDeleteItems = emptyList(),
                    isDeleteInProgress = false,
                    deleteSnackbarMessage = "No valid files selected"
                )
            }
            return
        }

        Log.d(TAG, "Stored ${items.size} pending delete items and ${normalizedUris.size} URIs.")
        _uiState.update { currentState ->
            currentState.copy(
                pendingDeleteUris = normalizedUris,
                pendingDeleteIds = ids,
                pendingDeleteItems = items,
                deleteRequestId = currentState.deleteRequestId + 1L,
                isDeleteInProgress = true
            )
        }
    }

    fun confirmDeleteSuccess() {
        onMediaDeleteSuccess()
    }

    fun onMediaDeleteSuccess(deletedIds: Set<Long>? = null) {
        val pendingIds = _uiState.value.pendingDeleteIds
        val resolvedDeletedIds = deletedIds?.takeIf { it.isNotEmpty() } ?: pendingIds
        if (resolvedDeletedIds.isEmpty()) {
            Log.w(TAG, "Delete result received with no deleted IDs.")
            onMediaDeleteCancelled()
            return
        }
        Log.d("DELETE_FLOW", "Step 5: Updating UI after delete")
        Log.d("DELETE_FLOW", "Step 5.1: Deleted IDs count = ${resolvedDeletedIds.size}")
        Log.d(TAG, "Delete flow completed. requested=${pendingIds.size}, deleted=${resolvedDeletedIds.size}")
        viewModelScope.launch {
            applyDeletionToState(resolvedDeletedIds)
        }
    }

    fun onMediaDeleteFailed() {
        val pendingIds = _uiState.value.pendingDeleteIds
        if (pendingIds.isEmpty()) return

        _uiState.update {
            it.copy(
                pendingDeleteIds = emptySet(),
                pendingDeleteUris = emptyList(),
                pendingDeleteItems = emptyList(),
                isDeleteInProgress = false
            )
        }
    }

    fun onMediaDeleteCancelled() {

        val pendingIds = _uiState.value.pendingDeleteIds
        if (pendingIds.isEmpty()) return
        Log.d(TAG, "Delete flow cancelled by user.")
        _uiState.update { currentState ->
            currentState.copy(
                pendingDeleteIds = emptySet(),
                pendingDeleteUris = emptyList(),
                pendingDeleteItems = emptyList(),
                isDeleteInProgress = false,
                deleteSnackbarMessage = null
            )
        }
    }

    fun undoLastDelete() {
        Log.d(TAG, "Undo delete requested, but UI-only restoration is disabled.")
        _uiState.update { currentState ->
            currentState.copy(
                deleteSnackbarMessage = null,
                lastDeletedItems = emptyList()
            )
        }
    }

    fun clearDeleteSnackbar() {
        _uiState.update { currentState -> currentState.copy(deleteSnackbarMessage = null, lastDeletedItems = emptyList()) }
    }

    private suspend fun applyDeletionToState(deletedIds: Set<Long>) {
        if (deletedIds.isEmpty()) {
            _uiState.update { currentState ->
                currentState.copy(
                    pendingDeleteIds = emptySet(),
                    pendingDeleteUris = emptyList(),
                    pendingDeleteItems = emptyList(),
                    isDeleteInProgress = false,
                    deleteSnackbarMessage = "No files were deleted"
                )
            }
            return
        }
        val currentState = _uiState.value
        val updatedState = withContext(Dispatchers.Default) {
            val remainingItems = currentState.allItems.filterNot { item -> item.id in deletedIds }
            val remainingMemeItems = currentState.memeItems.filterNot { item -> item.id in deletedIds }
            val remainingDuplicateItems = currentState.duplicateItems.filterNot { item -> item.id in deletedIds }
            val remainingSpamItems = currentState.spamItems.filterNot { item -> item.id in deletedIds }
            val remainingLargeFileItems = currentState.largeFileItems.filterNot { item -> item.id in deletedIds }
            val remainingSentFileItems = currentState.sentFileItems.filterNot { item -> item.id in deletedIds }
            val remainingTotalSize = remainingItems.sumOf { mediaItem -> mediaItem.sizeKb.toLong() * 1024L }
            val today = System.currentTimeMillis() - 86400000
            val remainingLargeToday = remainingItems.filter { mediaItem ->
                mediaItem.addedMillis > today && mediaItem.sizeKb > 5000
            }
            val remainingScreenshotsToday = remainingItems.filter { mediaItem ->
                mediaItem.addedMillis > today && mediaItem.name.startsWith("Screenshot", true)
            }
            val deletedBytes = currentState.allItems
                .filter { item -> item.id in deletedIds }
                .sumOf { mediaItem -> mediaItem.sizeKb.toLong() * 1024L }
            val streak = prefs.recordCleanupDay()
            val deleteMessage = buildString {
                append(if (deletedIds.size == 1) "1 item deleted" else "${deletedIds.size} items deleted")
                if (deletedBytes > 0L) append(" • ${formatSize(deletedBytes)} freed")
                append(" • ${streak}-day streak")
            }
            currentState.copy(
                pendingDeleteIds = emptySet(),
                pendingDeleteUris = emptyList(),
                pendingDeleteItems = emptyList(),
                isDeleteInProgress = false,
                allItems = remainingItems,
                filteredItems = filterList(
                    remainingItems,
                    currentState.currentFilter,
                    currentState.activeSuggestion,
                    currentState.settings
                ),
                summaryInfo = if (remainingItems.isEmpty()) {
                    "No media found."
                } else {
                    "Found ${remainingItems.size} files (${formatSize(remainingTotalSize)})"
                },
                memeCount = remainingMemeItems.size,
                memeItems = remainingMemeItems,
                duplicateCount = remainingDuplicateItems.size,
                duplicateItems = remainingDuplicateItems,
                spamCount = remainingSpamItems.size,
                spamItems = remainingSpamItems,
                largeFileItems = remainingLargeFileItems,
                sentFileItems = remainingSentFileItems,
                totalFiles = remainingItems.size,
                totalSize = remainingTotalSize,
                largeTodayCount = remainingLargeToday.size,
                largeTodaySizeText = formatSize(remainingLargeToday.sumOf { i -> i.sizeKb.toLong() * 1024 }),
                screenshotTodayCount = remainingScreenshotsToday.size,
                screenshotTodaySizeText = formatSize(remainingScreenshotsToday.sumOf { i -> i.sizeKb.toLong() * 1024 }),
                report = currentState.report.copy(
                    memeCount = remainingMemeItems.size,
                    duplicateCount = remainingDuplicateItems.size,
                    spamCount = remainingSpamItems.size
                ),
                deleteSnackbarMessage = deleteMessage,
                lastDeletedItems = emptyList(),
                shouldShowInterstitialForDelete = true
            )
        }
        _uiState.value = updatedState
        Log.d("DELETE_FLOW", "Step 5.2: Remaining items = ${updatedState.allItems.size}")
        _items.update { currentItems -> currentItems.filterNot { item -> item.id in deletedIds } }
    }

    fun onReviewClicked() {
        analytics.trackReviewClicked()
    }

    fun onStorageScreenOpened() {
        analytics.trackStorageScreenOpened()
    }

    fun onSettingsOpened() {
        analytics.trackSettingsOpened()
    }

    private fun loadPreferences() {
        val reminderDays = prefs.getReminderFrequencyDays()
        val reminderFreq = when (reminderDays) {
            3 -> ReminderFreq("Every 3 days", 3)
            7 -> ReminderFreq("Weekly", 7)
            else -> ReminderFreq("Every day", 1)
        }
        val reminderTime = ReminderTime(
            "%02d:%02d".format(prefs.getReminderTimeHour(), prefs.getReminderTimeMinute()),
            prefs.getReminderTimeHour(),
            prefs.getReminderTimeMinute()
        )
        _uiState.update { currentState ->
            currentState.copy(
                remindersEnabled = prefs.isRemindersEnabled(),
                selectedFrequency = reminderFreq,
                selectedTime = reminderTime,
                settings = SettingsUiState(
                    themeMode = prefs.getThemeMode(),
                    languageLabel = prefs.getLanguageLabel(),
                    dailyReminderEnabled = prefs.isRemindersEnabled(),
                    smartAlertEnabled = prefs.isSmartAlertEnabled(),
                    autoCleanFrequency = prefs.getAutoCleanFrequency(),
                    fileSizeFilterMb = prefs.getFileSizeFilterMb(),
                    showOnlyLargeFiles = prefs.showOnlyLargeFiles(),
                    includeScreenshots = prefs.includeScreenshots(),
                    includeMemes = prefs.includeMemes(),
                    includeDuplicates = prefs.includeDuplicates()
                ),
                hasExceededFreeLimit = prefs.getFreePremiumAttempts() >= 2
            )
        }

        if (prefs.isRemindersEnabled()) {
            ReminderScheduler.schedulePeriodicReminder(getApplication(), prefs.getAutoCleanFrequency().intervalMinutes)
        } else {
            ReminderScheduler.cancelPeriodicReminder(getApplication())
        }
    }
}
