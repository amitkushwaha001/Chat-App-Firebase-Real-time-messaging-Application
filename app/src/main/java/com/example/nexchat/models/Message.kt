package com.example.nexchat.models

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val timestamp: Long = 0,
    val seen: Boolean = false,
    val type: String = "text",
    val reactions: Map<String, String> = emptyMap(),
    val hiddenBy: List<String> = emptyList(),
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val replyToId: String? = null
)
