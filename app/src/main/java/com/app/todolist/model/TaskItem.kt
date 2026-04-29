package com.app.todolist.model

/**
 * Model tunggal untuk semua data tugas.
 * Digunakan oleh semua Activity dan Adapter.
 *
 * Digunakan oleh:
 *  - HomeActivity          (via TaskAdapter)
 *  - TaskListActivity      (via TaskListAdapter)
 *  - DetailTaskActivity    (via Intent extras)
 *  - EditTaskActivity      (via Intent extras)
 *  - NotificationListActivity (referensi taskId)
 */
data class TaskItem(
    val id: Int,
    val title: String,
    val category: String,
    val description: String,
    val dateTime: String,            // format "Hari ini, 14:00" — dipakai TaskListAdapter
    val date: String,                // format "14 Okt, 2023"   — dipakai TaskAdapter (Home)
    val priority: String = "Sedang", // "Tinggi" | "Sedang" | "Rendah"
    val assigneeTag: String? = null,
    var isCompleted: Boolean = false
)