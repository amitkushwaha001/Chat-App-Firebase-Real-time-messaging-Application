package com.example.nexchat.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.nexchat.ui.navigation.NexChatNavGraph
import com.example.nexchat.ui.navigation.Screen
import com.example.nexchat.ui.theme.NexChatTheme
import com.example.nexchat.utils.SessionManager
import com.example.nexchat.viewmodel.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val homeViewModel: HomeViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val startUserId = intent.getStringExtra("uid")
        val session = SessionManager(this)
        
        observeIncomingCalls()
        
        setContent {
            val isDark = when(session.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            
            NexChatTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                NexChatNavGraph(
                    navController = navController,
                    startDestination = if (startUserId != null) Screen.Chat.createRoute(startUserId) else Screen.Home.route,
                    homeViewModel = homeViewModel
                )
            }
        }
    }

    private fun observeIncomingCalls() {
        val db = FirebaseFirestore.getInstance()
        lifecycleScope.launch {
            homeViewModel.incomingCall.collectLatest { callId ->
                if (callId != null) {
                    db.collection("calls").document(callId).get().addOnSuccessListener { callDoc ->
                        if (callDoc.exists()) {
                            val intent = Intent(this@MainActivity, CallActivity::class.java).apply {
                                putExtra("uid", callDoc.getString("callerUid"))
                                putExtra("name", callDoc.getString("callerName"))
                                putExtra("image", callDoc.getString("callerImage"))
                                putExtra("isVideo", callDoc.getString("type") == "video")
                                putExtra("isIncoming", true)
                                putExtra("channel", callId)
                            }
                            startActivity(intent)
                            homeViewModel.clearCallSignal()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.fetchUsers()
    }
}
