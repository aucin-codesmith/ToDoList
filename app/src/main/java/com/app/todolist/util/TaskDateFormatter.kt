package com.app.todolist.util

import java.util.Calendar

/**
 * Helper format tanggal/waktu untuk TaskItem.
 * Dipakai AddTaskActivity & EditTaskActivity supaya format "date" dan "dateTime"
 * konsisten satu sama lain (dan konsisten dengan yang ditampilkan TaskAdapter/TaskListAdapter).
 */
object TaskDateFormatter {

    private val monthAbbrev = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Ags", "Sep", "Okt", "Nov", "Des"
    )

    /** Format tampilan di kolom input, contoh: "29 Apr 2026, 14:00" */
    fun formatDeadlineDisplay(cal: Calendar): String {
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = monthAbbrev[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR)
        val time = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        return "%02d %s %d, %s".format(day, month, year, time)
    }

    /** Field "date" pendek untuk HomeActivity, contoh: "29 Apr, 2026" */
    fun formatDateShort(cal: Calendar): String {
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = monthAbbrev[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR)
        return "%02d %s, %d".format(day, month, year)
    }

    /** Field "dateTime" relatif untuk TaskListActivity, contoh: "Hari ini, 14:00" */
    fun formatDateTimeRelative(cal: Calendar): String {
        val today = Calendar.getInstance()
        val diffDays = daysBetween(today, cal)
        val time = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

        val label = when (diffDays) {
            0 -> "Hari ini"
            1 -> "Besok"
            2 -> "Lusa"
            else -> {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val month = monthAbbrev[cal.get(Calendar.MONTH)]
                "%02d %s".format(day, month)
            }
        }
        return "$label, $time"
    }

    private fun daysBetween(from: Calendar, to: Calendar): Int {
        val start = Calendar.getInstance().apply {
            set(from.get(Calendar.YEAR), from.get(Calendar.MONTH), from.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        }
        val end = Calendar.getInstance().apply {
            set(to.get(Calendar.YEAR), to.get(Calendar.MONTH), to.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        }
        val diffMillis = end.timeInMillis - start.timeInMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}