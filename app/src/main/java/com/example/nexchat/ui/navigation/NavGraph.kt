package com.example.nexchat.ui.navigation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nexchat.activities.EditProfileActivity
import com.example.nexchat.activities.SearchUserActivity
import com.example.nexchat.activities.SettingsActivity
import com.example.nexchat.models.Message
import com.example.nexchat.models.User
import com.example.nexchat.ui.screens.*
import com.example.nexchat.utils.CloudinaryHelper
import com.example.nexchat.utils.SessionManager
import com.example.nexchat.viewmodel.ChatViewModel
import com.example.nexchat.viewmodel.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: String) = "chat/$userId"
    }
    object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    object Search : Screen("search")
    object Settings : Screen("settings")
    object Contacts : Screen("contacts")
    object EditProfile : Screen("edit_profile")
    object SavedMessages : Screen("saved_messages")
    object Notifications : Screen("notifications")
    object CreateGroup : Screen("create_group")
    object HelpContact : Screen("help_contact")
    object PrivacySecurity : Screen("privacy_security")
    object DataStorage : Screen("data_storage")
}

@Composable
fun NexChatNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val session = SessionManager(context)
    val users by homeViewModel.users.collectAsState()
    val currentUser by homeViewModel.currentUser.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                users = users,
                currentUser = currentUser,
                onUserClick = { user ->
                    navController.navigate(Screen.Chat.createRoute(user.uid))
                },
                onSearchClick = {
                    // Search is now integrated in Home TopBar
                },
                onCameraClick = {
                    Toast.makeText(context, "Status features coming soon!", Toast.LENGTH_SHORT).show()
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onLogoutClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    session.clear()
                    val intent = Intent(context, com.example.nexchat.activities.LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    if (context is android.app.Activity) (context as android.app.Activity).finish()
                },
                onFabClick = {
                    navController.navigate(Screen.Contacts.route)
                },
                onPreviewClick = { user ->
                    navController.navigate(Screen.Profile.createRoute(user.uid))
                },
                onProfileClick = {
                    val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().uid ?: ""
                    navController.navigate(Screen.Profile.createRoute(currentUid))
                },
                onDarkModeToggle = { mode ->
                    session.themeMode = mode
                    Toast.makeText(context, "Theme mode set to $mode. Refreshing UI.", Toast.LENGTH_SHORT).show()
                },
                onContactsClick = {
                    navController.navigate(Screen.Contacts.route)
                },
                onSavedMessagesClick = {
                    navController.navigate(Screen.SavedMessages.route)
                },
                onNewGroupClick = {
                    navController.navigate(Screen.CreateGroup.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() },
                onProfileClick = {
                    navController.navigate(Screen.Profile.createRoute(currentUser?.uid ?: ""))
                },
                onAccountClick = {
                    context.startActivity(Intent(context, EditProfileActivity::class.java))
                },
                onPrivacyClick = {
                    navController.navigate(Screen.PrivacySecurity.route)
                },
                onNotificationsClick = {
                    navController.navigate(Screen.Notifications.route)
                },
                onAppearanceClick = {
                    Toast.makeText(context, "Use Home 3-dot menu for instant theme switch", Toast.LENGTH_LONG).show()
                },
                onDataStorageClick = {
                    navController.navigate(Screen.DataStorage.route)
                },
                onHelpClick = {
                    navController.navigate(Screen.HelpContact.route)
                },
                onLogoutClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    session.clear()
                    val intent = Intent(context, com.example.nexchat.activities.LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    if (context is android.app.Activity) (context as android.app.Activity).finish()
                }
            )
        }

        composable(Screen.PrivacySecurity.route) {
            PrivacySecurityScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.DataStorage.route) {
            DataStorageScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.HelpContact.route) {
            HelpContactScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Contacts.route) {
            ContactsScreen(
                allUsers = users,
                onBackClick = { navController.popBackStack() },
                onUserClick = { user ->
                    navController.navigate(Screen.Chat.createRoute(user.uid))
                },
                onInviteClick = { phone ->
                    Toast.makeText(context, "Invite sent to $phone", Toast.LENGTH_SHORT).show()
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onLogoutClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    session.clear()
                    val intent = Intent(context, com.example.nexchat.activities.LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    if (context is android.app.Activity) (context as android.app.Activity).finish()
                }
            )
        }

        composable(Screen.SavedMessages.route) {
            SavedMessagesScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.CreateGroup.route) {
            NewGroupScreen(
                users = users,
                onBackClick = { navController.popBackStack() },
                onCreateGroup = { name, members ->
                    Toast.makeText(context, "Group '$name' created with ${members.size} members", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Chat.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val chatViewModel: ChatViewModel = hiltViewModel()
            val user = users.find { it.uid == userId } ?: (if (userId == currentUser?.uid) currentUser else null)
            val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().uid ?: ""
            val roomIds = listOf(currentUid, userId).sorted()
            val roomId = "${roomIds[0]}_${roomIds[1]}"
            
            LaunchedEffect(userId) {
                chatViewModel.loadMessages(currentUid, userId)
            }
            
            val messages by chatViewModel.messages.collectAsState()
            
            ChatScreen(
                userName = user?.name ?: "User",
                userStatus = user?.status ?: "offline",
                profileImageUrl = user?.profileImage,
                messages = messages,
                currentUserId = currentUid,
                targetUserId = userId,
                roomId = roomId, // Pass roomId to ChatScreen
                onBackClick = { navController.popBackStack() },
                onSendClick = { messageText ->
                    chatViewModel.sendMessage(currentUid, userId, messageText)
                },
                onAttachClick = { type, uri ->
                    val folder = "NexChat/Attachments/$type"
                    CloudinaryHelper.uploadFile(context, uri, folder) { url ->
                        if (url != null) {
                            chatViewModel.sendMessage(currentUid, userId, "Sent a $type", type, url)
                        } else {
                            Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onCallClick = { isVideo ->
                    val intent = Intent(context, com.example.nexchat.activities.CallActivity::class.java).apply {
                        putExtra("uid", userId)
                        putExtra("name", user?.name ?: "User")
                        putExtra("image", user?.profileImage)
                        putExtra("isVideo", isVideo)
                        putExtra("isIncoming", false)
                        putExtra("channel", roomId)
                    }
                    context.startActivity(intent)
                },
                onClearChat = {
                    chatViewModel.deleteChat(roomId) // Actually clear history logic
                    Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
                },
                onDeleteChat = {
                    chatViewModel.deleteChat(roomId)
                    navController.popBackStack()
                },
                onMoreClick = {
                    navController.navigate(Screen.Profile.createRoute(userId))
                },
                onEmojiClick = {
                    Toast.makeText(context, "Emoji Picker Clicked", Toast.LENGTH_SHORT).show()
                },
                onVoiceSend = { file ->
                    CloudinaryHelper.uploadFile(context, Uri.fromFile(file), "NexChat/Voice") { url ->
                        if (url != null) {
                            chatViewModel.sendMessage(currentUid, userId, "Voice Message", "audio", url)
                        }
                    }
                },
                onEditMessage = { msgId, newText, timestamp ->
                    chatViewModel.editMessage(roomId, msgId, newText, timestamp)
                },
                onDeleteMessage = { msgId, forEveryone ->
                    chatViewModel.deleteMessage(roomId, msgId, forEveryone)
                },
                onTypingStatusChange = { targetId ->
                    chatViewModel.setTypingStatus(currentUid, targetId)
                },
                onMarkAsSeen = { msgId ->
                    chatViewModel.markAsSeen(roomId, msgId)
                }
            )
        }

        composable(Screen.Profile.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val user = users.find { it.uid == userId } ?: (if (userId == currentUser?.uid) currentUser else null)
            val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().uid ?: ""
            val roomIds = listOf(currentUid, userId).sorted()
            val roomId = "${roomIds[0]}_${roomIds[1]}"

            ProfileScreen(
                userId = userId,
                currentUserId = currentUid,
                userName = user?.name ?: "User",
                userStatus = user?.status ?: "offline",
                phoneNumber = user?.phoneNumber ?: "Unknown",
                username = user?.username ?: "user",
                email = user?.email ?: "Not set",
                bio = user?.about ?: "Hey there!",
                lastSeen = user?.lastSeen ?: 0L,
                joinDate = user?.createdAt ?: 0L,
                profileImageUrl = user?.profileImage,
                onBackClick = { navController.popBackStack() },
                onMessageClick = { 
                    if (userId == currentUid) {
                        navController.navigate(Screen.SavedMessages.route)
                    } else {
                        navController.navigate(Screen.Chat.createRoute(userId))
                    }
                },
                onCallClick = { isVideo ->
                    val intent = Intent(context, com.example.nexchat.activities.CallActivity::class.java).apply {
                        putExtra("uid", userId)
                        putExtra("name", user?.name ?: "User")
                        putExtra("image", user?.profileImage)
                        putExtra("isVideo", isVideo)
                        putExtra("isIncoming", false)
                        putExtra("channel", roomId)
                    }
                    context.startActivity(intent)
                },
                onMuteClick = {
                    Toast.makeText(context, "Mute Clicked", Toast.LENGTH_SHORT).show()
                },
                onEditClick = {
                    context.startActivity(Intent(context, EditProfileActivity::class.java))
                },
                onLogoutClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    session.clear()
                    val intent = Intent(context, com.example.nexchat.activities.LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    if (context is android.app.Activity) (context as android.app.Activity).finish()
                }
            )
        }
    }
}
