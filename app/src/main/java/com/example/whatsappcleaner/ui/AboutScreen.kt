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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(modifier: Modifier = Modifier,onBack: () -> Unit = {}, versionLabel: String = "") {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("About ChatSweep", style = MaterialTheme.typography.headlineSmall)
        Text("Private offline media cleaner")
        if (versionLabel.isNotBlank()) Text("Version: $versionLabel")
        Text("This app is not affiliated with, endorsed by, or sponsored by WhatsApp LLC or Meta Platforms, Inc.", fontWeight = FontWeight.SemiBold)
    }
}
