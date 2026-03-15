package com.example.whatsappcleaner.ui.home

import android.app.Application
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
import com.example.whatsappcleaner.data.local.MediaLoader
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val remindersEnabled: Boolean = false,
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
    val report: StorageReport = StorageReport(0, 0, 0, 0, 0, 0)
)

fun generateTimeOptions(): List<ReminderTime> = (0..23).map { hour ->
    ReminderTime("%02d:00".format(hour), hour, 0)
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaLoader = MediaLoader(application)
    private val smartJunkAnalyzer = SmartJunkAnalyzer()
    private val spamMediaAnalyzer = SpamMediaAnalyzer()
    private val phoneRealityAnalyzer = PhoneRealityAnalyzer()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun updatePermissionStatus(granted: Boolean) {
        _uiState.update { it.copy(permissionGranted = granted) }
        if (granted) refreshMedia() else _uiState.update { it.copy(summaryInfo = "Permission needed to scan.", isLoading = false) }
    }

    fun refreshMedia() {
        if (!_uiState.value.permissionGranted) return
        viewModelScope.launch {
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
                    "No media found."
                } else {
                    "Found ${allItems.size} files (${formatSize(totalSize)})"
                }

                val today = System.currentTimeMillis() - 86400000
                val largeItems = allItems.filter { it.addedMillis > today && it.sizeKb > 5000 }
                val screenshots = allItems.filter { it.addedMillis > today && it.name.startsWith("Screenshot", true) }

                _uiState.update {
                    it.copy(
                        allItems = allItems,
                        filteredItems = filterList(allItems, it.currentFilter, it.activeSuggestion),
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
            } catch (error: Exception) {
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
            it.copy(currentFilter = filter, filteredItems = filterList(it.allItems, filter, it.activeSuggestion))
        }
    }

    fun setSuggestion(suggestion: SuggestionType) {
        _uiState.update {
            it.copy(activeSuggestion = suggestion, filteredItems = filterList(it.allItems, it.currentFilter, suggestion))
        }
    }

    private fun filterList(items: List<SimpleMediaItem>, filter: MediaFilter, suggestion: SuggestionType): List<SimpleMediaItem> {
        var result = when (filter) {
            MediaFilter.IMAGES -> items.filter { it.mimeType?.startsWith("image") == true }
            MediaFilter.VIDEOS -> items.filter { it.mimeType?.startsWith("video") == true }
            MediaFilter.OTHER -> items.filter { it.mimeType?.startsWith("image") != true && it.mimeType?.startsWith("video") != true }
            MediaFilter.ALL -> items
        }
        val today = System.currentTimeMillis() - 86400000
        result = when (suggestion) {
            SuggestionType.LARGE_TODAY -> result.filter { it.addedMillis > today && it.sizeKb > 5000 }
            SuggestionType.SCREENSHOTS_TODAY -> result.filter { it.addedMillis > today && it.name.startsWith("Screenshot", true) }
            SuggestionType.NONE -> result
        }
        return result
    }

    fun toggleReminders(enabled: Boolean) { _uiState.update { it.copy(remindersEnabled = enabled) } }
    fun setFrequency(option: ReminderFreq) { _uiState.update { it.copy(selectedFrequency = option) } }
    fun setTime(option: ReminderTime) { _uiState.update { it.copy(selectedTime = option) } }
}
