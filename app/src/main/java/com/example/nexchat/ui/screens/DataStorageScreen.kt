package com.example.nexchat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nexchat.ui.theme.TelegramBlue
import com.example.nexchat.viewmodel.StorageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStorageScreen(
    onBackClick: () -> Unit,
    viewModel: StorageViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data and Storage", color = MaterialTheme.colorScheme.onBackground) },
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
            StorageCategory(title = "Usage") {
                StorageItem(title = "Network Usage", value = "0 B") { 
                    android.widget.Toast.makeText(context, "Network usage stats coming soon", android.widget.Toast.LENGTH_SHORT).show()
                }
                StorageItem(title = "Storage Usage", value = "0 B") { 
                    android.widget.Toast.makeText(context, "Storage analysis coming soon", android.widget.Toast.LENGTH_SHORT).show()
                }
                
                ListItem(
                    headlineContent = { Text("Clear Cache", color = Color.Red) },
                    supportingContent = { Text("Delete all cached files from your device.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                    modifier = Modifier.clickable {
                        context.cacheDir.deleteRecursively()
                        android.widget.Toast.makeText(context, "Cache cleared", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            StorageCategory(title = "Automatic Media Download") {
                StorageItem(title = "When Using Mobile Data", value = settings.autoDownloadMobile.joinToString(", ").ifEmpty { "No Media" }) { 
                    android.widget.Toast.makeText(context, "Mobile data settings coming soon", android.widget.Toast.LENGTH_SHORT).show()
                }
                StorageItem(title = "When Connected on Wi-Fi", value = settings.autoDownloadWifi.joinToString(", ").ifEmpty { "No Media" }) { 
                    android.widget.Toast.makeText(context, "Wi-Fi settings coming soon", android.widget.Toast.LENGTH_SHORT).show()
                }
                StorageItem(title = "When Roaming", value = settings.autoDownloadRoaming.joinToString(", ").ifEmpty { "No Media" }) { 
                    android.widget.Toast.makeText(context, "Roaming settings coming soon", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            StorageCategory(title = "Media Quality") {
                ListItem(
                    headlineContent = { Text("Data Saver", color = MaterialTheme.colorScheme.onSurface) },
                    supportingContent = { Text("Reduce data usage for calls and media uploads.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                    trailingContent = {
                        Switch(checked = settings.dataSaver, onCheckedChange = { viewModel.toggleDataSaver(it) })
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StorageCategory(title: String, content: @Composable ColumnScope.() -> Unit) {
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
fun StorageItem(title: String, value: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, color = MaterialTheme.colorScheme.onSurface) },
        supportingContent = { Text(value, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
