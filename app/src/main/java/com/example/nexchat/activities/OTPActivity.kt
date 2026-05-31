package com.example.nexchat.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nexchat.R
import com.example.nexchat.databinding.ActivityOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private var phoneNumber: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        verificationId = intent.getStringExtra("verificationId")
        phoneNumber = intent.getStringExtra("phoneNumber")

        startResendTimer()

        binding.btnVerify.setOnClickListener {
            val code = binding.etOtp.text.toString().trim()
            if (code.length < 6) {
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            } else {
                verifyCode(code)
            }
        }

        binding.tvResend.setOnClickListener {
            if (phoneNumber != null) {
                resendOtp()
            }
        }
    }

    private fun startResendTimer() {
        binding.tvResend.isEnabled = false
        binding.tvResend.alpha = 0.5f
        
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvResend.text = getString(R.string.resend_code_in, millisUntilFinished / 1000)
            }

            override fun onFinish() {
                binding.tvResend.text = getString(R.string.resend_code)
                binding.tvResend.isEnabled = true
                binding.tvResend.alpha = 1.0f
            }
        }.start()
    }

    private fun resendOtp() {
        val currentPhoneNumber = phoneNumber
        if (currentPhoneNumber == null) {
            Toast.makeText(this, "Phone number missing", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show()
        
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(currentPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@OTPActivity, "Resend failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = id
                    resendToken = token
                    startResendTimer()
                    Toast.makeText(this@OTPActivity, "OTP Resent successfully", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        val currentVerificationId = verificationId
        if (currentVerificationId == null) {
            Toast.makeText(this, "Verification ID is null", Toast.LENGTH_SHORT).show()
            return
        }
        binding.progressBar.visibility = View.VISIBLE
        binding.btnVerify.isEnabled = false
        val credential = PhoneAuthProvider.getCredential(currentVerificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val database = FirebaseFirestore.getInstance()

                    // Optimized check: Don't wait for a full network sync if not needed
                    database.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->
                            binding.progressBar.visibility = View.GONE
                            if (document.exists()) {
                                database.collection("users").document(uid).update("status", "Online")
                                goToHome()
                            } else {
                                val intent = Intent(this, SetupProfileActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        }
                        .addOnFailureListener {
                            binding.progressBar.visibility = View.GONE
                            goToHome() // Fallback
                        }
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.btnVerify.isEnabled = true
                    Toast.makeText(this, "Verification Failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
