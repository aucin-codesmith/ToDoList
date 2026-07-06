package com.app.todolist.util

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import android.content.Context
import androidx.work.WorkerParameters
import com.app.todolist.R
import com.app.todolist.data.repository.NotificationRepository
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.model.NotificationItem
import com.app.todolist.model.NotifType
import com.app.todolist.ui.task.DetailTaskActivity
import com.app.todolist.util.SessionManager

/**
 * Dijalankan sekali oleh WorkManager persis di waktu reminder yang dihitung
 * ReminderScheduler. Selalu baca ulang data task dari Room dulu (bukan cuma
 * pakai data yang dikirim saat dijadwalkan) — soalnya task itu mungkin sudah
 * diedit/dihapus/ditandai selesai sejak reminder ini dijadwalkan.
 */
class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_TASK_ID = "task_id"
    }

    override suspend fun doWork(): Result {
        val taskId = inputData.getInt(KEY_TASK_ID, -1)
        if (taskId == -1) return Result.failure()

        val task = TaskRepository.getTaskItemById(applicationContext, taskId)
        // Task sudah dihapus, atau sudah ditandai selesai duluan — tidak perlu diingatkan lagi
        if (task == null || task.isCompleted) return Result.success()

        // --- TAMBAHAN UNTUK MULTI-USER ---
        // Cek siapa yang sedang login saat ini
        val currentUserId = SessionManager.getUserId(applicationContext)?.toString() ?: "0"

        // Jika yang sedang login bukan pemilik tugas ini, batalkan notifikasinya!
        if (task.userId != currentUserId) {
            return Result.success()
        }
        // ---------------------------------

        NotificationHelper.ensureChannel(applicationContext)
        showNotification(task.id, task.title, task.dateTime)

        // Catatan: Jika temanmu menambahkan kolom userId juga di NotificationItem (Soal 6),
        // pastikan kamu juga menyelipkan userId ke dalam fungsi saveToNotificationList ini nanti.
        saveToNotificationList(task.id, task.title, task.category, task.dateTime, task.priority)

        return Result.success()
    }

    /** Simpan juga ke NotificationRepository supaya kelihatan di NotificationListActivity. */
    private suspend fun saveToNotificationList(
        taskId: Int,
        taskTitle: String,
        taskCategory: String,
        taskDeadline: String,
        taskPriority: String
    ) {
        val notifItem = NotificationItem(
            id = 0, // diabaikan, Room yang generate id
            title = "Pengingat Deadline",
            body = "Jangan lupa \"$taskTitle\" deadline $taskDeadline.",
            bodyHighlight = "\"$taskTitle\"",
            time = "Baru saja",
            type = NotifType.REMINDER,
            isRead = false,
            taskId = taskId,
            taskCategory = taskCategory,
            taskDeadline = taskDeadline,
            taskPriority = taskPriority
        )
        NotificationRepository.addNotification(applicationContext, notifItem)
    }

    private fun showNotification(taskId: Int, title: String, deadlineText: String) {
        val detailIntent = Intent(applicationContext, DetailTaskActivity::class.java).apply {
            putExtra(DetailTaskActivity.EXTRA_TASK_ID, taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            taskId,
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Deadline mendekat: $title")
            .setContentText("Deadline: $deadlineText")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // checkSelfPermission aman dipanggil di semua level API — di bawah Android 13
        // izin ini otomatis dianggap granted, jadi tidak perlu cek Build.VERSION terpisah.
        val permissionGranted = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            return
        }

        try {
            NotificationManagerCompat.from(applicationContext).notify(taskId, notification)
        } catch (e: SecurityException) {
            // Izin dicabut tepat setelah pengecekan di atas — abaikan dengan aman.
        }
    }
}