package com.taskmanager.app.data.repository

import com.taskmanager.app.data.db.GoalDao
import com.taskmanager.app.data.db.GoalEntity
import com.taskmanager.app.data.db.GoalStepDao
import com.taskmanager.app.data.db.GoalStepEntity
import com.taskmanager.app.data.db.GoalWithSteps
import com.taskmanager.app.data.db.TaskDao
import kotlinx.coroutines.flow.Flow

class GoalRepository(
    private val goalDao: GoalDao,
    private val goalStepDao: GoalStepDao,
    private val taskDao: TaskDao,
) {

    fun observeAll(): Flow<List<GoalWithSteps>> = goalDao.observeAll()

    fun observeById(id: Long): Flow<GoalWithSteps?> = goalDao.observeById(id)

    suspend fun saveGoal(goal: GoalEntity): Long =
        if (goal.id == 0L) goalDao.insert(goal) else {
            goalDao.update(goal)
            goal.id
        }

    suspend fun delete(goal: GoalEntity) {
        // unlink tasks, then cascade-deletes the steps
        taskDao.clearGoalLink(goal.id)
        goalDao.delete(goal)
    }

    suspend fun addStep(goalId: Long, title: String, sortOrder: Int): Long =
        goalStepDao.insert(GoalStepEntity(goalId = goalId, title = title.trim(), sortOrder = sortOrder))

    suspend fun toggleStep(step: GoalStepEntity) {
        goalStepDao.update(step.copy(isDone = !step.isDone))
    }

    suspend fun deleteStep(step: GoalStepEntity) {
        goalStepDao.delete(step)
    }
}
