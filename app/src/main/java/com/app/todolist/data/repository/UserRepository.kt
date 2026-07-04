package com.app.todolist.data.repository

import android.content.Context
import com.app.todolist.data.AppDatabase
import com.app.todolist.model.UserProfile
import com.app.todolist.util.SessionManager

/**
 * UserRepository — single source of truth untuk data user yang sedang login.
 * currentUser di-set oleh LoginActivity setelah proses autentikasi ke Room berhasil,
 * atau dipulihkan otomatis lewat restoreSessionIfNeeded() (auto-login).
 */
object UserRepository {

    private var currentUser: UserProfile? = null

    fun setCurrentUser(user: UserProfile) {
        currentUser = user
    }

    fun clearCurrentUser() {
        currentUser = null
    }

    fun getCurrentUser(): UserProfile? = currentUser

    fun getUserName(): String    = currentUser?.name.orEmpty()
    fun getUserEmail(): String   = currentUser?.email.orEmpty()
    fun getUserUsername(): String = currentUser?.username.orEmpty()
    fun getUserInitials(): String = currentUser?.avatarInitials.orEmpty()

    /**
     * Coba pulihkan sesi login. Dipanggil di awal onCreate Activity yang butuh
     * user login (Home, Profile, dst) SEBELUM memutuskan redirect ke LoginActivity.
     *
     * - Kalau currentUser sudah ada di memory → langsung true, tidak query apa-apa.
     * - Kalau tidak ada, cek SharedPreferences (SessionManager) untuk userId tersimpan,
     *   lalu ambil data lengkapnya dari Room.
     *
     * @return true kalau ada sesi valid (baik dari memory maupun hasil restore),
     *         false kalau memang tidak ada user yang login.
     */
    suspend fun restoreSessionIfNeeded(context: Context): Boolean {
        if (currentUser != null) return true

        val savedUserId = SessionManager.getUserId(context) ?: return false

        val userDao = AppDatabase.getDatabase(context).userDao()
        val user = userDao.getUserById(savedUserId) ?: return false

        currentUser = UserProfile(
            id = user.id,
            name = user.username,
            username = user.username,
            email = user.email
        )
        return true
    }
}