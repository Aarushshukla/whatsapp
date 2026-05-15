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
fun TermsAndConditionsScreen(modifier: Modifier = Modifier,onBack: () -> Unit = {}) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Terms & Conditions", style = MaterialTheme.typography.headlineSmall)
        Text("You review selected files before deletion.")
        Text("Deleted files may not be recoverable on all devices.")
        Text("ChatSweep is a utility tool for chat media cleanup.")
        Text("ChatSweep is not affiliated with WhatsApp LLC or Meta Platforms, Inc.")
    }
}
