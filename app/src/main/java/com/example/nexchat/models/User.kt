package com.example.nexchat.models

data class User(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImage: String = "",
    val status: String = "Offline",
    val lastSeen: Long = 0,
    val about: String = "Hey there! I'm using NexChat.",
    val createdAt: Long = 0,
    val typingTo: String? = null,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val token: String = "" // For FCM
)
