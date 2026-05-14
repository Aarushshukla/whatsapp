package com.example.whatsappcleaner.ui

import androidx.compose.foundation.layout.Arrangement
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
fun TermsAndConditionsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TextButton(onClick = onBack) {
            Text(text = "Back")
        }

        Text(
            text = "Terms and Conditions",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 8.dp)
        )

        TermsSection(
            title = "Use of the app",
            body = "This app helps you review and clean chat-related media on your device. By using it, you agree to use it responsibly and only on files you are authorized to manage."
        )
        TermsSection(
            title = "File deletion",
            body = "Deletion actions are initiated by you. Always review selected files before confirming removal to avoid deleting content you want to keep."
        )
        TermsSection(
            title = "Local processing",
            body = "Core scanning and cleanup behavior is designed to run locally on your device. Your personal media is not required to be uploaded for standard cleanup flows."
        )
        TermsSection(
            title = "No recovery guarantee",
            body = "Some deletions may be permanent depending on your device and storage behavior. Recovery is not guaranteed after deletion is confirmed."
        )
        TermsSection(
            title = "User responsibility",
            body = "You are responsible for backups, reviewing results, and verifying any cleanup operation before proceeding."
        )
    }
}

@Composable
private fun TermsSection(title: String, body: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium
    )
}
