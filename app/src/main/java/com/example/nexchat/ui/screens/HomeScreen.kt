package com.example.nexchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun HomeScreen(
    users: List<User>,
    currentUser: User?,
    onUserClick: (User) -> Unit,
    onSearchClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onFabClick: () -> Unit,
    onPreviewClick: (User) -> Unit,
    onProfileClick: () -> Unit,
    onDarkModeToggle: (String) -> Unit,
    onContactsClick: () -> Unit,
    onSavedMessagesClick: () -> Unit,
    onNewGroupClick: () -> Unit
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val filteredUsers = if (searchQuery.isEmpty()) {
        users
    } else {
        users.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.username.contains(searchQuery, ignoreCase = true) 
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                TopAppBar(
                    title = {
                        if (isSearchActive) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search chats...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = TelegramBlue,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                ),
                                singleLine = true,
                                leadingIcon = {
                                    IconButton(onClick = { isSearchActive = false; searchQuery = "" }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            )
                        } else {
                            Text(
                                text = "NexChat",
                                color = TelegramBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                    },
                    actions = {
                        if (!isSearchActive) {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
                            }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("New Group") },
                                        onClick = { 
                                            showMenu = false
                                            onNewGroupClick()
                                        },
                                        leadingIcon = { Icon(Icons.Default.GroupAdd, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Saved Messages") },
                                        onClick = { 
                                            showMenu = false
                                            onSavedMessagesClick()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null) }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Dark Mode") },
                                        onClick = { 
                                            onDarkModeToggle("dark")
                                            showMenu = false 
                                        },
                                        leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Light Mode") },
                                        onClick = { 
                                            onDarkModeToggle("light")
                                            showMenu = false 
                                        },
                                        leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("System Default") },
                                        onClick = { 
                                            onDarkModeToggle("system")
                                            showMenu = false 
                                        },
                                        leadingIcon = { Icon(Icons.Default.SettingsBrightness, contentDescription = null) }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Logout", color = Color.Red) },
                                        onClick = { 
                                            showMenu = false
                                            onLogoutClick()
                                        },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red) }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )

                if (selectedNavIndex == 0 && !isSearchActive) {
                    SearchBar(onSearchClick = { isSearchActive = true })
                }
            }
        },
        floatingActionButton = {
            if (selectedNavIndex == 0 || selectedNavIndex == 1) {
                FloatingActionButton(
                    onClick = onFabClick,
                    containerColor = TelegramBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "New Chat")
                }
            }
        },
        bottomBar = {
            BottomNavPill(
                selectedIndex = selectedNavIndex,
                onItemClick = { index -> 
                    selectedNavIndex = index 
                    if (index == 0) { /* Stay on MainContent */ }
                    // Screen-specific navigation for sub-screens if not using inner Pager
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedNavIndex) {
                0 -> MainContent(filteredUsers, onUserClick, onPreviewClick)
                1 -> { /* Integrated in ContactsScreen via NavGraph */ }
                2 -> { /* Integrated in SettingsScreen via NavGraph */ }
                3 -> { /* Integrated in ProfileScreen via NavGraph */ }
            }
            
            // If they are selected, we should navigate. 
            // BUT, since we use a floating pill on Home, we need to handle the switch.
            // Let's use SideEffect or simple LaunchedEffect to navigate when index changes.
            LaunchedEffect(selectedNavIndex) {
                when(selectedNavIndex) {
                    1 -> onContactsClick()
                    2 -> onSettingsClick()
                    3 -> onProfileClick()
                }
            }
        }
    }
}

@Composable
fun SearchBar(onSearchClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onSearchClick() },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search Chats",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun BottomNavPill(selectedIndex: Int, onItemClick: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(icon = Icons.Default.Email, isSelected = selectedIndex == 0, onClick = { onItemClick(0) })
                BottomNavItem(icon = Icons.Default.People, isSelected = selectedIndex == 1, onClick = { onItemClick(1) })
                BottomNavItem(icon = Icons.Default.Build, isSelected = selectedIndex == 2, onClick = { onItemClick(2) })
                BottomNavItem(icon = Icons.Default.RemoveRedEye, isSelected = selectedIndex == 3, onClick = { onItemClick(3) })
            }
        }
    }
}

@Composable
fun MainContent(users: List<User>, onUserClick: (User) -> Unit, onPreviewClick: (User) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Your Contacts on NexChat",
                color = TelegramBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(users) { user ->
            ChatListItem(user = user, onClick = { onUserClick(user) }, onPreviewClick = { onPreviewClick(user) })
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        color = if (isSelected) TelegramBlue.copy(alpha = 0.15f) else Color.Transparent,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) TelegramBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ChatListItem(user: User, onClick: () -> Unit, onPreviewClick: (User) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.profileImage,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { onPreviewClick(user) },
            contentScale = ContentScale.Crop,
            error = androidx.compose.ui.res.painterResource(com.example.nexchat.R.drawable.ic_user_placeholder)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (user.name.isNotEmpty()) user.name else "@${user.username}",
                    color = TelegramBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "12:00 PM",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
            Text(
                text = user.about,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
