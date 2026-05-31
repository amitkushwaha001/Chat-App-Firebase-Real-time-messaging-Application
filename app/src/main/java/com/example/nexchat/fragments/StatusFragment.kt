package com.example.nexchat.fragments

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.nexchat.adapters.StatusAdapter
import com.example.nexchat.databinding.FragmentStatusBinding
import com.example.nexchat.models.Status
import com.example.nexchat.models.UserStatus
import com.example.nexchat.utils.CloudinaryHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class StatusFragment : Fragment() {

    private var _binding: FragmentStatusBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: StatusAdapter
    private val statusList = ArrayList<UserStatus>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                uploadStatusImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatusBinding.inflate(inflater, container, false)

        adapter = StatusAdapter(requireContext(), statusList)
        binding.rvStatus.adapter = adapter

        binding.fabCameraStatus.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePicker.launch(intent)
        }

        fetchStatuses()

        return binding.root
    }

    private fun uploadStatusImage(uri: Uri) {
        Toast.makeText(requireContext(), "Uploading status...", Toast.LENGTH_SHORT).show()
        CloudinaryHelper.uploadFile(requireContext(), uri, "image") { imageUrl: String? ->
            if (imageUrl != null) {
                saveStatusToFirestore(imageUrl)
            } else {
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveStatusToFirestore(imageUrl: String) {
        val uid = auth.currentUser?.uid ?: return
        val statusId = db.collection("statuses").document().id
        val status = Status(
            statusId = statusId,
            uid = uid,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )

        db.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
            val name = snapshot.getString("name") ?: ""
            val profileImage = snapshot.getString("profileImage") ?: ""

            val userStatusRef = db.collection("statuses").document(uid)
            
            userStatusRef.get().addOnSuccessListener { statusSnapshot ->
                if (statusSnapshot.exists()) {
                    userStatusRef.update(
                        "lastUpdated", status.timestamp,
                        "statuses", FieldValue.arrayUnion(status)
                    )
                } else {
                    val userStatus = UserStatus(
                        uid = uid,
                        name = name,
                        profileImage = profileImage,
                        lastUpdated = status.timestamp,
                        statuses = listOf(status)
                    )
                    userStatusRef.set(userStatus)
                }
            }
        }
    }

    private fun fetchStatuses() {
        db.collection("statuses")
            .orderBy("lastUpdated", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (_binding == null) return@addSnapshotListener
                if (snapshot != null) {
                    statusList.clear()
                    val allStatuses = snapshot.toObjects(UserStatus::class.java)
                    
                    // Sorting and categorizing logic can go here if using a complex adapter
                    // For now, let's at least make sure My Status is handled or prioritized
                    statusList.addAll(allStatuses)
                    adapter.notifyDataSetChanged()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
