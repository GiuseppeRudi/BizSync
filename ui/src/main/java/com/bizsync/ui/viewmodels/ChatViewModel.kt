package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.enumClass.MessageType
import com.bizsync.domain.model.*
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.model.ChatUiState
import com.bizsync.ui.model.UserUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.usecases.CreatePrivateChatUseCase
import com.bizsync.domain.usecases.InitializeChatUseCase
import com.bizsync.domain.usecases.LoadChatsUseCase
import com.bizsync.domain.usecases.LoadMessagesUseCase
import com.bizsync.domain.usecases.LoadUsersUseCase
import com.bizsync.domain.usecases.SendMessageUseCase
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val loadUsersUseCase: LoadUsersUseCase,
    private val initializeChatUseCase: InitializeChatUseCase,
    private val loadChatsUseCase: LoadChatsUseCase,
    private val loadMessagesUseCase: LoadMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val createPrivateChatUseCase: CreatePrivateChatUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _selectedChat = MutableStateFlow<Chat?>(null)
    val selectedChat: StateFlow<Chat?> = _selectedChat.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var chatListJob: kotlinx.coroutines.Job? = null
    private var messagesJob: kotlinx.coroutines.Job? = null

    fun loadUsers(user: UserUi) {
        Log.d(TAG, "🔄 Loading users for: ${user.nome} ${user.cognome}")
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // ✅ Usa Use Case invece di DAO diretto
                when (val result = loadUsersUseCase()) {
                    is Resource.Success -> {
                        val domainUser = user.toDomain()
                        Log.d(TAG, "✅ Users loaded: ${result.data.size} employees")

                        _uiState.update {
                            it.copy(
                                allEmployees = result.data,
                                isLoading = false,
                                currentUser = domainUser
                            )
                        }
                    }

                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error loading users: ${result.message}")
                        _uiState.update { it.copy(isLoading = false) }
                    }

                    is Resource.Empty -> {
                        Log.w(TAG, "⚠️ No users found")
                        _uiState.update {
                            it.copy(
                                allEmployees = emptyList(),
                                isLoading = false,
                                currentUser = user.toDomain()
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "🚨 Exception loading users: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun initializeChat(user: User, employees: List<User>) {
        Log.d(TAG, "🚀 Initializing chat for user: ${user.nome} ${user.cognome}")
        Log.d(TAG, "👥 Available employees: ${employees.size}")

        if (!_uiState.value.isLoading) {
            _uiState.update { it.copy(currentUser = user, allEmployees = employees) }

            loadChats(user, employees)

            viewModelScope.launch {
                try {
                    val dipartimenti = employees.map { it.dipartimento }.distinct()
                    Log.d(TAG, "🏢 Initializing default chats for departments: $dipartimenti")

                    // ✅ Usa Use Case invece del repository diretto
                    when (val result = initializeChatUseCase(user.idAzienda, dipartimenti)) {
                        is Resource.Success -> {
                            Log.d(TAG, "✅ Default chats initialized successfully")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "❌ Error initializing default chats: ${result.message}")
                        }
                        is Resource.Empty -> {
                            Log.w(TAG, "⚠️ Empty result from chat initialization")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "🚨 Exception initializing default chats: ${e.message}")
                }
            }
        }
    }

    private fun loadChats(user: User, employees: List<User>) {
        Log.d(TAG, "📝 Loading chats for user: ${user.uid}")

        chatListJob?.cancel()
        chatListJob = viewModelScope.launch {
            try {


                // ✅ Usa Use Case invece del repository diretto
                loadChatsUseCase(user, employees)
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                Log.d(TAG, "📬 Received ${result.data.size} chats")
                                _uiState.update { it.copy(chats = result.data, isLoading = false) }
                            }

                            is Resource.Error -> {
                                Log.e(TAG, "❌ Error loading chats: ${result.message}")
                                _uiState.update { it.copy(isLoading = false) }
                            }

                            is Resource.Empty -> {
                                Log.w(TAG, "⚠️ No chats found")
                                _uiState.update { it.copy(chats = emptyList(), isLoading = false) }
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "🚨 Exception loading chats: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectChat(chat: Chat) {
        Log.d(TAG, "🎯 Selecting chat: ${chat.nome} (${chat.tipo})")
        _selectedChat.value = chat

        _uiState.value.currentUser?.let { user ->
            loadMessages(chat.id, user.uid)
        } ?: Log.e(TAG, "❌ Cannot select chat: currentUser is null")
    }

    fun deselectChat() {
        Log.d(TAG, "🔙 Deselecting chat")
        _selectedChat.value = null
        messagesJob?.cancel()
        _messages.value = emptyList()
    }

    private fun loadMessages(chatId: String, userId: String) {
        Log.d(TAG, "💬 Loading messages for chat: $chatId")

        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            try {
                // ✅ Usa Use Case invece del repository diretto
                loadMessagesUseCase(chatId, userId)
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                Log.d(TAG, "📨 Received ${result.data.size} messages")
                                _messages.value = result.data
                            }

                            is Resource.Error -> {
                                Log.e(TAG, "❌ Error loading messages: ${result.message}")
                            }

                            is Resource.Empty -> {
                                Log.w(TAG, "⚠️ No messages found")
                                _messages.value = emptyList()
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "🚨 Exception loading messages: ${e.message}")
            }
        }
    }

    fun sendMessage(content: String, tipo: MessageType = MessageType.TEXT) {
        val chat = _selectedChat.value
        val user = _uiState.value.currentUser

        if (chat == null) {
            Log.e(TAG, "❌ Cannot send message: no chat selected")
            return
        }

        if (user == null) {
            Log.e(TAG, "❌ Cannot send message: currentUser is null")
            return
        }

        Log.d(TAG, "📤 Sending message: '${content.take(50)}...' to chat ${chat.nome}")

        viewModelScope.launch {
            try {
                // ✅ Usa Use Case invece del repository diretto
                when (val result = sendMessageUseCase(
                    chatId = chat.id,
                    senderId = user.uid,
                    senderNome = "${user.nome} ${user.cognome}",
                    content = content,
                    tipo = tipo
                )) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Message sent successfully")
                    }

                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error sending message: ${result.message}")
                    }

                    is Resource.Empty -> {
                        Log.w(TAG, "⚠️ Empty result from send message")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "🚨 Exception sending message: ${e.message}")
            }
        }
    }

    fun createPrivateChat(otherUser: User) {
        val user = _uiState.value.currentUser

        if (user == null) {
            Log.e(TAG, "❌ Cannot create private chat: currentUser is null")
            Log.e(TAG, "🔍 Current UI state: ${_uiState.value}")
            return
        }

        Log.d(TAG, "🆕 Creating private chat between:")
        Log.d(TAG, "👤 Current user: ${user.nome} ${user.cognome} (${user.uid})")
        Log.d(TAG, "👤 Other user: ${otherUser.nome} ${otherUser.cognome} (${otherUser.uid})")

        viewModelScope.launch {
            try {
                // ✅ Usa Use Case invece del repository diretto
                when (val result = createPrivateChatUseCase(user, otherUser)) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Private chat created with ID: ${result.data}")

                        // Ricarica le chat per mostrare la nuova chat
                        loadChats(user, _uiState.value.allEmployees)
                        Log.d(TAG, "🔄 Chats reloaded after creating private chat")
                    }

                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error creating private chat: ${result.message}")
                    }

                    is Resource.Empty -> {
                        Log.w(TAG, "⚠️ Empty result from create private chat")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "🚨 Exception creating private chat: ${e.message}", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 ChatViewModel cleared")
        chatListJob?.cancel()
        messagesJob?.cancel()
    }
}