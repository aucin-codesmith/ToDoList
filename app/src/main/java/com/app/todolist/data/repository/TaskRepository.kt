package com.app.todolist.data.repository

import com.app.todolist.model.TaskItem

/**
 * TaskRepository — single source of truth untuk semua data tugas.
 *
 * Satu list [taskItems] dipakai oleh SEMUA layer:
 *  - HomeActivity       (via getTasks / getRecentTasks)
 *  - TaskListActivity   (via getTaskItems)
 *  - DetailTaskActivity (via getTaskItemById)
 *  - EditTaskActivity   (via getTaskItemById)
 *  - NotificationRepository (via getTaskItemById untuk resolve task terkait)
 *
 * Ganti implementasi ini dengan Room / API call saat integrasi backend.
 */
object TaskRepository {

    // ── 5 dummy tasks — sumber data tunggal ───────────────────────────────────

    private val taskItems = mutableListOf(
        TaskItem(
            id          = 1,
            title       = "Desain Prototipe Mobile App",
            category    = "Design",
            description = "Mengembangkan prototipe high-fidelity menggunakan Figma yang mencakup " +
                    "alur registrasi pengguna, dasbor utama, dan visualisasi pengeluaran bulanan.",
            dateTime    = "Hari ini, 14:00",
            date        = "29 Apr, 2026",
            priority    = "Tinggi",
            assigneeTag = "KMP",
            isCompleted = false
        ),
        TaskItem(
            id          = 2,
            title       = "Review Laporan Mingguan",
            category    = "Management",
            description = "Melakukan audit terhadap laporan progres mingguan untuk memastikan " +
                    "pencapaian KPI dan mengidentifikasi hambatan teknis (blockers) pada sisi backend.",
            dateTime    = "Besok, 09:00",
            date        = "30 Apr, 2026",
            priority    = "Sedang",
            assigneeTag = null,
            isCompleted = true
        ),
        TaskItem(
            id          = 3,
            title       = "Update Dokumentasi API",
            category    = "Development",
            description = "Memperbarui referensi endpoint pada Swagger/Postman untuk mencerminkan " +
                    "perubahan skema database dan penambahan fitur autentikasi OAuth2.",
            dateTime    = "Hari ini, 16:00",
            date        = "29 Apr, 2026",
            priority    = "Rendah",
            assigneeTag = "DEV",
            isCompleted = false
        ),
        TaskItem(
            id          = 4,
            title       = "Daily Standup Meeting",
            category    = "Work",
            description = "Sesi standup harian tim engineering untuk membahas progres sprint, " +
                    "blocker, dan rencana kerja hari ini.",
            dateTime    = "Hari ini, 09:00",
            date        = "29 Apr, 2026",
            priority    = "Sedang",
            assigneeTag = null,
            isCompleted = false
        ),
        TaskItem(
            id          = 5,
            title       = "Persiapan Presentasi Klien",
            category    = "Work",
            description = "Menyusun slide deck dan demo interaktif untuk presentasi proposal " +
                    "redesign produk kepada klien pada akhir pekan ini.",
            dateTime    = "Lusa, 13:00",
            date        = "01 Mei, 2026",
            priority    = "Tinggi",
            assigneeTag = "KMP",
            isCompleted = false
        )
    )

    // ── Read ──────────────────────────────────────────────────────────────────

    /** Seluruh task — dipakai TaskListActivity. */
    fun getTaskItems(): MutableList<TaskItem> = taskItems

    /**
     * Task terbaru untuk HomeActivity (maks [limit] item, belum selesai diutamakan).
     * Default limit = 3 agar home card tidak terlalu panjang.
     */
    fun getRecentTasks(limit: Int = 3): List<TaskItem> =
        taskItems.sortedBy { it.isCompleted }.take(limit)

    /** Cari satu task berdasarkan id — dipakai DetailTaskActivity & EditTaskActivity. */
    fun getTaskItemById(id: Int): TaskItem? = taskItems.find { it.id == id }

    // ── Write ─────────────────────────────────────────────────────────────────

    /** Toggle status selesai — dipanggil dari checkbox maupun tombol di DetailTaskActivity. */
    fun updateTaskItemCompleted(id: Int, isCompleted: Boolean) {
        taskItems.indexOfFirst { it.id == id }.takeIf { it != -1 }?.let { idx ->
            taskItems[idx] = taskItems[idx].copy(isCompleted = isCompleted)
        }
    }

    /**
     * Simpan perubahan task dari EditTaskActivity.
     * Ganti implementasi ini dengan Room update saat integrasi backend.
     */
    fun updateTaskItem(updated: TaskItem) {
        taskItems.indexOfFirst { it.id == updated.id }.takeIf { it != -1 }?.let { idx ->
            taskItems[idx] = updated
        }
    }

    /**
     * Tambah task baru dari AddTaskActivity.
     * ID di-generate otomatis (max id + 1).
     */
    fun addTaskItem(task: TaskItem) {
        val newId   = (taskItems.maxOfOrNull { it.id } ?: 0) + 1
        taskItems.add(task.copy(id = newId))
    }

    /** Hapus task — dipanggil dari DetailTaskActivity. */
    fun deleteTaskItem(id: Int) {
        taskItems.removeAll { it.id == id }
    }

    // ── Summary helpers (untuk HomeActivity summary card) ─────────────────────

    fun getTotalCount(): Int     = taskItems.size
    fun getCompletedCount(): Int = taskItems.count { it.isCompleted }
    fun getRemainingCount(): Int = taskItems.count { !it.isCompleted }
}