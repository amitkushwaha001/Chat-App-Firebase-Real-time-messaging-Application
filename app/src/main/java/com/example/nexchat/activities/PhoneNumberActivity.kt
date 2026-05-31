package com.example.nexchat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nexchat.databinding.ActivityPhoneNumberBinding
import com.example.nexchat.models.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class PhoneNumberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneNumberBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnSendOtp.setOnClickListener {
            val phoneInput = binding.etPhone.text.toString().trim()
            if (phoneInput.isEmpty()) {
                Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Remove spaces and special characters
            val cleanNumber = phoneInput.replace("\\s".toRegex(), "").replace("-", "")
            
            val finalPhone = when {
                cleanNumber.startsWith("+") -> cleanNumber
                cleanNumber.length == 10 -> "+91$cleanNumber"
                else -> cleanNumber
            }

            if (finalPhone.length >= 10) {
                sendOtp(finalPhone)
            } else {
                Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification or instant verification
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.progressBar.visibility = View.GONE
                Log.e("PhoneAuth", "Verification Failed", e)
                
                val message = when {
                    e.message?.contains("BILLING_NOT_ENABLED") == true -> 
                        "Project requires SMS quota. Please use 'Test Phone Numbers' in Firebase or upgrade to Blaze plan."
                    e.message?.contains("TOO_MANY_REQUESTS") == true ->
                        "Too many attempts. Please try again later."
                    else -> "Error: ${e.localizedMessage ?: "Internal Error"}"
                }
                
                Toast.makeText(this@PhoneNumberActivity, message, Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(id, token)
                binding.progressBar.visibility = View.GONE
                verificationId = id
                val intent = Intent(this@PhoneNumberActivity, OTPActivity::class.java)
                val phone = binding.etPhone.text.toString().trim()
                val finalPhone = if (phone.startsWith("+")) phone else if (phone.length == 10) "+91$phone" else phone
                
                intent.putExtra("phoneNumber", finalPhone)
                intent.putExtra("verificationId", verificationId)
                startActivity(intent)
            }
        }
    }

    private fun sendOtp(phone: String) {
        binding.progressBar.visibility = View.VISIBLE
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        binding.progressBar.visibility = View.VISIBLE
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    checkUserStatus(uid)
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Sign-in Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserStatus(uid: String) {
        val database = FirebaseFirestore.getInstance()
        database.collection("users").document(uid).get().addOnSuccessListener { document ->
            binding.progressBar.visibility = View.GONE
            if (document.exists()) {
                database.collection("users").document(uid).update("status", "Online")
                goToHome()
            } else {
                // Redirect to setup profile if new user
                val intent = Intent(this, SetupProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            goToHome()
        }
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
