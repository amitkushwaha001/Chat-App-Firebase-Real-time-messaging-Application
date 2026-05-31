package com.example.nexchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexchat.R
import com.example.nexchat.databinding.ItemStatusBinding
import com.example.nexchat.models.UserStatus
import java.text.SimpleDateFormat
import java.util.*

class StatusAdapter(private val context: Context, private val statusList: List<UserStatus>) :
    RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    class StatusViewHolder(val binding: ItemStatusBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val binding = ItemStatusBinding.inflate(LayoutInflater.from(context), parent, false)
        return StatusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val userStatus = statusList[position]
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

        if (userStatus.uid == auth.uid) {
            holder.binding.username.text = "My Status"
        } else {
            holder.binding.username.text = userStatus.name
        }
        
        Glide.with(context).load(userStatus.profileImage).placeholder(R.drawable.ic_user_placeholder).into(holder.binding.profileImage)

        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        val timeStr = if (android.text.format.DateUtils.isToday(userStatus.lastUpdated)) {
            "Today, " + sdf.format(Date(userStatus.lastUpdated))
        } else {
            SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(userStatus.lastUpdated))
        }
        holder.binding.time.text = timeStr
    }

    override fun getItemCount(): Int = statusList.size
}
