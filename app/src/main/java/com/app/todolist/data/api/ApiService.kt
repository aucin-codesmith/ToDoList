package com.app.todolist.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("Tasks")
    suspend fun getAllTasks(
        @Query("userId") userId: String
    ): Response<List<TaskResponse>>

    @POST("Tasks")
    suspend fun addTask(@Body task: TaskResponse): Response<TaskResponse>

    @DELETE("Tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<TaskResponse>

    @PUT("Tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Body task: TaskResponse
    ): Response<TaskResponse>
}