package com.app.todolist.data.repository

import com.app.todolist.model.UserProfile

/**
 * UserRepository — single source of truth untuk data user yang sedang login.
 * currentUser di-set oleh LoginActivity setelah proses autentikasi ke Room berhasil.
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
}