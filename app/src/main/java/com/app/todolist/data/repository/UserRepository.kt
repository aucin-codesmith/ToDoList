package com.app.todolist.data.repository

import com.app.todolist.model.UserProfile

/**
 * UserRepository — single source of truth untuk data user.
 * Ganti implementasi ini dengan Room/API call saat integrasi backend.
 */
object UserRepository {

    private val currentUser = UserProfile(
        id       = 1,
        name     = "Budi Santoso",
        username = "budisantoso",
        email    = "budi.santoso@email.com",
        role     = "Member"
    )

    fun getCurrentUser(): UserProfile = currentUser

    fun getUserName(): String    = currentUser.name
    fun getUserEmail(): String   = currentUser.email
    fun getUserUsername(): String = currentUser.username
    fun getUserInitials(): String = currentUser.avatarInitials
}