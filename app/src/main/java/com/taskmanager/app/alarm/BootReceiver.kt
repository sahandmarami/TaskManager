package com.taskmanager.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.taskmanager.app.TaskManagerApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * After a device restart (or time/timezone change) all pending reminders
 * are re-scheduled because AlarmManager alarms do not survive reboots.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_TIMEZONE_CHANGED
        ) return

        val app = context.applicationContext as TaskManagerApp
        val pending = goAsync()
        app.applicationScope.launch(Dispatchers.IO) {
            try {
                app.container.alarmScheduler.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }
}
