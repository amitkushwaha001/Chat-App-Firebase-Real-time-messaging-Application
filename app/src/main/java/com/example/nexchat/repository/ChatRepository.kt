package com.example.nexchat.repository

import com.example.nexchat.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun getMessages(userId: String, otherId: String): Flow<List<Message>> {
        val roomIds = listOf(userId, otherId).sorted()
        val roomId = "${roomIds[0]}_${roomIds[1]}"

        return callbackFlow {
            val listener = db.collection("chats").document(roomId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val currentUid = auth.uid ?: ""
                    val list = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.takeIf { !it.hiddenBy.contains(currentUid) }
                    } ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun sendMessage(message: Message) {
        val roomIds = listOf(message.senderId, message.receiverId).sorted()
        val roomId = "${roomIds[0]}_${roomIds[1]}"
        
        db.collection("chats").document(roomId).collection("messages").document(message.messageId).set(message)
            .addOnSuccessListener {
                val conversationData = mapOf(
                    "lastMessage" to if (message.type == "text") message.message else "Sent a ${message.type}",
                    "lastMessageTimestamp" to message.timestamp,
                    "lastMessageSenderId" to message.senderId,
                    "roomId" to roomId,
                    "participants" to listOf(message.senderId, message.receiverId)
                )
                db.collection("conversations").document(roomId).set(conversationData)
            }
    }

    suspend fun editMessage(roomId: String, messageId: String, newMessage: String) {
        db.collection("chats").document(roomId).collection("messages").document(messageId)
            .update("message", newMessage, "isEdited", true)
    }

    suspend fun deleteMessage(roomId: String, messageId: String, forEveryone: Boolean) {
        if (forEveryone) {
            db.collection("chats").document(roomId).collection("messages").document(messageId)
                .update(
                    "message", "This message was deleted",
                    "isDeleted", true,
                    "type", "text",
                    "fileUrl", "",
                    "fileName", ""
                )
        } else {
            val currentUid = auth.uid ?: return
            db.collection("chats").document(roomId).collection("messages").document(messageId)
                .update("hiddenBy", FieldValue.arrayUnion(currentUid))
        }
    }

    suspend fun markAsSeen(roomId: String, messageId: String) {
        db.collection("chats").document(roomId).collection("messages").document(messageId)
            .update("seen", true)
    }

    suspend fun setTypingStatus(userId: String, targetId: String?) {
        db.collection("users").document(userId).update("typingTo", targetId)
    }
}
