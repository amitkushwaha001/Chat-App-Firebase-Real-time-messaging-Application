package com.example.nexchat.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexchat.R
import com.example.nexchat.activities.MainActivity
import com.example.nexchat.databinding.ItemUserBinding
import com.example.nexchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserAdapter(private val context: Context, private val userList: ArrayList<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.tvUserName.text = user.name
        
        if (user.username.isNotEmpty()) {
            holder.binding.tvUsername.visibility = View.VISIBLE
            holder.binding.tvUsername.text = "@${user.username}"
        } else {
            holder.binding.tvUsername.visibility = View.GONE
        }

        holder.binding.tvLastMessage.text = user.about

        Glide.with(context)
            .load(user.profileImage)
            .placeholder(R.drawable.ic_user_placeholder)
            .into(holder.binding.ivUser)

        if (user.status == "Online") {
            holder.binding.viewOnline.visibility = View.VISIBLE
        } else {
            holder.binding.viewOnline.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("uid", user.uid)
            intent.putExtra("image", user.profileImage)
            context.startActivity(intent)
            if (context is android.app.Activity) {
                context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

        holder.binding.ivPreview.setOnClickListener {
            val intent = Intent(context, com.example.nexchat.activities.ProfileActivity::class.java)
            intent.putExtra("uid", user.uid)
            intent.putExtra("name", user.name)
            intent.putExtra("image", user.profileImage)
            context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            showDeleteChatDialog(user)
            true
        }
    }

    private fun showDeleteChatDialog(user: User) {
        AlertDialog.Builder(context)
            .setTitle("Delete Chat")
            .setMessage("Are you sure you want to delete this conversation with ${user.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteChat(user)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteChat(user: User) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val senderId = auth.uid ?: return
        val roomIds = listOf(senderId, user.uid).sorted()
        val unifiedRoomId = "${roomIds[0]}_${roomIds[1]}"

        // Delete the conversation summary
        db.collection("conversations").document(unifiedRoomId).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Conversation deleted", Toast.LENGTH_SHORT).show()
                // The SnapshotListener in ChatsFragment will automatically update the UI
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = userList.size

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)
}
