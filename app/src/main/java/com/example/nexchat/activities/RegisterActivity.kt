package com.example.nexchat.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.nexchat.utils.CloudinaryHelper
import com.example.nexchat.databinding.ActivityRegisterBinding
import com.example.nexchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private var selectedImage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedImage = result.data?.data
                binding.ivProfile.setImageURI(selectedImage)
            }
        }

        binding.ivProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePicker.launch(intent)
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = android.view.View.VISIBLE
            
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            if (selectedImage != null) {
                                uploadImage(uid, name, email)
                            } else {
                                saveUserToFirestore(uid, name, email, "")
                            }
                        } else {
                            binding.progressBar.visibility = android.view.View.GONE
                            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun uploadImage(uid: String, name: String, email: String) {
        selectedImage?.let { uri ->
            CloudinaryHelper.uploadFile(this, uri, "NexChat/Profiles") { imageUrl ->
                saveUserToFirestore(uid, name, email, imageUrl ?: "")
            }
        } ?: saveUserToFirestore(uid, name, email, "")
    }

    private fun saveUserToFirestore(uid: String, name: String, email: String, imageUrl: String) {
        val user = User(
            uid = uid,
            name = name,
            email = email,
            profileImage = imageUrl,
            status = "Online",
            lastSeen = System.currentTimeMillis(),
            about = "Hey there! I'm using NexChat.",
            createdAt = System.currentTimeMillis()
        )
        database.collection("users").document(uid).set(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.progressBar.visibility = android.view.View.GONE
                    val session = com.example.nexchat.utils.SessionManager(this)
                    session.isLoggedIn = true
                    session.userId = uid
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Firestore Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
