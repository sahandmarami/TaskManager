package com.taskmanager.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        SubTaskEntity::class,
        CategoryEntity::class,
        GoalEntity::class,
        GoalStepEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class TaskManagerDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun subTaskDao(): SubTaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun goalDao(): GoalDao
    abstract fun goalStepDao(): GoalStepDao
}
