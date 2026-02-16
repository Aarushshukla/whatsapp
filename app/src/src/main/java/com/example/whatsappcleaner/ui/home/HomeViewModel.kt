//package com.example.whatsappcleaner.ui.home
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.whatsappcleaner.data.MediaStoreRepository
//
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.launchIn
//import kotlinx.coroutines.flow.onEach
//import kotlinx.coroutines.launch
//
//
//data class HomeUiState(
//    val todayItems: List<MediaItemEntity> = emptyList(),
//    val isLoading: Boolean = false,
//    val selectedUris: Set<String> = emptySet()
//)
//
//class HomeViewModel(
//    private val repo: MediaStoreRepository,
//    private val mediaDao: MediaDao
//) : ViewModel() {
//
//
//    private val _state = MutableStateFlow(HomeUiState())
//    val state: StateFlow<HomeUiState> = _state.asStateFlow()
//
//    init {
//        observeToday()
//    }
//
//    private fun observeToday() {
//        val now = System.currentTimeMillis()
//        val startOfDay = (now / 86400000) * 86400000
//        mediaDao.observeByDateRange(startOfDay, now)
//            .onEach { list ->
//                _state.value = _state.value.copy(todayItems = list, isLoading = false)
//            }
//            .launchIn(viewModelScope)
//    }
//
//    fun refreshToday() {
//        viewModelScope.launch {
//            _state.value = _state.value.copy(isLoading = true)
//            repo.scanTodayWhatsAppMedia()
//        }
//    }
//
//    fun toggleSelect(uri: String) {
//        val current = _state.value.selectedUris.toMutableSet()
//        if (current.contains(uri)) current.remove(uri) else current.add(uri)
//        _state.value = _state.value.copy(selectedUris = current)
//    }
//
//    fun applyRuleToSelection(rule: String, days: Int?) {
//        val uris = _state.value.selectedUris.toList()
//        val expireAt = days?.let { System.currentTimeMillis() + it * 24L * 60L * 60L * 1000L }
//
//        viewModelScope.launch {
//            mediaDao.updateStatus(uris, rule, expireAt)
//            _state.value = _state.value.copy(selectedUris = emptySet())
//        }
//    }
//}
