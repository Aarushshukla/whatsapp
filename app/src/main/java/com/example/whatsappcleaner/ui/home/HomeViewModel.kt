package com.example.whatsappcleaner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsappcleaner.data.MediaLoader
import com.example.whatsappcleaner.data.SimpleMediaItem
import com.example.whatsappcleaner.data.formatSize
import com.example.whatsappcleaner.ui.home.MediaFilter
import com.example.whatsappcleaner.ui.home.SuggestionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- NEW CLASS NAMES (To avoid "Redeclaration" error) ---
data class ReminderFreq(val label: String, val days: Int)
data class ReminderTime(val label: String, val hour: Int, val minute: Int)

data class HomeUiState(
    val allItems: List<SimpleMediaItem> = emptyList(),
    val filteredItems: List<SimpleMediaItem> = emptyList(),
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

fun generateTimeOptions(): List<ReminderTime> {
    return (0..23).map { hour ->
        val label = "%02d:00".format(hour)
        ReminderTime(label, hour, 0)
    }
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaLoader = MediaLoader(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun updatePermissionStatus(granted: Boolean) {
        _uiState.update { it.copy(permissionGranted = granted) }
        if (granted) {
            refreshMedia()
        } else {
            _uiState.update { it.copy(summaryInfo = "Permission needed to scan files.") }
        }
    }

    fun refreshMedia() {
        viewModelScope.launch {
            _uiState.update { it.copy(summaryInfo = "Scanning...") }

            val images = mediaLoader.loadWhatsAppMedia("image")
            val videos = mediaLoader.loadWhatsAppMedia("video")
            val allItems = (images + videos).sortedByDescending { it.addedMillis }

            val totalSize = allItems.sumOf { it.sizeKb.toLong() * 1024 }
            val count = allItems.size
            val summary = "Found $count files (${formatSize(totalSize)})"

            val today = System.currentTimeMillis() - 86400000
            val largeItems = allItems.filter { it.addedMillis > today && it.sizeKb > 5000 }

            _uiState.update {
                it.copy(
                    allItems = allItems,
                    filteredItems = filterList(allItems, it.currentFilter, it.activeSuggestion),
                    summaryInfo = summary,
                    largeTodayCount = largeItems.size,
                    largeTodaySizeText = formatSize(largeItems.sumOf { i -> i.sizeKb.toLong() * 1024 })
                )
            }
        }
    }

    fun setFilter(filter: MediaFilter) {
        _uiState.update {
            it.copy(
                currentFilter = filter,
                filteredItems = filterList(it.allItems, filter, it.activeSuggestion)
            )
        }
    }

    fun setSuggestion(suggestion: SuggestionType) {
        _uiState.update {
            it.copy(
                activeSuggestion = suggestion,
                filteredItems = filterList(it.allItems, it.currentFilter, suggestion)
            )
        }
    }

    private fun filterList(
        items: List<SimpleMediaItem>,
        filter: MediaFilter,
        suggestion: SuggestionType
    ): List<SimpleMediaItem> {
        var result = items

        result = when (filter) {
            MediaFilter.IMAGES -> result.filter { it.mimeType?.startsWith("image") == true }
            MediaFilter.VIDEOS -> result.filter { it.mimeType?.startsWith("video") == true }
            MediaFilter.OTHER -> result.filter {
                it.mimeType?.startsWith("image") != true && it.mimeType?.startsWith("video") != true
            }
            MediaFilter.ALL -> result
        }

        if (suggestion == SuggestionType.LARGE_TODAY) {
            val today = System.currentTimeMillis() - 86400000
            result = result.filter { it.addedMillis > today && it.sizeKb > 5000 }
        }

        return result
    }

    fun toggleReminders(enabled: Boolean) {
        _uiState.update { it.copy(remindersEnabled = enabled) }
    }

    fun setFrequency(option: ReminderFreq) {
        _uiState.update { it.copy(selectedFrequency = option) }
    }

    fun setTime(option: ReminderTime) {
        _uiState.update { it.copy(selectedTime = option) }
    }
}