package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.ui.components.FriendlyState
import com.example.whatsappcleaner.ui.components.LegitButton
import com.example.whatsappcleaner.ui.theme.BrandNavy
import com.example.whatsappcleaner.ui.theme.PrimaryBackground
import com.example.whatsappcleaner.ui.theme.SurfaceWhite
import com.example.whatsappcleaner.ui.theme.TextMain
import com.example.whatsappcleaner.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerScreen(
    allItems: List<SimpleMediaItem>,
    spamItems: List<SimpleMediaItem>,
    duplicateItems: List<SimpleMediaItem>,
    onOpenInSystem: (SimpleMediaItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableStateOf(MediaFilter.ALL) }
    var pendingDelete by remember { mutableStateOf<SimpleMediaItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filtered = when (tab) {
        MediaFilter.IMAGES -> allItems.filter { it.mimeType?.startsWith("image") == true }
        MediaFilter.VIDEOS -> allItems.filter { it.mimeType?.startsWith("video") == true }
        MediaFilter.MEMES -> allItems.filter { it.name.contains("meme", true) || it.path.contains("meme", true) }
        MediaFilter.DUPLICATES -> duplicateItems
        else -> allItems
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Media Viewer", color = BrandNavy)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandNavy)
                    }
                }
            )
        },
        containerColor = PrimaryBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Spam candidates: ${spamItems.size} • Duplicates: ${duplicateItems.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            item { FilterTabs(currentFilter = tab, onFilterChange = { tab = it }) }
            if (filtered.isEmpty()) {
                item {
                    FriendlyState(Icons.Default.PermMedia, "No media here yet", "Try another category or refresh the scan.")
                }
            } else {
                items(filtered, key = { it.uri.toString() }) { item ->
                    MediaSwipeRow(
                        item = item,
                        selected = false,
                        onClick = { onOpenInSystem(item) },
                        onDelete = { pendingDelete = item },
                        onKeep = { scope.launch { snackbarHostState.showSnackbar("Kept ${item.name}") } },
                        onOpen = { onOpenInSystem(item) }
                    )
                }
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this file?", color = TextMain) },
            text = { Text("Are you sure you want to delete ${item.name}?", color = TextSecondary) },
            confirmButton = {
                LegitButton(text = "Open delete flow", onClick = {
                    pendingDelete = null
                    onOpenInSystem(item)
                    scope.launch { snackbarHostState.showSnackbar("Opened ${item.name} for deletion") }
                })
            },
            dismissButton = {
                LegitButton(text = "Cancel", onClick = { pendingDelete = null })
            },
            containerColor = SurfaceWhite
        )
    }
}
