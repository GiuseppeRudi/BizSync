package com.bizsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.bizsync.domain.model.User
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bizsync.domain.constants.enumClass.ChatType
import com.bizsync.domain.model.*
import com.bizsync.ui.components.ChatRoomItem
import com.bizsync.ui.components.NewPrivateChatDialog
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {


    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    val dipartimenti = userState.azienda.areeLavoro
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedChat by viewModel.selectedChat.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()

    val employees = uiState.allEmployees
    val currentUser = uiState.currentUser

    LaunchedEffect(Unit)
    { viewModel.loadUsers(userState.user) }




    LaunchedEffect( employees, Unit) {
        viewModel.initializeChat(currentUser, employees)
    }

    if (selectedChat == null) {
        ChatRoomListScreen(
            dipartimenti = dipartimenti,
            currentUser = currentUser,
            chats = uiState.chats,
            employees = employees,
            onChatRoomClick = { chat ->
                viewModel.selectChat(chat)
            },
            onCreatePrivateChat = { otherUser ->
                viewModel.createPrivateChat(otherUser)
            },
            onBackClick = onBackClick
        )
    } else {
        ChatRoomScreen(
            currentUser = currentUser,
            chatRoom = selectedChat!!,
            messages = messages,
            onMessageSent = { content, tipo ->
                viewModel.sendMessage(content, tipo)
            },
            onBackClick = {
                viewModel.deselectChat()
            },
            dipartimenti = dipartimenti
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomListScreen(
    dipartimenti : List<AreaLavoro>,
    currentUser: User,
    chats: List<Chat>,
    employees: List<User>,
    onChatRoomClick: (Chat) -> Unit,
    onCreatePrivateChat: (User) -> Unit,
    onBackClick: () -> Unit
) {
    var showNewChatDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE9ECEF)
                    )
                )
            )
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = Color(0xFF3498DB),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Chat Aziendale",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (currentUser.isManager) "Manager" else currentUser.dipartimento,
                            color = Color(0xFF7F8C8D),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color(0xFF2C3E50)
                    )
                }
            },
            actions = {
                IconButton(onClick = { showNewChatDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Nuova chat privata",
                        tint = Color(0xFF2C3E50)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        if (chats.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sezione Chat Generale
                item {
                    Text(
                        text = "CHAT GENERALE",
                        color = Color(0xFF7F8C8D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(chats.filter { it.tipo == ChatType.GENERALE }) { chat ->
                    ChatRoomItem(
                        chat = chat,
                        dipartimenti = dipartimenti,
                        employees = employees, // ← AGGIUNGI
                        currentUser = currentUser, // ← AGGIUNGI
                        onClick = { onChatRoomClick(chat) }
                    )
                }


                // Sezione Chat Dipartimenti (visibile per manager o dipendenti del dipartimento)
                val departmentChats = if (currentUser.isManager) {
                    chats.filter { it.tipo == ChatType.DIPARTIMENTO }
                } else {
                    chats.filter { it.tipo == ChatType.DIPARTIMENTO && it.dipartimento == currentUser.dipartimento }
                }

                if (departmentChats.isNotEmpty()) {
                    item {
                        Text(
                            text = "CHAT DIPARTIMENTI",
                            color = Color(0xFF7F8C8D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(departmentChats) { chat ->
                        ChatRoomItem(
                            chat = chat,
                            dipartimenti = dipartimenti,
                            employees = employees, // ← AGGIUNGI
                            currentUser = currentUser, // ← AGGIUNGI
                            onClick = { onChatRoomClick(chat) }
                        )
                    }
                }

                // Sezione Chat Private
                val privateChats = chats.filter { it.tipo == ChatType.PRIVATA }
                if (privateChats.isNotEmpty()) {
                    item {
                        Text(
                            text = "CHAT PRIVATE",
                            color = Color(0xFF7F8C8D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(privateChats) { chat ->
                        ChatRoomItem(
                            chat = chat,
                            dipartimenti = dipartimenti,
                            employees = employees, // ← AGGIUNGI
                            currentUser = currentUser, // ← AGGIUNGI
                            onClick = { onChatRoomClick(chat) }
                        )
                    }
                }
            }
        }
    }

    // Dialog per nuova chat privata
    if (showNewChatDialog) {
        NewPrivateChatDialog(
            currentUser = currentUser,
            employees = employees.filter { it.uid != currentUser.uid },
            onUserSelected = { user ->
                onCreatePrivateChat(user)
                showNewChatDialog = false
            },
            onDismiss = { showNewChatDialog = false }
        )
    }
}


