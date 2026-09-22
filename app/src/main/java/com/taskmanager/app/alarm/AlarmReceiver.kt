package com.taskmanager.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.taskmanager.app.TaskManagerApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fired by AlarmManager at the exact reminder time.
 * Re-validates the task against the database (it may have been completed or
 * deleted after the alarm was scheduled) and shows the alarm notification
 * with a full-screen intent.
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
                    NotificationHelper.showReminderNotification(
                        context, taskId, task.title, task.description,
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_FIRE_REMINDER = "com.taskmanager.app.action.FIRE_REMINDER"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESCRIPTION = "extra_description"
    }
}
