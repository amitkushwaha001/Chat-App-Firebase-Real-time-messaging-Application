package com.example.nexchat.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nexchat.databinding.ActivityFullScreenImageBinding
import com.google.firebase.firestore.FirebaseFirestore

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra("imageUrl")
        val messageId = intent.getStringExtra("messageId")
        val roomId = intent.getStringExtra("roomId")
        
        Glide.with(this)
            .load(imageUrl)
            .into(binding.ivFullImage)

        binding.ivClose.setOnClickListener {
            finish()
        }

        binding.ivDelete.setOnClickListener {
            if (messageId != null && roomId != null) {
                showDeleteDialog(roomId, messageId)
            } else {
                Toast.makeText(this, "Cannot delete this image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteDialog(roomId: String, messageId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Image")
            .setMessage("Do you want to delete this image for everyone?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseFirestore.getInstance().collection("chats").document(roomId)
                    .collection("messages").document(messageId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}