package com.example.whatsappcleaner.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.whatsappcleaner.data.local.UserPrefs
import com.example.whatsappcleaner.data.analytics.trackEvent
import com.example.whatsappcleaner.notifications.ReminderNotificationHelper

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val userPrefs = UserPrefs.get(context)
        if (!userPrefs.isRemindersEnabled()) return Result.success()

        val notificationHelper = ReminderNotificationHelper(context)
        notificationHelper.createNotificationChannel()
        val cachedSummary = userPrefs.getCachedScanSummary()
        notificationHelper.showReminderNotification(
            potentialCleanupBytes = cachedSummary?.potentialCleanupBytes ?: 0L,
            hasCachedSummary = cachedSummary != null
        )
        trackEvent(context, "reminder_notification_shown")

        return Result.success()
    }
}
