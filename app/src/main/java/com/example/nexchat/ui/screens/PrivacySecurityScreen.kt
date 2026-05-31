package com.example.nexchat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nexchat.ui.theme.TelegramBlue
import com.example.nexchat.viewmodel.PrivacyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    onBackClick: () -> Unit,
    viewModel: PrivacyViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf<String?>(null) }

    if (showDialog != null) {
        PrivacyOptionDialog(
            title = showDialog!!,
            currentValue = when(showDialog) {
                "Last Seen & Online" -> settings.lastSeen
                "Profile Photos" -> settings.profilePhoto
                "About" -> settings.about
                else -> ""
            },
            onDismiss = { showDialog = null },
            onSelect = { newValue ->
                when(showDialog) {
                    "Last Seen & Online" -> viewModel.updateLastSeen(newValue)
                    "Profile Photos" -> viewModel.updateProfilePhoto(newValue)
                    "About" -> viewModel.updateAbout(newValue)
                }
                showDialog = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy and Security", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
            PrivacyCategory(title = "Privacy") {
                PrivacyItem(title = "Last Seen & Online", value = settings.lastSeen) {
                    showDialog = "Last Seen & Online"
                }
                PrivacyItem(title = "Profile Photos", value = settings.profilePhoto) {
                    showDialog = "Profile Photos"
                }
                PrivacyItem(title = "About", value = settings.about) {
                    showDialog = "About"
                }
                
                ListItem(
                    headlineContent = { Text("Read Receipts", color = MaterialTheme.colorScheme.onSurface) },
                    supportingContent = { Text("If turned off, you won't send or receive Read Receipts. Read Receipts are always sent for group chats.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                    trailingContent = {
                        Switch(checked = settings.readReceipts, onCheckedChange = { viewModel.toggleReadReceipts(it) })
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            PrivacyCategory(title = "Security") {
                PrivacyItem(title = "Two-Step Verification", value = if (settings.twoStepVerification) "On" else "Off") {
                    android.widget.Toast.makeText(context, "Two-step verification setup coming soon", android.widget.Toast.LENGTH_SHORT).show()
                }
                PrivacyItem(title = "Active Sessions", value = "1") {
                    android.widget.Toast.makeText(context, "Active sessions manager coming soon", android.widget.Toast.LENGTH_SHORT).show()
                }
                PrivacyItem(title = "Passcode Lock", value = if (settings.passcodeLock) "On" else "Off") {
                    android.widget.Toast.makeText(context, "Passcode and Biometric lock coming soon", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            PrivacyCategory(title = "Contacts") {
                PrivacyItem(title = "Blocked Users", value = "None") {
                    android.widget.Toast.makeText(context, "Blocked contacts list coming soon", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PrivacyOptionDialog(
    title: String,
    currentValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf("Everyone", "My Contacts", "Nobody")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(selected = option == currentValue, onClick = { onSelect(option) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PrivacyCategory(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = title,
            color = TelegramBlue,
            fontSize = 14.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun PrivacyItem(title: String, value: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, color = MaterialTheme.colorScheme.onSurface) },
        supportingContent = { Text(value, color = TelegramBlue) },
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
