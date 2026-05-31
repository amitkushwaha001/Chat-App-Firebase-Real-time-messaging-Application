@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.example.nexchat.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.nexchat.models.Message
import com.example.nexchat.ui.theme.*
import com.example.nexchat.utils.AudioRecorder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userName: String,
    userStatus: String,
    profileImageUrl: String?,
    messages: List<Message>,
    currentUserId: String,
    targetUserId: String,
    roomId: String,
    onBackClick: () -> Unit,
    onSendClick: (String) -> Unit,
    onAttachClick: (String, Uri) -> Unit,
    onCallClick: (Boolean) -> Unit,
    onClearChat: () -> Unit,
    onDeleteChat: () -> Unit,
    onMoreClick: () -> Unit,
    onEmojiClick: () -> Unit,
    onVoiceSend: (File) -> Unit,
    onEditMessage: (String, String, Long) -> Unit,
    onDeleteMessage: (String, Boolean) -> Unit,
    onTypingStatusChange: (String?) -> Unit,
    onMarkAsSeen: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showAttachDialog by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isMuted by remember { mutableStateOf(false) }
    
    var selectedMessageForAction by remember { mutableStateOf<Message?>(null) }
    var showMessageActions by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val recorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var recordingSeconds by remember { mutableIntStateOf(0) }

    // Typing indicator logic
    LaunchedEffect(messageText) {
        if (messageText.isNotEmpty()) {
            onTypingStatusChange(targetUserId)
        } else {
            onTypingStatusChange(null)
        }
    }

    // Mark messages as seen
    LaunchedEffect(messages) {
        messages.filter { it.senderId != currentUserId && !it.seen }.forEach {
            onMarkAsSeen(it.messageId)
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                kotlinx.coroutines.delay(1000)
                recordingSeconds++
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onAttachClick("image", it) }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onAttachClick("video", it) }
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onAttachClick("audio", it) }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onAttachClick("file", it) }
    }

    val recordPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "audio_record_${System.currentTimeMillis()}.mp3")
            audioFile = file
            recorder.start(file)
            isRecording = true
        }
    }

    val listState = rememberLazyListState()
    val filteredMessages = if (searchQuery.isEmpty()) {
        messages
    } else {
        messages.filter { it.message.contains(searchQuery, ignoreCase = true) }
    }

    if (showEmojiPicker) {
        ModalBottomSheet(
            onDismissRequest = { showEmojiPicker = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            EmojiPickerGrid { emoji ->
                messageText += emoji
                showEmojiPicker = false
            }
        }
    }

    if (showMessageActions) {
        AlertDialog(
            onDismissRequest = { showMessageActions = false },
            title = { Text("Message Actions") },
            text = {
                Column {
                    if (selectedMessageForAction?.senderId == currentUserId && 
                        (Date().time - (selectedMessageForAction?.timestamp ?: 0) <= 2 * 60 * 1000)) {
                        ListItem(
                            headlineContent = { Text("Edit") },
                            leadingContent = { Icon(Icons.Default.Edit, null) },
                            modifier = Modifier.clickable {
                                isEditing = true
                                messageText = selectedMessageForAction?.message ?: ""
                                showMessageActions = false
                            }
                        )
                    }
                    ListItem(
                        headlineContent = { Text("Delete for Me") },
                        leadingContent = { Icon(Icons.Default.Delete, null) },
                        modifier = Modifier.clickable {
                            onDeleteMessage(selectedMessageForAction?.messageId ?: "", false)
                            showMessageActions = false
                        }
                    )
                    if (selectedMessageForAction?.senderId == currentUserId) {
                        ListItem(
                            headlineContent = { Text("Delete for Everyone", color = Color.Red) },
                            leadingContent = { Icon(Icons.Default.DeleteForever, null, tint = Color.Red) },
                            modifier = Modifier.clickable {
                                onDeleteMessage(selectedMessageForAction?.messageId ?: "", true)
                                showMessageActions = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMessageActions = false }) { Text("Cancel") }
            }
        )
    }

    if (showAttachDialog) {
        AlertDialog(
            onDismissRequest = { showAttachDialog = false },
            title = { Text("Select Attachment") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Image") },
                        leadingContent = { Icon(Icons.Default.Image, null, tint = TelegramBlue) },
                        modifier = Modifier.clickable {
                            showAttachDialog = false
                            imagePicker.launch("image/*")
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Video") },
                        leadingContent = { Icon(Icons.Default.VideoFile, null, tint = TelegramBlue) },
                        modifier = Modifier.clickable {
                            showAttachDialog = false
                            videoPicker.launch("video/*")
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Audio") },
                        leadingContent = { Icon(Icons.Default.AudioFile, null, tint = TelegramBlue) },
                        modifier = Modifier.clickable {
                            showAttachDialog = false
                            audioPicker.launch("audio/*")
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Document") },
                        leadingContent = { Icon(Icons.Default.Description, null, tint = TelegramBlue) },
                        modifier = Modifier.clickable {
                            showAttachDialog = false
                            filePicker.launch("*/*")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search messages...", color = Color.White.copy(alpha = 0.6f)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { onMoreClick() }
                        ) {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = userName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = userStatus,
                                    fontSize = 12.sp,
                                    color = TelegramTextSecondary
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = if (isSearching) { { isSearching = false; searchQuery = "" } } else onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (!isSearching) {
                        IconButton(onClick = { onCallClick(false) }) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                        }
                        IconButton(onClick = { onCallClick(true) }) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (isMuted) "Unmute Notifications" else "Mute Notifications") },
                                    onClick = { 
                                        isMuted = !isMuted
                                        showMenu = false 
                                    },
                                    leadingIcon = { Icon(if (isMuted) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Search") },
                                    onClick = { 
                                        showMenu = false
                                        isSearching = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.Search, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear History") },
                                    onClick = { 
                                        showMenu = false
                                        onClearChat()
                                    },
                                    leadingIcon = { Icon(Icons.Default.DeleteSweep, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Chat", color = Color.Red) },
                                    onClick = { 
                                        showMenu = false
                                        onDeleteChat()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramPrimary)
            )
        },
        bottomBar = {
            ChatComposer(
                text = messageText,
                onTextChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        if (isEditing) {
                            onEditMessage(selectedMessageForAction?.messageId ?: "", messageText, selectedMessageForAction?.timestamp ?: 0)
                            isEditing = false
                        } else {
                            onSendClick(messageText)
                        }
                        messageText = ""
                    }
                },
                onAttachClick = { showAttachDialog = true },
                onEmojiClick = { showEmojiPicker = true },
                isRecording = isRecording,
                recordingSeconds = recordingSeconds,
                onVoiceClick = {
                    if (!isRecording) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            val file = File(context.cacheDir, "audio_record_${System.currentTimeMillis()}.mp3")
                            audioFile = file
                            recorder.start(file)
                            isRecording = true
                        } else {
                            recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    } else {
                        recorder.stop()
                        isRecording = false
                        audioFile?.let { onVoiceSend(it) }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                state = listState,
                reverseLayout = true
            ) {
                items(filteredMessages.reversed()) { message ->
                    MessageBubble(
                        message = message, 
                        isCurrentUser = message.senderId == currentUserId,
                        roomId = roomId,
                        onLongClick = {
                            if (!message.isDeleted) {
                                selectedMessageForAction = message
                                showMessageActions = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmojiPickerGrid(onEmojiSelected: (String) -> Unit) {
    val emojis = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
        "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
        "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
        "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬",
        "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗",
        "🤔", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯",
        "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪", "😵", "🤐",
        "🥴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕", "🤑", "🤠", "😈",
        "👿", "👹", "👺", "🤡", "💩", "👻", "💀", "☠️", "👽", "👾"
    )
    Column(modifier = Modifier.fillMaxHeight(0.4f).padding(16.dp)) {
        Text("Emojis", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
fun ChatComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    onEmojiClick: () -> Unit,
    isRecording: Boolean,
    recordingSeconds: Int,
    onVoiceClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = onEmojiClick) {
                Icon(Icons.Default.Face, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            if (isRecording) {
                Row(
                    modifier = Modifier.weight(1f).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%02d:%02d", recordingSeconds / 60, recordingSeconds % 60),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Recording...", color = Color.Red.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            } else {
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    placeholder = { Text("Message", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = TelegramBlue,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 5
                )
            }

            IconButton(onClick = onAttachClick) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            Box(
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp).size(48.dp).clip(CircleShape).background(if (isRecording) Color.Red else TelegramBlue),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = if (text.isBlank()) onVoiceClick else onSendClick) {
                    Icon(
                        imageVector = if (text.isBlank()) (if (isRecording) Icons.Default.Stop else Icons.Default.Mic) else Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean, roomId: String, onLongClick: () -> Unit) {
    val bubbleColor = if (isCurrentUser) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
    val textColor = if (isCurrentUser) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSecondary
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isCurrentUser) 16.dp else 2.dp, bottomEnd = if (isCurrentUser) 2.dp else 16.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.combinedClickable(onClick = {}, onLongClick = onLongClick)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (message.isDeleted) {
                    Text("This message was deleted", color = textColor.copy(alpha = 0.5f), fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                } else {
                    when (message.type) {
                        "image" -> {
                            AsyncImage(
                                model = message.fileUrl, 
                                contentDescription = null, 
                                modifier = Modifier
                                    .sizeIn(maxWidth = 240.dp, maxHeight = 320.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = {
                                            val intent = Intent(context, com.example.nexchat.activities.FullScreenImageActivity::class.java).apply {
                                                putExtra("imageUrl", message.fileUrl)
                                                putExtra("messageId", message.messageId)
                                                putExtra("roomId", roomId)
                                            }
                                            context.startActivity(intent)
                                        },
                                        onLongClick = onLongClick
                                    ), 
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        "video" -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(Uri.parse(message.fileUrl), "video/*")
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        val chooser = Intent.createChooser(intent, "Play Video")
                                        context.startActivity(chooser)
                                    },
                                    onLongClick = onLongClick
                                )
                            ) {
                                AsyncImage(model = message.fileUrl, contentDescription = null, modifier = Modifier.sizeIn(maxWidth = 240.dp, maxHeight = 320.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
                                Icon(Icons.Default.PlayCircle, null, tint = Color.White, modifier = Modifier.size(48.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        "audio" -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.combinedClickable(
                                    onClick = { /* Could potentially play/pause here too */ },
                                    onLongClick = onLongClick
                                )
                            ) {
                                val player = remember { android.media.MediaPlayer() }
                                var isPlaying by remember { mutableStateOf(false) }
                                
                                IconButton(onClick = {
                                    if (!isPlaying) {
                                        player.reset()
                                        player.setDataSource(message.fileUrl)
                                        player.prepareAsync()
                                        player.setOnPreparedListener { 
                                            it.start()
                                            isPlaying = true 
                                        }
                                        player.setOnCompletionListener { isPlaying = false }
                                    } else {
                                        player.stop()
                                        isPlaying = false
                                    }
                                }) {
                                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = textColor)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (message.fileName.isNotEmpty()) message.fileName else "Voice Message",
                                    color = textColor,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        "file" -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(textColor.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(Uri.parse(message.fileUrl), "*/*")
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            val chooser = Intent.createChooser(intent, "Open File")
                                            try {
                                                context.startActivity(chooser)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "No app found to open this file", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onLongClick = onLongClick
                                    )
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Default.Description, null, tint = TelegramBlue, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (message.fileName.isNotEmpty()) message.fileName else "Document",
                                        color = textColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Tap to open",
                                        color = textColor.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                    if (message.message.isNotEmpty() && message.type == "text") {
                        Text(text = message.message, color = textColor, fontSize = 16.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.End).padding(top = 2.dp)) {
                    if (message.isEdited) Text("Edited ", fontSize = 10.sp, color = textColor.copy(alpha = 0.4f))
                    Text(text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp)), fontSize = 10.sp, color = textColor.copy(alpha = 0.6f))
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = if (message.seen) Icons.Default.DoneAll else Icons.Default.Check, contentDescription = null, tint = if (message.seen) TelegramBlue else textColor.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.DarkGray, CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No messages here yet...",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "Send a message or tap the greeting below.",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
