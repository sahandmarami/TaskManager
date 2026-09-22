package com.taskmanager.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.taskmanager.app.data.db.TaskDao

/**
 * Schedules alarms with [AlarmManager.setAlarmClock] — the exact API the
 * built-in Clock app uses. It is always exact, fires through Doze and
 * battery savers, needs no extra permission on any Android version, and
 * shows the alarm icon in the status bar / lock screen just like the
 * system clock.
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
     * Schedules a reminder alarm for [taskId] at [triggerAtMillis] using the
     * clock-app path (always exact). Returns true when scheduled.
     */
    fun scheduleReminder(taskId: Long, title: String, description: String, triggerAtMillis: Long): Boolean {
        if (triggerAtMillis <= System.currentTimeMillis()) return false
        val pi = reminderPendingIntent(taskId, title, description)
        val showIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, com.taskmanager.app.MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent),
            pi,
        )
        return true
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
