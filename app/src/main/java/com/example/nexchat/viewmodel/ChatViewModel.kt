package com.example.nexchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexchat.models.Message
import com.example.nexchat.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun loadMessages(userId: String, otherId: String) {
        viewModelScope.launch {
            repository.getMessages(userId, otherId).collectLatest {
                _messages.value = it
            }
        }
    }

    fun sendMessage(senderId: String, receiverId: String, text: String, type: String = "text", fileUrl: String = "", fileName: String = "") {
        val message = Message(
            messageId = UUID.randomUUID().toString(),
            senderId = senderId,
            receiverId = receiverId,
            message = text,
            timestamp = Date().time,
            type = type,
            fileUrl = fileUrl,
            fileName = fileName
        )
        viewModelScope.launch {
            repository.sendMessage(message)
        }
    }

    fun editMessage(roomId: String, messageId: String, newMessage: String, timestamp: Long) {
        val now = Date().time
        if (now - timestamp <= 2 * 60 * 1000) { // 2 minutes window
            viewModelScope.launch {
                repository.editMessage(roomId, messageId, newMessage)
            }
        }
    }

    fun deleteMessage(roomId: String, messageId: String, forEveryone: Boolean) {
        viewModelScope.launch {
            repository.deleteMessage(roomId, messageId, forEveryone)
        }
    }

    fun clearChat(roomId: String) {
        viewModelScope.launch {
            // Implementation: mark all current messages as hiddenBy current user
        }
    }

    fun deleteChat(roomId: String) {
        viewModelScope.launch {
            // Implementation: delete conversation document
        }
    }

    fun markAsSeen(roomId: String, messageId: String) {
        viewModelScope.launch {
            repository.markAsSeen(roomId, messageId)
        }
    }

    fun setTypingStatus(userId: String, targetId: String?) {
        viewModelScope.launch {
            repository.setTypingStatus(userId, targetId)
        }
    }
}
