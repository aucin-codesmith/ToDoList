package com.app.todolist.model

data class NotificationItem(
    val id: Int,
    val title: String,
    val body: String,
    val time: String,
    val type: NotifType,        // menentukan icon & warna background
    val isRead: Boolean = false
)

enum class NotifType {
    DEADLINE,   // bell icon  – purple bg
    REMINDER,   // calendar-clock icon – purple bg
    DONE,       // check-circle icon   – grey bg
    SYSTEM      // refresh icon        – grey bg
}