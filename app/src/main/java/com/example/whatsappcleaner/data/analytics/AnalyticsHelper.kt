package com.example.whatsappcleaner.data.analytics

import android.util.Log
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

object AnalyticsHelper {

    fun logSmartClean() {
        Log.d("ANALYTICS", "Smart Clean Clicked")
        Firebase.analytics.logEvent("smart_clean_clicked", null)
    }

    fun logScanStarted() {
        Log.d("SCAN", "Scan started")
        Firebase.analytics.logEvent("scan_started", null)
    }

    fun logAITool(toolName: String) {
        Log.d("ANALYTICS", "AI Tool Opened: $toolName")
        Firebase.analytics.logEvent("ai_tool_opened") {
            param("tool_name", toolName)
        }
    }

    fun logDelete(count: Int) {
        Log.d("ANALYTICS", "Delete clicked: $count files")
        Firebase.analytics.logEvent("delete_clicked") {
            param("count", count.toLong())
        }
    }
}
