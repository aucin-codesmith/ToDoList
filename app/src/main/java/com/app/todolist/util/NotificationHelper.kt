package com.app.todolist.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Setup NotificationChannel — wajib ada sebelum bisa nampilin notifikasi
 * apa pun di Android 8 (Oreo) ke atas.
 */
object NotificationHelper {

    const val CHANNEL_ID = "task_reminder_channel"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pengingat Tugas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat deadline tugas yang akan datang"
            }
            manager.createNotificationChannel(channel)
        }
    }
}