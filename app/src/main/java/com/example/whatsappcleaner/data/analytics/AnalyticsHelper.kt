package com.example.whatsappcleaner.data.analytics

import android.util.Log

object AnalyticsHelper {

    fun logSmartClean() {
        Log.d("ANALYTICS", "Smart Clean Clicked")
    }

    fun logScanStarted() {
        Log.d("SCAN", "Scan started")
    }

    fun logAITool(toolName: String) {
        Log.d("ANALYTICS", "AI Tool Opened: $toolName")
    }

    fun logDelete(count: Int) {
        Log.d("ANALYTICS", "Delete clicked: $count files")
    }
}
