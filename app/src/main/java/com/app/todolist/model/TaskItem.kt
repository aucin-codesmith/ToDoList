package com.app.todolist.model

data class TaskItem(
    val id: Int,
    val title: String,
    val category: String,
    val description: String,
    val dateTime: String,
    val priority: String = "Sedang", // "Tinggi", "Sedang", "Rendah"
    val assigneeTag: String? = null,
    var isCompleted: Boolean = false
)