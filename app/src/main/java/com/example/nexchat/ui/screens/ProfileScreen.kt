package com.example.nexchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nexchat.ui.theme.*
import com.example.nexchat.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    currentUserId: String,
    userName: String,
    userStatus: String,
    phoneNumber: String,
    username: String,
    email: String,
    bio: String,
    lastSeen: Long,
    joinDate: Long,
    profileImageUrl: String?,
    onBackClick: () -> Unit,
    onMessageClick: () -> Unit,
    onCallClick: (Boolean) -> Unit,
    onMuteClick: () -> Unit,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val isOwnProfile = userId == currentUserId
    val context = LocalContext.current
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    val mutedUsers by viewModel.mutedUsers.collectAsState()
    val isMuted = mutedUsers.containsKey(userId)

    var showMenu by remember { mutableStateOf(false) }
    var showMuteDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }

    if (showMuteDialog) {
        val options = listOf("8 Hours", "1 Week", "Always")
        AlertDialog(
            onDismissRequest = { showMuteDialog = false },
            title = { Text("Mute Notifications") },
            text = {
                Column {
                    options.forEach { option ->
                        ListItem(
                            headlineContent = { Text(option) },
                            modifier = Modifier.clickable {
                                viewModel.muteUser(userId, option)
                                android.widget.Toast.makeText(context, "Muted for $option", android.widget.Toast.LENGTH_SHORT).show()
                                showMuteDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMuteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text("Block User?") },
            text = { Text("Blocked users will not be able to call you or send you messages.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.blockUser(userId)
                    android.widget.Toast.makeText(context, "User Blocked", android.widget.Toast.LENGTH_SHORT).show()
                    showBlockDialog = false
                }) { Text("Block", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            if (isOwnProfile) {
                                DropdownMenuItem(
                                    text = { Text("Edit Profile") },
                                    onClick = { 
                                        showMenu = false
                                        onEditClick()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Logout", color = Color.Red) },
                                    onClick = { 
                                        showMenu = false
                                        onLogoutClick()
                                    },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red) }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Report User") },
                                    onClick = { 
                                        showMenu = false
                                        android.widget.Toast.makeText(context, "Report sent", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Report, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Block User", color = Color.Red) },
                                    onClick = { 
                                        showMenu = false
                                        showBlockDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red) }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        error = androidx.compose.ui.res.painterResource(com.example.nexchat.R.drawable.ic_user_placeholder),
                        contentScale = ContentScale.Crop
                    )
                    if (isOwnProfile) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(TelegramBlue)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (isOwnProfile) {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = TelegramBlue, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Text(
                    text = if (userStatus == "Online") "Online" else "Last seen ${if(lastSeen > 0) timeSdf.format(Date(lastSeen)) else "recently"}",
                    fontSize = 14.sp,
                    color = TelegramBlue
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileActionItem(icon = Icons.Default.Chat, label = "Message", onClick = onMessageClick)
                ProfileActionItem(
                    icon = if(isMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications, 
                    label = if(isMuted) "Unmute" else "Mute", 
                    onClick = {
                        if(isMuted) viewModel.unmuteUser(userId) else showMuteDialog = true
                    }
                )
                ProfileActionItem(icon = Icons.Default.Call, label = "Call", onClick = { onCallClick(false) })
                ProfileActionItem(icon = Icons.Default.Videocam, label = "Video", onClick = { onCallClick(true) })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow(label = "Mobile", value = phoneNumber)
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(label = "Email", value = email)
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    
                    if (isOwnProfile) {
                        InfoRowWithEdit(label = "Bio", value = bio, onEdit = onEditClick)
                    } else {
                        InfoRow(label = "Bio", value = bio)
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    
                    if (isOwnProfile) {
                        InfoRowWithEdit(label = "Username", value = "@$username", onEdit = onEditClick)
                    } else {
                        InfoRow(label = "Username", value = "@$username")
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(label = "Joined", value = if (joinDate > 0) sdf.format(Date(joinDate)) else "Unknown")
                }
            }
        }
    }
}

@Composable
fun InfoRowWithEdit(label: String, value: String, onEdit: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = value, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            Text(text = label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun ProfileActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
        ) {
            Icon(icon, contentDescription = label, tint = TelegramBlue)
        }
        Text(text = label, color = TelegramBlue, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(text = value, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
        Text(text = label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}
