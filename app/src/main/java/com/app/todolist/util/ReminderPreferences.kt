package com.app.todolist.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Menyimpan preferensi "berapa menit sebelum deadline reminder muncul".
 * Diatur user lewat dialog di Profile > Notifikasi.
 */
object ReminderPreference {

    private const val PREF_NAME = "todolist_reminder_prefs"
    private const val KEY_OFFSET_MINUTES = "reminder_offset_minutes"
    const val DEFAULT_OFFSET_MINUTES = 60

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getOffsetMinutes(context: Context): Int =
        prefs(context).getInt(KEY_OFFSET_MINUTES, DEFAULT_OFFSET_MINUTES)

    fun setOffsetMinutes(context: Context, minutes: Int) {
        prefs(context).edit().putInt(KEY_OFFSET_MINUTES, minutes).apply()
    }
}