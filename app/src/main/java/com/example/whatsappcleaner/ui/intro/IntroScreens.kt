package com.example.whatsappcleaner.ui.intro

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape // <-- Added this Import
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.ui.theme.*
import com.example.whatsappcleaner.ui.components.LegitButton
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Show logo for 2.5 seconds
        onSplashFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(BrandNavy),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Logo Placeholder
                Box(Modifier.size(80.dp).background(AccentBlue, CircleShape))
                Spacer(Modifier.height(16.dp))
                Text("Cleaner Pro", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var step by remember { mutableStateOf(0) }

    val titles = listOf("Smart Cleaning", "Secure & Private", "Boost Storage")
    val descs = listOf(
        "Automatically detect junk files and duplicates.",
        "Your data never leaves your device.",
        "Reclaim gigabytes of space in seconds."
    )

    Column(
        modifier = Modifier.fillMaxSize().background(PrimaryBackground).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))

        // Illustration Placeholder
        Box(
            Modifier
                .size(250.dp)
                .background(Color.LightGray.copy(0.2f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Step ${step + 1}", style = MaterialTheme.typography.displayMedium, color = BrandNavy.copy(0.2f))
        }

        Spacer(Modifier.height(40.dp))

        // Text Content
        Text(titles[step], style = MaterialTheme.typography.headlineMedium, color = BrandNavy, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(descs[step], style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

        Spacer(Modifier.weight(1f))

        // Navigation Buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (step < 2) {
                TextButton(onClick = onFinished) { Text("Skip", color = TextSecondary) }
                LegitButton("Next", { step++ }, Modifier.width(120.dp))
            } else {
                Spacer(Modifier.width(16.dp))
                LegitButton("Get Started", onFinished, Modifier.fillMaxWidth())
            }
        }
    }
}