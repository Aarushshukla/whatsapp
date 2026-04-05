package com.example.whatsappcleaner.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.whatsappcleaner.data.local.MediaLoader
import com.example.whatsappcleaner.data.local.UserPrefs
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.notifications.ReminderNotificationHelper

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val userPrefs = UserPrefs.get(context)
        if (!userPrefs.isRemindersEnabled()) return Result.success()

        val loader = MediaLoader(context)
        val now = System.currentTimeMillis()
        val todayItems = loader.queryMediaStore("all", now - 86400000L, now)
        if (todayItems.isEmpty()) return Result.success()

        val todayBytes = todayItems.sumOf { mediaItem -> mediaItem.sizeKb.toLong() * 1024L }
        val predictedFreeableBytes = (todayBytes * 0.35f).toLong().coerceAtLeast(0L)
        val sizeText = formatSize(predictedFreeableBytes)
        val totalTodayText = formatSize(todayBytes)
        val dynamicBody = buildDynamicBody(todayItems.size, totalTodayText, sizeText)

        val notificationHelper = ReminderNotificationHelper(context)
        notificationHelper.createNotificationChannel()
        notificationHelper.showReminderNotification(predictedFreeableBytes, dynamicBody)

        return Result.success()
    }

    private fun buildDynamicBody(itemCount: Int, todayAddedText: String, freeableText: String): String {
        return when {
            itemCount >= 40 -> "High activity today: $itemCount new files ($todayAddedText). You can likely free around $freeableText by cleaning now."
            itemCount >= 15 -> "$itemCount new files detected today ($todayAddedText). A quick sweep could free about $freeableText."
            else -> "Storage check-in: $itemCount recent files ($todayAddedText). You could free up to $freeableText in under a minute."
        }
    }
}
