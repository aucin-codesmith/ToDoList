package com.app.todolist.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String,
    val description: String,
    val dateTime: String,
    val date: String,
    val priority: String,
    val assigneeTag: String? = null,
    val isCompleted: Boolean = false
)