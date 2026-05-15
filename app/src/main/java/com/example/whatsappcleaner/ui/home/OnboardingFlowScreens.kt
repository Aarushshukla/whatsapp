package com.example.whatsappcleaner.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Bg = Color(0xFFF7F9FC)
private val Green = Color(0xFF20A64A)

@Composable
fun PermissionIntroScreen(onAllow: () -> Unit, message: String?) {
    Column(Modifier.fillMaxSize().background(Bg).padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Your cleanup is just two steps away", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) { Text("Storage access needed", Modifier.padding(16.dp)) }
            Text("1. Give storage access\nWe need permission to scan your chat photos, videos, audio, and files.")
            Text("2. Scan for junk\nWe’ll show what can be safely reviewed to free up space.")
            if (!message.isNullOrBlank()) Text(message, color = Color(0xFFEF4444))
        }
        Button(onClick = onAllow, modifier = Modifier.fillMaxWidth()) { Text("ALLOW STORAGE ACCESS") }
    }
}

@Composable
fun CheckSuccessScreen(title: String, subtitle: String, buttonText: String, onContinue: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    visible = true
    Column(Modifier.fillMaxSize().background(Bg), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        AnimatedVisibility(visible, enter = scaleIn() + fadeIn()) {
            Box(Modifier.size(110.dp).background(Green.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                androidx.compose.material3.Icon(Icons.Default.Check, null, tint = Green, modifier = Modifier.size(54.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(subtitle)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onContinue) { Text(buttonText) }
    }
}

@Composable
fun ScanIntroScreen(onScan: () -> Unit, scanning: Boolean) {
    Column(Modifier.fillMaxSize().background(Bg), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("ChatSweep", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Private offline media cleaner")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onScan, enabled = !scanning, shape = CircleShape, modifier = Modifier.size(170.dp)) { Text("SCAN") }
        Spacer(Modifier.height(18.dp))
        Text("Find duplicate, large, old, and junk chat media.")
        Text("Offline scan • No auto-delete • Review first")
    }
}

@Composable
fun ScanProgressScreen(scanUiState: ScanUiState) {
    val progress = (scanUiState as? ScanUiState.Loading)?.progress ?: 0.2f
    val stage = (scanUiState as? ScanUiState.Loading)?.stage ?: "Finding junk…"
    Column(Modifier.fillMaxSize().background(Bg).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(160.dp), strokeWidth = 12.dp)
            Text("${(progress * 100).toInt()}%")
        }
        Spacer(Modifier.height(16.dp))
        AnimatedContent(targetState = stage, label = "stage") { Text(it, fontWeight = FontWeight.SemiBold) }
    }
}
