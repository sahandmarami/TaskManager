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
 *
 * The channel plays the real ALARM ringtone (not a quiet notification ding) —
 * so even when the system shows the heads-up banner instead of launching the
 * full-screen intent, the phone still sounds like an alarm. The notification
 * is insistent: the alarm sound repeats until the user opens/dismisses it.
 */
object NotificationHelper {

    // _v2: notification channels are immutable once created by the system;
    // a new id is required for the alarm-sound change to take effect on
    // devices where the old channel already exists.
    const val CHANNEL_REMINDERS = "task_manager_reminders_v2"
    private const val LEGACY_CHANNEL_REMINDERS = "task_manager_reminders"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        // Remove the old channel so every device ends up with the alarm-sound one.
        manager.deleteNotificationChannel(LEGACY_CHANNEL_REMINDERS)

        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            context.getString(R.string.channel_reminders_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.channel_reminders_description)
            enableVibration(true)
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            setSound(
                soundUri,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
        }
        manager.createNotificationChannel(channel)
    }

    fun notificationsEnabled(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    /**
     * Android 14+ no longer grants USE_FULL_SCREEN_INTENT automatically.
     * Returns true when the app may auto-launch the full-screen alarm page.
     */
    fun canUseFullScreenIntent(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            context.getSystemService(NotificationManager::class.java)
                .canUseFullScreenIntent()

    /** Opens the system page where the user grants full-screen intent access. */
    fun fullScreenIntentSettingsIntent(): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Intent(android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
        } else null

    private fun alarmActivityPendingIntent(context: Context, taskId: Long): PendingIntent {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
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

    /**
     * Time-critical alarm notification:
     *  - full-screen intent → the system opens [AlarmActivity] automatically
     *    when the screen is off / device locked (permission granted).
     *  - insistent + ALARM-usage channel sound → the alarm ringtone keeps
     *    playing until the user reacts, even if no activity could launch.
     */
    fun showAlarmNotification(context: Context, taskId: Long, title: String, description: String) {
        if (!notificationsEnabled(context)) return
        val pi = alarmActivityPendingIntent(context, taskId)
        val text = description.ifBlank { "زمان انجام این وظیفه رسیده است" }
        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title.ifBlank { "یادآوری وظیفه" })
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(true)
            .setFullScreenIntent(pi, true)
            .setContentIntent(pi)
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

        val notification = builder.build()
        // FLAG_INSISTENT makes the channel's alarm sound repeat until the
        // user opens or dismisses the notification.
        notification.flags = notification.flags or android.app.Notification.FLAG_INSISTENT
        try {
            NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted; the direct activity launch
            // from AlarmReceiver still shows the alarm screen.
        }
    }

    fun cancelNotification(context: Context, taskId: Long) {
        NotificationManagerCompat.from(context).cancel(taskId.toInt())
    }
}
