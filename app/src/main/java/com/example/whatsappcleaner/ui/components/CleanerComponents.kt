package com.example.whatsappcleaner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))))
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun PrimaryGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val isEnabled = enabled && !loading
    Button(onClick = onClick, enabled = isEnabled, modifier = modifier.height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)), MaterialTheme.shapes.large)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
            }
            Text(text = text, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    OutlinedButton(onClick = onClick, enabled = enabled && !loading, modifier = modifier.height(52.dp)) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}

enum class SafetyLevel { SAFE_TO_DELETE, PROBABLY_JUNK, REVIEW_CAREFULLY, PROTECTED }

@Composable
fun SafetyBadge(level: SafetyLevel, modifier: Modifier = Modifier) {
    val (label, color) = when (level) {
        SafetyLevel.SAFE_TO_DELETE -> "Safe to delete" to Color(0xFF1B8F5A)
        SafetyLevel.PROBABLY_JUNK -> "Probably junk" to Color(0xFF4C6FFF)
        SafetyLevel.REVIEW_CAREFULLY -> "Review carefully" to Color(0xFFB77B00)
        SafetyLevel.PROTECTED -> "Protected" to Color(0xFF8B2E2E)
    }
    Surface(modifier = modifier, color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
fun StorageStatCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CategoryCard(title: String, countLabel: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(countLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptyStateView(title: String, subtitle: String, modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier.fillMaxWidth()) {
        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ErrorStateView(title: String, subtitle: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    PremiumCard(modifier = modifier.fillMaxWidth()) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (onRetry != null) {
            Spacer(Modifier.height(12.dp))
            SecondaryActionButton(text = "Retry", onClick = onRetry)
        }
    }
}

@Composable
fun LoadingScanStageView(stageLabel: String, modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
            Spacer(Modifier.width(10.dp))
            Text(stageLabel, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun FileGridItem(
    filename: String,
    duration: String,
    sizeLabel: String,
    checked: Boolean,
    riskLevel: SafetyLevel,
    thumbnailModel: Any?,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    PremiumCard(modifier = modifier) {
        var thumbnailFailed by remember(thumbnailModel) { mutableStateOf(thumbnailModel == null) }
        Box(Modifier.fillMaxWidth().aspectRatio(1.35f).clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant)) {
            AsyncImage(
                model = thumbnailModel,
                contentDescription = filename,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                onSuccess = { thumbnailFailed = false },
                onError = { thumbnailFailed = true }
            )
            if (thumbnailFailed) {
                Icon(Icons.Default.BrokenImage, contentDescription = null, modifier = Modifier.align(Alignment.Center), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
                Text(sizeLabel, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(filename, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(duration, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            SafetyBadge(level = riskLevel)
            FilterChip(selected = checked, onClick = { onCheckedChange(!checked) }, label = { Text(if (checked) "Selected" else "Select") })
        }
    }
}

@Composable
fun StickySelectionBar(selectedCount: Int, selectedSize: String, modifier: Modifier = Modifier, onDeleteClick: () -> Unit = {}, loading: Boolean = false) {
    Surface(modifier = modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("$selectedCount selected", fontWeight = FontWeight.SemiBold)
                Text(selectedSize, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            PrimaryGradientButton(text = "Delete", onClick = onDeleteClick, loading = loading, modifier = Modifier.width(140.dp))
        }
    }
}

@Composable
fun ConfirmDeleteSheet(itemCount: Int, totalSize: String, modifier: Modifier = Modifier, loading: Boolean = false, onConfirm: () -> Unit = {}, onCancel: () -> Unit = {}) {
    PremiumCard(modifier = modifier.fillMaxWidth()) {
        Text("Confirm deletion", style = MaterialTheme.typography.titleLarge)
        Text("Delete $itemCount items ($totalSize)?", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryActionButton(text = "Cancel", onClick = onCancel, modifier = Modifier.weight(1f))
            PrimaryGradientButton(text = "Confirm", onClick = onConfirm, loading = loading, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun CleanupReceiptCard(title: String, lines: List<String>, modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        lines.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
fun PermissionExplanationCard(title: String, description: String, modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(6.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
