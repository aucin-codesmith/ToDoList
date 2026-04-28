package com.app.todolist.model

data class Task(
    val id: Int,
    val title: String,
    val date: String,
    val priority: Priority,
    var isCompleted: Boolean = false
)

enum class Priority {
    TINGGI,
    MEDIUM,
    RENDAH
}