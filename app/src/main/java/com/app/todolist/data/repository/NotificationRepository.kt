package com.app.todolist.data.repository

import android.content.Context
import com.app.todolist.data.AppDatabase
import com.app.todolist.data.entity.NotificationEntity
import com.app.todolist.model.NotificationItem
import com.app.todolist.model.NotifType

/**
 * NotificationRepository — sudah persisted ke SQLite via Room (bukan dummy
 * in-memory lagi). Tidak ada seed data dummy sama sekali — semua notifikasi
 * yang tampil murni berasal dari kejadian nyata di app, misalnya reminder
 * deadline dari TaskReminderWorker.
 *
 * Semua fungsi jadi `suspend` karena akses database. Panggil dari
 * lifecycleScope.launch { } di Activity, JANGAN dari Main thread langsung.
 */
object NotificationRepository {

    private fun dao(context: Context) = AppDatabase.getDatabase(context).notificationDao()

    // ── Mapper: NotificationEntity (Room) <-> NotificationItem (UI model) ──────

    private fun NotificationEntity.toItem() = NotificationItem(
        id = id,
        title = title,
        body = body,
        bodyHighlight = bodyHighlight,
        time = time,
        type = NotifType.valueOf(type),
        isRead = isRead,
        taskId = taskId,
        taskCategory = taskCategory,
        taskDeadline = taskDeadline,
        taskPriority = taskPriority
    )

    private fun NotificationItem.toEntity() = NotificationEntity(
        id = id,
        title = title,
        body = body,
        bodyHighlight = bodyHighlight,
        time = time,
        type = type.name,
        isRead = isRead,
        taskId = taskId,
        taskCategory = taskCategory,
        taskDeadline = taskDeadline,
        taskPriority = taskPriority
    )

    // ── Read ──────────────────────────────────────────────────────────────────

    /** Semua notifikasi (terbaru duluan) — dipakai NotificationListActivity. */
    suspend fun getNotifications(context: Context): List<NotificationItem> =
        dao(context).getAllNotifications().map { it.toItem() }

    /** Cari satu notifikasi berdasarkan id — dipakai DetailNotificationActivity/NotificationListActivity. */
    suspend fun getNotificationById(context: Context, id: Int): NotificationItem? =
        dao(context).getNotificationById(id)?.toItem()

    /** Jumlah notifikasi yang belum dibaca — dipakai badge di HomeFragment. */
    suspend fun getUnreadCount(context: Context): Int =
        dao(context).getUnreadCount()

    // ── Write ─────────────────────────────────────────────────────────────────

    /** Tandai notifikasi sebagai sudah dibaca. */
    suspend fun markAsRead(context: Context, id: Int) {
        dao(context).markAsRead(id)
    }

    /** Tandai semua notifikasi sebagai sudah dibaca. */
    suspend fun markAllAsRead(context: Context) {
        dao(context).markAllAsRead()
    }

    /**
     * Tambah notifikasi baru. Dipanggil dari TaskReminderWorker saat reminder
     * deadline muncul, supaya kelihatan juga di NotificationListActivity
     * (bukan cuma di notification tray sistem).
     * id di-set 0 supaya Room auto-generate id baru.
     */
    suspend fun addNotification(context: Context, item: NotificationItem) {
        dao(context).insertNotification(item.toEntity().copy(id = 0))
    }
}