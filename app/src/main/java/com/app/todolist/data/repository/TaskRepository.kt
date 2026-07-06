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
        id = id, title = title, category = category, description = description,
        dateTime = dateTime, date = date, priority = priority, assigneeTag = assigneeTag,
        isCompleted = isCompleted
    )

    private fun TaskItem.toEntity() = TaskEntity(
        id = id, title = title, category = category, description = description,
        dateTime = dateTime, date = date, priority = priority, assigneeTag = assigneeTag,
        isCompleted = isCompleted
    )

    private fun TaskResponse.toEntity() = TaskEntity(
        id = id, title = title, category = category, description = description,
        dateTime = dateTime, date = date, priority = priority, assigneeTag = null,
        isCompleted = isCompleted
    )

    private fun TaskItem.toResponse() = TaskResponse(
        id = id, title = title, description = description, category = category,
        priority = priority, dateTime = dateTime, date = date, isCompleted = isCompleted
    )

    // --- Read ---

    suspend fun getTaskItems(context: Context): List<TaskItem> {
        try {
            val response = ApiClient.instance.getAllTasks()
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
        return dao(context).getAllTasks().map { it.toTaskItem() }
    }

    suspend fun getRecentTasks(context: Context, limit: Int = 3): List<TaskItem> =
        getTaskItems(context).sortedBy { it.isCompleted }.take(limit)

    suspend fun getTaskItemById(context: Context, id: Int): TaskItem? =
        dao(context).getTaskById(id)?.toTaskItem()

    // --- Write ---

    suspend fun addTaskItem(context: Context, task: TaskItem) {
        try {
            val response = ApiClient.instance.addTask(task.toResponse())
            if (response.isSuccessful && response.body() != null) {
                dao(context).insertTask(response.body()!!.toEntity())
            } else {
                dao(context).insertTask(task.toEntity().copy(id = 0))
            }
        } catch (e: Exception) {
            Log.e("API_SYNC", "Offline/Error POST: ${e.message}")
            dao(context).insertTask(task.toEntity().copy(id = 0))
        }
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

    suspend fun getTotalCount(context: Context): Int = getTaskItems(context).size
    suspend fun getCompletedCount(context: Context): Int = getTaskItems(context).count { it.isCompleted }
    suspend fun getRemainingCount(context: Context): Int = getTaskItems(context).count { !it.isCompleted }
}