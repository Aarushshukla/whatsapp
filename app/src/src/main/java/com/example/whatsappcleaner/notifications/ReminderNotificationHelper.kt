package com.example.whatsappcleaner.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.whatsappcleaner.MainActivity
import com.example.whatsappcleaner.R

class ReminderNotificationHelper(
    private val context: Context
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        notificationManager.createNotificationChannel(channel)
    }

    fun showReminderNotification() {
        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Quick cleanup reminder")
            .setContentText("Take 60 seconds to review today's chat media.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Quick cleanup reminder\nTake 60 seconds to review today's chat media and keep storage under control."
                )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "clean_reminder_channel"
        private const val CHANNEL_NAME = "Cleanup reminders"
        private const val CHANNEL_DESCRIPTION = "Reminder notifications for periodic cleanup"
        private const val NOTIFICATION_ID = 9001
    }
}
