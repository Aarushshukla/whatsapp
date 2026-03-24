package com.example.whatsappcleaner.ui.home

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import com.example.whatsappcleaner.data.MediaItem

@Composable
fun DeleteTestScreen() {
    val context = LocalContext.current
    var status by remember { mutableStateOf("Idle") }
    var mediaItems by remember { mutableStateOf(emptyList<MediaItem>()) }
    var selectedItems by remember { mutableStateOf(emptyList<MediaItem>()) }

    fun reloadItems() {
        Log.d("DELETE_DEBUG", "Reloading MediaStore items")
        mediaItems = loadMediaStoreItems(context, limit = 30)
        selectedItems = mediaItems.take(3)
        Log.d("DELETE_DEBUG", "Loaded ${mediaItems.size} items")
        Log.d("DELETE_DEBUG", "Selected ${selectedItems.size} items for delete test")
        mediaItems.forEachIndexed { index, item ->
            Log.d("DELETE_DEBUG", "Item[$index] id=${item.id}, uri=${item.uri}")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        Log.d("DELETE_DEBUG", "Permission results=$results")
        val granted = results.isNotEmpty() && results.values.all { it }
        status = if (granted) {
            "Read permission granted"
        } else {
            "Read permission denied"
        }
        if (granted) {
            reloadItems()
        }
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("DELETE_DEBUG", "Delete success")
            status = "Delete success"
        } else {
            Log.e("DELETE_DEBUG", "Delete cancelled")
            status = "Delete cancelled"
        }
        reloadItems()
    }

    fun requestDelete(context: Context, uris: List<android.net.Uri>) {
        Log.d("DELETE_DEBUG", "Delete called with ${uris.size} items")

        if (uris.isEmpty()) {
            Log.e("DELETE_DEBUG", "Empty URI list ❌")
            status = "No selected items"
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Log.e("DELETE_DEBUG", "MediaStore delete popup requires Android 11+")
            status = "Delete requires Android 11+"
            return
        }

        uris.forEach {
            Log.d("DELETE_DEBUG", "URI: $it")
        }

        try {
            val pendingIntent = MediaStore.createDeleteRequest(
                context.contentResolver,
                uris
            )
            Log.d("DELETE_DEBUG", "Delete request created")

            val request = IntentSenderRequest.Builder(
                pendingIntent.intentSender
            ).build()

            deleteLauncher.launch(request)
            Log.d("DELETE_DEBUG", "Delete launcher triggered 🚀")
        } catch (e: Exception) {
            Log.e("DELETE_DEBUG", "Delete error", e)
            status = "Delete launch failed: ${e.message}"
        }
    }

    LaunchedEffect(Unit) {
        val requiredPermissions = requiredMediaReadPermissions()
        val allGranted = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        Log.d("DELETE_DEBUG", "Required permissions=${requiredPermissions.joinToString()}, allGranted=$allGranted")
        if (allGranted) {
            status = "Permission already granted"
            reloadItems()
        } else {
            Log.d("DELETE_DEBUG", "Launching media permission request")
            permissionLauncher.launch(requiredPermissions)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "DeleteTestScreen", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Status: $status")
        Text(text = "Loaded items: ${mediaItems.size}")
        Text(text = "Selected items: ${selectedItems.size}")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { reloadItems() }) {
                Text("Reload")
            }

            Button(
                enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && selectedItems.isNotEmpty(),
                onClick = {
                    Log.d("DELETE_DEBUG", "Delete button clicked")
                    val uris = selectedItems.map { it.uri }
                    requestDelete(context, uris)
                }
            ) {
                Text("Delete selected")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mediaItems, key = { it.id }) { item ->
                Column {
                    Text(text = item.name.ifBlank { "(unnamed)" })
                    Text(text = item.uri.toString(), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun loadMediaStoreItems(context: Context, limit: Int): List<MediaItem> {
    val output = mutableListOf<MediaItem>()

    fun loadFromCollection(collection: android.net.Uri) {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE
        )

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)

            while (cursor.moveToNext() && output.size < limit) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collection, id)
                output.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        name = cursor.getString(nameColumn).orEmpty(),
                        size = cursor.getLong(sizeColumn)
                    )
                )
            }
        }
    }

    loadFromCollection(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    if (output.size < limit) {
        loadFromCollection(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
    }

    return output.take(limit)
}

private fun requiredMediaReadPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
