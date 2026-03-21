package com.example.whatsappcleaner.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.whatsappcleaner.R
import com.example.whatsappcleaner.data.local.UserPrefs
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.data.local.MediaLoader
import kotlin.random.Random

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val loader = MediaLoader(context)
        val now = System.currentTimeMillis()
        val todayItems = loader.queryMediaStore("all", now - 86400000L, now)
        val todayBytes = todayItems.sumOf { it.sizeKb.toLong() * 1024L }

        if (todayItems.isEmpty()) return Result.success()

        val sizeText = formatSize(todayBytes)
        createChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Cleanly AI")
            .setContentText("Today you added ~$sizeText. Review now?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(Random.nextInt(), builder.build())
            }
        } catch (e: SecurityException) { }

        UserPrefs.get(context) // Ensure initialized
        return Result.success()
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Cleanly AI Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Daily reminders to review space and cleanup opportunities"
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "whats_clean_reminders"
    }
}