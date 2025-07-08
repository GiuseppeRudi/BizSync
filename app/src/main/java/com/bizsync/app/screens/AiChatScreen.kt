//package com.bizsync.app.screens
//
//import androidx.compose.animation.core.animateFloatAsState
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.material.icons.filled.SmartToy
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import java.text.SimpleDateFormat
//import java.util.*
//
//data class ChatMessage(
//    val id: String = UUID.randomUUID().toString(),
//    val text: String,
//    val isFromUser: Boolean,
//    val timestamp: Date = Date(),
//    val isTyping: Boolean = false
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AIChatScreen(
//    onBackClick: () -> Unit = {}
//) {
//    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
//    var inputText by remember { mutableStateOf("") }
//    var isTyping by remember { mutableStateOf(false) }
//    val listState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    // Messaggio di benvenuto iniziale
//    LaunchedEffect(Unit) {
//        messages = listOf(
//            ChatMessage(
//                text = "Ciao! Sono l'assistente AI di BizSync. Posso aiutarti con informazioni sui dipendenti, turni di lavoro, contratti e molto altro. Come posso esserti utile oggi?",
//                isFromUser = false
//            )
//        )
//    }
//
//    // Auto-scroll quando arrivano nuovi messaggi
//    LaunchedEffect(messages.size) {
//        if (messages.isNotEmpty()) {
//            coroutineScope.launch {
//                listState.animateScrollToItem(messages.size - 1)
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(
//                        Color(0xFFF8F9FA),
//                        Color(0xFFE9ECEF)
//                    )
//                )
//            )
//    ) {
//        // Header
//        TopAppBar(
//            title = {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.SmartToy,
//                        contentDescription = null,
//                        tint = Color(0xFF3498DB),
//                        modifier = Modifier.size(24.dp)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Column {
//                        Text(
//                            text = "AI Assistant",
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF2C3E50),
//                            fontSize = 18.sp
//                        )
//                        Text(
//                            text = if (isTyping) "Sta scrivendo..." else "Online",
//                            color = if (isTyping) Color(0xFFF39C12) else Color(0xFF2ECC71),
//                            fontSize = 12.sp
//                        )
//                    }
//                }
//            },
//            navigationIcon = {
//                IconButton(onClick = onBackClick) {
//                    Icon(
//                        imageVector = Icons.Default.ArrowBack,
//                        contentDescription = "Indietro",
//                        tint = Color(0xFF2C3E50)
//                    )
//                }
//            },
//            colors = TopAppBarDefaults.topAppBarColors(
//                containerColor = Color.White
//            )
//        )
//
//        // Chat messages
//        LazyColumn(
//            state = listState,
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxWidth(),
//            contentPadding = PaddingValues(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            items(messages) { message ->
//                if (message.isTyping) {
//                    TypingIndicator()
//                } else {
//                    MessageBubble(message = message)
//                }
//            }
//        }
//
//        // Input area
//        Surface(
//            color = Color.White,
//            shadowElevation = 8.dp
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                verticalAlignment = Alignment.Bottom
//            ) {
//                OutlinedTextField(
//                    value = inputText,
//                    onValueChange = { inputText = it },
//                    modifier = Modifier.weight(1f),
//                    placeholder = { Text("Scrivi il tuo messaggio...") },
//                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
//                    keyboardActions = KeyboardActions(
//                        onSend = {
//                            if (inputText.isNotBlank()) {
//                                sendMessage(
//                                    text = inputText,
//                                    messages = messages,
//                                    onMessagesUpdate = { newMessages -> messages = newMessages },
//                                    onTypingUpdate = { typing -> isTyping = typing }
//                                )
//                                inputText = ""
//                                keyboardController?.hide()
//                            }
//                        }
//                    ),
//                    shape = RoundedCornerShape(24.dp),
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedBorderColor = Color(0xFF3498DB),
//                        unfocusedBorderColor = Color(0xFFE0E0E0)
//                    )
//                )
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                FloatingActionButton(
//                    onClick = {
//                        if (inputText.isNotBlank()) {
//                            sendMessage(
//                                text = inputText,
//                                messages = messages,
//                                onMessagesUpdate = { newMessages -> messages = newMessages },
//                                onTypingUpdate = { typing -> isTyping = typing }
//                            )
//                            inputText = ""
//                            keyboardController?.hide()
//                        }
//                    },
//                    modifier = Modifier.size(48.dp),
//                    containerColor = Color(0xFF3498DB),
//                    contentColor = Color.White
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Send,
//                        contentDescription = "Invia",
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun MessageBubble(message: ChatMessage) {
//    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
//    ) {
//        if (!message.isFromUser) {
//            // Avatar dell'AI
//            Box(
//                modifier = Modifier
//                    .size(36.dp)
//                    .clip(CircleShape)
//                    .background(Color(0xFF3498DB)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.SmartToy,
//                    contentDescription = null,
//                    tint = Color.White,
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.width(8.dp))
//        }
//
//        Column(
//            modifier = Modifier.widthIn(max = 280.dp)
//        ) {
//            Card(
//                shape = RoundedCornerShape(
//                    topStart = if (message.isFromUser) 18.dp else 4.dp,
//                    topEnd = if (message.isFromUser) 4.dp else 18.dp,
//                    bottomStart = 18.dp,
//                    bottomEnd = 18.dp
//                ),
//                colors = CardDefaults.cardColors(
//                    containerColor = if (message.isFromUser)
//                        Color(0xFF3498DB)
//                    else
//                        Color.White
//                ),
//                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//            ) {
//                Text(
//                    text = message.text,
//                    color = if (message.isFromUser) Color.White else Color(0xFF2C3E50),
//                    modifier = Modifier.padding(12.dp),
//                    fontSize = 16.sp,
//                    lineHeight = 20.sp
//                )
//            }
//
//            Text(
//                text = timeFormat.format(message.timestamp),
//                color = Color(0xFF95A5A6),
//                fontSize = 12.sp,
//                modifier = Modifier.padding(
//                    horizontal = 12.dp,
//                    vertical = 2.dp
//                ),
//                textAlign = if (message.isFromUser) TextAlign.End else TextAlign.Start
//            )
//        }
//
//        if (message.isFromUser) {
//            Spacer(modifier = Modifier.width(8.dp))
//
//            // Avatar dell'utente
//            Box(
//                modifier = Modifier
//                    .size(36.dp)
//                    .clip(CircleShape)
//                    .background(Color(0xFF2ECC71)),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "U",
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 16.sp
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun TypingIndicator() {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.Start
//    ) {
//        // Avatar dell'AI
//        Box(
//            modifier = Modifier
//                .size(36.dp)
//                .clip(CircleShape)
//                .background(Color(0xFF3498DB)),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = Icons.Default.SmartToy,
//                contentDescription = null,
//                tint = Color.White,
//                modifier = Modifier.size(20.dp)
//            )
//        }
//
//        Spacer(modifier = Modifier.width(8.dp))
//
//        Card(
//            shape = RoundedCornerShape(
//                topStart = 4.dp,
//                topEnd = 18.dp,
//                bottomStart = 18.dp,
//                bottomEnd = 18.dp
//            ),
//            colors = CardDefaults.cardColors(containerColor = Color.White),
//            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//        ) {
//            Row(
//                modifier = Modifier.padding(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                repeat(3) { index ->
//                    val alpha by animateFloatAsState(
//                        targetValue = if ((System.currentTimeMillis() / 500) % 3 == index.toLong()) 1f else 0.3f,
//                        label = "typing_dot_$index"
//                    )
//
//                    Box(
//                        modifier = Modifier
//                            .size(8.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF95A5A6).copy(alpha = alpha))
//                    )
//
//                    if (index < 2) {
//                        Spacer(modifier = Modifier.width(4.dp))
//                    }
//                }
//            }
//        }
//    }
//}
//
//// Funzione per simulare la risposta dell'AI
//fun sendMessage(
//    text: String,
//    messages: List<ChatMessage>,
//    onMessagesUpdate: (List<ChatMessage>) -> Unit,
//    onTypingUpdate: (Boolean) -> Unit
//) {
//    // Aggiungi messaggio utente
//    val userMessage = ChatMessage(text = text, isFromUser = true)
//    val updatedMessages = messages + userMessage
//    onMessagesUpdate(updatedMessages)
//
//    // Simula AI che sta scrivendo
//    onTypingUpdate(true)
//
//    // Simula delay di risposta
//    kotlinx.coroutines.GlobalScope.launch {
//        delay(1500) // Simula tempo di elaborazione
//
//        val aiResponse = generateAIResponse(text)
//        val aiMessage = ChatMessage(text = aiResponse, isFromUser = false)
//
//        onMessagesUpdate(updatedMessages + aiMessage)
//        onTypingUpdate(false)
//    }
//}
//
//// Funzione per generare risposte simulate dell'AI
//fun generateAIResponse(userMessage: String): String {
//    return when {
//        userMessage.contains("dipendente", ignoreCase = true) ||
//                userMessage.contains("employee", ignoreCase = true) -> {
//            "Posso aiutarti con la gestione dei dipendenti. Puoi chiedermi informazioni su:\n\n• Elenco dipendenti\n• Dettagli contrattuali\n• Turni di lavoro\n• Assenze\n• Permessi\n\nCosa ti serve nello specifico?"
//        }
//
//        userMessage.contains("turno", ignoreCase = true) ||
//                userMessage.contains("orario", ignoreCase = true) -> {
//            "Per quanto riguarda i turni di lavoro, posso aiutarti con:\n\n• Programmazione turni\n• Modifiche orari\n• Sostituzioni\n• Straordinari\n• Storico presenze\n\nHai qualche turno specifico da gestire?"
//        }
//
//        userMessage.contains("contratto", ignoreCase = true) ||
//                userMessage.contains("ccnl", ignoreCase = true) -> {
//            "Riguardo ai contratti, posso fornirti informazioni su:\n\n• Tipologie contrattuali\n• CCNL applicati\n• Scadenze\n• Rinnovi\n• Documenti richiesti\n\nDi quale aspetto contrattuale hai bisogno?"
//        }
//
//        userMessage.contains("ferie", ignoreCase = true) ||
//                userMessage.contains("permesso", ignoreCase = true) ||
//                userMessage.contains("assenza", ignoreCase = true) -> {
//            "Per le assenze e i permessi posso aiutarti con:\n\n• Richieste ferie\n• Permessi straordinari\n• Malattie\n• Permessi studio\n• Calcolo giorni residui\n\nChe tipo di assenza devi gestire?"
//        }
//
//        userMessage.contains("stipendio", ignoreCase = true) ||
//                userMessage.contains("paga", ignoreCase = true) ||
//                userMessage.contains("salario", ignoreCase = true) -> {
//            "Per quanto riguarda gli stipendi, posso aiutarti con:\n\n• Calcolo retribuzioni\n• Buste paga\n• Contributi\n• Detrazioni\n• Bonus e premi\n\nCosa ti serve sapere sugli stipendi?"
//        }
//
//        userMessage.contains("ciao", ignoreCase = true) ||
//                userMessage.contains("salve", ignoreCase = true) -> {
//            "Ciao! Sono qui per aiutarti con la gestione aziendale. Posso rispondere a domande su dipendenti, turni, contratti, permessi e molto altro. Come posso esserti utile?"
//        }
//
//        userMessage.contains("grazie", ignoreCase = true) -> {
//            "Prego! Sono sempre qui per aiutarti. Se hai altre domande non esitare a chiedere!"
//        }
//
//        else -> {
//            "Ho capito la tua richiesta. Posso aiutarti con vari aspetti della gestione aziendale come:\n\n• Gestione dipendenti\n• Pianificazione turni\n• Contratti e CCNL\n• Permessi e assenze\n• Stipendi e buste paga\n\nPotresti essere più specifico così posso darti un aiuto mirato?"
//        }
//    }
//}