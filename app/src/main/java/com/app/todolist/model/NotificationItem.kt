package com.app.todolist.model

data class NotificationItem(
    val id: Int,
    val title: String,
    val body: String,               // plain text body (untuk list)
    val bodyHighlight: String = "", // kata yang di-highlight warna primary (opsional)
    val time: String,
    val type: NotifType,
    val isRead: Boolean = false,
    // ── Referensi ke task terkait ──────────────────────────────────────────────
    val taskId: Int? = null,        // null jika notif tidak punya task terkait (misal SYSTEM)
    val taskCategory: String = "-",
    val taskDeadline: String = "-",
    val taskPriority: String = "-"
)

enum class NotifType {
    DEADLINE,   // bell icon    – primary bg
    REMINDER,   // clock icon   – primary bg
    DONE,       // check icon   – grey bg
    SYSTEM      // refresh icon – grey bg
}