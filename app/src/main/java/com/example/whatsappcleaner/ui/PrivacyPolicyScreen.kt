package com.example.whatsappcleaner.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyScreen(modifier: Modifier = Modifier,onBack: () -> Unit = {}) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Privacy Policy", style = MaterialTheme.typography.headlineSmall)
        Text("ChatSweep scans chat media locally on your device only.")
        Text("No cloud upload is required for cleanup suggestions.")
        Text("ChatSweep does not run a backend database for your personal media files.")
        Text("If configured, Firebase Analytics may collect basic app usage events.")
        Text("If configured, Firebase Crashlytics may collect crash diagnostics.")
        Text("You manually review and confirm every deletion.")
        Text("ChatSweep is not affiliated with WhatsApp LLC or Meta Platforms, Inc.")
    }
}
