package com.taskmanager.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.taskmanager.app.domain.model.Priority

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String = "",
    val categoryId: Long? = null,
    val goalId: Long? = null,
    /** 0=LOW, 1=MEDIUM, 2=HIGH, 3=CRITICAL */
    val priority: Int = Priority.MEDIUM.ordinal,
    /** Exact due moment. If [hasTime] is false this is start-of-day, otherwise date+time. Null = no date. */
    val dueAtMillis: Long? = null,
    val hasTime: Boolean = false,
    val reminderEnabled: Boolean = false,
    /** Minutes before [dueAtMillis] for the alarm (only meaningful when hasTime && reminderEnabled). */
    val reminderMinutesBefore: Int = 0,
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class SubTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val taskId: Long,
    val title: String,
    val isDone: Boolean = false,
    val sortOrder: Int = 0,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    /** Index into CategoryColors palette. */
    val colorIndex: Int = 0,
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String = "",
    val startAtMillis: Long? = null,
    val dueAtMillis: Long? = null,
    val isCompleted: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "goal_steps",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("goalId")]
)
data class GoalStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val goalId: Long,
    val title: String,
    val isDone: Boolean = false,
    val sortOrder: Int = 0,
)
