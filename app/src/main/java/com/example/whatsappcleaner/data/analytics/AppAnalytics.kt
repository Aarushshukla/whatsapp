package com.example.whatsappcleaner.data.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.example.whatsappcleaner.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

private const val TAG = "AppAnalytics"

class AppAnalytics private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val firebaseAnalytics: FirebaseAnalytics? = initializeAnalytics()

    private fun initializeAnalytics(): FirebaseAnalytics? {
        if (!BuildConfig.HAS_GOOGLE_SERVICES) {
            Log.w(TAG, "google-services.json is missing; Firebase Analytics is disabled and events will be logged locally only.")
            return null
        }
        return runCatching {
            FirebaseApp.initializeApp(appContext) ?: FirebaseApp.getInstance()
            FirebaseAnalytics.getInstance(appContext)
        }.onSuccess {
            Log.d(TAG, "Firebase Analytics initialized successfully.")
        }.onFailure { error ->
            Log.e(TAG, "Firebase Analytics initialization failed. Falling back to local logs.", error)
        }.getOrNull()
    }

    fun logEvent(name: String, paramsBuilder: Bundle.() -> Unit = {}) {
        val bundle = Bundle().apply(paramsBuilder)
        Log.d(TAG, "logEvent(name=$name, params=$bundle)")
        runCatching { firebaseAnalytics?.logEvent(name, bundle) }
            .onFailure { error -> Log.e(TAG, "Failed to send Firebase event: $name", error) }
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

fun trackEvent(context: Context, eventName: String) {
    val analytics = FirebaseAnalytics.getInstance(context.applicationContext)
    val bundle = Bundle()
    bundle.putString("action", eventName)
    analytics.logEvent(eventName, bundle)
}
