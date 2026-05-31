package com.example.nexchat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.nexchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _incomingCall = MutableStateFlow<String?>(null)
    val incomingCall: StateFlow<String?> = _incomingCall

    private var userListener: ListenerRegistration? = null

    init {
        fetchUsers()
    }

    fun fetchUsers() {
        val currentUid = auth.uid ?: return
        
        userListener?.remove()
        userListener = db.collection("users").document(currentUid).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val user = snapshot?.toObject(User::class.java)
            _currentUser.value = user
            
            val callId = snapshot?.getString("currentCallId")
            if (callId != null) {
                _incomingCall.value = callId
            }
        }

        db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            
            val userList = mutableListOf<User>()
            snapshot?.documents?.forEach { doc ->
                val user = doc.toObject(User::class.java)
                if (user != null && user.uid != currentUid) {
                    userList.add(user)
                }
            }
            _users.value = userList
        }
    }

    fun clearCallSignal() {
        val uid = auth.uid ?: return
        db.collection("users").document(uid).update("currentCallId", null)
        _incomingCall.value = null
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
    }
}
