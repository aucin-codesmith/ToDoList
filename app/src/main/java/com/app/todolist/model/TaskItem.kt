package com.app.todolist.model

data class TaskItem(
    val id: Int,
    val title: String,
    val category: String,
    val dateTime: String,
    val assigneeTag: String? = null,
    var isCompleted: Boolean = false
)