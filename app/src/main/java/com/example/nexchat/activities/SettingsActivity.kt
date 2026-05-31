package com.example.nexchat.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.nexchat.R
import com.example.nexchat.databinding.ActivitySettingsBinding
import com.example.nexchat.models.User
import com.example.nexchat.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        session = SessionManager(this)

        setupToolbar()
        setupSettingsItems()
        loadUserData()

        binding.cvProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.llLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSettingsItems() {
        // Account
        binding.itemAccount.ivIcon.setImageResource(R.drawable.ic_user_placeholder)
        binding.itemAccount.tvTitle.text = "Account (Profile, Email, Name)"
        binding.itemAccount.root.setOnClickListener { 
            startActivity(Intent(this, EditProfileActivity::class.java)) 
        }

        // Privacy
        binding.itemPrivacy.ivIcon.setImageResource(R.drawable.ic_check)
        binding.itemPrivacy.tvTitle.text = "Privacy & Security"
        binding.itemPrivacy.root.setOnClickListener { 
            Toast.makeText(this, "Privacy settings coming soon", Toast.LENGTH_SHORT).show()
        }

        // Notifications
        binding.itemNotifications.ivIcon.setImageResource(R.drawable.ic_check)
        binding.itemNotifications.tvTitle.text = "Notifications"
        binding.itemNotifications.root.setOnClickListener { 
            Toast.makeText(this, "Notification settings coming soon", Toast.LENGTH_SHORT).show()
        }

        // Appearance
        binding.itemAppearance.ivIcon.setImageResource(R.drawable.ic_check)
        binding.itemAppearance.tvTitle.text = "Appearance (Theme)"
        binding.itemAppearance.root.setOnClickListener { 
            showThemeSelectionDialog()
        }

        // Storage
        binding.itemStorage.ivIcon.setImageResource(R.drawable.ic_check)
        binding.itemStorage.tvTitle.text = "Data and Storage"
        binding.itemStorage.root.setOnClickListener { 
            Toast.makeText(this, "Storage settings coming soon", Toast.LENGTH_SHORT).show()
        }

        // Help
        binding.itemHelp.ivIcon.setImageResource(R.drawable.ic_check)
        binding.itemHelp.tvTitle.text = "Help & Contact Us"
        binding.itemHelp.root.setOnClickListener { 
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Help & Contact")
            builder.setMessage("Phone: 8700530415\nEmail: amitkushwaha200215@gmail.com\n\nFeatures:\n✓ Contact Us\n✓ Report Bug\n✓ Feedback\n✓ About App")
            builder.setPositiveButton("OK", null)
            builder.show()
        }
    }

    private fun showThemeSelectionDialog() {
        val options = arrayOf("Light", "Dark", "System Default")
        val checkedItem = when(session.themeMode) {
            "light" -> 0
            "dark" -> 1
            else -> 2
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val mode = when(which) {
                    0 -> "light"
                    1 -> "dark"
                    else -> "system"
                }
                session.themeMode = mode
                applyTheme(mode)
                dialog.dismiss()
            }
            .show()
    }

    private fun applyTheme(mode: String) {
        when(mode) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun loadUserData() {
        val uid = auth.uid ?: return
        database.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                user?.let {
                    binding.tvName.text = if(it.name.isNotEmpty()) it.name else "@${it.username}"
                    binding.tvPhone.text = it.phoneNumber
                    Glide.with(this)
                        .load(it.profileImage)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivProfile)
                }
            }
        }
    }

    private fun logoutUser() {
        auth.signOut()
        session.clear()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
