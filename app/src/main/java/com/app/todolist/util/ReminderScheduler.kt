package com.app.todolist.util

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.app.todolist.data.repository.TaskRepository
import java.util.concurrent.TimeUnit
import com.app.todolist.util.SessionManager

/**
 * "Cron job"-nya task reminder — tapi berbasis WorkManager, bukan alarm exact.
 * Boleh telat beberapa menit (sesuai kebutuhan app ini), tapi otomatis bertahan
 * lewat restart HP tanpa perlu BroadcastReceiver khusus BOOT_COMPLETED.
 */
object ReminderScheduler {

    private fun workName(taskId: Int) = "reminder_task_$taskId"

    /**
     * Jadwalkan reminder buat satu task. Kalau task ini sudah punya reminder
     * terjadwal sebelumnya, otomatis diganti (ExistingWorkPolicy.REPLACE) —
     * jadi aman dipanggil ulang tiap kali task dibuat/diedit.
     */
    fun scheduleReminder(context: Context, taskId: Int, deadlineMillis: Long) {
        if (deadlineMillis <= 0L) {
            cancelReminder(context, taskId)
            return
        }

        val offsetMinutes = ReminderPreference.getOffsetMinutes(context)
        val triggerAt = deadlineMillis - (offsetMinutes * 60_000L)
        val delay = triggerAt - System.currentTimeMillis()

        if (delay <= 0L) {
            // Waktu reminder-nya sudah lewat (deadline terlalu dekat/sudah lewat) — jangan jadwalkan
            cancelReminder(context, taskId)
            return
        }

        val data = workDataOf(TaskReminderWorker.KEY_TASK_ID to taskId)

        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(taskId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelReminder(context: Context, taskId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(taskId))
    }

    /**
     * Dipanggil setelah user ganti offset reminder di Profile > Notifikasi.
     * Semua task aktif (belum selesai, deadline masih di masa depan) dijadwalkan
     * ulang pakai offset baru; task yang sudah selesai/lewat deadline dibatalkan.
     */
    suspend fun rescheduleAllReminders(context: Context) {
        // --- AMBIL ID USER DAN MASUKKAN KE FUNGSI GET ---
        val currentUserId = SessionManager.getUserId(context)?.toString() ?: "0"
        val tasks = TaskRepository.getTaskItems(context, currentUserId)

        tasks.forEach { task ->
            if (!task.isCompleted && task.deadlineMillis > System.currentTimeMillis()) {
                scheduleReminder(context, task.id, task.deadlineMillis)
            } else {
                cancelReminder(context, task.id)
            }
        }
    }
}