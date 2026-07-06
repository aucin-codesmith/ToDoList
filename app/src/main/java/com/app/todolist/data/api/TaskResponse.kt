package com.app.todolist.data.api

import com.google.gson.annotations.SerializedName

data class TaskResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("priority")
    val priority: String,
    @SerializedName("dateTime")
    val dateTime: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("isCompleted")
    val isCompleted: Boolean
)