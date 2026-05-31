package com.example.nexchat.utils

object Constants {
    const val AGORA_APP_ID = "6f0b4d4b18c54c3080689369f9d774e5"
    
    // Cloudinary Config
    const val CLOUDINARY_CLOUD_NAME = "dpdr51wnk"
    const val CLOUDINARY_API_KEY = "269335299185984"
    const val CLOUDINARY_API_SECRET = "og4cGv9Aww2kfuj-_qiA4Q7b7pk"

    fun formatLastSeen(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat(
            "hh:mm a",
            java.util.Locale.getDefault()
        )
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60000 -> "online"
            else -> "last seen today at ${sdf.format(java.util.Date(timestamp))}"
        }
    }
}
