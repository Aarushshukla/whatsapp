package com.example.whatsappcleaner.ui.paywall

import androidx.compose.foundation.background
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

    LaunchedEffect(subscriptionState.isProUser) {
        if (subscriptionState.isProUser && subscriptionState.currentPlan != SubscriptionPlan.FREE) {
            selectedPlan = when (subscriptionState.currentPlan) {
                SubscriptionPlan.MONTHLY -> BillingProduct.MONTHLY
                SubscriptionPlan.YEARLY -> BillingProduct.YEARLY
                SubscriptionPlan.LIFETIME -> BillingProduct.LIFETIME
                SubscriptionPlan.FREE -> BillingProduct.YEARLY
            }
        }
    }

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
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(AccentBlue.copy(alpha = 0.18f), AccentPurple.copy(alpha = 0.16f))))
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
                        Text("⚡ Clean X GB instantly", style = MaterialTheme.typography.headlineMedium, color = TextMain, fontWeight = FontWeight.Bold)
                        Text(
                            "Unlock the safest premium cleanup flow for duplicates, memes, junk analysis, auto clean reminders, and deeper insights.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (exceededFreeLimit) {
                            Text(
                                "You have reached the free advanced cleanup limit. Go Pro to keep cleaning without interruption.",
                                color = AccentGreen,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            "Source: ${source.replace('_', ' ').replaceFirstChar { firstChar -> firstChar.uppercase() }}",
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            item {
                FeatureListCard()
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PricingCard(
                        title = "Monthly",
                        subtitle = "Try the fastest way to clean on demand",
                        price = subscriptionState.prices[BillingProduct.MONTHLY] ?: "Available in Play Billing",
                        selected = selectedPlan == BillingProduct.MONTHLY,
                        badge = "Cancel anytime",
                        onClick = { selectedPlan = BillingProduct.MONTHLY }
                    )
                    PricingCard(
                        title = "Yearly",
                        subtitle = "Best for regular cleanup and alerts",
                        price = subscriptionState.prices[BillingProduct.YEARLY] ?: "Best value in Play Billing",
                        selected = selectedPlan == BillingProduct.YEARLY,
                        badge = "BEST VALUE",
                        highlight = true,
                        onClick = { selectedPlan = BillingProduct.YEARLY }
                    )
                    PricingCard(
                        title = "Lifetime",
                        subtitle = "One payment. Keep Cleanly AI Pro forever",
                        price = subscriptionState.prices[BillingProduct.LIFETIME] ?: "One-time unlock",
                        selected = selectedPlan == BillingProduct.LIFETIME,
                        badge = "No subscription",
                        onClick = { selectedPlan = BillingProduct.LIFETIME }
                    )
                }
            }
            item {
                GradientHeroButton(
                    text = "🚀 Go Pro Now",
                    onClick = { onPurchaseClick(selectedPlan) },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.AutoAwesome
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
                Text(
                    text = subscriptionState.lastMessage ?: "Cancel anytime on monthly or yearly plans. Purchases and restore flows depend on your Play Billing setup.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
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
            Text("Why Pro works", style = MaterialTheme.typography.titleLarge, color = TextMain)
            listOf(
                "Remove duplicates instantly",
                "Clean WhatsApp junk",
                "AI detects useless files",
                "Free up space in 1 tap"
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
                .background(borderBrush)
                .padding(1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite, RoundedCornerShape(23.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = TextMain, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .background(if (highlight) AccentPurple.copy(alpha = 0.18f) else AccentBlue.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
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
}
