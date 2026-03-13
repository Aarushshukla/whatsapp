package com.example.whatsappcleaner.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsappcleaner.data.local.MediaLoader
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val todayItems: List<SimpleMediaItem> = emptyList(),
    val olderItems: List<SimpleMediaItem> = emptyList(),
    val summaryInfo: String = "Loading...",
    val permissionGranted: Boolean = false,
    val currentFilter: MediaFilter = MediaFilter.ALL,
    val activeSuggestion: SuggestionType = SuggestionType.NONE,
    val largeTodayCount: Int = 0,
    val largeTodaySizeText: String = "",
    val screenshotTodayCount: Int = 0,
    val screenshotTodaySizeText: String = "",
    val remindersEnabled: Boolean = false,
    val selectedFrequency: ReminderFreq = ReminderFreq("Every day", 1),
    val selectedTime: ReminderTime = ReminderTime("09:00", 9, 0),
    val timeOptions: List<ReminderTime> = generateTimeOptions()
)

fun generateTimeOptions(): List<ReminderTime> = (0..23).map { ReminderTime("%02d:00".format(it), it, 0) }

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaLoader = MediaLoader(application)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Store original list to apply filters without reloading
    private var allRawItems: List<SimpleMediaItem> = emptyList()

    fun updatePermissionStatus(granted: Boolean) {
        _uiState.update { it.copy(permissionGranted = granted) }
        if (granted) refreshMedia() else _uiState.update { it.copy(summaryInfo = "Permission needed to scan.") }
    }

    fun refreshMedia() {
        viewModelScope.launch {
            _uiState.update { it.copy(summaryInfo = "Scanning Entire Gallery...") }

            val images = mediaLoader.loadAllDeviceMedia("image")
            val videos = mediaLoader.loadAllDeviceMedia("video")
            allRawItems = (images + videos).sortedByDescending { it.addedMillis }

            applyFiltersAndBifurcate()
        }
    }

    fun setFilter(filter: MediaFilter) {
        _uiState.update { it.copy(currentFilter = filter) }
        applyFiltersAndBifurcate()
    }

    fun setSuggestion(suggestion: SuggestionType) {
        _uiState.update { it.copy(activeSuggestion = suggestion) }
        applyFiltersAndBifurcate()
    }

    private fun applyFiltersAndBifurcate() {
        val state = _uiState.value

        // 1. Apply user filters
        val filtered = when (state.currentFilter) {
            MediaFilter.IMAGES -> allRawItems.filter { it.mimeType?.startsWith("image", true) == true || it.name.endsWith(".jpg", true) || it.name.endsWith(".png", true) }
            MediaFilter.VIDEOS -> allRawItems.filter { it.mimeType?.startsWith("video", true) == true || it.name.endsWith(".mp4", true) }
            MediaFilter.OTHER -> allRawItems.filter { it.mimeType?.startsWith("image", true) != true && it.mimeType?.startsWith("video", true) != true && !it.name.endsWith(".mp4", true) }
            MediaFilter.ALL -> allRawItems
        }

        // 2. Get Start of Today in Milliseconds
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis

        // 3. Bifurcate (Split the list)
        val todayList = filtered.filter { it.addedMillis >= startOfToday }
        val olderList = filtered.filter { it.addedMillis < startOfToday }

        val totalSize = filtered.sumOf { it.sizeKb.toLong() * 1024 }
        val summary = "Found ${filtered.size} files (${formatSize(totalSize)})"

        _uiState.update {
            it.copy(
                todayItems = todayList,
                olderItems = olderList,
                summaryInfo = summary
            )
        }
    }

    fun toggleReminders(enabled: Boolean) = _uiState.update { it.copy(remindersEnabled = enabled) }
    fun setFrequency(option: ReminderFreq) = _uiState.update { it.copy(selectedFrequency = option) }
    fun setTime(option: ReminderTime) = _uiState.update { it.copy(selectedTime = option) }
}