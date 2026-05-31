package com.example.nexchat.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexchat.adapters.UserAdapter
import com.example.nexchat.databinding.ActivityNewMessageBinding
import com.example.nexchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NewMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewMessageBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val contactsList = ArrayList<User>()
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        adapter = UserAdapter(this, contactsList)
        binding.rvContacts.layoutManager = LinearLayoutManager(this)
        binding.rvContacts.adapter = adapter

        loadContacts()
    }

    private fun loadContacts() {
        database.collection("users").get().addOnSuccessListener { snapshot ->
            contactsList.clear()
            snapshot.toObjects(User::class.java).forEach {
                if (it.uid != auth.uid) contactsList.add(it)
            }
            adapter.notifyDataSetChanged()
        }
    }
}
