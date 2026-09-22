package com.taskmanager.app

import android.app.Application
import com.taskmanager.app.alarm.NotificationHelper
import com.taskmanager.app.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TaskManagerApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannels(this)
        applicationScope.launch(Dispatchers.IO) {
            container.categoryRepository.ensureDefaultCategories()
            container.alarmScheduler.rescheduleAll()
        }
    }
}
