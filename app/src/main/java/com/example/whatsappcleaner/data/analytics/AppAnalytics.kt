package com.example.whatsappcleaner.data.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

class AppAnalytics private constructor(context: Context) {
    private val firebaseAnalytics: FirebaseAnalytics? = runCatching {
        FirebaseApp.initializeApp(context.applicationContext)
        FirebaseAnalytics.getInstance(context.applicationContext)
    }.getOrNull()

    fun logEvent(name: String, paramsBuilder: Bundle.() -> Unit = {}) {
        val bundle = Bundle().apply(paramsBuilder)
        runCatching { firebaseAnalytics?.logEvent(name, bundle) }
    }

    fun trackPaywallViewed(source: String) = logEvent("paywall_viewed") { putString("source", source) }
    fun trackPurchaseClicked(plan: String, source: String) = logEvent("purchase_clicked") {
        putString("plan", plan)
        putString("source", source)
    }
    fun trackPurchaseSuccess(plan: String) = logEvent("purchase_success") { putString("plan", plan) }
    fun trackPurchaseFailed(plan: String, reason: String) = logEvent("purchase_failed") {
        putString("plan", plan)
        putString("reason", reason.take(80))
    }
    fun trackRestorePurchaseClicked(source: String) = logEvent("restore_purchase_clicked") { putString("source", source) }
    fun trackSmartCleanClicked() = logEvent("smart_clean_clicked")
    fun trackDeleteClicked(origin: String) = logEvent("delete_clicked") { putString("origin", origin) }
    fun trackReviewClicked() = logEvent("review_clicked")
    fun trackStorageScreenOpened() = logEvent("storage_screen_opened")
    fun trackSettingsOpened() = logEvent("settings_opened")
    fun trackShareResultClicked(source: String) = logEvent("share_result_clicked") { putString("source", source) }

    companion object {
        @Volatile
        private var INSTANCE: AppAnalytics? = null

        fun get(context: Context): AppAnalytics =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppAnalytics(context).also { INSTANCE = it }
            }
    }
}
