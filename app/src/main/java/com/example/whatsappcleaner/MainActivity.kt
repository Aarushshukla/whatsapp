package com.example.whatsappcleaner

import android.Manifest
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.whatsappcleaner.ui.home.SimpleHomeScreen
import com.example.whatsappcleaner.ui.theme.WhatsCleanTheme // IMPORTED CORRECTLY

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            viewModel.updatePermissionStatus(granted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()

        setContent {
            // USES THE NAME FROM YOUR THEME FILE
            WhatsCleanTheme {
                val state by viewModel.uiState.collectAsState()

                SimpleHomeScreen(
                    items = state.filteredItems,
                    onRefreshClick = { viewModel.refreshMedia() },
                    // permissionInfo line REMOVED completely
                    summaryInfo = state.summaryInfo,
                    currentFilter = state.currentFilter,
                    onFilterChange = { viewModel.setFilter(it) },
                    largeTodayCount = state.largeTodayCount,
                    largeTodaySizeText = state.largeTodaySizeText,
                    screenshotTodayCount = state.screenshotTodayCount,
                    screenshotTodaySizeText = state.screenshotTodaySizeText,
                    activeSuggestion = state.activeSuggestion,
                    onSuggestionChange = { viewModel.setSuggestion(it) },
                    remindersEnabled = state.remindersEnabled,
                    selectedFrequency = state.selectedFrequency,
                    onFrequencyChange = { viewModel.setFrequency(it) },
                    selectedTime = state.selectedTime,
                    allTimeOptions = state.timeOptions,
                    onTimeChange = { viewModel.setTime(it) },
                    onRemindersToggle = { viewModel.toggleReminders(it) },
                    onOpenInSystem = { item -> openFileInSystem(item.uri) },
                    onOpenSystemStorage = { openSystemStorage() }
                )
            }
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
        viewModel.updatePermissionStatus(allGranted)
    }

    private fun openFileInSystem(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun openSystemStorage() {
        val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }
}