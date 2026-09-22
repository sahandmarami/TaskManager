package com.taskmanager.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmanager.app.calendar.HolidaysDataSource
import com.taskmanager.app.calendar.JalaliCalendar
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

class CalendarViewModel(private val container: AppContainer) : ViewModel() {

    val selected = MutableStateFlow(PersianCalendar.todayJalali())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val tasksFlow = selected.flatMapLatest {
        container.taskRepository.observeAll()
    }

    val state: StateFlow<CalendarUiState> = combine(
        tasksFlow,
        container.categoryRepository.observeAll(),
        selected,
    ) { tasks, categories, sel ->
        val dayStart = PersianCalendar.startOfDayMillis(sel)
        val dayEnd = PersianCalendar.endOfDayMillis(sel)
        val dayTasks = tasks.filter { it.task.dueAtMillis != null && it.task.dueAtMillis!! in dayStart..dayEnd }
            .map { TaskCardData(it, categories.firstOrNull { c -> c.id == it.task.categoryId }) }
        CalendarUiState(
            selected = sel,
            occasions = HolidaysDataSource.occasionsFor(sel.year, sel.month, sel.day),
            dayTasks = dayTasks,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState(selected = PersianCalendar.todayJalali()))

    fun select(j: JalaliCalendar.JalaliDate) {
        selected.value = j
    }

    fun setCompleted(taskId: Long, completed: Boolean) {
        viewModelScope.launch { container.taskRepository.setCompleted(taskId, completed) }
    }
}

data class CalendarUiState(
    val selected: JalaliCalendar.JalaliDate,
    val occasions: List<HolidaysDataSource.Occasion> = emptyList(),
    val dayTasks: List<TaskCardData> = emptyList(),
)
