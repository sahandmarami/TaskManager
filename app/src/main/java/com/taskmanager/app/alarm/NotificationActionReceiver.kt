package com.taskmanager.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.taskmanager.app.TaskManagerApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles "انجام شد" and "بعداً یادآوری کن" actions on the alarm notification. */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val app = context.applicationContext as TaskManagerApp
        NotificationHelper.cancelNotification(context, taskId)

        when (intent.action) {
            ACTION_COMPLETE -> {
                app.applicationScope.launch(Dispatchers.IO) {
                    app.container.taskRepository.setCompleted(taskId, true)
                }
            }
            ACTION_SNOOZE -> {
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 5)
                val pending = goAsync()
                app.applicationScope.launch(Dispatchers.IO) {
                    try {
                        val task = app.container.taskRepository.getTask(taskId) ?: return@launch
                        app.container.alarmScheduler.snooze(
                            taskId, task.title, task.description, minutes,
                        )
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.taskmanager.app.action.COMPLETE_TASK"
        const val ACTION_SNOOZE = "com.taskmanager.app.action.SNOOZE_TASK"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_MINUTES = "extra_minutes"
    }
}
