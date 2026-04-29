package com.app.todolist.model

data class UserProfile(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val role: String = "Member",
    val avatarInitials: String = name.take(2).uppercase()
)