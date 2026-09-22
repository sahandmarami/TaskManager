package com.taskmanager.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmanager.app.calendar.PersianCalendar
import com.taskmanager.app.di.AppContainer
import com.taskmanager.app.ui.components.TaskCardData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val todayTasks: List<TaskCardData> = emptyList(),
    val overdueTasks: List<TaskCardData> = emptyList(),
    val importantTasks: List<TaskCardData> = emptyList(),
    val doneToday: Int = 0,
    val totalToday: Int = 0,
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val _refreshKey = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val tasksFlow = _refreshKey.flatMapLatest {
        container.taskRepository.observeAll()
    }

    val state: StateFlow<HomeUiState> = combine(
        tasksFlow,
        container.categoryRepository.observeAll(),
        container.goalRepository.observeAll(),
    ) { tasks, categories, _ ->
        val today = PersianCalendar.todayJalali()
        val dayStart = PersianCalendar.startOfDayMillis(today)
        val dayEnd = PersianCalendar.endOfDayMillis(today)
        val cardData = tasks.map { t ->
            TaskCardData(t, categories.firstOrNull { it.id == t.task.categoryId })
        }
        val todayTasks = cardData.filter {
            it.item.task.dueAtMillis != null && it.item.task.dueAtMillis!! in dayStart..dayEnd
        }
        val overdue = cardData.filter {
            it.item.task.dueAtMillis != null && it.item.task.dueAtMillis!! < dayStart && !it.item.task.isCompleted
        }
        val important = cardData.filter {
            !it.item.task.isCompleted && it.item.task.priority >= 2
        }.take(4)
        HomeUiState(
            todayTasks = todayTasks,
            overdueTasks = overdue,
            importantTasks = important,
            doneToday = todayTasks.count { it.item.task.isCompleted },
            totalToday = todayTasks.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setCompleted(taskId: Long, completed: Boolean) {
        viewModelScope.launch { container.taskRepository.setCompleted(taskId, completed) }
    }
}
