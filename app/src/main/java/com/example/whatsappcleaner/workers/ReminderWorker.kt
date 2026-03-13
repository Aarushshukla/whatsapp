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
import java.util.Calendar
import kotlin.random.Random

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val loader = MediaLoader(context)

        // 1. Fetch all media
        val images = loader.loadAllDeviceMedia("image")
        val videos = loader.loadAllDeviceMedia("video")
        val allItems = images + videos

        // 2. Calculate the start of "Today"
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // 3. Filter only what was added today
        val todayItems = allItems.filter { it.addedMillis >= startOfToday }
        val todayBytes = todayItems.sumOf { it.sizeKb.toLong() * 1024L }

        // If nothing was added today, don't bother the user with a notification
        if (todayItems.isEmpty()) return Result.success()

        val sizeText = formatSize(todayBytes)
        createChannel(context)

        // 4. Create the Notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Ensure you have this icon in res/drawable
            .setContentTitle("Gallery Cleaner")
            .setContentText("You saved ~$sizeText of media today. Review now?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(Random.nextInt(), builder.build())
            }
        } catch (e: SecurityException) {
            // Handle missing notification permission gracefully
        }

        UserPrefs.get(context) // Ensure initialized
        return Result.success()
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Gallery Clean Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to review gallery media"
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "gallery_clean_reminders"
    }
}