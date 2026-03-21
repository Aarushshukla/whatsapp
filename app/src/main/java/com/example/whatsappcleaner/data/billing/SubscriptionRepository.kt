package com.example.whatsappcleaner.data.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.whatsappcleaner.data.analytics.AppAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val PREFS_NAME = "subscription_prefs"
private const val KEY_IS_PRO = "is_pro_user"
private const val KEY_PLAN = "current_plan"

enum class BillingProduct(val productId: String, val productType: String, val label: String) {
    MONTHLY("cleanly_ai_monthly", BillingClient.ProductType.SUBS, "Monthly"),
    YEARLY("cleanly_ai_yearly", BillingClient.ProductType.SUBS, "Yearly"),
    LIFETIME("cleanly_ai_lifetime", BillingClient.ProductType.INAPP, "Lifetime")
}

enum class SubscriptionPlan(val displayName: String) {
    FREE("Free"),
    MONTHLY("Monthly Pro"),
    YEARLY("Yearly Pro"),
    LIFETIME("Lifetime Pro")
}

data class SubscriptionState(
    val isProUser: Boolean = false,
    val currentPlan: SubscriptionPlan = SubscriptionPlan.FREE,
    val prices: Map<BillingProduct, String> = emptyMap(),
    val billingReady: Boolean = false,
    val lastMessage: String? = null
)

class SubscriptionRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val analytics = AppAnalytics.get(appContext)
    private val productDetails = mutableMapOf<BillingProduct, ProductDetails>()
    private val _state = MutableStateFlow(
        SubscriptionState(
            isProUser = prefs.getBoolean(KEY_IS_PRO, false),
            currentPlan = SubscriptionPlan.valueOf(prefs.getString(KEY_PLAN, SubscriptionPlan.FREE.name) ?: SubscriptionPlan.FREE.name)
        )
    )
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(appContext)
            .setListener(::onPurchasesUpdated)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()
    }

    fun start() {
        if (billingClient.isReady) {
            queryCatalogAndPurchases()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                val ready = result.responseCode == BillingClient.BillingResponseCode.OK
                _state.update { it.copy(billingReady = ready, lastMessage = result.debugMessage.takeIf { msg -> msg.isNotBlank() }) }
                if (ready) queryCatalogAndPurchases()
            }

            override fun onBillingServiceDisconnected() {
                _state.update { it.copy(billingReady = false, lastMessage = "Billing temporarily unavailable") }
            }
        })
    }

    fun launchPurchase(activity: Activity, product: BillingProduct, source: String): BillingResult? {
        val details = productDetails[product] ?: run {
            analytics.trackPurchaseFailed(product.name.lowercase(), "missing_product_details")
            _state.update { it.copy(lastMessage = "Pricing is still loading. Please try again in a moment.") }
            return null
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply {
                if (product.productType == BillingClient.ProductType.SUBS) {
                    val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
                    if (offerToken != null) setOfferToken(offerToken)
                }
            }
            .build()
        analytics.trackPurchaseClicked(product.name.lowercase(), source)
        return billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productParams)).build()
        )
    }

    fun restorePurchases(source: String = "settings") {
        analytics.trackRestorePurchaseClicked(source)
        queryActivePurchases()
    }

    fun refreshPurchases() {
        queryActivePurchases()
    }

    private fun queryCatalogAndPurchases() {
        queryProductDetails()
        queryActivePurchases()
    }

    private fun queryProductDetails() {
        val products = BillingProduct.entries.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it.productId)
                .setProductType(it.productType)
                .build()
        }
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(products).build()
        ) { _, detailsList ->
            val priceMap = buildMap {
                detailsList.forEach { details ->
                    val match = BillingProduct.entries.firstOrNull { it.productId == details.productId } ?: return@forEach
                    productDetails[match] = details
                    val formattedPrice = when (match) {
                        BillingProduct.MONTHLY, BillingProduct.YEARLY -> details.subscriptionOfferDetails
                            ?.firstOrNull()
                            ?.pricingPhases
                            ?.pricingPhaseList
                            ?.firstOrNull()
                            ?.formattedPrice
                        BillingProduct.LIFETIME -> details.oneTimePurchaseOfferDetails?.formattedPrice
                    }
                    if (formattedPrice != null) put(match, formattedPrice)
                }
            }
            _state.update { current -> current.copy(prices = priceMap.ifEmpty { current.prices }) }
        }
    }

    private fun queryActivePurchases() {
        if (!billingClient.isReady) return
        val results = mutableListOf<Purchase>()
        val listener = PurchasesResponseListener { _, purchases ->
            results += purchases
            if (results.size >= 0) updateFromPurchases(results)
        }
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
            listener
        )
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
            listener
        )
    }

    private fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            updateFromPurchases(purchases)
            return
        }
        if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            val attemptedPlan = _state.value.currentPlan.name.lowercase()
            analytics.trackPurchaseFailed(attemptedPlan, result.debugMessage.ifBlank { "billing_error_${result.responseCode}" })
        }
        _state.update { it.copy(lastMessage = result.debugMessage.takeIf { message -> message.isNotBlank() }) }
    }

    private fun updateFromPurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                billingClient.acknowledgePurchase(params) { }
            }
        }
        val activePurchase = purchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        val plan = when {
            activePurchase == null -> SubscriptionPlan.FREE
            activePurchase.products.contains(BillingProduct.LIFETIME.productId) -> SubscriptionPlan.LIFETIME
            activePurchase.products.contains(BillingProduct.YEARLY.productId) -> SubscriptionPlan.YEARLY
            activePurchase.products.contains(BillingProduct.MONTHLY.productId) -> SubscriptionPlan.MONTHLY
            else -> SubscriptionPlan.FREE
        }
        persistState(plan != SubscriptionPlan.FREE, plan)
        if (plan != SubscriptionPlan.FREE) {
            analytics.trackPurchaseSuccess(plan.name.lowercase())
        }
    }

    private fun persistState(isPro: Boolean, plan: SubscriptionPlan) {
        prefs.edit().putBoolean(KEY_IS_PRO, isPro).putString(KEY_PLAN, plan.name).apply()
        _state.update { it.copy(isProUser = isPro, currentPlan = plan) }
    }

    companion object {
        @Volatile
        private var INSTANCE: SubscriptionRepository? = null

        fun get(context: Context): SubscriptionRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SubscriptionRepository(context).also { INSTANCE = it }
            }
    }
}
