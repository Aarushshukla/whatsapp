package com.example.whatsappcleaner.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.whatsappcleaner.workers.ReminderWorker
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    const val UNIQUE_REMINDER_WORK_NAME = "cleanly_periodic_reminder"

    fun schedulePeriodicReminder(
        context: Context,
        intervalMinutes: Long
    ) {
        val normalizedIntervalMinutes = intervalMinutes.coerceIn(15L, 24L * 60L)
        val periodicRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            normalizedIntervalMinutes,
            TimeUnit.MINUTES
        ).addTag(UNIQUE_REMINDER_WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }

    fun cancelPeriodicReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_REMINDER_WORK_NAME)
    }
}
