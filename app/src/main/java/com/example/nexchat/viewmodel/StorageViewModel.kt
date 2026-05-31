package com.example.nexchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexchat.models.StorageSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _settings = MutableStateFlow(StorageSettings())
    val settings: StateFlow<StorageSettings> = _settings

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val uid = auth.uid ?: return
        db.collection("storageSettings").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.toObject(StorageSettings::class.java)?.let {
                _settings.value = it
            }
        }
    }

    fun toggleDataSaver(value: Boolean) {
        updateSetting("dataSaver", value)
    }

    private fun updateSetting(key: String, value: Any) {
        val uid = auth.uid ?: return
        viewModelScope.launch {
            db.collection("storageSettings").document(uid).update(key, value)
                .addOnFailureListener {
                    db.collection("storageSettings").document(uid).set(mapOf(key to value))
                }
        }
    }
}
