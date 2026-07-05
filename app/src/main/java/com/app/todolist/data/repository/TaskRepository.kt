package com.app.todolist.data.repository

import android.content.Context
import com.app.todolist.data.AppDatabase
import com.app.todolist.data.entity.TaskEntity
import com.app.todolist.model.TaskItem

/**
 * TaskRepository — single source of truth untuk semua data tugas.
 * Sudah persisted ke SQLite via Room (bukan in-memory lagi).
 *
 * Semua fungsi jadi `suspend` karena akses database. Panggil dari
 * lifecycleScope.launch { } di Activity, JANGAN dari Main thread langsung.
 */
object TaskRepository {

    private fun dao(context: Context) = AppDatabase.getDatabase(context).taskDao()

    // ── Mapper: TaskEntity (Room) <-> TaskItem (UI model) ──────────────────────

    private fun TaskEntity.toTaskItem() = TaskItem(
        id = id,
        title = title,
        category = category,
        description = description,
        dateTime = dateTime,
        date = date,
        priority = priority,
        assigneeTag = assigneeTag,
        isCompleted = isCompleted,
        deadlineMillis = deadlineMillis
    )

    private fun TaskItem.toEntity() = TaskEntity(
        id = id,
        title = title,
        category = category,
        description = description,
        dateTime = dateTime,
        date = date,
        priority = priority,
        assigneeTag = assigneeTag,
        isCompleted = isCompleted,
        deadlineMillis = deadlineMillis
    )

    // ── Read ──────────────────────────────────────────────────────────────────

    /** Seluruh task — dipakai TaskListActivity. */
    suspend fun getTaskItems(context: Context): List<TaskItem> =
        dao(context).getAllTasks().map { it.toTaskItem() }

    /**
     * Task terbaru untuk HomeActivity (maks [limit] item, belum selesai diutamakan).
     * Default limit = 3 agar home card tidak terlalu panjang.
     */
    suspend fun getRecentTasks(context: Context, limit: Int = 3): List<TaskItem> =
        getTaskItems(context).sortedBy { it.isCompleted }.take(limit)

    /** Cari satu task berdasarkan id — dipakai DetailTaskActivity & EditTaskActivity. */
    suspend fun getTaskItemById(context: Context, id: Int): TaskItem? =
        dao(context).getTaskById(id)?.toTaskItem()

    // ── Write ─────────────────────────────────────────────────────────────────

    /** Toggle status selesai — dipanggil dari checkbox maupun tombol di DetailTaskActivity. */
    suspend fun updateTaskItemCompleted(context: Context, id: Int, isCompleted: Boolean) {
        dao(context).updateCompleted(id, isCompleted)
    }

    /** Simpan perubahan task dari EditTaskActivity. */
    suspend fun updateTaskItem(context: Context, updated: TaskItem) {
        dao(context).updateTask(updated.toEntity())
    }

    /**
     * Tambah task baru dari AddTaskActivity.
     * id di-set 0 supaya Room auto-generate id baru (abaikan id yang dikirim).
     * @return id baru yang di-generate Room — dipakai buat jadwalkan reminder.
     */
    suspend fun addTaskItem(context: Context, task: TaskItem): Int {
        val newId = dao(context).insertTask(task.toEntity().copy(id = 0))
        return newId.toInt()
    }

    /** Hapus task — dipanggil dari DetailTaskActivity. */
    suspend fun deleteTaskItem(context: Context, id: Int) {
        dao(context).deleteTaskById(id)
    }

    // ── Summary helpers (untuk HomeActivity & ProfileActivity) ─────────────────

    suspend fun getTotalCount(context: Context): Int =
        getTaskItems(context).size

    suspend fun getCompletedCount(context: Context): Int =
        getTaskItems(context).count { it.isCompleted }

    suspend fun getRemainingCount(context: Context): Int =
        getTaskItems(context).count { !it.isCompleted }
}