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
import com.example.whatsappcleaner.ui.WhatsCleanAppRoot
import com.example.whatsappcleaner.ui.home.HomeViewModel
import com.example.whatsappcleaner.ui.theme.WhatsCleanTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

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
        syncPermissionState()

        setContent {
            WhatsCleanTheme {
                val state by viewModel.uiState.collectAsState()
                WhatsCleanAppRoot(
                    state = state,
                    onRefreshClick = { viewModel.refreshMedia() },
                    onFilterChange = { viewModel.setFilter(it) },
                    onSuggestionChange = { viewModel.setSuggestion(it) },
                    onFrequencyChange = { viewModel.setFrequency(it) },
                    onTimeChange = { viewModel.setTime(it) },
                    onRemindersToggle = { viewModel.toggleReminders(it) },
                    onOpenInSystem = { item -> openFileInSystem(item.uri) },
                    onOpenSystemStorage = { openSystemStorage() },
                    onRequestPermission = { requestStoragePermissions() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
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
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(intent)
    }

    private fun openSystemStorage() {
        val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
        runCatching { startActivity(intent) }
            .onFailure { startActivity(Intent(Settings.ACTION_SETTINGS)) }
    }
}
