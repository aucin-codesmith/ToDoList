package com.app.todolist.data.repository

import android.content.Context
import android.util.Log
import com.app.todolist.data.AppDatabase
import com.app.todolist.data.api.ApiClient
import com.app.todolist.data.api.TaskResponse
import com.app.todolist.data.entity.TaskEntity
import com.app.todolist.model.TaskItem

object TaskRepository {

    private fun dao(context: Context) = AppDatabase.getDatabase(context).taskDao()

    // --- Mappers ---

    private fun TaskEntity.toTaskItem() = TaskItem(
        id = id, userId = userId, title = title, category = category, description = description,
        dateTime = dateTime, date = date, priority = priority, assigneeTag = assigneeTag,
        isCompleted = isCompleted, deadlineMillis = deadlineMillis
    )

    private fun TaskItem.toEntity() = TaskEntity(
        id = id, userId = userId, title = title, category = category, description = description,
        dateTime = dateTime, date = date, priority = priority, assigneeTag = assigneeTag,
        isCompleted = isCompleted, deadlineMillis = deadlineMillis
    )

    private fun TaskResponse.toEntity() = TaskEntity(
        id = id, userId = userId, title = title, category = category, description = description,
        dateTime = dateTime, date = date, priority = priority, assigneeTag = null,
        isCompleted = isCompleted, deadlineMillis = 0L
    )

    private fun TaskItem.toResponse() = TaskResponse(
        id = id, userId = userId, title = title, description = description, category = category,
        priority = priority, dateTime = dateTime, date = date, isCompleted = isCompleted
    )

    // --- Read ---

    // Menambahkan parameter userId untuk filtrasi API
    suspend fun getTaskItems(context: Context, userId: String): List<TaskItem> {
        try {
            val response = ApiClient.instance.getAllTasks(userId)
            if (response.isSuccessful) {
                response.body()?.forEach { taskFromApi ->
                    try {
                        dao(context).insertTask(taskFromApi.toEntity())
                    } catch (e: Exception) {
                        // Abaikan duplikat
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("API_SYNC", "Offline/Error GET: ${e.message}")
        }
        // Pastikan di TaskDao kamu juga membuat fungsi query filter berdasarkan userId jika ingin lokal terpisah sempurna
        return dao(context).getAllTasks().map { it.toTaskItem() }.filter { it.userId == userId }
    }

    suspend fun getRecentTasks(context: Context, userId: String, limit: Int = 3): List<TaskItem> =
        getTaskItems(context, userId).sortedBy { it.isCompleted }.take(limit)

    suspend fun getTaskItemById(context: Context, id: Int): TaskItem? =
        dao(context).getTaskById(id)?.toTaskItem()

    // --- Write ---

    suspend fun addTaskItem(context: Context, task: TaskItem): Int {
        var localId = 0L
        try {
            val response = ApiClient.instance.addTask(task.toResponse())
            if (response.isSuccessful && response.body() != null) {
                val entityToSave = response.body()!!.toEntity().copy(deadlineMillis = task.deadlineMillis)
                localId = dao(context).insertTask(entityToSave)
            } else {
                localId = dao(context).insertTask(task.toEntity().copy(id = 0))
            }
        } catch (e: Exception) {
            Log.e("API_SYNC", "Offline/Error POST: ${e.message}")
            localId = dao(context).insertTask(task.toEntity().copy(id = 0))
        }
        return localId.toInt()
    }

    suspend fun deleteTaskItem(context: Context, id: Int) {
        try {
            ApiClient.instance.deleteTask(id)
        } catch (e: Exception) {
            Log.e("API_SYNC", "Offline/Error DELETE: ${e.message}")
        }
        dao(context).deleteTaskById(id)
    }

    suspend fun updateTaskItemCompleted(context: Context, id: Int, isCompleted: Boolean) {
        dao(context).updateCompleted(id, isCompleted)
        dao(context).getTaskById(id)?.let { updatedEntity ->
            try {
                ApiClient.instance.updateTask(id, updatedEntity.toTaskItem().toResponse())
            } catch (e: Exception) {
                Log.e("API_SYNC", "Offline/Error PUT Status: ${e.message}")
            }
        }
    }

    suspend fun updateTaskItem(context: Context, updated: TaskItem) {
        dao(context).updateTask(updated.toEntity())
        try {
            ApiClient.instance.updateTask(updated.id, updated.toResponse())
        } catch (e: Exception) {
            Log.e("API_SYNC", "Offline/Error PUT Update: ${e.message}")
        }
    }

    // --- Summary Helpers ---

    suspend fun getTotalCount(context: Context, userId: String): Int = getTaskItems(context, userId).size
    suspend fun getCompletedCount(context: Context, userId: String): Int = getTaskItems(context, userId).count { it.isCompleted }
    suspend fun getRemainingCount(context: Context, userId: String): Int = getTaskItems(context, userId).count { !it.isCompleted }
}