package com.example.nexchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexchat.models.PrivacySettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _settings = MutableStateFlow(PrivacySettings())
    val settings: StateFlow<PrivacySettings> = _settings

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val uid = auth.uid ?: return
        db.collection("privacySettings").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.toObject(PrivacySettings::class.java)?.let {
                _settings.value = it
            }
        }
    }

    fun updateLastSeen(value: String) {
        updateSetting("lastSeen", value)
    }

    fun updateProfilePhoto(value: String) {
        updateSetting("profilePhoto", value)
    }

    fun updateAbout(value: String) {
        updateSetting("about", value)
    }

    fun toggleReadReceipts(value: Boolean) {
        updateSetting("readReceipts", value)
    }

    private fun updateSetting(key: String, value: Any) {
        val uid = auth.uid ?: return
        viewModelScope.launch {
            db.collection("privacySettings").document(uid).update(key, value)
                .addOnFailureListener {
                    // If document doesn't exist, create it
                    db.collection("privacySettings").document(uid).set(mapOf(key to value))
                }
        }
    }
}
