package com.example.whatsappcleaner.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Bg = Color(0xFFF7F9FC)
private val Green = Color(0xFF20A64A)
private val MainText = Color(0xFF20242A)
private val SecondaryText = Color(0xFF6B7280)

@Composable
fun PermissionIntroScreen(
    onAllow: () -> Unit,
    message: String?,
    showOpenSettings: Boolean,
    onOpenSettings: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Bg).padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Your chat cleanup is just two steps away", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MainText)
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) { Text("Storage access needed", Modifier.padding(16.dp), color = MainText) }
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Give media access", color = MainText, fontWeight = FontWeight.SemiBold)
                    Text("We need permission to scan your chat photos, videos, audio, and files.", color = SecondaryText)
                    Text("Scan chat junk", color = MainText, fontWeight = FontWeight.SemiBold)
                    Text("We’ll show what can be safely reviewed to free up space.", color = SecondaryText)
                }
            }
            if (!message.isNullOrBlank()) Text(message, color = Color(0xFFEF4444))
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onAllow, modifier = Modifier.fillMaxWidth()) { Text("ALLOW STORAGE ACCESS") }
            if (!message.isNullOrBlank()) {
                Button(onClick = onAllow, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5A623), contentColor = Color.White)) { Text("Try again") }
                if (showOpenSettings) {
                    Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MainText)) { Text("Open settings") }
                }
            }
        }
    }
}

@Composable
fun CheckSuccessScreen(title: String, subtitle: String, buttonText: String, onContinue: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) { visible = true }
    Column(Modifier.fillMaxSize().background(Bg), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        AnimatedVisibility(visible, enter = scaleIn() + fadeIn()) {
            Box(Modifier.size(110.dp).background(Green.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                androidx.compose.material3.Icon(Icons.Default.Check, null, tint = Green, modifier = Modifier.size(54.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MainText)
        Text(subtitle, color = SecondaryText)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onContinue, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F6FED), contentColor = Color.White)) { Text(buttonText) }
    }
}

@Composable
fun ScanIntroScreen(onScan: () -> Unit, scanning: Boolean) {
    val scale by animateFloatAsState(if (scanning) 0.96f else 1f, tween(180), label = "scan_button_scale")
    Column(Modifier.fillMaxSize().background(Bg), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("ChatSweep", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MainText)
        Text("Private offline media cleaner", color = SecondaryText)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onScan,
            enabled = !scanning,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.size(170.dp).scale(scale)
        ) { Text(if (scanning) "SCANNING..." else "SCAN") }
        Spacer(Modifier.height(18.dp))
        Text("Find duplicate, large, old, and junk chat media.", color = SecondaryText)
        Text("Offline scan • No auto-delete • Review first", color = SecondaryText)
    }
}

@Composable
fun ScanProgressScreen(scanUiState: ScanUiState) {
    val stages = listOf(
        "Reading chat folders",
        "Finding duplicates",
        "Checking large videos",
        "Detecting old media",
        "Finding statuses and stickers",
        "Preparing results"
    )
    val realProgress = (scanUiState as? ScanUiState.Loading)?.progress?.coerceIn(0f, 1f) ?: 0f
    val isLoading = scanUiState is ScanUiState.Loading
    var visualProgress by remember { mutableStateOf(0f) }

    androidx.compose.runtime.LaunchedEffect(isLoading, realProgress) {
        if (!isLoading) {
            visualProgress = 1f
        } else {
            visualProgress = maxOf(realProgress, (visualProgress + 0.06f).coerceAtMost(0.9f))
        }
    }
    val animatedProgress by animateFloatAsState(
        targetValue = visualProgress,
        animationSpec = tween(durationMillis = 320),
        label = "smooth_scan_progress"
    )

    val stageIndex = remember(realProgress) {
        ((realProgress.coerceAtMost(0.999f)) * stages.size).toInt().coerceIn(0, stages.lastIndex)
    }
    val pulse = rememberInfiniteTransition(label = "stage_pulse").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(850), repeatMode = RepeatMode.Reverse),
        label = "pulse"
    )

    Column(Modifier.fillMaxSize().background(Bg).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.size(160.dp), strokeWidth = 12.dp)
            Text("${(animatedProgress * 100).toInt()}%")
        }
        Spacer(Modifier.height(20.dp))
        stages.forEachIndexed { index, label ->
            val done = index < stageIndex
            val current = index == stageIndex && isLoading
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(0.86f).padding(vertical = 6.dp)) {
                if (done) {
                    androidx.compose.material3.Icon(Icons.Default.Check, null, tint = Green, modifier = Modifier.size(18.dp))
                } else {
                    Box(
                        Modifier.size(10.dp).background(if (current) Green.copy(alpha = pulse.value) else Color(0xFFD1D5DB), CircleShape)
                    )
                }
                Spacer(Modifier.width(10.dp))
                AnimatedContent(targetState = label, transitionSpec = {
                    (fadeIn(tween(260)) + slideInVertically { it / 2 }) togetherWith fadeOut(tween(220))
                }, label = "stage_text") {
                    Text(it, fontWeight = if (current) FontWeight.SemiBold else FontWeight.Normal, color = if (done || current) Color(0xFF0F172A) else Color(0xFF6B7280))
                }
            }
        }
    }
}
