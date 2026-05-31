package com.example.nexchat.models

data class Status(
    val statusId: String = "",
    val uid: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0,
    val caption: String = ""
)

data class UserStatus(
    val uid: String = "",
    val name: String = "",
    val profileImage: String = "",
    val lastUpdated: Long = 0,
    val statuses: List<Status> = emptyList()
)
