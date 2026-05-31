package com.example.nexchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nexchat.adapters.CallsAdapter
import com.example.nexchat.databinding.FragmentCallsBinding
import com.example.nexchat.models.CallLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Filter

class CallsFragment : Fragment() {

    private var _binding: FragmentCallsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CallsAdapter
    private val callList = ArrayList<CallLog>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallsBinding.inflate(inflater, container, false)

        adapter = CallsAdapter(requireContext(), callList)
        binding.rvCalls.adapter = adapter

        fetchCallHistory()

        return binding.root
    }

    private fun fetchCallHistory() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Fetch calls where current user is in the participants list
        db.collection("calls")
            .whereArrayContains("participants", currentUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    callList.clear()
                    val logs = snapshot.toObjects(CallLog::class.java)
                    callList.addAll(logs)
                    adapter.notifyDataSetChanged()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
