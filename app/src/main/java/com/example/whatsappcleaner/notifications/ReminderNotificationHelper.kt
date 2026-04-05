package com.example.whatsappcleaner.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.whatsappcleaner.MainActivity
import com.example.whatsappcleaner.R
import kotlin.random.Random

class ReminderNotificationHelper(
    private val context: Context
) {

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Cleanly AI Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Smart reminders to keep storage clean"
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showReminderNotification(freeableBytes: Long, messageBody: String) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val spaceText = if (freeableBytes > 0) "Potential free space: ${com.example.whatsappcleaner.data.local.formatSize(freeableBytes)}" else "Quick storage check available"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Cleanly AI")
            .setContentText(spaceText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(Random.nextInt(), notification)
        } catch (_: SecurityException) {
        }
    }

    companion object {
        const val CHANNEL_ID = "whats_clean_reminders"
    }
}
