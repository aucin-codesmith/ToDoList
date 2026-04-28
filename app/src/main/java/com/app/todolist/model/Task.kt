package com.app.todolist.model

data class Task(
    val id: Int,
    val title: String,
    val date: String,
    var isCompleted: Boolean = false
)