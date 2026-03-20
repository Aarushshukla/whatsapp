package com.example.whatsappcleaner.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.ui.theme.AccentBlue
import com.example.whatsappcleaner.ui.theme.AccentError
import com.example.whatsappcleaner.ui.theme.AccentGreen
import com.example.whatsappcleaner.ui.theme.AccentPurple
import com.example.whatsappcleaner.ui.theme.SurfaceMuted
import com.example.whatsappcleaner.ui.theme.SurfaceWhite
import com.example.whatsappcleaner.ui.theme.TextMain
import com.example.whatsappcleaner.ui.theme.TextSecondary

@Composable
fun LegitButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDestructive) AccentError else AccentBlue,
            contentColor = Color.White,
            disabledContainerColor = SurfaceMuted
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp),
        modifier = modifier
            .height(54.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun GradientHeroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.AutoAwesome
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "hero_scale")
    val brush = Brush.horizontalGradient(listOf(AccentBlue, AccentPurple))

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(),
        modifier = modifier
            .height(60.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush, RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.size(10.dp))
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun LegitCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(22.dp),
        modifier = modifier.fillMaxWidth(),
        content = content
    )
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.18f),
            Color.White.copy(alpha = 0.08f)
        ),
        start = Offset.Zero,
        end = Offset(translateAnim, translateAnim)
    )
    background(brush, RoundedCornerShape(18.dp))
}

@Composable
fun FriendlyState(
    icon: ImageVector,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(AccentGreen.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(34.dp), tint = AccentGreen)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextMain)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
fun StorageRing(progress: Float, label: String, subtitle: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(90.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            trackColor = SurfaceMuted,
            strokeWidth = 8.dp,
            color = AccentBlue,
            modifier = Modifier.size(90.dp)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = TextMain)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}
