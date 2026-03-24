package com.example.whatsappcleaner.ui.home

import android.app.Activity
import android.app.RecoverableSecurityException
import android.net.Uri
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.MediaItem
import com.example.whatsappcleaner.data.MediaStoreRepository

@Composable
fun DeleteTestScreen() {
    val tag = "DeleteTestScreen"
    val context = LocalContext.current
    val repo = remember(context) { MediaStoreRepository(context) }

    var items by remember { mutableStateOf(emptyList<MediaItem>()) }
    var selectedItems by remember { mutableStateOf(emptyList<MediaItem>()) }
    var message by remember { mutableStateOf("Idle") }
    var pendingQDeleteUris by remember { mutableStateOf(emptyList<Uri>()) }

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.i(tag, "Delete confirmation result=OK")
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && pendingQDeleteUris.isNotEmpty()) {
                var deletedCount = 0
                pendingQDeleteUris.forEach { uri ->
                    try {
                        deletedCount += context.contentResolver.delete(uri, null, null)
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to delete approved URI on Q: $uri", e)
                    }
                }
                pendingQDeleteUris = emptyList()
                if (deletedCount > 0) {
                    message = "Deleted successfully"
                    items = repo.loadImages().take(10)
                    selectedItems = emptyList()
                    Log.i(tag, "Q approved delete success, deletedCount=$deletedCount")
                } else {
                    message = "Delete failed"
                    Log.e(tag, "Q approved delete had no successful deletes")
                }
            } else {
                message = "Deleted successfully"
                items = repo.loadImages().take(10)
                selectedItems = emptyList()
                Log.i(tag, "Delete completed successfully")
            }
        } else {
            message = "Delete cancelled"
            Log.w(tag, "Delete cancelled by user")
            pendingQDeleteUris = emptyList()
        }
    }

    fun isMediaStoreUri(uri: Uri): Boolean {
        return uri.scheme == "content" && uri.authority == MediaStore.AUTHORITY
    }

    fun requestDelete() {
        val uris = selectedItems.map { it.uri }.filter { isMediaStoreUri(it) }

        if (uris.isEmpty()) {
            message = "No items selected"
            Log.w(tag, "Delete requested with no valid MediaStore URIs")
            return
        }

        Log.i(tag, "Delete requested. sdk=${Build.VERSION.SDK_INT}, uriCount=${uris.size}")

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                Log.i(tag, "Using Android 11+ delete path: MediaStore.createDeleteRequest")
                try {
                    val pendingIntent = MediaStore.createDeleteRequest(
                        context.contentResolver,
                        uris
                    )
                    val request = IntentSenderRequest.Builder(
                        pendingIntent.intentSender
                    ).build()
                    deleteLauncher.launch(request)
                    message = "Delete request sent"
                } catch (e: Exception) {
                    Log.e(tag, "Android 11+ delete flow failed", e)
                    message = "Delete failed"
                }
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                Log.i(tag, "Using Android 10 delete path: direct delete + RecoverableSecurityException")
                var deletedCount = 0
                val recoverableUris = mutableListOf<Uri>()
                var recoverableIntentSender: android.content.IntentSender? = null

                uris.forEach { uri ->
                    try {
                        deletedCount += context.contentResolver.delete(uri, null, null)
                    } catch (e: RecoverableSecurityException) {
                        Log.w(tag, "Recoverable delete required on Q for uri=$uri")
                        recoverableUris += uri
                        if (recoverableIntentSender == null) {
                            recoverableIntentSender = e.userAction.actionIntent.intentSender
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Delete failed on Q for uri=$uri", e)
                    }
                }

                if (recoverableUris.isNotEmpty()) {
                    pendingQDeleteUris = recoverableUris
                    if (recoverableIntentSender != null) {
                        val request = IntentSenderRequest.Builder(
                            recoverableIntentSender!!
                        ).build()
                        deleteLauncher.launch(request)
                        message = "Delete request sent"
                        Log.i(tag, "Launched recoverable delete confirmation on Q")
                    } else {
                        Log.e(tag, "Recoverable URIs found on Q but no intent sender available")
                        message = if (deletedCount > 0) "Deleted successfully" else "Delete failed"
                    }
                } else {
                    if (deletedCount > 0) {
                        message = "Deleted successfully"
                        items = repo.loadImages().take(10)
                        selectedItems = emptyList()
                        Log.i(tag, "Q direct delete success, deletedCount=$deletedCount")
                    } else {
                        message = "Delete failed"
                        Log.e(tag, "Q delete failed for all URIs")
                    }
                }
            }
            else -> {
                Log.i(tag, "Using Android 9 and below delete path: direct delete")
                var deletedCount = 0
                uris.forEach { uri ->
                    try {
                        deletedCount += context.contentResolver.delete(uri, null, null)
                    } catch (e: Exception) {
                        Log.e(tag, "Delete failed on API<29 for uri=$uri", e)
                    }
                }

                if (deletedCount > 0) {
                    message = "Deleted successfully"
                    items = repo.loadImages().take(10)
                    selectedItems = emptyList()
                    Log.i(tag, "API<29 direct delete success, deletedCount=$deletedCount")
                } else {
                    message = "Delete failed"
                    Log.e(tag, "API<29 delete failed for all URIs")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        items = repo.loadImages().take(10)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "DeleteTestScreen", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Loaded items: ${items.size}")
        Text(text = "Selected items: ${selectedItems.size}")
        Text(text = "Message: $message")

        Button(onClick = { requestDelete() }) {
            Text("DELETE SELECTED")
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                val isSelected = item in selectedItems
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedItems = if (isSelected) {
                                selectedItems - item
                            } else {
                                selectedItems + item
                            }
                        }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.name.ifBlank { "(unnamed)" })
                        Text(
                            text = item.uri.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(text = if (isSelected) "✓" else "")
                }
            }
        }
    }
}
