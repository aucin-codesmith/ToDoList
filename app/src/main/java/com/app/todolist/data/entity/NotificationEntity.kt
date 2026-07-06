package com.app.todolist.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_table")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val body: String,
    val bodyHighlight: String = "",
    val time: String,
    val type: String, // disimpan sebagai String, di-mapping ke/dari enum NotifType
    val isRead: Boolean = false,
    val taskId: Int? = null,
    val taskCategory: String = "-",
    val taskDeadline: String = "-",
    val taskPriority: String = "-",
    val createdAtMillis: Long = System.currentTimeMillis()
)