package com.example.nexchat.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.nexchat.adapters.UserAdapter
import com.example.nexchat.databinding.ActivitySearchUserBinding
import com.example.nexchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SearchUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchUserBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userAdapter: UserAdapter
    private val userList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        userAdapter = UserAdapter(this, userList)
        binding.rvSearchResults.adapter = userAdapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                } else {
                    userList.clear()
                    userAdapter.notifyDataSetChanged()
                    binding.llNoResults.visibility = View.VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.llNoResults.visibility = View.GONE

        // Since Firestore doesn't support complex OR queries easily across multiple fields with substring match
        // We will fetch and filter in memory for better UX in this small project context, 
        // or perform multiple queries. For now, let's fetch all users and filter.
        // In a real app, you'd use Algolia or multiple whereEqualTo if searching by exact username/email/phone.
        
        database.collection("users").get().addOnSuccessListener { snapshot ->
            binding.progressBar.visibility = View.GONE
            userList.clear()
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val user = doc.toObject(User::class.java)
                    if (user != null && user.uid != auth.uid) {
                        if (user.name.lowercase().contains(query) ||
                            user.username.lowercase().contains(query.removePrefix("@")) ||
                            user.phoneNumber.contains(query) ||
                            user.email.lowercase().contains(query)) {
                            userList.add(user)
                        }
                    }
                }
            }
            userAdapter.notifyDataSetChanged()
            if (userList.isEmpty()) {
                binding.llNoResults.visibility = View.VISIBLE
            } else {
                binding.llNoResults.visibility = View.GONE
            }
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            binding.llNoResults.visibility = View.VISIBLE
        }
    }
}
