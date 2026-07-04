package com.app.todolist.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Menyimpan id user yang sedang login ke SharedPreferences,
 * supaya sesi tetap "diingat" walau app di-kill dan dibuka lagi (auto-login).
 *
 * Yang disimpan HANYA userId — bukan password, bukan data sensitif lain.
 * Data lengkap user tetap diambil ulang dari Room via userId ini.
 */
object SessionManager {

    private const val PREF_NAME = "todolist_session"
    private const val KEY_USER_ID = "user_id"
    private const val NO_USER = -1

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveUserId(context: Context, userId: Int) {
        prefs(context).edit().putInt(KEY_USER_ID, userId).apply()
    }

    fun getUserId(context: Context): Int? {
        val id = prefs(context).getInt(KEY_USER_ID, NO_USER)
        return if (id == NO_USER) null else id
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_USER_ID).apply()
    }
}