package com.taskmanager.app.data.repository

import com.taskmanager.app.data.db.CategoryDao
import com.taskmanager.app.data.db.CategoryEntity
import com.taskmanager.app.data.db.TaskDao
import com.taskmanager.app.domain.model.DefaultCategories
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val taskDao: TaskDao,
) {

    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    suspend fun ensureDefaultCategories() {
        if (categoryDao.count() == 0) {
            categoryDao.insertAll(
                DefaultCategories.list.mapIndexed { index, (name, color) ->
                    CategoryEntity(id = (index + 1).toLong(), name = name, colorIndex = color)
                }
            )
        }
    }

    suspend fun add(name: String, colorIndex: Int): Long =
        categoryDao.insert(CategoryEntity(name = name.trim(), colorIndex = colorIndex))

    /** Deleting a category un-links its tasks (they stay, without a category). */
    suspend fun delete(category: CategoryEntity) {
        taskDao.clearCategoryLink(category.id)
        categoryDao.delete(category)
    }
}
