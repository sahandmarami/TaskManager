package com.taskmanager.app.di

import android.content.Context
import androidx.room.Room
import com.taskmanager.app.alarm.AlarmScheduler
import com.taskmanager.app.data.db.TaskManagerDatabase
import com.taskmanager.app.data.repository.BackupManager
import com.taskmanager.app.data.repository.CategoryRepository
import com.taskmanager.app.data.repository.GoalRepository
import com.taskmanager.app.data.repository.TaskRepository
import com.taskmanager.app.data.settings.SettingsRepository
import com.taskmanager.app.focus.FocusEngine

/** Manual dependency injection container. */
class AppContainer(context: Context) {

    private val database: TaskManagerDatabase = Room.databaseBuilder(
        context, TaskManagerDatabase::class.java, "task_manager.db",
    ).build()

    val settingsRepository = SettingsRepository(context)
    val alarmScheduler = AlarmScheduler(context, database.taskDao())

    val taskRepository = TaskRepository(
        taskDao = database.taskDao(),
        subTaskDao = database.subTaskDao(),
        alarmScheduler = alarmScheduler,
        settingsRepository = settingsRepository,
    )

    val categoryRepository = CategoryRepository(
        categoryDao = database.categoryDao(),
        taskDao = database.taskDao(),
    )

    val goalRepository = GoalRepository(
        goalDao = database.goalDao(),
        goalStepDao = database.goalStepDao(),
        taskDao = database.taskDao(),
    )

    val backupManager = BackupManager(
        taskDao = database.taskDao(),
        subTaskDao = database.subTaskDao(),
        categoryDao = database.categoryDao(),
        goalDao = database.goalDao(),
        goalStepDao = database.goalStepDao(),
        settingsRepository = settingsRepository,
    )

    /** Focus (Pomodoro) engine is activity-independent so the timer survives navigation. */
    val focusEngine = FocusEngine(settingsRepository)
}
