package com.bizsync.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.domain.constants.enumClass.ChatType
import com.bizsync.domain.constants.enumClass.MessageType
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Chat
import com.bizsync.domain.model.Message
import com.bizsync.domain.model.User
import com.bizsync.ui.components.MessageBubble
import com.bizsync.ui.components.getDepartmentColor
import com.bizsync.ui.components.getDepartmentIcon
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    dipartimenti : List<AreaLavoro>,
    currentUser: User,
    chatRoom: Chat,
    messages: List<Message>,
    onMessageSent: (String, MessageType) -> Unit,
    onBackClick: () -> Unit
) {

    val dipartimento = dipartimenti.firstOrNull { it.id == chatRoom.dipartimentoId }

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
                                when (chatRoom.tipo) {
                                    ChatType.GENERALE -> Color(0xFF3498DB)
                                    ChatType.DIPARTIMENTO -> getDepartmentColor(chatRoom.dipartimentoId, dipartimenti )
                                    ChatType.PRIVATA -> Color(0xFF95A5A6)
                                    else -> { Color(0xFF95A5A6)}
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (chatRoom.tipo) {
                                ChatType.GENERALE -> Icons.Default.Public
                                ChatType.DIPARTIMENTO -> getDepartmentIcon(chatRoom.dipartimentoId, dipartimenti )
                                ChatType.PRIVATA -> Icons.Default.Person
                                else -> { Icons.Default.Person}
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = chatRoom.nome,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                            fontSize = 16.sp
                        )
                        Text(
                            text = when (chatRoom.tipo) {
                                ChatType.GENERALE -> "Tutti i dipendenti"
                                ChatType.DIPARTIMENTO -> "Dipartimento ${chatRoom.dipartimentoId}"
                                ChatType.PRIVATA -> "Chat privata"
                                else -> {"Chat privata"}
                            },
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
                MessageBubble(
                    message = message,
                    isFromCurrentUser = message.senderId == currentUser.uid
                )
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
                                onMessageSent(inputText, MessageType.TEXT)
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
                if (currentUser.isManager) {
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
                            onMessageSent(inputText, MessageType.TEXT)
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

    // Dialog per tipo di messaggio (solo per manager)
    if (showMessageTypeDialog && currentUser.isManager) {
        AlertDialog(
            onDismissRequest = { showMessageTypeDialog = false },
            title = { Text("Tipo di messaggio") },
            text = {
                Column {
                    Text("Vuoi inviare questo messaggio come annuncio importante?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = inputText,
                        color = Color(0xFF7F8C8D),
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onMessageSent(inputText, MessageType.ANNOUNCEMENT)
                            inputText = ""
                            showMessageTypeDialog = false
                        }
                    }
                ) {
                    Text("Invia come Annuncio")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMessageTypeDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}


