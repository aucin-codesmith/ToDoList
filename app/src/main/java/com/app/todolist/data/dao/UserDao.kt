package com.app.todolist.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.todolist.data.entity.User

@Dao
interface UserDao {
    // Untuk proses Register
    @Insert
    suspend fun insertUser(user: User)

    // Untuk proses Login (mencari user berdasarkan email)
    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    // Untuk cek apakah username sudah dipakai user lain saat Register
    @Query("SELECT * FROM user_table WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    // Untuk restore session (auto-login) berdasarkan userId yang tersimpan di SharedPreferences
    @Query("SELECT * FROM user_table WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    // Untuk fitur Ubah Username
    @Query("UPDATE user_table SET username = :username WHERE id = :id")
    suspend fun updateUsername(id: Int, username: String)

    // Untuk fitur Ubah Password
    @Query("UPDATE user_table SET password = :password WHERE id = :id")
    suspend fun updatePassword(id: Int, password: String)
}