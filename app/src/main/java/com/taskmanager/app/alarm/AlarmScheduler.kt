package com.taskmanager.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.taskmanager.app.data.db.TaskDao

/**
 * Schedules precise alarms with [AlarmManager.setExactAndAllowWhileIdle].
 * Works even when the app is closed. On Android 12+ (API 31) it checks
 * canScheduleExactAlarms() and falls back to setWindow when the user has
 * not granted the exact-alarm permission.
 */
class AlarmScheduler(
    private val context: Context,
    private val taskDao: TaskDao,
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    /** Opens the system settings screen for exact alarms (Android 12+). */
    fun exactAlarmSettingsIntent(): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        } else null

    /**
     * Schedules a reminder alarm for [taskId] at [triggerAtMillis].
     * Returns true when an exact alarm was used.
     */
    fun scheduleReminder(taskId: Long, title: String, description: String, triggerAtMillis: Long): Boolean {
        if (triggerAtMillis <= System.currentTimeMillis()) return false
        val pi = reminderPendingIntent(taskId, title, description)
        return if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            true
        } else {
            // Inexact but still reliable fallback before the user grants exact alarms.
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 10 * 60_000L, pi)
            false
        }
    }

    fun snooze(taskId: Long, title: String, description: String, minutes: Int): Boolean =
        scheduleReminder(
            taskId, title, description,
            System.currentTimeMillis() + minutes * 60_000L,
        )

    fun cancelReminder(taskId: Long) {
        alarmManager.cancel(reminderPendingIntent(taskId, "", ""))
    }

    /** Re-schedules every valid pending reminder (used after boot / time change). */
    suspend fun rescheduleAll() {
        val now = System.currentTimeMillis()
        taskDao.getReminderTasks().forEach { task ->
            val due = task.dueAtMillis ?: return@forEach
            val trigger = due - task.reminderMinutesBefore * 60_000L
            if (trigger > now) {
                scheduleReminder(task.id, task.title, task.description, trigger)
            }
        }
    }

    private fun reminderPendingIntent(
        taskId: Long,
        title: String,
        description: String,
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_REMINDER
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AlarmReceiver.EXTRA_TITLE, title)
            putExtra(AlarmReceiver.EXTRA_DESCRIPTION, description)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
