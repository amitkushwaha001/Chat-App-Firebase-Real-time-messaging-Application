package com.example.nexchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nexchat.models.User
import com.example.nexchat.ui.theme.TelegramBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGroupScreen(
    users: List<User>,
    onBackClick: () -> Unit,
    onCreateGroup: (String, List<String>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    val selectedMembers = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Group", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (selectedMembers.isNotEmpty() && groupName.isNotBlank()) {
                FloatingActionButton(
                    onClick = { onCreateGroup(groupName, selectedMembers.toList()) },
                    containerColor = TelegramBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Create")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Group Name Input
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.People, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                TextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    placeholder = { Text("Group Name") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }

            Divider()

            Text(
                "Select Members",
                modifier = Modifier.padding(16.dp),
                color = TelegramBlue,
                fontSize = 14.sp
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(users) { user ->
                    val isSelected = selectedMembers.contains(user.uid)
                    ListItem(
                        headlineContent = { Text(user.name) },
                        supportingContent = { Text("@${user.username}") },
                        leadingContent = {
                            AsyncImage(
                                model = user.profileImage,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray),
                                contentScale = ContentScale.Crop
                            )
                        },
                        trailingContent = {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (it) selectedMembers.add(user.uid)
                                    else selectedMembers.remove(user.uid)
                                }
                            )
                        },
                        modifier = Modifier.clickable {
                            if (isSelected) selectedMembers.remove(user.uid)
                            else selectedMembers.add(user.uid)
                        }
                    )
                }
            }
        }
    }
}
