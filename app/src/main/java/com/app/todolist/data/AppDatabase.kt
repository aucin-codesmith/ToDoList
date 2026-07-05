package com.app.todolist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.todolist.data.entity.User
import com.app.todolist.data.entity.TaskEntity
import com.app.todolist.data.dao.UserDao
import com.app.todolist.data.dao.TaskDao

@Database(entities = [User::class, TaskEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todo_database"
                )
                    // Skala project ini masih dev/belajar — daripada nulis Migration
                    // manual tiap ubah skema, kita destroy & rebuild aja saat versi naik.
                    // Efeknya: data lama (termasuk user & task) akan hilang saat update ini
                    // pertama kali jalan. Kalau app sudah production, ganti ini dengan
                    // Migration object yang proper agar data user tidak hilang.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}