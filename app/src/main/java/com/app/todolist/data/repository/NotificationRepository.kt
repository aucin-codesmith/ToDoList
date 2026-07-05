package com.app.todolist.data.repository

import com.app.todolist.model.NotificationItem
import com.app.todolist.model.NotifType

/**
 * NotificationRepository — single source of truth untuk semua data notifikasi.
 *
 * Notifikasi yang memiliki task terkait (DEADLINE, REMINDER, DONE) menyimpan [taskId]
 * yang dapat di-resolve ke [TaskItem] melalui [TaskRepository.getTaskItemById].
 *
 * Digunakan oleh:
 *  - NotificationListActivity
 *  - DetailNotificationActivity
 *
 * Ganti implementasi ini dengan Room / API call saat integrasi backend.
 */
object NotificationRepository {

    private val notifications = mutableListOf(
        // ── Notif 1 — DEADLINE → Task 1 (Desain Prototipe Mobile App) ──────────
        NotificationItem(
            id            = 1,
            title         = "Tugas Segera Berakhir",
            body          = "Selesaikan \"Desain Prototipe Mobile App\" sebelum jam 14:00 hari ini.",
            bodyHighlight = "\"Desain Prototipe Mobile App\"",
            time          = "5 mnt yang lalu",
            type          = NotifType.DEADLINE,
            isRead        = false,
            taskId        = 1,
            taskCategory  = "Design",
            taskDeadline  = "Hari ini, 14:00",
            taskPriority  = "Tinggi"
        ),
        // ── Notif 2 — REMINDER → Task 4 (Daily Standup Meeting) ────────────────
        NotificationItem(
            id            = 2,
            title         = "Pengingat Harian",
            body          = "Jangan lupa \"Daily Standup Meeting\" dimulai jam 09:00 hari ini.",
            bodyHighlight = "\"Daily Standup Meeting\"",
            time          = "2 jam yang lalu",
            type          = NotifType.REMINDER,
            isRead        = false,
            taskId        = 4,
            taskCategory  = "Work",
            taskDeadline  = "Hari ini, 09:00",
            taskPriority  = "Sedang"
        ),
        // ── Notif 3 — DONE → Task 2 (Review Laporan Mingguan) ──────────────────
        NotificationItem(
            id            = 3,
            title         = "Tugas Selesai",
            body          = "\"Review Laporan Mingguan\" telah ditandai sebagai selesai oleh Anda.",
            bodyHighlight = "\"Review Laporan Mingguan\"",
            time          = "Kemarin",
            type          = NotifType.DONE,
            isRead        = true,
            taskId        = 2,
            taskCategory  = "Management",
            taskDeadline  = "Kemarin, 09:00",
            taskPriority  = "Sedang"
        ),
        // ── Notif 4 — DEADLINE → Task 5 (Persiapan Presentasi Klien) ───────────
        NotificationItem(
            id            = 4,
            title         = "Deadline Mendekat",
            body          = "\"Persiapan Presentasi Klien\" jatuh tempo lusa, 13:00.",
            bodyHighlight = "\"Persiapan Presentasi Klien\"",
            time          = "1 jam yang lalu",
            type          = NotifType.DEADLINE,
            isRead        = false,
            taskId        = 5,
            taskCategory  = "Work",
            taskDeadline  = "Lusa, 13:00",
            taskPriority  = "Tinggi"
        ),
        // ── Notif 5 — SYSTEM — tidak punya task terkait ─────────────────────────
        NotificationItem(
            id            = 5,
            title         = "Pembaruan Sistem",
            body          = "Versi 2.1.0 telah tersedia dengan perbaikan bug sinkronisasi cloud.",
            bodyHighlight = "",
            time          = "2 hari yang lalu",
            type          = NotifType.SYSTEM,
            isRead        = true,
            taskId        = null
        )
    )

    // ── Read ──────────────────────────────────────────────────────────────────

    /** Semua notifikasi — dipakai NotificationListActivity. */
    fun getNotifications(): MutableList<NotificationItem> = notifications

    /** Cari satu notifikasi berdasarkan id — dipakai DetailNotificationActivity. */
    fun getNotificationById(id: Int): NotificationItem? = notifications.find { it.id == id }

    /** Jumlah notifikasi yang belum dibaca — dipakai HomeActivity badge. */
    fun getUnreadCount(): Int = notifications.count { !it.isRead }

    // ── Write ─────────────────────────────────────────────────────────────────

    /** Tandai notifikasi sebagai sudah dibaca. */
    fun markAsRead(id: Int) {
        val idx = notifications.indexOfFirst { it.id == id }
        if (idx != -1) {
            notifications[idx] = notifications[idx].copy(isRead = true)
        }
    }

    /** Tandai semua notifikasi sebagai sudah dibaca. */
    fun markAllAsRead() {
        notifications.replaceAll { it.copy(isRead = true) }
    }

    /**
     * Tambah notifikasi baru ke urutan paling atas. Dipanggil dari TaskReminderWorker
     * saat reminder deadline muncul, supaya kelihatan juga di NotificationListActivity
     * (bukan cuma di notification tray sistem).
     */
    fun addNotification(item: NotificationItem) {
        notifications.add(0, item)
    }

    /** Generate id baru yang belum kepakai — dipakai saat bikin NotificationItem baru. */
    fun nextId(): Int = (notifications.maxOfOrNull { it.id } ?: 0) + 1
}