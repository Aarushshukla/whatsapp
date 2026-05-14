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
import com.example.whatsappcleaner.data.UserPrefs
import com.example.whatsappcleaner.data.formatSize
import com.example.whatsappcleaner.data.MediaLoader
import kotlin.random.Random

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext

        // Compute today's WhatsApp/media size (reuse MediaLoader)
        val loader = MediaLoader(context)
        val todayItems = loader.loadTodayWhatsAppMedia()
        val todayBytes = todayItems.sumOf { it.sizeKb.toLong() * 1024L }

        if (todayItems.isEmpty()) {
            return Result.success()
        }

        val sizeText = formatSize(todayBytes)

        createChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // create a simple vector icon
            .setContentTitle("ChatSweep")
            .setContentText("Today you added ~$sizeText. Review now?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(Random.nextInt(), builder.build())
        }

        // Optionally, touch prefs so streak logic can know a reminder was sent
        UserPrefs.get(context) // ensure prefs are initialised

        return Result.success()
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "WhatsClean Reminders"
            val descriptionText = "Daily reminders to review chat media"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "whats_clean_reminders"
    }
}
