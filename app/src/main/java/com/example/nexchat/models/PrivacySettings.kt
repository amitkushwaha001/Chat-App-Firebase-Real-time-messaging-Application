package com.example.nexchat.models

data class PrivacySettings(
    val lastSeen: String = "Everyone", // Everyone, My Contacts, Nobody
    val profilePhoto: String = "Everyone",
    val about: String = "Everyone",
    val readReceipts: Boolean = true,
    val twoStepVerification: Boolean = false,
    val passcodeLock: Boolean = false
)
