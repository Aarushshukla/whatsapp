package com.example.whatsappcleaner.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.components.FriendlyState
import com.example.whatsappcleaner.ui.components.LegitButton
import com.example.whatsappcleaner.ui.components.LegitCard
import com.example.whatsappcleaner.ui.theme.AccentGreen
import com.example.whatsappcleaner.ui.theme.BrandNavy
import com.example.whatsappcleaner.ui.theme.PrimaryBackground
import com.example.whatsappcleaner.ui.theme.SurfaceWhite
import com.example.whatsappcleaner.ui.theme.TextMain
import com.example.whatsappcleaner.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun SimpleMediaItem.safeDisplayName(): String = name.ifBlank { "Media file" }

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
    var successMessage by remember { mutableStateOf<String?>(null) }
    var removedItemIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(1800)
            successMessage = null
        }
    }

    val filtered = when (tab) {
        MediaFilter.IMAGES -> allItems.filter { mediaItem -> mediaItem.mimeType?.startsWith("image") == true }
        MediaFilter.VIDEOS -> allItems.filter { mediaItem -> mediaItem.mimeType?.startsWith("video") == true }
        MediaFilter.MEMES -> allItems.filter { mediaItem -> mediaItem.name.contains("meme", true) || mediaItem.path.contains("meme", true) }
        MediaFilter.DUPLICATES -> duplicateItems
        else -> allItems
    }
    val remainingVisibleItems = filtered.count { mediaItem -> mediaItem.uri.toString() !in removedItemIds }

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
                AnimatedVisibility(
                    visible = successMessage != null,
                    enter = fadeIn(animationSpec = tween(360)) + scaleIn(animationSpec = tween(360), initialScale = 0.82f),
                    exit = fadeOut(animationSpec = tween(260)) + scaleOut(animationSpec = tween(260), targetScale = 0.92f)
                ) {
                    successMessage?.let { message ->
                        LegitCard {
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen)
                                Text(message, color = AccentGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            item {
                Text(
                    "Spam candidates: ${spamItems.size} • Duplicates: ${duplicateItems.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            item {
                FilterTabs(
                    currentFilter = tab,
                    onFilterChange = { selectedFilter -> tab = selectedFilter }
                )
            }
            if (remainingVisibleItems == 0) {
                item {
                    FriendlyState(Icons.Default.PermMedia, "No media here yet", "Try another category or refresh the scan.")
                }
            } else {
                items(filtered, key = { mediaItem -> mediaItem.uri.toString() }) { item ->
                    AnimatedVisibility(
                        visible = item.uri.toString() !in removedItemIds,
                        enter = fadeIn(animationSpec = tween(420)) + slideInVertically(
                            animationSpec = tween(420),
                            initialOffsetY = { offset -> offset / 3 }
                        ),
                        exit = fadeOut(animationSpec = tween(320)) + shrinkVertically(animationSpec = tween(320))
                    ) {
                        MediaSwipeRow(
                            item = item,
                            selected = false,
                            onClick = { onOpenInSystem(item) },
                            onDelete = { pendingDelete = item },
                            onKeep = { scope.launch { snackbarHostState.showSnackbar("Kept ${item.safeDisplayName()}") } },
                            onOpen = { onOpenInSystem(item) }
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this file?", color = TextMain) },
            text = { Text("Are you sure you want to delete ${item.safeDisplayName()}?", color = TextSecondary) },
            confirmButton = {
                LegitButton(text = "Open delete flow", onClick = {
                    pendingDelete = null
                    removedItemIds = removedItemIds + item.uri.toString()
                    successMessage = "🎉 ${formatSize(item.sizeKb.toLong() * 1024L)} Freed!"
                    onOpenInSystem(item)
                    scope.launch { snackbarHostState.showSnackbar("Opened ${item.safeDisplayName()} for deletion") }
                })
            },
            dismissButton = {
                LegitButton(text = "Cancel", onClick = { pendingDelete = null })
            },
            containerColor = SurfaceWhite
        )
    }
}
