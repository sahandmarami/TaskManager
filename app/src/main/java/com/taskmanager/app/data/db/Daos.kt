package com.taskmanager.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(parentColumn = "id", entityColumn = "taskId")
    val subtasks: List<SubTaskEntity>,
)

data class GoalWithSteps(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goalId")
    val steps: List<GoalStepEntity>,
)

@Dao
interface TaskDao {

    @Transaction
    @Query(
        """SELECT * FROM tasks ORDER BY isCompleted ASC,
           CASE WHEN dueAtMillis IS NULL THEN 1 ELSE 0 END ASC, dueAtMillis ASC,
           priority DESC, createdAtMillis DESC"""
    )
    fun observeAll(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Long): Flow<TaskWithSubtasks?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Transaction
    @Query("SELECT * FROM tasks WHERE goalId = :goalId ORDER BY isCompleted ASC, createdAtMillis ASC")
    fun observeTasksForGoal(goalId: Long): Flow<List<TaskWithSubtasks>>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :completed, completedAtMillis = :completedAt, updatedAtMillis = :now WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean, completedAt: Long?, now: Long)

    @Query("UPDATE tasks SET goalId = NULL WHERE goalId = :goalId")
    suspend fun clearGoalLink(goalId: Long)

    @Query("UPDATE tasks SET categoryId = NULL WHERE categoryId = :categoryId")
    suspend fun clearCategoryLink(categoryId: Long)

    /** All incomplete tasks with a timed reminder active. */
    @Query(
        """SELECT * FROM tasks
           WHERE isCompleted = 0 AND hasTime = 1 AND reminderEnabled = 1 AND dueAtMillis IS NOT NULL"""
    )
    suspend fun getReminderTasks(): List<TaskEntity>

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompleted()

    @Query("SELECT * FROM tasks")
    suspend fun getAllRaw(): List<TaskEntity>

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}

@Dao
interface SubTaskDao {

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY sortOrder ASC, id ASC")
    fun observeForTask(taskId: Long): Flow<List<SubTaskEntity>>

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY sortOrder ASC, id ASC")
    suspend fun getForTask(taskId: Long): List<SubTaskEntity>

    @Insert
    suspend fun insert(subtask: SubTaskEntity): Long

    @Insert
    suspend fun insertAll(subtasks: List<SubTaskEntity>)

    @Update
    suspend fun update(subtask: SubTaskEntity)

    @Delete
    suspend fun delete(subtask: SubTaskEntity)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteForTask(taskId: Long)

    @Query("DELETE FROM subtasks")
    suspend fun deleteAll()
}

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllRaw(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}

@Dao
interface GoalDao {

    @Transaction
    @Query("SELECT * FROM goals ORDER BY isCompleted ASC, createdAtMillis DESC")
    fun observeAll(): Flow<List<GoalWithSteps>>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeById(id: Long): Flow<GoalWithSteps?>

    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("UPDATE goals SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Query("SELECT * FROM goals")
    suspend fun getAllRaw(): List<GoalEntity>

    @Query("DELETE FROM goals")
    suspend fun deleteAll()
}

@Dao
interface GoalStepDao {

    @Query("SELECT * FROM goal_steps WHERE goalId = :goalId ORDER BY sortOrder ASC, id ASC")
    fun observeForGoal(goalId: Long): Flow<List<GoalStepEntity>>

    @Insert
    suspend fun insert(step: GoalStepEntity): Long

    @Update
    suspend fun update(step: GoalStepEntity)

    @Delete
    suspend fun delete(step: GoalStepEntity)

    @Query("UPDATE goal_steps SET isDone = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)

    @Query("SELECT * FROM goal_steps WHERE goalId = :goalId")
    suspend fun getAllRawForGoal(goalId: Long): List<GoalStepEntity>

    @Query("DELETE FROM goal_steps")
    suspend fun deleteAll()
}
