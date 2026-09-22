package com.taskmanager.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmanager.app.calendar.PersianCalendar
import com.taskmanager.app.di.AppContainer
import com.taskmanager.app.domain.model.TaskFilter
import com.taskmanager.app.ui.components.TaskCardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(private val container: AppContainer) : ViewModel() {

    val query = MutableStateFlow("")
    val filter = MutableStateFlow(TaskFilter.ALL)

    val state: StateFlow<TasksUiState> = combine(
        container.taskRepository.observeAll(),
        container.categoryRepository.observeAll(),
        query,
        filter,
    ) { tasks, categories, q, f ->
        val today = PersianCalendar.todayJalali()
        val dayStart = PersianCalendar.startOfDayMillis(today)
        val dayEnd = PersianCalendar.endOfDayMillis(today)
        val data = tasks.map { t ->
            TaskCardData(t, categories.firstOrNull { it.id == t.task.categoryId })
        }
        val filtered = data.filter { d ->
            val t = d.item.task
            val matchesQuery = q.isBlank() ||
                t.title.contains(q, ignoreCase = true) ||
                t.description.contains(q, ignoreCase = true)
            val due = t.dueAtMillis
            val matchesFilter = when (f) {
                TaskFilter.ALL -> true
                TaskFilter.TODAY -> due != null && due in dayStart..dayEnd
                TaskFilter.UPCOMING -> due != null && due > dayEnd
                TaskFilter.OVERDUE -> due != null && due < dayStart && !t.isCompleted
                TaskFilter.COMPLETED -> t.isCompleted
                TaskFilter.NOT_COMPLETED -> !t.isCompleted
                TaskFilter.IMPORTANT -> !t.isCompleted && t.priority >= 2
                TaskFilter.NO_DATE -> due == null
            }
            matchesQuery && matchesFilter
        }
        TasksUiState(
            tasks = filtered,
            counts = TaskFilter.entries.associateWith { f2 ->
                data.count { d ->
                    val t = d.item.task
                    val due = t.dueAtMillis
                    when (f2) {
                        TaskFilter.ALL -> true
                        TaskFilter.TODAY -> due != null && due in dayStart..dayEnd
                        TaskFilter.UPCOMING -> due != null && due > dayEnd
                        TaskFilter.OVERDUE -> due != null && due < dayStart && !t.isCompleted
                        TaskFilter.COMPLETED -> t.isCompleted
                        TaskFilter.NOT_COMPLETED -> !t.isCompleted
                        TaskFilter.IMPORTANT -> !t.isCompleted && t.priority >= 2
                        TaskFilter.NO_DATE -> due == null
                    }
                }
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

    fun setQuery(q: String) { query.value = q }
    fun setFilter(f: TaskFilter) { filter.value = f }

    fun setCompleted(taskId: Long, completed: Boolean) {
        viewModelScope.launch { container.taskRepository.setCompleted(taskId, completed) }
    }
}

data class TasksUiState(
    val tasks: List<TaskCardData> = emptyList(),
    val counts: Map<TaskFilter, Int> = emptyMap(),
)
