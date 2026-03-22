package com.example.whatsappcleaner.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.billing.BillingProduct
import com.example.whatsappcleaner.data.billing.SubscriptionPlan
import com.example.whatsappcleaner.data.billing.SubscriptionState
import com.example.whatsappcleaner.ui.components.GradientHeroButton
import com.example.whatsappcleaner.ui.components.LegitButton
import com.example.whatsappcleaner.ui.theme.AccentBlue
import com.example.whatsappcleaner.ui.theme.AccentGreen
import com.example.whatsappcleaner.ui.theme.AccentPurple
import com.example.whatsappcleaner.ui.theme.PrimaryBackground
import com.example.whatsappcleaner.ui.theme.SurfaceMuted
import com.example.whatsappcleaner.ui.theme.SurfaceWhite
import com.example.whatsappcleaner.ui.theme.TextMain
import com.example.whatsappcleaner.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    subscriptionState: SubscriptionState,
    source: String,
    exceededFreeLimit: Boolean,
    onBack: () -> Unit,
    onPurchaseClick: (BillingProduct) -> Unit,
    onRestoreClick: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf(BillingProduct.YEARLY) }

    LaunchedEffect(subscriptionState.currentPlan) {
        selectedPlan = when (subscriptionState.currentPlan) {
            SubscriptionPlan.MONTHLY -> BillingProduct.MONTHLY
            SubscriptionPlan.YEARLY -> BillingProduct.YEARLY
            SubscriptionPlan.LIFETIME -> BillingProduct.LIFETIME
            SubscriptionPlan.FREE -> BillingProduct.YEARLY
        }
    }

    val ctaEnabled = subscriptionState.billingReady && (subscriptionState.hasPrices || subscriptionState.currentPlan != SubscriptionPlan.FREE)

    Scaffold(
        containerColor = PrimaryBackground,
        topBar = {
            TopAppBar(
                title = { Text("Cleanly AI Pro", color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextMain)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroCard(source = source, exceededFreeLimit = exceededFreeLimit, subscriptionState = subscriptionState)
            }
            item {
                FeatureListCard()
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PricingCard(
                        title = "Monthly",
                        subtitle = "Unlock Smart Clean, meme detection, duplicate review, and bulk delete.",
                        price = subscriptionState.prices[BillingProduct.MONTHLY] ?: "Available from Google Play",
                        selected = selectedPlan == BillingProduct.MONTHLY,
                        badge = "Cancel anytime",
                        onClick = { selectedPlan = BillingProduct.MONTHLY }
                    )
                    PricingCard(
                        title = "Yearly",
                        subtitle = "Best for regular cleanup with the lowest recurring cost.",
                        price = subscriptionState.prices[BillingProduct.YEARLY] ?: "Best value on Google Play",
                        selected = selectedPlan == BillingProduct.YEARLY,
                        badge = "BEST VALUE",
                        highlight = true,
                        onClick = { selectedPlan = BillingProduct.YEARLY }
                    )
                    PricingCard(
                        title = "Lifetime",
                        subtitle = "One purchase for permanent Pro access on this Play account.",
                        price = subscriptionState.prices[BillingProduct.LIFETIME] ?: "One-time unlock",
                        selected = selectedPlan == BillingProduct.LIFETIME,
                        badge = "No renewal",
                        onClick = { selectedPlan = BillingProduct.LIFETIME }
                    )
                }
            }
            item {
                GradientHeroButton(
                    text = "Go Pro",
                    onClick = { onPurchaseClick(selectedPlan) },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.AutoAwesome,
                    enabled = ctaEnabled
                )
            }
            item {
                LegitButton(
                    text = "Restore purchase",
                    onClick = onRestoreClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                BillingFootnote(subscriptionState = subscriptionState, ctaEnabled = ctaEnabled)
            }
        }
    }
}

@Composable
private fun HeroCard(source: String, exceededFreeLimit: Boolean, subscriptionState: SubscriptionState) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AccentBlue.copy(alpha = 0.18f),
                            AccentPurple.copy(alpha = 0.16f)
                        )
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(AccentBlue.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AccentBlue)
                    Text("Premium unlock", color = TextMain, style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(
                "Go Pro to clean smarter",
                style = MaterialTheme.typography.headlineMedium,
                color = TextMain,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Unlock premium cleanup flows for Smart Clean, duplicate detection, meme detection, and faster bulk review.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge
            )
            when {
                subscriptionState.isProUser -> {
                    Text(
                        "${subscriptionState.currentPlan.displayName} is already active on this device.",
                        color = AccentGreen,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                exceededFreeLimit -> {
                    Text(
                        "You’ve reached the free advanced cleanup limit. Upgrade to keep using premium tools without interruption.",
                        color = AccentGreen,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                "Opened from ${source.replace('_', ' ').replaceFirstChar { firstChar -> firstChar.uppercase() }}",
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun FeatureListCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("What Pro unlocks", style = MaterialTheme.typography.titleLarge, color = TextMain)
            listOf(
                "Smart Clean advanced cleanup flow",
                "Duplicate detection with grouped review",
                "Meme detection for quick cleanup",
                "Bulk delete shortcuts and premium insights"
            ).forEach { feature ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(AccentGreen.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                            .padding(6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen)
                    }
                    Text(feature, color = TextMain, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun PricingCard(
    title: String,
    subtitle: String,
    price: String,
    selected: Boolean,
    badge: String,
    onClick: () -> Unit,
    highlight: Boolean = false
) {
    val borderBrush = if (selected || highlight) {
        Brush.horizontalGradient(listOf(AccentBlue, AccentPurple))
    } else {
        Brush.horizontalGradient(listOf(SurfaceMuted, SurfaceMuted))
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, brush = borderBrush, shape = RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = TextMain, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .background(
                            if (highlight) AccentPurple.copy(alpha = 0.18f) else AccentBlue.copy(alpha = 0.14f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(badge, color = if (highlight) AccentPurple else AccentBlue, style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(price, color = TextMain, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            if (selected) {
                Spacer(Modifier.height(2.dp))
                Text("Selected plan", color = AccentGreen, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun BillingFootnote(subscriptionState: SubscriptionState, ctaEnabled: Boolean) {
    val message = when {
        subscriptionState.lastMessage != null -> subscriptionState.lastMessage
        !subscriptionState.billingReady -> "Connecting to Google Play Billing…"
        !ctaEnabled -> "Prices are still loading from Google Play. You can restore purchases anytime."
        else -> "Monthly and yearly plans are managed in Google Play. Lifetime is a one-time purchase."
    }
    Text(
        text = message.orEmpty(),
        color = if (subscriptionState.billingReady) TextSecondary else Color(0xFF8A6A00),
        style = MaterialTheme.typography.bodySmall
    )
}
