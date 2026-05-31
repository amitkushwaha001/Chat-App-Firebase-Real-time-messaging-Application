package com.example.nexchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexchat.R
import com.example.nexchat.databinding.ItemCallBinding
import com.example.nexchat.models.CallLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CallsAdapter(private val context: Context, private val callList: List<CallLog>) :
    RecyclerView.Adapter<CallsAdapter.CallViewHolder>() {

    class CallViewHolder(val binding: ItemCallBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        val binding = ItemCallBinding.inflate(LayoutInflater.from(context), parent, false)
        return CallViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        val call = callList[position]
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid

        val isCaller = call.callerUid == currentUid
        val otherPartyName = if (isCaller) call.receiverName else call.callerName
        val otherPartyImage = if (isCaller) call.receiverImage else call.callerImage

        holder.binding.username.text = otherPartyName
        Glide.with(context).load(otherPartyImage).placeholder(R.drawable.ic_user_placeholder).into(holder.binding.profileImage)

        val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
        holder.binding.time.text = sdf.format(Date(call.timestamp))

        if (call.type == "video") {
            holder.binding.callIcon.setImageResource(R.drawable.ic_video)
        } else {
            holder.binding.callIcon.setImageResource(R.drawable.ic_phone)
        }

        if (isCaller) {
            holder.binding.callStatusIcon.setImageResource(R.drawable.ic_check)
            holder.binding.callStatusIcon.setColorFilter(context.getColor(R.color.green))
        } else {
            if (call.isMissed) {
                holder.binding.callStatusIcon.setImageResource(R.drawable.ic_call_end)
                holder.binding.callStatusIcon.setColorFilter(context.getColor(R.color.red))
            } else {
                holder.binding.callStatusIcon.setImageResource(R.drawable.ic_check_double)
                holder.binding.callStatusIcon.setColorFilter(context.getColor(R.color.green))
            }
        }

        // Long press to delete call log
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Call Log")
                .setMessage("Remove this call from history?")
                .setPositiveButton("Delete") { _, _ ->
                    FirebaseFirestore.getInstance().collection("calls").document(call.callId).delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
    }

    override fun getItemCount(): Int = callList.size
}
