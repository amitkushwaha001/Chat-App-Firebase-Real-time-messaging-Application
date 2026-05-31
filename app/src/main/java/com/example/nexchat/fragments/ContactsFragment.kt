package com.example.nexchat.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.nexchat.adapters.UserAdapter
import com.example.nexchat.databinding.FragmentContactsBinding
import com.example.nexchat.models.User
import com.example.nexchat.utils.ContactsHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: UserAdapter
    private val contactList = ArrayList<User>()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            syncContacts()
        } else {
            Toast.makeText(context, "Contacts permission is required to find friends", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        
        adapter = UserAdapter(requireContext(), contactList)
        binding.rvContacts.adapter = adapter

        checkPermissionAndSync()

        binding.fabAdd.setOnClickListener {
            checkPermissionAndSync()
        }
    }

    private fun checkPermissionAndSync() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED -> {
                syncContacts()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun syncContacts() {
        val phoneContacts = ContactsHelper.getContactList(requireContext())
        if (phoneContacts.isEmpty()) return

        database.collection("users").get().addOnSuccessListener { snapshot ->
            contactList.clear()
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val user = doc.toObject(User::class.java)
                    if (user != null && user.uid != auth.uid) {
                        // Check if this firestore user is in our phone contacts
                        val userPhone = user.phoneNumber.replace("[^0-9]".toRegex(), "")
                        if (phoneContacts.any { it.replace("[^0-9]".toRegex(), "").contains(userPhone.takeLast(10)) }) {
                            contactList.add(user)
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged()
            if (contactList.isEmpty()) {
                Toast.makeText(context, "No NexChat users found in your contacts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
