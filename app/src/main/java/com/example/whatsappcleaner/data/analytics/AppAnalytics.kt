package com.example.whatsappcleaner.data.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log

private const val TAG = "AppAnalytics"

class AppAnalytics private constructor(context: Context) {
    private val appContext = context.applicationContext

    fun logEvent(name: String, paramsBuilder: Bundle.() -> Unit = {}) {
        val bundle = Bundle().apply(paramsBuilder)
        Log.d(TAG, "logEvent(name=$name, params=$bundle)")
        // Privacy-first offline app: analytics events are only logged locally.
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
    fun trackCleanupCompleted(deletedCount: Int, failedCount: Int, freedBytes: Long) = logEvent("cleanup_completed") {
        putInt("deleted_count", deletedCount)
        putInt("failed_count", failedCount)
        putLong("freed_bytes", freedBytes)
    }
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
    AppAnalytics.get(context).logEvent(eventName) { putString("action", eventName) }
}

fun AppAnalytics.trackScanStarted() = logEvent("scan_started")
fun AppAnalytics.trackScanCompleted(totalFiles: Int, totalSizeBytes: Long) = logEvent("scan_completed") {
    putInt("total_files", totalFiles)
    putLong("total_size_bytes", totalSizeBytes)
}
fun AppAnalytics.trackScanFailed(reason: String) = logEvent("scan_failed") { putString("reason", reason.take(80)) }
