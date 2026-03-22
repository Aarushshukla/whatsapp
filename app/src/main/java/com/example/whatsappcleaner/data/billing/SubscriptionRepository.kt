package com.example.whatsappcleaner.data.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.whatsappcleaner.data.analytics.AppAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val TAG = "SubscriptionRepo"
private const val PREFS_NAME = "subscription_prefs"
private const val KEY_IS_PRO = "is_pro_user"
private const val KEY_PLAN = "current_plan"
private const val KEY_BILLING_MESSAGE = "billing_message"
private const val KEY_LAST_ENTITLED_PLAN = "last_entitled_plan"

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
    val lastMessage: String? = null,
    val hasPrices: Boolean = false
)

class SubscriptionRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val analytics = AppAnalytics.get(appContext)
    private val productDetails = mutableMapOf<BillingProduct, ProductDetails>()
    private val _state = MutableStateFlow(
        SubscriptionState(
            isProUser = prefs.getBoolean(KEY_IS_PRO, false),
            currentPlan = safePlan(prefs.getString(KEY_PLAN, SubscriptionPlan.FREE.name)),
            lastMessage = prefs.getString(KEY_BILLING_MESSAGE, null),
            hasPrices = false
        )
    )
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(appContext)
            .setListener(::onPurchasesUpdated)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()
    }

    fun start() {
        if (billingClient.isReady) {
            Log.d(TAG, "Billing client already ready.")
            queryCatalogAndPurchases()
            return
        }
        runCatching {
            Log.d(TAG, "Starting BillingClient connection.")
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    Log.d(TAG, "Billing setup finished: code=${result.responseCode}, message=${result.debugMessage}")
                    val ready = result.responseCode == BillingClient.BillingResponseCode.OK
                    updateMessage(result.debugMessage.ifBlank { if (ready) "Billing ready" else "Billing unavailable" }, ready)
                    if (ready) {
                        queryCatalogAndPurchases()
                    } else {
                        analytics.trackPurchaseFailed(_state.value.currentPlan.name.lowercase(), "setup_${result.responseCode}")
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Log.w(TAG, "Billing service disconnected.")
                    updateMessage("Billing temporarily unavailable. Purchases can be retried when Play Store reconnects.", false)
                    start()
                }
            })
        }.onFailure { error ->
            Log.e(TAG, "Unable to start billing connection.", error)
            updateMessage("Billing is unavailable on this device right now.", false)
        }
    }

    fun launchPurchase(activity: Activity, product: BillingProduct, source: String): BillingResult? {
        if (!billingClient.isReady) {
            Log.w(TAG, "launchPurchase called before billing was ready.")
            start()
            analytics.trackPurchaseFailed(product.name.lowercase(), "billing_not_ready")
            updateMessage("Play Billing is still connecting. Please try again in a moment.", false)
            return null
        }
        val details = productDetails[product]
        if (details == null) {
            Log.w(TAG, "Missing product details for ${product.productId}.")
            analytics.trackPurchaseFailed(product.name.lowercase(), "missing_product_details")
            queryProductDetails()
            updateMessage("Pricing is still loading. Please try again shortly.", true)
            return null
        }

        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply {
                if (product.productType == BillingClient.ProductType.SUBS) {
                    val offerToken = details.subscriptionOfferDetails
                        ?.firstOrNull()
                        ?.offerToken
                    if (offerToken.isNullOrBlank()) {
                        Log.w(TAG, "No offer token available for ${product.productId}.")
                    } else {
                        setOfferToken(offerToken)
                    }
                }
            }
            .build()

        analytics.trackPurchaseClicked(product.name.lowercase(), source)
        return runCatching {
            Log.d(TAG, "Launching billing flow for ${product.productId} from $source.")
            billingClient.launchBillingFlow(
                activity,
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(productParams))
                    .build()
            )
        }.onFailure { error ->
            Log.e(TAG, "Failed to launch billing flow for ${product.productId}.", error)
            analytics.trackPurchaseFailed(product.name.lowercase(), "launch_exception")
            updateMessage("Unable to open the Play purchase sheet right now.", true)
        }.getOrNull()
    }

    fun restorePurchases(source: String = "settings") {
        Log.d(TAG, "Restoring purchases from $source.")
        analytics.trackRestorePurchaseClicked(source)
        queryActivePurchases(forceReconnect = true)
    }

    fun refreshPurchases() {
        queryActivePurchases(forceReconnect = false)
    }

    private fun queryCatalogAndPurchases() {
        queryProductDetails()
        queryActivePurchases(forceReconnect = false)
    }

    private fun queryProductDetails() {
        if (!billingClient.isReady) {
            Log.w(TAG, "queryProductDetails skipped because billing is not ready.")
            return
        }
        val products = BillingProduct.entries.map { product ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(product.productId)
                .setProductType(product.productType)
                .build()
        }
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build()
        ) { result, queryProductDetailsResult ->
            val detailsList = queryProductDetailsResult.productDetailsList.orEmpty()
            val unfetchedProducts = queryProductDetailsResult.unfetchedProductList.orEmpty()
            Log.d(
                TAG,
                "Product details response: code=${result.responseCode}, count=${detailsList.size}, unfetched=${unfetchedProducts.size}"
            )
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                updateMessage(result.debugMessage.ifBlank { "Unable to load pricing from Play Billing." }, true)
                return@queryProductDetailsAsync
            }

            val priceMap = buildMap {
                detailsList.forEach { details ->
                    val match = BillingProduct.entries.firstOrNull { billingProduct ->
                        billingProduct.productId == details.productId
                    } ?: return@forEach
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
                    if (!formattedPrice.isNullOrBlank()) {
                        put(match, formattedPrice)
                    }
                }
            }

            _state.update { currentState ->
                currentState.copy(
                    prices = if (priceMap.isEmpty()) currentState.prices else priceMap,
                    billingReady = true,
                    lastMessage = if (priceMap.isEmpty()) "Pricing unavailable. You can try again later." else currentState.lastMessage,
                    hasPrices = priceMap.isNotEmpty() || currentState.hasPrices
                )
            }
        }
    }

    private fun queryActivePurchases(forceReconnect: Boolean) {
        if (!billingClient.isReady) {
            if (forceReconnect) start() else Log.d(TAG, "queryActivePurchases skipped while billing is offline.")
            return
        }

        val purchases = mutableListOf<Purchase>()
        var callbacksRemaining = 2
        fun handleBatch(result: BillingResult, batch: List<Purchase>) {
            Log.d(TAG, "Purchases response: code=${result.responseCode}, count=${batch.size}")
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases += batch
            } else {
                updateMessage(result.debugMessage.ifBlank { "Unable to refresh purchases." }, true)
            }
            callbacksRemaining -= 1
            if (callbacksRemaining == 0) {
                updateFromPurchases(purchases)
            }
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { result, batch -> handleBatch(result, batch) }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { result, batch -> handleBatch(result, batch) }
    }

    private fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated: code=${result.responseCode}, count=${purchases?.size ?: 0}")
        when {
            result.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty() -> {
                updateFromPurchases(purchases)
            }
            result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> {
                updateMessage("Purchase cancelled.", true)
            }
            else -> {
                val attemptedPlan = _state.value.currentPlan.name.lowercase()
                analytics.trackPurchaseFailed(attemptedPlan, result.debugMessage.ifBlank { "billing_error_${result.responseCode}" })
                updateMessage(result.debugMessage.ifBlank { "Purchase failed. Please try again later." }, true)
            }
        }
    }

    private fun updateFromPurchases(purchases: List<Purchase>) {
        Log.d(TAG, "Updating local subscription state from ${purchases.size} purchases.")
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
        }

        val activePurchase = purchases
            .filter { purchase -> purchase.purchaseState == Purchase.PurchaseState.PURCHASED }
            .sortedByDescending(::purchasePriority)
            .firstOrNull()
        val plan = when {
            activePurchase == null -> SubscriptionPlan.FREE
            activePurchase.products.contains(BillingProduct.LIFETIME.productId) -> SubscriptionPlan.LIFETIME
            activePurchase.products.contains(BillingProduct.YEARLY.productId) -> SubscriptionPlan.YEARLY
            activePurchase.products.contains(BillingProduct.MONTHLY.productId) -> SubscriptionPlan.MONTHLY
            else -> SubscriptionPlan.FREE
        }
        val previousPlan = safePlan(prefs.getString(KEY_LAST_ENTITLED_PLAN, SubscriptionPlan.FREE.name))
        persistState(plan != SubscriptionPlan.FREE, plan)
        prefs.edit().putString(KEY_LAST_ENTITLED_PLAN, plan.name).apply()
        if (plan != SubscriptionPlan.FREE) {
            if (previousPlan != plan) {
                analytics.trackPurchaseSuccess(plan.name.lowercase())
            }
            updateMessage("${plan.displayName} active.", true)
        } else {
            updateMessage("Using free plan.", billingClient.isReady)
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged for token=${purchase.purchaseToken.take(6)}***")
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: code=${result.responseCode}, message=${result.debugMessage}")
                analytics.trackPurchaseFailed(planNameFromPurchase(purchase), "ack_${result.responseCode}")
            }
        }
    }

    private fun purchasePriority(purchase: Purchase): Int = when {
        purchase.products.contains(BillingProduct.LIFETIME.productId) -> 3
        purchase.products.contains(BillingProduct.YEARLY.productId) -> 2
        purchase.products.contains(BillingProduct.MONTHLY.productId) -> 1
        else -> 0
    }

    private fun planNameFromPurchase(purchase: Purchase): String = when {
        purchase.products.contains(BillingProduct.LIFETIME.productId) -> BillingProduct.LIFETIME.name.lowercase()
        purchase.products.contains(BillingProduct.YEARLY.productId) -> BillingProduct.YEARLY.name.lowercase()
        purchase.products.contains(BillingProduct.MONTHLY.productId) -> BillingProduct.MONTHLY.name.lowercase()
        else -> SubscriptionPlan.FREE.name.lowercase()
    }

    private fun persistState(isPro: Boolean, plan: SubscriptionPlan) {
        prefs.edit()
            .putBoolean(KEY_IS_PRO, isPro)
            .putString(KEY_PLAN, plan.name)
            .apply()
        _state.update { currentState -> currentState.copy(isProUser = isPro, currentPlan = plan) }
    }

    private fun updateMessage(message: String, billingReady: Boolean) {
        prefs.edit().putString(KEY_BILLING_MESSAGE, message).apply()
        _state.update { currentState -> currentState.copy(lastMessage = message, billingReady = billingReady) }
    }

    private fun safePlan(raw: String?): SubscriptionPlan = runCatching {
        SubscriptionPlan.valueOf(raw ?: SubscriptionPlan.FREE.name)
    }.getOrElse {
        SubscriptionPlan.FREE
    }

    companion object {
        @Volatile
        private var INSTANCE: SubscriptionRepository? = null

        fun get(context: Context): SubscriptionRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SubscriptionRepository(context).also { repository ->
                    INSTANCE = repository
                }
            }
    }
}
