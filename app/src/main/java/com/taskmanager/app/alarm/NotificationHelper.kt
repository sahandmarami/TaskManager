package com.taskmanager.app.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.taskmanager.app.R

/**
 * Creates the high-importance reminder notification channel and builds
 * the alarm notifications (with full-screen intent for time-sensitive reminders).
 */
object NotificationHelper {

    const val CHANNEL_REMINDERS = "task_manager_reminders"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            context.getString(R.string.channel_reminders_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.channel_reminders_description)
            enableVibration(true)
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            setSound(
                soundUri,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun notificationsEnabled(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private fun fullScreenPendingIntent(context: Context, taskId: Long): PendingIntent {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmActivity.EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun actionPendingIntent(context: Context, taskId: Long, action: String, minutes: Int = 0): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId)
            if (minutes > 0) putExtra(NotificationActionReceiver.EXTRA_MINUTES, minutes)
        }
        return PendingIntent.getBroadcast(
            context,
            (taskId * 100 + action.hashCode()).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun showReminderNotification(context: Context, taskId: Long, title: String, description: String) {
        if (!notificationsEnabled(context)) return
        val fsPi = fullScreenPendingIntent(context, taskId)
        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title.ifBlank { "یادآوری وظیفه" })
            .setContentText(description.ifBlank { "زمان انجام این وظیفه رسیده است" })
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(description.ifBlank { "زمان انجام این وظیفه رسیده است" })
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setOngoing(true)
            .setFullScreenIntent(fsPi, true)
            .setContentIntent(fsPi)
            .addAction(
                R.drawable.ic_notification,
                "انجام شد",
                actionPendingIntent(context, taskId, NotificationActionReceiver.ACTION_COMPLETE),
            )
            .addAction(
                R.drawable.ic_notification,
                "۵ دقیقه بعد",
                actionPendingIntent(context, taskId, NotificationActionReceiver.ACTION_SNOOZE, 5),
            )

        try {
            NotificationManagerCompat.from(context).notify(taskId.toInt(), builder.build())
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted; the in-app alarm screen still works.
        }
    }

    fun cancelNotification(context: Context, taskId: Long) {
        NotificationManagerCompat.from(context).cancel(taskId.toInt())
    }
}
