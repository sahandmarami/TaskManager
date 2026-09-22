package com.taskmanager.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.taskmanager.app.TaskManagerApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fired by AlarmManager at the exact reminder time.
 *
 * The goal: the phone MUST jump to the full-screen AlarmActivity automatically,
 * wherever it is — lock screen, another app, screen off — without any tap.
 *
 * Three cooperating layers guarantee that:
 *  1. Direct activity launch from this receiver (works on Android 8/9 always,
 *     on Android 10-13 while the app task sits in Recents, and on Android 14+
 *     exact-alarm apps are explicitly exempt from background-launch limits).
 *  2. A full-screen-intent notification (the system path that wakes the phone
 *     and shows the alarm over the lock screen when the screen is off).
 *  3. If launching is ever blocked by the system, the notification itself now
 *     plays the real alarm ringtone in a loop (insistent) so the alarm is never
 *     silent — tapping it still opens the ringing AlarmActivity.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE_REMINDER) return
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val app = context.applicationContext as TaskManagerApp
        val pending = goAsync()
        app.applicationScope.launch(Dispatchers.IO) {
            try {
                val task = app.container.taskRepository.getTask(taskId)
                if (task != null && !task.isCompleted && task.hasTime && task.reminderEnabled) {
                    val title = task.title
                    val description = task.description

                    // Layer 2: full-screen-intent notification first — it is the
                    // system-blessed way to wake a sleeping device and show the
                    // alarm over the lock screen. Also acts as the fallback UI.
                    NotificationHelper.showAlarmNotification(
                        context, taskId, title, description,
                    )

                    // Layer 1: launch the alarm screen directly. When the screen
                    // is on and the user is inside another app, this is what
                    // makes the phone jump to the alarm page instantly.
                    launchAlarmActivity(context, taskId, title, description)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun launchAlarmActivity(context: Context, taskId: Long, title: String, description: String) {
        try {
            // Keep the CPU awake for a few seconds so the activity can draw
            // even on aggressive OEM battery savers.
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "taskmanager:alarm_launch",
            )
            wakeLock.acquire(30_000L)

            val activity = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(AlarmActivity.EXTRA_TASK_ID, taskId)
                putExtra(AlarmActivity.EXTRA_TITLE, title)
                putExtra(AlarmActivity.EXTRA_DESCRIPTION, description)
            }
            context.startActivity(activity)
        } catch (_: Exception) {
            // Background launch blocked — layer 2 (full-screen/insistent
            // notification) still rings and opens the alarm screen on tap.
        }
    }

    companion object {
        const val ACTION_FIRE_REMINDER = "com.taskmanager.app.action.FIRE_REMINDER"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESCRIPTION = "extra_description"
    }
}
