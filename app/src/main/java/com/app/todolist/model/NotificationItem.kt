package com.app.todolist.model

data class NotificationItem(
    val id: Int,
    val title: String,
    val body: String,           // plain text body (untuk list)
    val bodyHighlight: String = "", // kata yang di-highlight warna primary (opsional)
    val time: String,
    val type: NotifType,
    val isRead: Boolean = false,
    // ── Data task terkait (untuk halaman detail) ──
    val taskCategory: String = "-",
    val taskDeadline: String = "-",
    val taskPriority: String = "-"
)

enum class NotifType {
    DEADLINE,   // bell icon   – purple bg
    REMINDER,   // clock icon  – purple bg
    DONE,       // check icon  – grey bg
    SYSTEM      // refresh icon – grey bg
}