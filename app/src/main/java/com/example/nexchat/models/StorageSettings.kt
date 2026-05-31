package com.example.nexchat.models

data class StorageSettings(
    val autoDownloadMobile: List<String> = listOf("Photos"),
    val autoDownloadWifi: List<String> = listOf("Photos", "Videos", "Audio", "Documents"),
    val autoDownloadRoaming: List<String> = emptyList(),
    val dataSaver: Boolean = false
)
