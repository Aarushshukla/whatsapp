package com.example.whatsappcleaner.ui.home

import android.app.Application
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
import com.example.whatsappcleaner.ui.settings.AppThemeMode
import com.example.whatsappcleaner.ui.settings.ReminderFrequencyOption
import com.example.whatsappcleaner.ui.settings.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PremiumFeature(val analyticsKey: String, val paywallSource: String) {
    SMART_CLEAN_ADVANCED("smart_clean_clicked", "smart_clean_advanced"),
    DUPLICATE_DETECTION("duplicate_detection", "duplicate_detection"),
    MEME_DETECTION("meme_detection", "meme_detection"),
    BULK_DELETE("bulk_delete", "bulk_delete"),
    AUTO_CLEAN("auto_clean", "auto_clean"),
    ADVANCED_ANALYTICS("advanced_analytics", "advanced_analytics")
}

data class HomeUiState(
    val allItems: List<SimpleMediaItem> = emptyList(),
    val filteredItems: List<SimpleMediaItem> = emptyList(),
    val summaryInfo: String = "Loading...",
    val permissionGranted: Boolean = false,
    val isLoading: Boolean = false,
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
    val report: StorageReport = StorageReport(0, 0, 0, 0, 0, 0),
    val settings: SettingsUiState = SettingsUiState(),
    val subscriptionState: SubscriptionState = SubscriptionState(),
    val paywallSource: String = "dashboard",
    val hasExceededFreeLimit: Boolean = false,
    val lastCleanupBytes: Long = 0L
) {
    val isProUser: Boolean get() = subscriptionState.isProUser
}

fun generateTimeOptions(): List<ReminderTime> = (0..23).map { hour ->
    ReminderTime("%02d:00".format(hour), hour, 0)
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val mediaLoader = MediaLoader(application)
    private val smartJunkAnalyzer = SmartJunkAnalyzer()
    private val spamMediaAnalyzer = SpamMediaAnalyzer()
    private val phoneRealityAnalyzer = PhoneRealityAnalyzer()
    private val prefs = UserPrefs.get(application)
    private val analytics = AppAnalytics.get(application)
    private val subscriptionRepository = SubscriptionRepository.get(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
        subscriptionRepository.start()
        subscriptionRepository.refreshPurchases()
        viewModelScope.launch {
            subscriptionRepository.state.collectLatest { subscriptionState ->
                _uiState.update { it.copy(subscriptionState = subscriptionState) }
            }
        }
    }

    fun updatePermissionStatus(granted: Boolean) {
        Log.d(TAG, "Permission state updated: granted=$granted")
        _uiState.update { it.copy(permissionGranted = granted) }
        if (granted) refreshMedia() else _uiState.update { it.copy(summaryInfo = "Permission needed to scan.", isLoading = false) }
    }

    fun refreshMedia() {
        if (!_uiState.value.permissionGranted) return
        viewModelScope.launch {
            Log.d(TAG, "Refreshing media library.")
            _uiState.update { it.copy(summaryInfo = "Scanning...", isLoading = true) }
            try {
                val images = mediaLoader.loadAllDeviceMedia("image")
                val videos = mediaLoader.loadAllDeviceMedia("video")
                val allItems = (images + videos).sortedByDescending { it.addedMillis }

                val memeClassifier = MemeClassifier(getApplication())
                val memes = allItems.filter { item -> isMeme(item, memeClassifier) }
                memeClassifier.close()

                val junkBreakdown = smartJunkAnalyzer.buildBreakdown(allItems)
                val junkItems = smartJunkAnalyzer.findJunk(allItems)
                val spamItems = spamMediaAnalyzer.findSpamMedia(allItems)
                val baseReport = phoneRealityAnalyzer.generateReport(allItems)

                val totalSize = allItems.sumOf { it.sizeKb.toLong() * 1024 }
                val summary = if (allItems.isEmpty()) {
                    Log.d(TAG, "No media items found on device.")
                    "No media found."
                } else {
                    Log.d(TAG, "Loaded ${allItems.size} media items.")
                    "Found ${allItems.size} files (${formatSize(totalSize)})"
                }

                val today = System.currentTimeMillis() - 86400000
                val largeItems = allItems.filter { it.addedMillis > today && it.sizeKb > 5000 }
                val screenshots = allItems.filter { it.addedMillis > today && it.name.startsWith("Screenshot", true) }

                _uiState.update { current ->
                    current.copy(
                        allItems = allItems,
                        filteredItems = filterList(allItems, current.currentFilter, current.activeSuggestion, current.settings),
                        summaryInfo = summary,
                        isLoading = false,
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
                        report = baseReport.copy(
                            memeCount = memes.size,
                            duplicateCount = junkBreakdown.duplicates.size,
                            spamCount = spamItems.size
                        )
                    )
                }
            } catch (error: SecurityException) {
                Log.e(TAG, "Media scan failed due to permission issue.", error)
                _uiState.update { it.copy(summaryInfo = "Scan failed: permission denied", isLoading = false) }
            } catch (error: Exception) {
                Log.e(TAG, "Media scan failed.", error)
                _uiState.update { it.copy(summaryInfo = "Scan failed: ${error.message ?: "Unknown error"}", isLoading = false) }
            }
        }
    }

    private suspend fun isMeme(item: SimpleMediaItem, classifier: MemeClassifier): Boolean {
        val likelyByName = item.name.contains("meme", ignoreCase = true) || item.path.contains("meme", ignoreCase = true)
        if (item.mimeType?.startsWith("image") != true) return likelyByName
        val classification = classifier.classify(item.uri)
        return classification.category == ImageCategory.MEME || likelyByName
    }

    fun setFilter(filter: MediaFilter) {
        _uiState.update {
            it.copy(currentFilter = filter, filteredItems = filterList(it.allItems, filter, it.activeSuggestion, it.settings))
        }
    }

    fun setSuggestion(suggestion: SuggestionType) {
        _uiState.update {
            it.copy(activeSuggestion = suggestion, filteredItems = filterList(it.allItems, it.currentFilter, suggestion, it.settings))
        }
    }

    private fun filterList(
        items: List<SimpleMediaItem>,
        filter: MediaFilter,
        suggestion: SuggestionType,
        settings: SettingsUiState
    ): List<SimpleMediaItem> {
        var result = when (filter) {
            MediaFilter.IMAGES -> items.filter { it.mimeType?.startsWith("image") == true }
            MediaFilter.VIDEOS -> items.filter { it.mimeType?.startsWith("video") == true }
            MediaFilter.MEMES -> items.filter { it.name.contains("meme", ignoreCase = true) || it.path.contains("meme", ignoreCase = true) }
            MediaFilter.DUPLICATES -> items.groupBy { "${it.name.lowercase()}_${it.sizeKb}" }.values.filter { group -> group.size > 1 }.flatten()
            MediaFilter.OTHER -> items.filter { it.mimeType?.startsWith("image") != true && it.mimeType?.startsWith("video") != true }
            MediaFilter.ALL -> items
        }
        result = result.filter { item ->
            val passesSize = item.sizeKb >= settings.fileSizeFilterMb * 1024 || !settings.showOnlyLargeFiles
            val passesScreenshots = settings.includeScreenshots || !item.name.startsWith("Screenshot", true)
            val passesMemes = settings.includeMemes || !(item.name.contains("meme", ignoreCase = true) || item.path.contains("meme", ignoreCase = true))
            val isDuplicateCandidate = items.count { other -> other.name.equals(item.name, true) && other.sizeKb == item.sizeKb } > 1
            val passesDuplicates = settings.includeDuplicates || !isDuplicateCandidate
            passesSize && passesScreenshots && passesMemes && passesDuplicates
        }
        val today = System.currentTimeMillis() - 86400000
        result = when (suggestion) {
            SuggestionType.LARGE_TODAY -> result.filter { it.addedMillis > today && it.sizeKb > 5000 }
            SuggestionType.SCREENSHOTS_TODAY -> result.filter { it.addedMillis > today && it.name.startsWith("Screenshot", true) }
            SuggestionType.NONE -> result
        }
        return result
    }

    fun toggleReminders(enabled: Boolean) {
        prefs.setRemindersEnabled(enabled)
        _uiState.update { it.copy(remindersEnabled = enabled, settings = it.settings.copy(dailyReminderEnabled = enabled)) }
    }

    fun setFrequency(option: ReminderFreq) {
        prefs.setReminderFrequencyDays(option.days)
        _uiState.update { it.copy(selectedFrequency = option) }
    }

    fun setTime(option: ReminderTime) {
        prefs.setReminderTime(option.hour, option.minute)
        _uiState.update { it.copy(selectedTime = option) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.setThemeMode(mode)
        _uiState.update { it.copy(settings = it.settings.copy(themeMode = mode)) }
    }

    fun setSmartAlerts(enabled: Boolean) {
        prefs.setSmartAlertEnabled(enabled)
        _uiState.update { it.copy(settings = it.settings.copy(smartAlertEnabled = enabled)) }
    }

    fun setAutoCleanFrequency(option: ReminderFrequencyOption) {
        prefs.setAutoCleanFrequency(option)
        _uiState.update { it.copy(settings = it.settings.copy(autoCleanFrequency = option)) }
    }

    fun setFileSizeFilter(valueMb: Int) {
        prefs.setFileSizeFilterMb(valueMb)
        _uiState.update {
            val updatedSettings = it.settings.copy(fileSizeFilterMb = valueMb)
            it.copy(settings = updatedSettings, filteredItems = filterList(it.allItems, it.currentFilter, it.activeSuggestion, updatedSettings))
        }
    }

    fun setShowOnlyLargeFiles(enabled: Boolean) {
        prefs.setShowOnlyLargeFiles(enabled)
        _uiState.update {
            val updatedSettings = it.settings.copy(showOnlyLargeFiles = enabled)
            it.copy(settings = updatedSettings, filteredItems = filterList(it.allItems, it.currentFilter, it.activeSuggestion, updatedSettings))
        }
    }

    fun setIncludeScreenshots(enabled: Boolean) {
        prefs.setIncludeScreenshots(enabled)
        _uiState.update {
            val updatedSettings = it.settings.copy(includeScreenshots = enabled)
            it.copy(settings = updatedSettings, filteredItems = filterList(it.allItems, it.currentFilter, it.activeSuggestion, updatedSettings))
        }
    }

    fun setIncludeMemes(enabled: Boolean) {
        prefs.setIncludeMemes(enabled)
        _uiState.update {
            val updatedSettings = it.settings.copy(includeMemes = enabled)
            it.copy(settings = updatedSettings, filteredItems = filterList(it.allItems, it.currentFilter, it.activeSuggestion, updatedSettings))
        }
    }

    fun setIncludeDuplicates(enabled: Boolean) {
        prefs.setIncludeDuplicates(enabled)
        _uiState.update {
            val updatedSettings = it.settings.copy(includeDuplicates = enabled)
            it.copy(settings = updatedSettings, filteredItems = filterList(it.allItems, it.currentFilter, it.activeSuggestion, updatedSettings))
        }
    }

    fun onPremiumFeatureRequested(feature: PremiumFeature): Boolean {
        if (feature == PremiumFeature.SMART_CLEAN_ADVANCED) analytics.trackSmartCleanClicked()
        val isPro = _uiState.value.isProUser
        if (isPro) return true
        val attempts = prefs.incrementFreePremiumAttempts()
        _uiState.update {
            it.copy(
                paywallSource = feature.paywallSource,
                hasExceededFreeLimit = attempts >= 2
            )
        }
        analytics.trackPaywallViewed(feature.paywallSource)
        return false
    }

    fun notePaywallViewed(source: String) {
        _uiState.update { it.copy(paywallSource = source) }
        analytics.trackPaywallViewed(source)
    }

    fun restorePurchases(source: String) {
        subscriptionRepository.restorePurchases(source)
    }

    fun recordCleanupResult(bytes: Long) {
        prefs.recordCleanup()
        _uiState.update { it.copy(lastCleanupBytes = bytes) }
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
        _uiState.update {
            it.copy(
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
    }
}
