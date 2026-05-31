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
import com.example.nexchat.databinding.FragmentChatsBinding
import com.example.nexchat.models.User
import com.example.nexchat.utils.ContactsHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatsFragment : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private val users = ArrayList<User>()
    private val contacts = ArrayList<User>()
    private lateinit var userAdapter: UserAdapter
    private lateinit var contactAdapter: UserAdapter
    private var contactList: List<String> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        userAdapter = UserAdapter(requireContext(), users)
        binding.rvUsers.adapter = userAdapter

        contactAdapter = UserAdapter(requireContext(), contacts)
        binding.rvContactsSection.adapter = contactAdapter

        binding.progressBar.visibility = View.VISIBLE
        checkContactsPermission()
        
        binding.btnInvite.setOnClickListener { inviteFriends() }
    }

    private fun checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            syncContacts()
        } else {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) syncContacts() else loadAllUsers() }.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun syncContacts() {
        contactList = ContactsHelper.getContactList(requireContext())
        loadAllUsers()
    }

    private fun loadAllUsers() {
        database.collection("users").addSnapshotListener { snapshot, error ->
            if (_binding == null || error != null) return@addSnapshotListener
            binding.progressBar.visibility = View.GONE
            
            users.clear()
            contacts.clear()
            snapshot?.toObjects(User::class.java)?.forEach { user ->
                if (user.uid != auth.currentUser?.uid) {
                    if (isNumberInContacts(user.phoneNumber)) {
                        contacts.add(user)
                    } else {
                        users.add(user)
                    }
                }
            }
            userAdapter.notifyDataSetChanged()
            contactAdapter.notifyDataSetChanged()
            updateEmptyState()
        }
    }

    private fun isNumberInContacts(phoneNumber: String): Boolean {
        if (phoneNumber.isEmpty()) return false
        val clean = phoneNumber.replace("[^0-9]".toRegex(), "")
        return contactList.any { it.replace("[^0-9]".toRegex(), "").endsWith(clean.takeLast(10)) }
    }

    private fun updateEmptyState() {
        if (_binding == null) return
        val isEmpty = users.isEmpty() && contacts.isEmpty()
        binding.llEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun inviteFriends() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Join me on NexChat!")
        }
        startActivity(Intent.createChooser(intent, "Invite via"))
    }

    fun filterUsers(query: String?) {
        // Logic for filtering if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
