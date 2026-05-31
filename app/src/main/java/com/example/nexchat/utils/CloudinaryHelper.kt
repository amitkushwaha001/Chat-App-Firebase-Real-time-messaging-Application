package com.example.nexchat.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryHelper {

    fun uploadFile(context: Context, uri: Uri, folder: String, callback: (String?) -> Unit) {
        // Detect resource type based on Uri or folder
        val resourceType = when {
            folder.contains("video", ignoreCase = true) -> "video"
            folder.contains("image", ignoreCase = true) -> "image"
            else -> "auto" // For documents/raw files
        }

        MediaManager.get().upload(uri)
            .option("folder", folder)
            .option("resource_type", resourceType)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    android.util.Log.d("Cloudinary", "Upload started: $requestId")
                }
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url") as? String
                    android.util.Log.d("Cloudinary", "Upload success: $url")
                    callback(url)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    android.util.Log.e("Cloudinary", "Upload error: ${error?.description}")
                    callback(null)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }
}
