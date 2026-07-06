package com.app.todolist.data.repository

import android.content.Context
import com.app.todolist.data.AppDatabase
import com.app.todolist.model.UserProfile
import com.app.todolist.util.PasswordHasher
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

    /**
     * Ubah username. Mengecek dulu apakah username baru sudah dipakai user lain.
     * @return null kalau berhasil, atau pesan error kalau gagal.
     */
    suspend fun updateUsername(context: Context, newUsername: String): String? {
        val user = currentUser ?: return "Sesi tidak ditemukan, silakan login ulang"
        val trimmed = newUsername.trim()

        if (trimmed.isEmpty()) return "Username tidak boleh kosong"
        if (trimmed == user.username) return null

        val userDao = AppDatabase.getDatabase(context).userDao()
        val existing = userDao.getUserByUsername(trimmed)
        if (existing != null && existing.id != user.id) return "Username sudah dipakai"

        userDao.updateUsername(user.id, trimmed)
        currentUser = user.copy(
            username = trimmed,
            name = trimmed,
            avatarInitials = trimmed.take(2).uppercase()
        )
        return null
    }

    /**
     * Ubah password. Mengecek dulu apakah password lama yang dimasukkan cocok
     * dengan yang tersimpan di Room.
     * @return null kalau berhasil, atau pesan error kalau gagal.
     */
    suspend fun updatePassword(context: Context, oldPassword: String, newPassword: String): String? {
        val user = currentUser ?: return "Sesi tidak ditemukan, silakan login ulang"

        if (oldPassword.isEmpty()) return "Masukkan password lama"
        if (newPassword.length < 6) return "Password baru minimal 6 karakter"

        val userDao = AppDatabase.getDatabase(context).userDao()
        val dbUser = userDao.getUserById(user.id) ?: return "User tidak ditemukan"
        if (!PasswordHasher.verify(oldPassword, dbUser.password)) return "Password lama salah"

        userDao.updatePassword(user.id, PasswordHasher.hash(newPassword))
        return null
    }
}