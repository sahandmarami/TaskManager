package com.taskmanager.app.data.repository

import com.taskmanager.app.data.db.CategoryEntity
import com.taskmanager.app.data.db.GoalEntity
import com.taskmanager.app.data.db.GoalStepEntity
import com.taskmanager.app.data.db.SubTaskEntity
import com.taskmanager.app.data.db.TaskDao
import com.taskmanager.app.data.db.SubTaskDao
import com.taskmanager.app.data.db.CategoryDao
import com.taskmanager.app.data.db.GoalDao
import com.taskmanager.app.data.db.GoalStepDao
import com.taskmanager.app.data.db.TaskEntity
import com.taskmanager.app.data.settings.SettingsRepository
import org.json.JSONArray
import org.json.JSONObject

/**
 * Full offline backup: tasks, subtasks, categories, goals, goal steps and settings
 * are exported/imported as a single JSON document.
 */
class BackupManager(
    private val taskDao: TaskDao,
    private val subTaskDao: SubTaskDao,
    private val categoryDao: CategoryDao,
    private val goalDao: GoalDao,
    private val goalStepDao: GoalStepDao,
    private val settingsRepository: SettingsRepository,
) {

    companion object {
        private const val FORMAT_VERSION = 1
    }

    suspend fun exportJson(): String {
        val tasks = mutableListOf<JSONObject>()
        taskDao.getAllRaw().forEach { t ->
            tasks.add(
                JSONObject()
                    .put("id", t.id)
                    .put("title", t.title)
                    .put("description", t.description)
                    .put("categoryId", t.categoryId ?: JSONObject.NULL)
                    .put("goalId", t.goalId ?: JSONObject.NULL)
                    .put("priority", t.priority)
                    .put("dueAtMillis", t.dueAtMillis ?: JSONObject.NULL)
                    .put("hasTime", t.hasTime)
                    .put("reminderEnabled", t.reminderEnabled)
                    .put("reminderMinutesBefore", t.reminderMinutesBefore)
                    .put("isCompleted", t.isCompleted)
                    .put("completedAtMillis", t.completedAtMillis ?: JSONObject.NULL)
                    .put("createdAtMillis", t.createdAtMillis)
                    .put("updatedAtMillis", t.updatedAtMillis)
            )
        }

        val subtasks = mutableListOf<JSONObject>()
        taskDao.getAllRaw().forEach { t ->
            subTaskDao.getForTask(t.id).forEach { s ->
                subtasks.add(
                    JSONObject()
                        .put("id", s.id)
                        .put("taskId", s.taskId)
                        .put("title", s.title)
                        .put("isDone", s.isDone)
                        .put("sortOrder", s.sortOrder)
                )
            }
        }

        val categories = mutableListOf<JSONObject>()
        categoryDao.getAllRaw().forEach { c ->
            categories.add(
                JSONObject()
                    .put("id", c.id)
                    .put("name", c.name)
                    .put("colorIndex", c.colorIndex)
            )
        }

        val goals = mutableListOf<JSONObject>()
        goalDao.getAllRaw().forEach { g ->
            goals.add(
                JSONObject()
                    .put("id", g.id)
                    .put("title", g.title)
                    .put("description", g.description)
                    .put("startAtMillis", g.startAtMillis ?: JSONObject.NULL)
                    .put("dueAtMillis", g.dueAtMillis ?: JSONObject.NULL)
                    .put("isCompleted", g.isCompleted)
                    .put("createdAtMillis", g.createdAtMillis)
            )
        }

        val steps = mutableListOf<JSONObject>()
        goalDao.getAllRaw().forEach { g ->
            goalStepDao.getAllRawForGoal(g.id).forEach { s ->
                steps.add(
                    JSONObject()
                        .put("id", s.id)
                        .put("goalId", s.goalId)
                        .put("title", s.title)
                        .put("isDone", s.isDone)
                        .put("sortOrder", s.sortOrder)
                )
            }
        }

        val settingsJson = JSONObject()
        settingsRepository.snapshot().forEach { (k, v) -> settingsJson.put(k, v) }

        return JSONObject()
            .put("app", "TaskManager")
            .put("formatVersion", FORMAT_VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("tasks", JSONArray(tasks))
            .put("subtasks", JSONArray(subtasks))
            .put("categories", JSONArray(categories))
            .put("goals", JSONArray(goals))
            .put("goalSteps", JSONArray(steps))
            .put("settings", settingsJson)
            .toString(2)
    }

    /** Imports a backup document, replacing all current data. Returns number of imported tasks. */
    suspend fun importJson(json: String): Int {
        val root = JSONObject(json)
        require(root.optString("app") == "TaskManager") { "فایل پشتیبان معتبر نیست" }

        val tasks = root.optJSONArray("tasks") ?: JSONArray()
        val subtasks = root.optJSONArray("subtasks") ?: JSONArray()
        val categories = root.optJSONArray("categories") ?: JSONArray()
        val goals = root.optJSONArray("goals") ?: JSONArray()
        val steps = root.optJSONArray("goalSteps") ?: JSONArray()

        // Replace data
        subTaskDao.deleteAll()
        taskDao.deleteAll()
        goalStepDao.deleteAll()
        goalDao.deleteAll()
        categoryDao.deleteAll()

        for (i in 0 until categories.length()) {
            val o = categories.getJSONObject(i)
            categoryDao.insert(
                CategoryEntity(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    colorIndex = o.optInt("colorIndex", 0),
                )
            )
        }

        for (i in 0 until goals.length()) {
            val o = goals.getJSONObject(i)
            goalDao.insert(
                GoalEntity(
                    id = o.getLong("id"),
                    title = o.getString("title"),
                    description = o.optString("description", ""),
                    startAtMillis = if (o.isNull("startAtMillis")) null else o.getLong("startAtMillis"),
                    dueAtMillis = if (o.isNull("dueAtMillis")) null else o.getLong("dueAtMillis"),
                    isCompleted = o.optBoolean("isCompleted", false),
                    createdAtMillis = o.optLong("createdAtMillis", System.currentTimeMillis()),
                )
            )
        }

        for (i in 0 until steps.length()) {
            val o = steps.getJSONObject(i)
            goalStepDao.insert(
                GoalStepEntity(
                    id = o.getLong("id"),
                    goalId = o.getLong("goalId"),
                    title = o.getString("title"),
                    isDone = o.optBoolean("isDone", false),
                    sortOrder = o.optInt("sortOrder", 0),
                )
            )
        }

        for (i in 0 until tasks.length()) {
            val o = tasks.getJSONObject(i)
            taskDao.insert(
                TaskEntity(
                    id = o.getLong("id"),
                    title = o.getString("title"),
                    description = o.optString("description", ""),
                    categoryId = if (o.isNull("categoryId")) null else o.getLong("categoryId"),
                    goalId = if (o.isNull("goalId")) null else o.getLong("goalId"),
                    priority = o.optInt("priority", 1),
                    dueAtMillis = if (o.isNull("dueAtMillis")) null else o.getLong("dueAtMillis"),
                    hasTime = o.optBoolean("hasTime", false),
                    reminderEnabled = o.optBoolean("reminderEnabled", false),
                    reminderMinutesBefore = o.optInt("reminderMinutesBefore", 0),
                    isCompleted = o.optBoolean("isCompleted", false),
                    completedAtMillis = if (o.isNull("completedAtMillis")) null else o.getLong("completedAtMillis"),
                    createdAtMillis = o.optLong("createdAtMillis", System.currentTimeMillis()),
                    updatedAtMillis = o.optLong("updatedAtMillis", System.currentTimeMillis()),
                )
            )
        }

        for (i in 0 until subtasks.length()) {
            val o = subtasks.getJSONObject(i)
            subTaskDao.insert(
                SubTaskEntity(
                    id = o.getLong("id"),
                    taskId = o.getLong("taskId"),
                    title = o.getString("title"),
                    isDone = o.optBoolean("isDone", false),
                    sortOrder = o.optInt("sortOrder", 0),
                )
            )
        }

        val settingsJson = root.optJSONObject("settings")
        if (settingsJson != null) {
            val map = mutableMapOf<String, Any>()
            settingsJson.keys().forEach { key ->
                val v = settingsJson.get(key)
                if (v is Boolean || v is Number) map[key] = v
            }
            settingsRepository.restore(map)
        }

        return tasks.length()
    }
}
