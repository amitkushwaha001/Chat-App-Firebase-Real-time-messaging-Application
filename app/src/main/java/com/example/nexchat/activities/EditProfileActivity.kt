package com.example.nexchat.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nexchat.R
import com.example.nexchat.databinding.ActivityEditProfileBinding
import com.example.nexchat.models.User
import com.example.nexchat.utils.CloudinaryHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private var selectedImage: Uri? = null
    private var isUsernameAvailable = true
    private var currentUsername = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        loadUserData()

        val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedImage = result.data?.data
                binding.ivProfile.setImageURI(selectedImage)
            }
        }

        binding.fabEditImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePicker.launch(intent)
        }

        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val username = s.toString().lowercase().trim()
                if (username == currentUsername) {
                    binding.ivUsernameStatus.visibility = View.GONE
                    isUsernameAvailable = true
                    return
                }
                if (username.length < 3) {
                    binding.ivUsernameStatus.visibility = View.VISIBLE
                    binding.ivUsernameStatus.setImageResource(android.R.drawable.ic_delete)
                    isUsernameAvailable = false
                    return
                }
                if (!isValidUsername(username)) {
                    binding.ivUsernameStatus.visibility = View.VISIBLE
                    binding.ivUsernameStatus.setImageResource(android.R.drawable.ic_delete)
                    isUsernameAvailable = false
                    Toast.makeText(this@EditProfileActivity, "Invalid characters", Toast.LENGTH_SHORT).show()
                    return
                }
                checkUsernameAvailability(username)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun isValidUsername(username: String): Boolean {
        val regex = "^[a-z0-9_.]+$".toRegex()
        return regex.matches(username)
    }

    private fun checkUsernameAvailability(username: String) {
        binding.pbUsernameCheck.visibility = View.VISIBLE
        binding.ivUsernameStatus.visibility = View.GONE
        
        database.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                binding.pbUsernameCheck.visibility = View.GONE
                if (documents.isEmpty) {
                    binding.ivUsernameStatus.visibility = View.VISIBLE
                    binding.ivUsernameStatus.setImageResource(android.R.drawable.checkbox_on_background)
                    isUsernameAvailable = true
                } else {
                    binding.ivUsernameStatus.visibility = View.VISIBLE
                    binding.ivUsernameStatus.setImageResource(android.R.drawable.ic_delete)
                    isUsernameAvailable = false
                }
            }
            .addOnFailureListener {
                binding.pbUsernameCheck.visibility = View.GONE
            }
    }

    private fun loadUserData() {
        val uid = auth.uid ?: return
        database.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                user?.let {
                    binding.etName.setText(it.name)
                    binding.etUsername.setText(it.username)
                    currentUsername = it.username
                    binding.etBio.setText(it.about)
                    binding.etEmail.setText(it.email)
                    binding.etPhone.setText(it.phoneNumber)
                    Glide.with(this)
                        .load(it.profileImage)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivProfile)
                }
            }
        }
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val username = binding.etUsername.text.toString().lowercase().trim()
        val bio = binding.etBio.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return
        }

        if (!isUsernameAvailable) {
            Toast.makeText(this, "Username not available", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        if (selectedImage != null) {
            uploadImage(name, username, bio, email, phone)
        } else {
            updateFirestore(name, username, bio, email, phone, null)
        }
    }

    private fun uploadImage(name: String, username: String, bio: String, email: String, phone: String) {
        CloudinaryHelper.uploadFile(this, selectedImage!!, "NexChat/Profiles") { imageUrl ->
            if (imageUrl != null) {
                updateFirestore(name, username, bio, email, phone, imageUrl)
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFirestore(name: String, username: String, bio: String, email: String, phone: String, imageUrl: String?) {
        val uid = auth.uid!!
        val map = mutableMapOf<String, Any>(
            "name" to name,
            "username" to username,
            "about" to bio,
            "email" to email,
            "phoneNumber" to phone
        )
        imageUrl?.let { map["profileImage"] = it }

        database.collection("users").document(uid).update(map).addOnCompleteListener { task ->
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
            if (task.isSuccessful) {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Update failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
