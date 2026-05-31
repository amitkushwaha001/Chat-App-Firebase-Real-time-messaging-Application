package com.example.nexchat.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexchat.databinding.ItemReceivedMessageBinding
import com.example.nexchat.databinding.ItemSentMessageBinding
import com.example.nexchat.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val context: Context, private val messageList: ArrayList<Message>, private val roomId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_SENT = 1
    private val ITEM_RECEIVED = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val binding = ItemSentMessageBinding.inflate(LayoutInflater.from(context), parent, false)
            SentViewHolder(binding)
        } else {
            val binding = ItemReceivedMessageBinding.inflate(LayoutInflater.from(context), parent, false)
            ReceivedViewHolder(binding)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (FirebaseAuth.getInstance().uid == message.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVED
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))

        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            viewHolder.binding.tvTime.text = time
            
            when (message.type) {
                "image" -> {
                    viewHolder.binding.ivMessage.visibility = View.VISIBLE
                    viewHolder.binding.tvMessage.visibility = View.GONE
                    viewHolder.binding.llDocument.visibility = View.GONE
                    Glide.with(context).load(message.fileUrl).into(viewHolder.binding.ivMessage)
                    
                    viewHolder.binding.ivMessage.setOnClickListener {
                        openFullScreenImage(message.fileUrl, message.messageId)
                    }

                    viewHolder.binding.ivMessage.setOnLongClickListener {
                        showDeleteOptions(message)
                        true
                    }
                }
                "video" -> {
                    viewHolder.binding.ivMessage.visibility = View.VISIBLE
                    viewHolder.binding.tvMessage.visibility = View.GONE
                    viewHolder.binding.llDocument.visibility = View.GONE
                    // Use a thumbnail or a video icon
                    Glide.with(context).load(message.fileUrl).frame(1000000).into(viewHolder.binding.ivMessage)
                }
                "audio" -> {
                    viewHolder.binding.ivMessage.visibility = View.GONE
                    viewHolder.binding.tvMessage.visibility = View.GONE
                    viewHolder.binding.llDocument.visibility = View.VISIBLE
                    viewHolder.binding.ivDocumentIcon.setImageResource(com.example.nexchat.R.drawable.ic_audio)
                    viewHolder.binding.tvDocumentName.text = "Audio Message"
                }
                "document" -> {
                    viewHolder.binding.ivMessage.visibility = View.GONE
                    viewHolder.binding.tvMessage.visibility = View.GONE
                    viewHolder.binding.llDocument.visibility = View.VISIBLE
                    viewHolder.binding.ivDocumentIcon.setImageResource(com.example.nexchat.R.drawable.ic_document)
                    viewHolder.binding.tvDocumentName.text = message.fileName
                }
                else -> {
                    viewHolder.binding.ivMessage.visibility = View.GONE
                    viewHolder.binding.tvMessage.visibility = View.VISIBLE
                    viewHolder.binding.llDocument.visibility = View.GONE
                    viewHolder.binding.tvMessage.text = message.message
                }
            }

            if (message.seen) {
                viewHolder.binding.ivStatus.setImageResource(com.example.nexchat.R.drawable.ic_check_double)
            } else {
                viewHolder.binding.ivStatus.setImageResource(com.example.nexchat.R.drawable.ic_check)
            }

            viewHolder.binding.tvMessage.setOnLongClickListener {
                showDeleteOptions(message)
                true
            }

            viewHolder.binding.llMessage.setOnLongClickListener {
                showDeleteOptions(message)
                true
            }

            viewHolder.itemView.setOnLongClickListener {
                showDeleteOptions(message)
                true
            }

        } else {
            val viewHolder = holder as ReceivedViewHolder
            viewHolder.binding.tvTime.text = time

            when (message.type) {
                "image" -> {
                    viewHolder.binding.ivMessage.visibility = View.VISIBLE
                    viewHolder.binding.tvMessage.visibility = View.GONE
                    viewHolder.binding.llDocument.visibility = View.GONE
                    Glide.with(context).load(message.fileUrl).into(viewHolder.binding.ivMessage)
                    
                    viewHolder.binding.ivMessage.setOnClickListener {
                        openFullScreenImage(message.fileUrl, message.messageId)
                    }

                    viewHolder.binding.ivMessage.setOnLongClickListener {
                        showDeleteOptions(message)
                        true
                    }
                }
                "video" -> {
                    viewHolder.binding.ivMessage.visibility = View.VISIBLE
                    viewHolder.binding.tvMessage.visibility = View.GONE
                    viewHolder.binding.llDocument.visibility = View.GONE
                    // Use a thumbnail or a video icon
                    Glide.with(context).load(message.fileUrl).frame(1000000).into(viewHolder.binding.ivMessage)
                }
                "audio" -> {
                    viewHolder.binding.ivMessage.visibility = View.GONE
                    viewHolder.binding.tvMessage.visibility = View.GONE
                    viewHolder.binding.llDocument.visibility = View.VISIBLE
                    viewHolder.binding.ivDocumentIcon.setImageResource(com.example.nexchat.R.drawable.ic_audio)
                    viewHolder.binding.tvDocumentName.text = "Audio Message"
                }
                "document" -> {
                    viewHolder.binding.ivMessage.visibility = View.GONE
                    viewHolder.binding.tvMessage.visibility = View.GONE
                    viewHolder.binding.llDocument.visibility = View.VISIBLE
                    viewHolder.binding.ivDocumentIcon.setImageResource(com.example.nexchat.R.drawable.ic_document)
                    viewHolder.binding.tvDocumentName.text = message.fileName
                }
                else -> {
                    viewHolder.binding.ivMessage.visibility = View.GONE
                    viewHolder.binding.tvMessage.visibility = View.VISIBLE
                    viewHolder.binding.llDocument.visibility = View.GONE
                    viewHolder.binding.tvMessage.text = message.message
                }
            }

            viewHolder.binding.tvMessage.setOnLongClickListener {
                showDeleteOptions(message)
                true
            }

            viewHolder.binding.llMessage.setOnLongClickListener {
                showDeleteOptions(message)
                true
            }

            viewHolder.itemView.setOnLongClickListener {
                showDeleteOptions(message)
                true
            }
        }
    }

    private fun showDeleteOptions(message: Message) {
        val currentUid = FirebaseAuth.getInstance().uid
        val isMyMessage = message.senderId == currentUid
        
        val options = if (isMyMessage) {
            arrayOf("Delete for Me", "Delete for Everyone", "Cancel")
        } else {
            arrayOf("Delete for Me", "Cancel")
        }

        AlertDialog.Builder(context)
            .setTitle("Delete Message?")
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "Delete for Me" -> deleteMessageForMe(message)
                    "Delete for Everyone" -> deleteMessageForEveryone(message)
                }
            }
            .show()
    }

    private fun deleteMessageForMe(message: Message) {
        val db = FirebaseFirestore.getInstance()
        val currentUid = FirebaseAuth.getInstance().uid ?: return
        
        // WhatsApp logic: "Delete for me" usually means hiding it locally.
        // We implement this by adding the current user's UID to a 'hiddenBy' list in Firestore.
        db.collection("chats").document(roomId).collection("messages").document(message.messageId)
            .update("hiddenBy", com.google.firebase.firestore.FieldValue.arrayUnion(currentUid))
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted for me", Toast.LENGTH_SHORT).show()
                // The SnapshotListener in ChatActivity will filter this out
            }
    }

    private fun deleteMessageForEveryone(message: Message) {
        val db = FirebaseFirestore.getInstance()
        db.collection("chats").document(roomId).collection("messages").document(message.messageId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Message deleted for everyone", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openFullScreenImage(imageUrl: String, messageId: String) {
        val intent = android.content.Intent(context, com.example.nexchat.activities.FullScreenImageActivity::class.java)
        intent.putExtra("imageUrl", imageUrl)
        intent.putExtra("messageId", messageId)
        intent.putExtra("roomId", roomId)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int = messageList.size

    class SentViewHolder(val binding: ItemSentMessageBinding) : RecyclerView.ViewHolder(binding.root)
    class ReceivedViewHolder(val binding: ItemReceivedMessageBinding) : RecyclerView.ViewHolder(binding.root)
}
