package com.example.whatsappcleaner.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ScreenshotMonitor
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.components.LegitButton
import kotlinx.coroutines.delay

enum class AiFeature(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val actionLabel: String
) {
    SMART_SUGGESTIONS(
        title = "Smart Suggestions",
        subtitle = "AI-picked files to review",
        description = "Review AI-ranked media candidates before cleanup.",
        icon = Icons.Default.AutoAwesome,
        actionLabel = "Review Files"
    ),
    DUPLICATE_DETECTOR(
        title = "Duplicate Detector",
        subtitle = "Find repeated media",
        description = "Detect repeated photos and videos to recover fast storage.",
        icon = Icons.Default.ContentCopy,
        actionLabel = "Scan Now"
    ),
    LARGE_FILES_FINDER(
        title = "Large Files Finder",
        subtitle = "Clean biggest space hogs",
        description = "Surface heavy media files that occupy the most storage.",
        icon = Icons.Default.FolderOpen,
        actionLabel = "Scan Now"
    ),
    OLD_MEDIA_CLEANER(
        title = "Old Media Cleaner",
        subtitle = "Review stale files",
        description = "Find older media that is likely safe to clean.",
        icon = Icons.Default.Schedule,
        actionLabel = "Review Files"
    ),
    WHATSAPP_MEDIA_CLEANER(
        title = "WhatsApp Media Cleaner",
        subtitle = "Tame forwarded media",
        description = "Inspect forwarded WhatsApp media and remove noise quickly.",
        icon = Icons.Default.Collections,
        actionLabel = "Auto Select"
    ),
    MEME_CLEANER(
        title = "Meme Cleaner",
        subtitle = "Review funny but bulky files",
        description = "Browse meme-heavy media and keep only the best ones.",
        icon = Icons.Default.Image,
        actionLabel = "Review Files"
    ),
    BLURRY_PHOTOS(
        title = "Blurry Photos",
        subtitle = "Identify weak-quality shots",
        description = "Collect low-clarity photos for quick cleanup decisions.",
        icon = Icons.Default.BrokenImage,
        actionLabel = "Scan Now"
    ),
    SCREENSHOTS_CLEANER(
        title = "Screenshots Cleaner",
        subtitle = "Remove temporary captures",
        description = "Review screenshots and delete temporary captures in one pass.",
        icon = Icons.Default.ScreenshotMonitor,
        actionLabel = "Review Files"
    ),
    SPAM_MEDIA_DETECTOR(
        title = "Spam Media Detector",
        subtitle = "Detect likely junk media",
        description = "Prioritize media that appears promotional, duplicated, or spammy.",
        icon = Icons.Default.Shield,
        actionLabel = "Scan Now"
    )
}

val AiFeatureItems: List<AiFeature> = listOf(
    AiFeature.SMART_SUGGESTIONS,
    AiFeature.DUPLICATE_DETECTOR,
    AiFeature.LARGE_FILES_FINDER,
    AiFeature.OLD_MEDIA_CLEANER,
    AiFeature.WHATSAPP_MEDIA_CLEANER,
    AiFeature.MEME_CLEANER,
    AiFeature.BLURRY_PHOTOS,
    AiFeature.SCREENSHOTS_CLEANER,
    AiFeature.SPAM_MEDIA_DETECTOR
)

@Composable
fun PremiumSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AiFeatureCard(
    feature: AiFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "ai_feature_card_scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 6.dp,
        animationSpec = tween(durationMillis = 140),
        label = "ai_feature_card_elevation"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isPressed) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(durationMillis = 150),
        label = "ai_feature_card_container"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = feature.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open ${feature.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AiToolsSection(
    onFeatureClick: (AiFeature) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PremiumSectionHeader(
                title = "AI Tools",
                subtitle = "Premium cleanup intelligence built for faster media decisions."
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 3,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AiFeatureItems.forEach { feature ->
                    AiFeatureCard(
                        feature = feature,
                        onClick = { onFeatureClick(feature) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiFeatureScreenScaffold(
    feature: AiFeature,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(feature.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                        Text(feature.subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            content()
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    helper: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(helper, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SmoothPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LegitButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun AiFeatureDetailScreen(
    feature: AiFeature,
    stats: List<Triple<String, String, String>>,
    onBack: () -> Unit,
    onActionClick: () -> Unit
) {
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        contentVisible = true
    }

    AiFeatureScreenScaffold(feature = feature, onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = contentVisible,
                    enter = androidx.compose.animation.fadeIn(animationSpec = tween(300)) +
                        androidx.compose.animation.slideInVertically(initialOffsetY = { it / 3 })
                ) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(feature.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "This screen is fully wired for future AI logic integration.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            items(stats, key = { it.first }) { (title, value, helper) ->
                StatsCard(title = title, value = value, helper = helper)
            }
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Take action",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        SmoothPrimaryButton(text = feature.actionLabel, onClick = onActionClick)
                    }
                }
            }
            item {
                EmptyStateCard(
                    title = "No processed results yet",
                    body = "Run ${feature.actionLabel.lowercase()} to prepare an actionable list for review."
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun SmartSuggestionsFeatureScreen(totalSuggested: Int, totalSpaceToFree: Long, onBack: () -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.SMART_SUGGESTIONS,
        stats = listOf(
            Triple("Suggestions", totalSuggested.toString(), "AI-prioritized items ready for review"),
            Triple("Potential savings", formatSize(totalSpaceToFree), "Estimated recoverable storage"),
            Triple("Confidence", "High", "Built from duplicate, size, and age signals")
        ),
        onBack = onBack,
        onActionClick = {}
    )
}

@Composable
fun DuplicateDetectorFeatureScreen(duplicateCount: Int, onBack: () -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.DUPLICATE_DETECTOR,
        stats = listOf(
            Triple("Detected duplicates", duplicateCount.toString(), "Items sharing similar identity patterns"),
            Triple("Scan quality", "Stable", "Checks remain aligned with your existing media loader"),
            Triple("Mode", "Review-first", "No auto deletion performed from this UI")
        ),
        onBack = onBack,
        onActionClick = {}
    )
}

@Composable
fun LargeFilesFinderFeatureScreen(count: Int, totalBytes: Long, onBack: () -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.LARGE_FILES_FINDER,
        stats = listOf(
            Triple("Large files", count.toString(), "Files over cleanup threshold"),
            Triple("Heavy media size", formatSize(totalBytes), "Storage impact from large files"),
            Triple("Priority", "Top space hogs", "Sorted for fastest storage relief")
        ),
        onBack = onBack,
        onActionClick = {}
    )
}

@Composable
fun OldMediaCleanerFeatureScreen(count: Int, onBack: () -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.OLD_MEDIA_CLEANER,
        stats = listOf(
            Triple("Old files", count.toString(), "Media older than cleanup threshold"),
            Triple("Cleanup strategy", "Conservative", "Review-first to avoid accidental removal"),
            Triple("Recommended", "Monthly", "Run periodically to keep storage fresh")
        ),
        onBack = onBack,
        onActionClick = {}
    )
}

@Composable
fun WhatsAppMediaCleanerFeatureScreen(sentCount: Int, onBack: () -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.WHATSAPP_MEDIA_CLEANER,
        stats = listOf(
            Triple("Forwarded or sent", sentCount.toString(), "WhatsApp-heavy shareable media"),
            Triple("Review workflow", "One tap", "Built for quick batch review sessions"),
            Triple("Safety", "Manual confirmation", "Uses your current delete confirmation flow")
        ),
        onBack = onBack,
        onActionClick = {}
    )
}

@Composable
fun MemeCleanerFeatureScreen(memeCount: Int, onBack: () -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.MEME_CLEANER,
        stats = listOf(
            Triple("Meme candidates", memeCount.toString(), "Detected by current classifier"),
            Triple("Mode", "Review and keep", "Sort humor content before cleanup"),
            Triple("Expected impact", "Medium", "Useful for social-media heavy libraries")
        ),
        onBack = onBack,
        onActionClick = {}
    )
}

@Composable
fun BlurryPhotosFeatureScreen(imageCount: Int, onBack: () -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.BLURRY_PHOTOS,
        stats = listOf(
            Triple("Photos analyzed", imageCount.toString(), "Image pool ready for quality scoring"),
            Triple("Status", "UI ready", "Awaiting future blur-scoring integration"),
            Triple("Workflow", "Safe review", "No automatic cleanup actions performed")
        ),
        onBack = onBack,
        onActionClick = {}
    )
}

@Composable
fun ScreenshotsCleanerFeatureScreen(screenshotCount: Int, onBack: () -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.SCREENSHOTS_CLEANER,
        stats = listOf(
            Triple("Screenshots", screenshotCount.toString(), "Captured temporary images"),
            Triple("Cleanup type", "Quick wins", "Usually low-risk for cleanup"),
            Triple("Batching", "Enabled", "Designed for fast review sessions")
        ),
        onBack = onBack,
        onActionClick = {}
    )
}

@Composable
fun SpamMediaDetectorFeatureScreen(spamCount: Int, onBack: () -> Unit) {
    AiFeatureDetailScreen(
        feature = AiFeature.SPAM_MEDIA_DETECTOR,
        stats = listOf(
            Triple("Potential spam", spamCount.toString(), "Likely junk media candidates"),
            Triple("Source quality", "Current analyzer", "Based on existing spam scoring logic"),
            Triple("Action mode", "Review-first", "Keeps cleanup flow safe and transparent")
        ),
        onBack = onBack,
        onActionClick = {}
    )
}
