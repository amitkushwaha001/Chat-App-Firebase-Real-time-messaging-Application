package com.example.nexchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nexchat.models.User
import com.example.nexchat.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: User?,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAccountClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onDataStorageClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfileClick() },
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = currentUser?.profileImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop,
                        error = androidx.compose.ui.res.painterResource(com.example.nexchat.R.drawable.ic_user_placeholder)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: "User Name",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.phoneNumber ?: "+1 234 567 890",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        tint = TelegramBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            SettingsCategory(title = "Account") {
                SettingsItem(icon = Icons.Default.Key, title = "Account", onClick = onAccountClick)
                SettingsItem(icon = Icons.Default.Lock, title = "Privacy and Security", onClick = onPrivacyClick)
                SettingsItem(icon = Icons.Default.Notifications, title = "Notifications and Sounds", onClick = onNotificationsClick)
                SettingsItem(icon = Icons.Default.DataUsage, title = "Data and Storage", onClick = onDataStorageClick)
                SettingsItem(icon = Icons.Default.Palette, title = "Appearance", onClick = onAppearanceClick)
            }

            SettingsCategory(title = "Support") {
                SettingsItem(icon = Icons.Default.QuestionAnswer, title = "Ask a Question", onClick = onHelpClick)
                SettingsItem(icon = Icons.Default.Info, title = "NexChat FAQ", onClick = onHelpClick)
                SettingsItem(icon = Icons.Default.Shield, title = "Privacy Policy", onClick = onHelpClick)
            }

            Spacer(modifier = Modifier.height(16.dp))

            ListItem(
                headlineContent = { Text("Logout", color = Color.Red) },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red) },
                modifier = Modifier.clickable { onLogoutClick() },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsCategory(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = title,
            color = TelegramBlue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, color = MaterialTheme.colorScheme.onSurface) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
