package com.taskmanager.app.data.repository

import com.taskmanager.app.alarm.AlarmScheduler
import com.taskmanager.app.data.db.SubTaskDao
import com.taskmanager.app.data.db.SubTaskEntity
import com.taskmanager.app.data.db.TaskDao
import com.taskmanager.app.data.db.TaskEntity
import com.taskmanager.app.data.db.TaskWithSubtasks
import com.taskmanager.app.data.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Single source of truth for tasks. Keeps alarms in sync with the database:
 * - save   -> cancel old alarm, schedule new one when valid
 * - delete -> cancel alarm
 * - complete -> cancel alarm; un-complete re-schedules
 */
class TaskRepository(
    private val taskDao: TaskDao,
    private val subTaskDao: SubTaskDao,
    private val alarmScheduler: AlarmScheduler,
    private val settingsRepository: SettingsRepository,
) {

    fun observeAll(): Flow<List<TaskWithSubtasks>> = taskDao.observeAll()

    fun observeById(id: Long): Flow<TaskWithSubtasks?> = taskDao.observeById(id)

    fun observeTasksForGoal(goalId: Long): Flow<List<TaskWithSubtasks>> =
        taskDao.observeTasksForGoal(goalId)

    suspend fun getTask(id: Long): TaskEntity? = taskDao.getById(id)

    suspend fun getSubtasks(taskId: Long): List<SubTaskEntity> = subTaskDao.getForTask(taskId)

    /**
     * Inserts or updates a task and syncs its reminder alarm.
     * @return the task id
     */
    suspend fun saveTask(
        task: TaskEntity,
        subtasks: List<SubTaskEntity> = emptyList(),
    ): Long {
        val id = if (task.id == 0L) taskDao.insert(task) else {
            taskDao.update(task)
            task.id
        }
        if (task.id != 0L) {
            subTaskDao.deleteForTask(id)
        }
        if (subtasks.isNotEmpty()) {
            subTaskDao.insertAll(subtasks.map { s -> s.copy(id = 0L, taskId = id) })
        }

        syncAlarm(id)
        return id
    }

    /** Cancels + re-schedules the reminder for the current state of the task. */
    suspend fun syncAlarm(taskId: Long) {
        val scheduler = alarmScheduler
        scheduler.cancelReminder(taskId)
        val task = taskDao.getById(taskId) ?: return
        val settings = settingsRepository.settings.first()
        val due = task.dueAtMillis
        if (!task.isCompleted && settings.remindersEnabled && task.hasTime && task.reminderEnabled && due != null) {
            val trigger = due - task.reminderMinutesBefore * 60_000L
            if (trigger > System.currentTimeMillis()) {
                scheduler.scheduleReminder(taskId, task.title, task.description, trigger)
            }
        }
    }

    suspend fun setCompleted(id: Long, completed: Boolean) {
        val now = System.currentTimeMillis()
        taskDao.setCompleted(id, completed, if (completed) now else null, now)
        if (completed) {
            alarmScheduler.cancelReminder(id)
        } else {
            syncAlarm(id)
        }
    }

    suspend fun delete(task: TaskEntity) {
        alarmScheduler.cancelReminder(task.id)
        taskDao.delete(task)
    }

    suspend fun deleteSubtask(subtask: SubTaskEntity) {
        subTaskDao.delete(subtask)
    }

    suspend fun toggleSubtask(subtask: SubTaskEntity) {
        subTaskDao.update(subtask.copy(isDone = !subtask.isDone))
    }

    suspend fun rescheduleAllAlarms() {
        alarmScheduler.rescheduleAll()
    }
}
