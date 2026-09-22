package com.taskmanager.app.ui.goals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmanager.app.calendar.PersianCalendar
import com.taskmanager.app.data.db.GoalEntity
import com.taskmanager.app.data.db.GoalStepEntity
import com.taskmanager.app.di.AppContainer
import com.taskmanager.app.ui.components.TaskCardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GoalCardData(
    val goal: GoalEntity,
    val steps: List<GoalStepEntity>,
    val tasks: List<TaskCardData>,
) {
    val progress: Float
        get() {
            val total = steps.size + tasks.size
            if (total == 0) return 0f
            val done = steps.count { it.isDone } + tasks.count { it.item.task.isCompleted }
            return done.toFloat() / total
        }
}

class GoalsViewModel(private val container: AppContainer) : ViewModel() {

    val state: StateFlow<GoalsUiState> = combine(
        container.goalRepository.observeAll(),
        container.taskRepository.observeAll(),
        container.categoryRepository.observeAll(),
    ) { goals, tasks, categories ->
        GoalsUiState(
            goals = goals.map { g ->
                GoalCardData(
                    goal = g.goal,
                    steps = g.steps,
                    tasks = tasks.filter { it.task.goalId == g.goal.id }
                        .map { TaskCardData(it, categories.firstOrNull { c -> c.id == it.task.categoryId }) },
                )
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

    fun addGoal(title: String, description: String, startAtMillis: Long?, dueAtMillis: Long?, onDone: () -> Unit) {
        viewModelScope.launch {
            container.goalRepository.saveGoal(
                GoalEntity(
                    title = title.trim(),
                    description = description.trim(),
                    startAtMillis = startAtMillis,
                    dueAtMillis = dueAtMillis,
                )
            )
            onDone()
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch { container.goalRepository.delete(goal) }
    }
}

data class GoalsUiState(
    val goals: List<GoalCardData> = emptyList(),
)

// ---------------- Goal detail ----------------

class GoalDetailViewModel(
    private val container: AppContainer,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val goalId: Long = savedStateHandle.get<Long>("goalId") ?: -1L

    val state: StateFlow<GoalDetailUiState> = combine(
        container.goalRepository.observeById(goalId),
        container.taskRepository.observeTasksForGoal(goalId),
        container.categoryRepository.observeAll(),
    ) { goalWithSteps, tasks, categories ->
        val data = GoalCardData(
            goal = goalWithSteps?.goal ?: GoalEntity(id = goalId, title = "…"),
            steps = goalWithSteps?.steps ?: emptyList(),
            tasks = tasks.map { TaskCardData(it, categories.firstOrNull { c -> c.id == it.task.categoryId }) },
        )
        GoalDetailUiState(
            loading = goalWithSteps == null,
            data = data,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalDetailUiState(loading = true))

    fun addStep(title: String) {
        if (title.isBlank()) return
        val current = state.value.data?.steps?.size ?: 0
        viewModelScope.launch {
            container.goalRepository.addStep(goalId, title, current)
        }
    }

    fun toggleStep(step: GoalStepEntity) {
        viewModelScope.launch { container.goalRepository.toggleStep(step) }
    }

    fun deleteStep(step: GoalStepEntity) {
        viewModelScope.launch { container.goalRepository.deleteStep(step) }
    }

    fun deleteGoal(onDone: () -> Unit) {
        val goal = state.value.data?.goal ?: return
        viewModelScope.launch {
            container.goalRepository.delete(goal)
            onDone()
        }
    }

    fun toggleTask(taskId: Long, completed: Boolean) {
        viewModelScope.launch { container.taskRepository.setCompleted(taskId, completed) }
    }
}

data class GoalDetailUiState(
    val loading: Boolean = false,
    val data: GoalCardData? = null,
)

fun formatGoalDate(millis: Long?): String =
    if (millis == null) "—" else PersianCalendar.formatJalali(PersianCalendar.fromMillis(millis))
