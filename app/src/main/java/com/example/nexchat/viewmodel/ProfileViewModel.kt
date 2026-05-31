package com.example.nexchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _mutedUsers = MutableStateFlow<Map<String, Long>>(emptyMap())
    val mutedUsers: StateFlow<Map<String, Long>> = _mutedUsers

    init {
        loadMuteSettings()
    }

    private fun loadMuteSettings() {
        val uid = auth.uid ?: return
        db.collection("muteSettings").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val rawData = snapshot?.get("mutedUsers") as? Map<*, *>
            val mappedData = rawData?.mapNotNull { (key, value) ->
                val stringKey = key as? String ?: return@mapNotNull null
                val longValue = (value as? Number)?.toLong() ?: return@mapNotNull null
                stringKey to longValue
            }?.toMap() ?: emptyMap()
            _mutedUsers.value = mappedData
        }
    }

    fun muteUser(targetUid: String, duration: String) {
        val uid = auth.uid ?: return
        val muteUntil = when(duration) {
            "8 Hours" -> System.currentTimeMillis() + 8 * 60 * 60 * 1000
            "1 Week" -> System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
            else -> Long.MAX_VALUE
        }
        
        viewModelScope.launch {
            db.collection("muteSettings").document(uid).update("mutedUsers.$targetUid", muteUntil)
                .addOnFailureListener {
                    db.collection("muteSettings").document(uid).set(mapOf("mutedUsers" to mapOf(targetUid to muteUntil)))
                }
        }
    }

    fun unmuteUser(targetUid: String) {
        val uid = auth.uid ?: return
        viewModelScope.launch {
            db.collection("muteSettings").document(uid).update("mutedUsers.$targetUid", FieldValue.delete())
        }
    }

    fun blockUser(targetUid: String) {
        val uid = auth.uid ?: return
        viewModelScope.launch {
            db.collection("users").document(uid).update("blockedUsers", FieldValue.arrayUnion(targetUid))
            db.collection("users").document(targetUid).update("blockedBy", FieldValue.arrayUnion(uid))
        }
    }

    fun reportUser(targetUid: String, reason: String) {
        val report = mapOf(
            "reportedBy" to auth.uid,
            "targetUid" to targetUid,
            "reason" to reason,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("reports").add(report)
    }
}
