package com.app.todolist.util

import android.app.AlertDialog
import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch

/**
 * Dialog sederhana buat pilih "berapa lama sebelum deadline reminder muncul".
 * Dipicu dari btnNotifications di ProfileFragment.
 */
object NotificationSettingsDialog {

    // Pair<menit, label>
    private val options = listOf(
        15 to "15 menit sebelum deadline",
        30 to "30 menit sebelum deadline",
        60 to "1 jam sebelum deadline",
        120 to "2 jam sebelum deadline",
        1440 to "1 hari sebelum deadline"
    )

    fun show(context: Context, lifecycleScope: LifecycleCoroutineScope) {
        val currentOffset = ReminderPreference.getOffsetMinutes(context)
        val labels = options.map { it.second }.toTypedArray()
        val checkedIndex = options.indexOfFirst { it.first == currentOffset }
            .let { if (it == -1) options.indexOfFirst { pair -> pair.first == ReminderPreference.DEFAULT_OFFSET_MINUTES } else it }

        AlertDialog.Builder(context)
            .setTitle("Pengingat Deadline")
            .setSingleChoiceItems(labels, checkedIndex) { dialog, index ->
                val selectedMinutes = options[index].first
                ReminderPreference.setOffsetMinutes(context, selectedMinutes)

                // Reschedule semua reminder task aktif pakai offset baru
                lifecycleScope.launch {
                    ReminderScheduler.rescheduleAllReminders(context)
                }

                dialog.dismiss()
            }
            .setNegativeButton("Tutup", null)
            .show()
    }
}