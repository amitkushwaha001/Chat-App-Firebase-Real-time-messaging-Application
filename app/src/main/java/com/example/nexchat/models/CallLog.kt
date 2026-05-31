package com.example.nexchat.models

data class CallLog(
    val callId: String = "",
    val callerUid: String = "",
    val receiverUid: String = "",
    val callerName: String = "",
    val receiverName: String = "",
    val callerImage: String = "",
    val receiverImage: String = "",
    val timestamp: Long = 0,
    val type: String = "audio", // audio or video
    val isMissed: Boolean = false,
    val duration: String = "",
    val participants: List<String> = emptyList()
)
