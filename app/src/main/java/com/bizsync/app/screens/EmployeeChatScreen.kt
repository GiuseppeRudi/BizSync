package com.bizsync.app.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Data classes
data class Employee(
    val id: String,
    val name: String,
    val department: Department,
    val role: String,
    val avatar: String = ""
)

data class Department(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

data class ChatRoom(
    val id: String,
    val name: String,
    val type: ChatRoomType,
    val department: Department? = null,
    val members: List<Employee> = emptyList(),
    val lastMessage: ChatMessage? = null,
    val unreadCount: Int = 0
)

enum class ChatRoomType {
    GLOBAL, DEPARTMENT
}

enum class MessageType {
    TEXT, ANNOUNCEMENT, SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sender: Employee,
    val timestamp: Date = Date(),
    val type: MessageType = MessageType.TEXT,
    val isRead: Boolean = false
)

// Dati di esempio
val sampleDepartments = listOf(
    Department("1", "Risorse Umane", Icons.Default.People, Color(0xFF3498DB)),
    Department("2", "IT", Icons.Default.Computer, Color(0xFF9B59B6)),
    Department("3", "Marketing", Icons.Default.Campaign, Color(0xFFE74C3C)),
    Department("4", "Vendite", Icons.Default.TrendingUp, Color(0xFF2ECC71)),
    Department("5", "Produzione", Icons.Default.Campaign, Color(0xFFF39C12))
)

val sampleEmployees = listOf(
    Employee("1", "Marco Rossi", sampleDepartments[0], "HR Manager"),
    Employee("2", "Anna Bianchi", sampleDepartments[1], "Developer"),
    Employee("3", "Luigi Verdi", sampleDepartments[2], "Marketing Specialist"),
    Employee("4", "Sara Neri", sampleDepartments[3], "Sales Manager"),
    Employee("5", "Paolo Blu", sampleDepartments[4], "Production Lead"),
    Employee("6", "Giulia Rosa", sampleDepartments[0], "HR Assistant"),
    Employee("7", "Davide Giallo", sampleDepartments[1], "System Admin")
)

val currentUser = sampleEmployees[0] // Marco Rossi (HR Manager)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeChatScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedChatRoom by remember { mutableStateOf<ChatRoom?>(null) }
    var chatRooms by remember { mutableStateOf(initializeChatRooms()) }
    var messages by remember { mutableStateOf(mapOf<String, List<ChatMessage>>()) }

    // Inizializza i messaggi per ogni chat room
    LaunchedEffect(Unit) {
        messages = initializeMessages()
    }

    if (selectedChatRoom == null) {
        ChatRoomListScreen(
            chatRooms = chatRooms,
            onChatRoomClick = { chatRoom ->
                selectedChatRoom = chatRoom
                // Segna come letti i messaggi
                chatRooms = chatRooms.map { room ->
                    if (room.id == chatRoom.id) {
                        room.copy(unreadCount = 0)
                    } else room
                }
            },
            onBackClick = onBackClick
        )
    } else {
        ChatRoomScreen(
            chatRoom = selectedChatRoom!!,
            messages = messages[selectedChatRoom!!.id] ?: emptyList(),
            onMessageSent = { message ->
                val roomId = selectedChatRoom!!.id
                val currentMessages = messages[roomId] ?: emptyList()
                messages = messages.toMutableMap().apply {
                    put(roomId, currentMessages + message)
                }

                // Aggiorna ultimo messaggio nella chat room
                chatRooms = chatRooms.map { room ->
                    if (room.id == roomId) {
                        room.copy(lastMessage = message)
                    } else room
                }
            },
            onBackClick = { selectedChatRoom = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomListScreen(
    chatRooms: List<ChatRoom>,
    onChatRoomClick: (ChatRoom) -> Unit,
    onBackClick: () -> Unit
) {
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
        // Header
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color(0xFF3498DB),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chat Aziendale",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        fontSize = 18.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color(0xFF2C3E50)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatRooms) { chatRoom ->
                ChatRoomItem(
                    chatRoom = chatRoom,
                    onClick = { onChatRoomClick(chatRoom) }
                )
            }
        }
    }
}

@Composable
fun ChatRoomItem(
    chatRoom: ChatRoom,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar/Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (chatRoom.type == ChatRoomType.GLOBAL)
                            Color(0xFF3498DB)
                        else
                            chatRoom.department?.color ?: Color(0xFF95A5A6)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (chatRoom.type == ChatRoomType.GLOBAL)
                        Icons.Default.Public
                    else
                        chatRoom.department?.icon ?: Icons.Default.Group,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenuto principale
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chatRoom.name,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        fontSize = 16.sp
                    )

                    if (chatRoom.lastMessage != null) {
                        Text(
                            text = timeFormat.format(chatRoom.lastMessage.timestamp),
                            color = Color(0xFF95A5A6),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chatRoom.lastMessage?.let { message ->
                            when (message.type) {
                                MessageType.ANNOUNCEMENT -> "📢 ${message.text}"
                                MessageType.SYSTEM -> "🔔 ${message.text}"
                                MessageType.TEXT -> "${message.sender.name}: ${message.text}"
                            }
                        } ?: "Nessun messaggio",
                        color = Color(0xFF7F8C8D),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (chatRoom.unreadCount > 0) {
                        Badge(
                            containerColor = Color(0xFFE74C3C)
                        ) {
                            Text(
                                text = chatRoom.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    chatRoom: ChatRoom,
    messages: List<ChatMessage>,
    onMessageSent: (ChatMessage) -> Unit,
    onBackClick: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var showMessageTypeDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-scroll quando arrivano nuovi messaggi
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

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
        // Header
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (chatRoom.type == ChatRoomType.GLOBAL)
                                    Color(0xFF3498DB)
                                else
                                    chatRoom.department?.color ?: Color(0xFF95A5A6)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (chatRoom.type == ChatRoomType.GLOBAL)
                                Icons.Default.Public
                            else
                                chatRoom.department?.icon ?: Icons.Default.Group,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = chatRoom.name,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${chatRoom.members.size} membri",
                            color = Color(0xFF7F8C8D),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color(0xFF2C3E50)
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { /* Mostra info chat */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info chat",
                        tint = Color(0xFF2C3E50)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // Messaggi
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                EmployeeMessageBubble(message = message, currentUser = currentUser)
            }
        }

        // Input area
        Surface(
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Scrivi un messaggio...") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                sendEmployeeMessage(
                                    text = inputText,
                                    sender = currentUser,
                                    type = MessageType.TEXT,
                                    onMessageSent = onMessageSent
                                )
                                inputText = ""
                                keyboardController?.hide()
                            }
                        }
                    ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3498DB),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Pulsante per annunci (solo per manager)
                if (currentUser.role.contains("Manager", ignoreCase = true)) {
                    FloatingActionButton(
                        onClick = { showMessageTypeDialog = true },
                        modifier = Modifier.size(48.dp),
                        containerColor = Color(0xFFF39C12),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Annuncio",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            sendEmployeeMessage(
                                text = inputText,
                                sender = currentUser,
                                type = MessageType.TEXT,
                                onMessageSent = onMessageSent
                            )
                            inputText = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = Color(0xFF3498DB),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Invia",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Dialog per tipo di messaggio
    if (showMessageTypeDialog) {
        AlertDialog(
            onDismissRequest = { showMessageTypeDialog = false },
            title = { Text("Tipo di messaggio") },
            text = {
                Column {
                    Text("Seleziona il tipo di messaggio da inviare:")
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (inputText.isNotBlank()) {
                                    sendEmployeeMessage(
                                        text = inputText,
                                        sender = currentUser,
                                        type = MessageType.ANNOUNCEMENT,
                                        onMessageSent = onMessageSent
                                    )
                                    inputText = ""
                                    showMessageTypeDialog = false
                                }
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFFF39C12)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Annuncio importante")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMessageTypeDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
fun EmployeeMessageBubble(
    message: ChatMessage,
    currentUser: Employee
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val isFromCurrentUser = message.sender.id == currentUser.id

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromCurrentUser) {
            // Avatar del mittente
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(message.sender.department.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.sender.name.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // Nome del mittente (solo se non è l'utente corrente)
            if (!isFromCurrentUser) {
                Text(
                    text = message.sender.name,
                    color = Color(0xFF7F8C8D),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }

            Card(
                shape = RoundedCornerShape(
                    topStart = if (isFromCurrentUser) 18.dp else 4.dp,
                    topEnd = if (isFromCurrentUser) 4.dp else 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = when (message.type) {
                        MessageType.ANNOUNCEMENT -> Color(0xFFFFF3CD)
                        MessageType.SYSTEM -> Color(0xFFD4EDDA)
                        MessageType.TEXT -> if (isFromCurrentUser)
                            Color(0xFF3498DB)
                        else
                            Color.White
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Icona per annunci
                    if (message.type == MessageType.ANNOUNCEMENT) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color(0xFFF39C12),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ANNUNCIO",
                                color = Color(0xFFF39C12),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = message.text,
                        color = when (message.type) {
                            MessageType.ANNOUNCEMENT -> Color(0xFF856404)
                            MessageType.SYSTEM -> Color(0xFF155724)
                            MessageType.TEXT -> if (isFromCurrentUser) Color.White else Color(0xFF2C3E50)
                        },
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Text(
                text = timeFormat.format(message.timestamp),
                color = Color(0xFF95A5A6),
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 2.dp
                ),
                textAlign = if (isFromCurrentUser) TextAlign.End else TextAlign.Start
            )
        }

        if (isFromCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))

            // Avatar dell'utente corrente
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(currentUser.department.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser.name.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// Funzioni di supporto
fun initializeChatRooms(): List<ChatRoom> {
    return listOf(
        ChatRoom(
            id = "global",
            name = "Chat Generale",
            type = ChatRoomType.GLOBAL,
            members = sampleEmployees,
            lastMessage = ChatMessage(
                text = "Benvenuti nella chat aziendale!",
                sender = sampleEmployees[0],
                type = MessageType.ANNOUNCEMENT
            ),
            unreadCount = 3
        )
    ) + sampleDepartments.map { department ->
        ChatRoom(
            id = department.id,
            name = department.name,
            type = ChatRoomType.DEPARTMENT,
            department = department,
            members = sampleEmployees.filter { it.department.id == department.id },
            lastMessage = ChatMessage(
                text = "Riunione di team domani alle 10:00",
                sender = sampleEmployees.first { it.department.id == department.id },
                type = MessageType.TEXT
            ),
            unreadCount = if (department.id == "1") 1 else 0
        )
    }
}

fun initializeMessages(): Map<String, List<ChatMessage>> {
    return mapOf(
        "global" to listOf(
            ChatMessage(
                text = "Benvenuti nella chat aziendale di BizSync! Qui potete condividere aggiornamenti e comunicazioni importanti.",
                sender = sampleEmployees[0],
                type = MessageType.ANNOUNCEMENT
            ),
            ChatMessage(
                text = "Ciao a tutti! Sono nuovo nel team IT",
                sender = sampleEmployees[6],
                type = MessageType.TEXT
            ),
            ChatMessage(
                text = "Benvenuto Davide! Se hai domande non esitare a chiedere",
                sender = sampleEmployees[1],
                type = MessageType.TEXT
            )
        ),
        "1" to listOf(
            ChatMessage(
                text = "Riunione di team domani alle 10:00 in sala conferenze",
                sender = sampleEmployees[0],
                type = MessageType.TEXT
            ),
            ChatMessage(
                text = "Perfetto, sarò presente!",
                sender = sampleEmployees[5],
                type = MessageType.TEXT
            )
        ),
        "2" to listOf(
            ChatMessage(
                text = "Aggiornamento sistema previsto per questo weekend",
                sender = sampleEmployees[6],
                type = MessageType.ANNOUNCEMENT
            )
        )
    )
}

fun sendEmployeeMessage(
    text: String,
    sender: Employee,
    type: MessageType,
    onMessageSent: (ChatMessage) -> Unit
) {
    val message = ChatMessage(
        text = text,
        sender = sender,
        type = type
    )
    onMessageSent(message)
}