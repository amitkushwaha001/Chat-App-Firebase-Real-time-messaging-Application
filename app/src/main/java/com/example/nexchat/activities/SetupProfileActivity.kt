package com.example.nexchat.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.nexchat.utils.CloudinaryHelper
import com.example.nexchat.databinding.ActivitySetupProfileBinding
import com.example.nexchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private var selectedImage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val selectImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    selectedImage = data.data
                    binding.ivSetupProfile.setImageURI(selectedImage)
                }
            }
        }

        binding.ivSetupProfile.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            selectImage.launch(intent)
        }

        binding.btnNext.setOnClickListener {
            val name = binding.etSetupName.text.toString().trim()
            val about = binding.etSetupAbout.text.toString().trim().ifEmpty { "Hey there! I am using NexChat." }

            if (name.isEmpty()) {
                binding.etSetupName.error = "Please enter your name"
                return@setOnClickListener
            }

            if (selectedImage != null) {
                uploadImage(name, about)
            } else {
                saveUserToFirestore(name, about)
            }
        }
    }

    private fun uploadImage(name: String, about: String) {
        binding.setupProgressBar.visibility = View.VISIBLE
        binding.btnNext.isEnabled = false
        
        val uid = auth.uid ?: return
        val phone = auth.currentUser?.phoneNumber ?: ""
        
        // WhatsApp speed: Create user object
        val user = User(
            uid = uid,
            name = name,
            phoneNumber = phone,
            profileImage = "", // Initially empty for speed
            about = about,
            status = "Online",
            lastSeen = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )

        // Save to Firestore IMMEDIATELY (Fast Mode)
        database.collection("users").document(uid).set(user)
            .addOnSuccessListener {
                val session = com.example.nexchat.utils.SessionManager(this)
                session.isLoggedIn = true
                session.userId = uid
                // If there's an image, upload in background WITHOUT blocking the user
                selectedImage?.let { uri ->
                    CloudinaryHelper.uploadFile(this, uri, "NexChat/Profiles") { imageUrl ->
                        if (imageUrl != null) {
                            database.collection("users").document(uid).update("profileImage", imageUrl)
                        }
                    }
                }

                // Redirect to Home INSTANTLY
                goToHome()
            }
            .addOnFailureListener {
                binding.setupProgressBar.visibility = View.GONE
                binding.btnNext.isEnabled = true
                Toast.makeText(this, "Connection error", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun saveUserToFirestore(name: String, about: String) {
        // No longer used, merged into uploadImage for unified fast-flow
        uploadImage(name, about)
    }
}
