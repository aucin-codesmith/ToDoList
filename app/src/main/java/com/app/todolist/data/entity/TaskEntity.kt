package com.app.todolist.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val title: String,
    val category: String,
    val description: String,
    val dateTime: String,
    val date: String,
    val priority: String,
    val assigneeTag: String? = null,
    val isCompleted: Boolean = false,
    // Timestamp asli (epoch millis) dari deadline — dipakai buat jadwalin reminder.
    // 0L berarti task ini belum punya deadline valid (data lama sebelum field ini ada).
    val deadlineMillis: Long = 0L
)