package com.example.whatsappcleaner.ui.home

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
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

    fun reloadItems() {
        Log.d("DELETE_DEBUG", "Reloading MediaStore items")
        mediaItems = loadMediaStoreItems(context, limit = 10)
        Log.d("DELETE_DEBUG", "Loaded ${mediaItems.size} items")
        mediaItems.forEachIndexed { index, item ->
            Log.d("DELETE_DEBUG", "Item[$index] id=${item.id}, uri=${item.uri}, mime=${item.mimeType}")
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
        val approved = result.resultCode == ComponentActivity.RESULT_OK
        Log.d("DELETE_DEBUG", "Delete launcher resultCode=${result.resultCode}, approved=$approved")
        status = if (approved) "Delete approved by system dialog" else "Delete cancelled"
        reloadItems()
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

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { reloadItems() }) {
                Text("Reload")
            }

            Button(
                enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && mediaItems.isNotEmpty(),
                onClick = {
                    val deleteTargets = mediaItems.take(3)
                    val uris = deleteTargets.map(MediaItem::uri)
                    Log.d("DELETE_DEBUG", "Delete button pressed with ${uris.size} URIs")
                    uris.forEachIndexed { index, uri ->
                        Log.d("DELETE_DEBUG", "Delete target[$index]=$uri")
                    }

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                        status = "Delete request requires Android 11+"
                        Log.w("DELETE_DEBUG", status)
                        return@Button
                    }

                    runCatching {
                        Log.d("DELETE_DEBUG", "Creating MediaStore delete request")
                        val request = MediaStore.createDeleteRequest(context.contentResolver, uris)
                        val intentSenderRequest = IntentSenderRequest.Builder(request.intentSender).build()
                        Log.d("DELETE_DEBUG", "Launching StartIntentSenderForResult delete request")
                        deleteLauncher.launch(intentSenderRequest)
                    }.onFailure { error ->
                        Log.e("DELETE_DEBUG", "Failed to launch delete request", error)
                        status = "Delete launch failed: ${error.message}"
                    }
                }
            ) {
                Text("Delete first 3")
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
    val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.MIME_TYPE
    )

    val output = mutableListOf<MediaItem>()

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
        val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

        while (cursor.moveToNext() && output.size < limit) {
            val id = cursor.getLong(idColumn)
            val uri = ContentUris.withAppendedId(collection, id)
            output.add(
                MediaItem(
                    id = id,
                    uri = uri,
                    name = cursor.getString(nameColumn).orEmpty(),
                    size = cursor.getLong(sizeColumn),
                    mimeType = cursor.getString(mimeColumn)
                )
            )
        }
    }

    return output
}

private fun requiredMediaReadPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
