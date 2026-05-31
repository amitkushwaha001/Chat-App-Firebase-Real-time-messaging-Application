package com.example.nexchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nexchat.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpContactScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Contact", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramPrimary)
            )
        },
        containerColor = TelegramBackground
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            ListItem(
                headlineContent = { Text("Phone", color = Color.White) },
                supportingContent = { Text("8700530415", color = TelegramTextSecondary) },
                leadingContent = { Icon(Icons.Default.Phone, contentDescription = null, tint = TelegramBlue) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            ListItem(
                headlineContent = { Text("Email", color = Color.White) },
                supportingContent = { Text("amitkushwaha200215@gmail.com", color = TelegramTextSecondary) },
                leadingContent = { Icon(Icons.Default.Email, contentDescription = null, tint = TelegramBlue) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = TelegramSecondary)
            TextButton(onClick = { /* Contact Us */ }) { Text("Contact Us", color = TelegramBlue) }
            TextButton(onClick = { /* Bug Report */ }) { Text("Report a Bug", color = TelegramBlue) }
            TextButton(onClick = { /* Feedback */ }) { Text("Send Feedback", color = TelegramBlue) }
            TextButton(onClick = { /* About */ }) { Text("About NexChat", color = TelegramBlue) }
        }
    }
}
