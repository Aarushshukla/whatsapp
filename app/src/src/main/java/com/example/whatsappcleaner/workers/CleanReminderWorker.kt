package com.example.whatsappcleaner.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.whatsappcleaner.notifications.ReminderNotificationHelper

class CleanReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val notificationHelper = ReminderNotificationHelper(applicationContext)
        notificationHelper.createNotificationChannel()
        notificationHelper.showReminderNotification()
        return Result.success()
    }
}
