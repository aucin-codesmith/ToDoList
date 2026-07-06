package com.app.todolist.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.todolist.data.entity.NotificationEntity

@Dao
interface NotificationDao {

    @Insert
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("SELECT * FROM notification_table ORDER BY createdAtMillis DESC, id DESC")
    suspend fun getAllNotifications(): List<NotificationEntity>

    @Query("SELECT * FROM notification_table WHERE id = :id LIMIT 1")
    suspend fun getNotificationById(id: Int): NotificationEntity?

    @Query("SELECT COUNT(*) FROM notification_table WHERE isRead = 0")
    suspend fun getUnreadCount(): Int

    @Query("UPDATE notification_table SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE notification_table SET isRead = 1")
    suspend fun markAllAsRead()
}