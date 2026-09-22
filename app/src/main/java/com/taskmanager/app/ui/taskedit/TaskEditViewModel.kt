package com.taskmanager.app.ui.taskedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmanager.app.alarm.AlarmScheduler
import com.taskmanager.app.calendar.JalaliCalendar
import com.taskmanager.app.calendar.PersianCalendar
import com.taskmanager.app.data.db.CategoryEntity
import com.taskmanager.app.data.db.GoalEntity
import com.taskmanager.app.data.db.SubTaskEntity
import com.taskmanager.app.data.db.TaskEntity
import com.taskmanager.app.di.AppContainer
import com.taskmanager.app.domain.model.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

data class SubTaskDraft(
    val id: Long? = null,
    val title: String,
    val isDone: Boolean = false,
)

data class TaskEditUiState(
    val isEdit: Boolean = false,
    val title: String = "",
    val description: String = "",
    val hasDate: Boolean = false,
    val jYear: Int = 0,
    val jMonth: Int = 0,
    val jDay: Int = 0,
    val hasTime: Boolean = false,
    val hour: Int = 9,
    val minute: Int = 0,
    val priority: Priority = Priority.MEDIUM,
    val categoryId: Long? = null,
    val goalId: Long? = null,
    val reminderEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 0,
    val subtasks: List<SubTaskDraft> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val saved: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val needsExactAlarm: Boolean = false,
    val showExactAlarmDialog: Boolean = false,
)

class TaskEditViewModel(
    private val container: AppContainer,
    savedStateHandle: SavedStateHandle,
    prefillDateMillis: Long,
) : ViewModel() {

    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: -1L

    private val _state = MutableStateFlow(TaskEditUiState())
    val state: StateFlow<TaskEditUiState> = _state

    private val scheduler: AlarmScheduler = container.alarmScheduler

    init {
        viewModelScope.launch {
            container.categoryRepository.observeAll().collect { categories ->
                _state.update { it.copy(categories = categories) }
            }
        }
        viewModelScope.launch {
            container.goalRepository.observeAll().collect { goals ->
                _state.update { it.copy(goals = goals.map { g -> g.goal }) }
            }
        }
        viewModelScope.launch {
            if (taskId > 0) {
                val withSub = container.taskRepository.observeById(taskId).first()
                val task = withSub?.task
                if (task != null) {
                    val jalali = task.dueAtMillis?.let { PersianCalendar.fromMillis(it) }
                    val dateTime = task.dueAtMillis?.let {
                        LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it), ZoneId.systemDefault())
                    }
                    _state.update {
                        it.copy(
                            isEdit = true,
                            title = task.title,
                            description = task.description,
                            hasDate = task.dueAtMillis != null,
                            jYear = jalali?.year ?: 0,
                            jMonth = jalali?.month ?: 0,
                            jDay = jalali?.day ?: 0,
                            hasTime = task.hasTime,
                            hour = dateTime?.hour ?: 9,
                            minute = dateTime?.minute ?: 0,
                            priority = Priority.fromOrdinal(task.priority),
                            categoryId = task.categoryId,
                            goalId = task.goalId,
                            reminderEnabled = task.reminderEnabled,
                            reminderMinutesBefore = task.reminderMinutesBefore,
                            subtasks = withSub.subtasks.map { s ->
                                SubTaskDraft(id = s.id, title = s.title, isDone = s.isDone)
                            },
                        )
                    }
                }
            } else if (prefillDateMillis > 0) {
                val j = PersianCalendar.fromMillis(prefillDateMillis)
                _state.update { it.copy(hasDate = true, jYear = j.year, jMonth = j.month, jDay = j.day) }
            }
        }
    }

    fun update(transform: (TaskEditUiState) -> TaskEditUiState) {
        _state.update(transform)
    }

    fun setSubtasks(list: List<SubTaskDraft>) {
        _state.update { it.copy(subtasks = list) }
    }

    fun checkExactAlarm() {
        if (!scheduler.canScheduleExact()) {
            _state.update { it.copy(showExactAlarmDialog = true) }
        }
    }

    fun dismissExactAlarmDialog() {
        _state.update { it.copy(showExactAlarmDialog = false) }
    }

    fun openExactAlarmSettings(context: android.content.Context) {
        val intent = scheduler.exactAlarmSettingsIntent()
        if (intent != null) {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
        _state.update { it.copy(showExactAlarmDialog = false) }
    }

    fun requestDeleteConfirm() {
        _state.update { it.copy(showDeleteConfirm = true) }
    }

    fun dismissDeleteConfirm() {
        _state.update { it.copy(showDeleteConfirm = false) }
    }

    fun deleteTask() {
        if (taskId <= 0) return
        viewModelScope.launch {
            val task = container.taskRepository.getTask(taskId) ?: return@launch
            container.taskRepository.delete(task)
            _state.update { it.copy(saved = true) }
        }
    }

    fun save() {
        val s = _state.value
        if (s.title.isBlank()) return

        val dueAt: Long? = if (s.hasDate) {
            val g = JalaliCalendar.toGregorian(s.jYear, s.jMonth, s.jDay)
            val date = LocalDate.of(g.year, g.month, g.day)
            if (s.hasTime) {
                date.atTime(s.hour, s.minute)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        } else null

        val task = TaskEntity(
            id = if (taskId > 0) taskId else 0L,
            title = s.title.trim(),
            description = s.description.trim(),
            categoryId = s.categoryId,
            goalId = s.goalId,
            priority = s.priority.ordinal,
            dueAtMillis = dueAt,
            hasTime = s.hasDate && s.hasTime,
            reminderEnabled = s.reminderEnabled,
            reminderMinutesBefore = s.reminderMinutesBefore,
            isCompleted = false,
            createdAtMillis = if (taskId > 0) (System.currentTimeMillis()) else System.currentTimeMillis(),
            updatedAtMillis = System.currentTimeMillis(),
        )

        viewModelScope.launch {
            // Preserve lifecycle fields when editing an existing task
            val existing = if (taskId > 0) container.taskRepository.getTask(taskId) else null
            val finalTask = task.copy(
                id = if (taskId > 0) taskId else 0L,
                isCompleted = existing?.isCompleted ?: false,
                completedAtMillis = existing?.completedAtMillis,
                createdAtMillis = existing?.createdAtMillis ?: System.currentTimeMillis(),
            )
            val subtasks = s.subtasks
                .filter { it.title.isNotBlank() }
                .map { st -> SubTaskEntity(taskId = 0L, title = st.title.trim(), isDone = st.isDone) }
            container.taskRepository.saveTask(finalTask, subtasks)
            _state.update { it.copy(saved = true) }
        }
    }
}
