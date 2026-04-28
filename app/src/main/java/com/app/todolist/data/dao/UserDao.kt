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
}