package com.example.nexchat.activities

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nexchat.R
import com.example.nexchat.databinding.ActivityProfileBinding
import com.example.nexchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val targetUid = intent.getStringExtra("uid")
        
        if (targetUid != null && targetUid != auth.uid) {
            setupOtherUserProfile(targetUid)
        } else {
            setupMyProfile()
        }
    }

    private fun setupOtherUserProfile(uid: String) {
        binding.btnEdit.visibility = View.GONE
        
        binding.llMessage.setOnClickListener { finish() } // Already in chat
        binding.llCall.setOnClickListener { startCall(uid, false) }
        binding.llVideo.setOnClickListener { startCall(uid, true) }
        binding.llMute.setOnClickListener { Toast.makeText(this, "Notifications Muted", Toast.LENGTH_SHORT).show() }

        database.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                user?.let {
                    binding.tvName.text = it.name
                    binding.tvUsername.text = if (it.username.isNotEmpty()) "@${it.username}" else "Not set"
                    binding.tvAbout.text = it.about
                    binding.tvPhone.text = it.phoneNumber
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    binding.tvJoinedDate.text = if (it.createdAt > 0) sdf.format(Date(it.createdAt)) else "Unknown"
                    binding.tvStatus.text = it.status
                    Glide.with(this)
                        .load(it.profileImage)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivProfile)
                }
            }
        }
    }

    private fun setupMyProfile() {
        binding.btnEdit.visibility = View.VISIBLE
        binding.llMessage.visibility = View.GONE
        binding.llMute.visibility = View.GONE
        
        loadUserData()

        binding.btnEdit.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    private fun loadUserData() {
        val uid = auth.uid ?: return
        database.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                user?.let {
                    binding.tvName.text = it.name
                    binding.tvUsername.text = if (it.username.isNotEmpty()) "@${it.username}" else "Not set"
                    binding.tvAbout.text = it.about
                    binding.tvPhone.text = it.phoneNumber
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    binding.tvJoinedDate.text = if (it.createdAt > 0) sdf.format(Date(it.createdAt)) else "Unknown"
                    binding.tvStatus.text = "Online"
                    Glide.with(this)
                        .load(it.profileImage)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivProfile)
                }
            }
        }
    }

    private fun startCall(uid: String, isVideo: Boolean) {
        val intent = Intent(this, CallActivity::class.java)
        intent.putExtra("uid", uid)
        intent.putExtra("isVideo", isVideo)
        intent.putExtra("name", binding.tvName.text.toString())
        startActivity(intent)
    }
}
