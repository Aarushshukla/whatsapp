package com.example.whatsappcleaner.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.whatsappcleaner.workers.CleanReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val UNIQUE_REMINDER_WORK_NAME = "clean_reminder_work"

    fun schedulePeriodicReminder(
        context: Context,
        frequencyDays: Int,
        reminderHour: Int,
        reminderMinute: Int
    ) {
        val periodicRequest = PeriodicWorkRequestBuilder<CleanReminderWorker>(
            frequencyDays.coerceAtLeast(1).toLong(),
            TimeUnit.DAYS
        )
            .setInitialDelay(
                calculateInitialDelayMillis(reminderHour, reminderMinute),
                TimeUnit.MILLISECONDS
            )
            .addTag(UNIQUE_REMINDER_WORK_NAME)
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

    private fun calculateInitialDelayMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val nextRun = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (nextRun.before(now)) {
            nextRun.add(Calendar.DAY_OF_YEAR, 1)
        }

        return nextRun.timeInMillis - now.timeInMillis
    }
}
