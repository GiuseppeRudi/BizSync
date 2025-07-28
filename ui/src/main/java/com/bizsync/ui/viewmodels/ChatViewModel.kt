package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.ChatRepository
import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.enumClass.MessageType
import com.bizsync.domain.model.*
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.model.ChatUiState
import com.bizsync.ui.model.UserUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userDao: UserDao
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

    // ⭐ CORREZIONE: Sempre usare _uiState.value.currentUser direttamente

    private var chatListJob: kotlinx.coroutines.Job? = null
    private var messagesJob: kotlinx.coroutines.Job? = null

    fun loadUsers(user: UserUi) {
        Log.d(TAG, "🔄 Loading users for: ${user.nome} ${user.cognome}")
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val users = userDao.getDipendenti()
                val domainUser = user.toDomain()

                Log.d(TAG, "✅ Users loaded: ${users.size} employees")
                _uiState.update {
                    it.copy(
                        allEmployees = users.toDomainList(),
                        isLoading = false,
                        currentUser = domainUser
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading users: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun initializeChat(user: User, employees: List<User>) {
        Log.d(TAG, "🚀 Initializing chat for user: ${user.nome} ${user.cognome}")
        Log.d(TAG, "👥 Available employees: ${employees.size}")

        if (!_uiState.value.isLoading) {
            // ⭐ CORREZIONE: Aggiorna sia l'UI state che la variabile locale
            _uiState.update { it.copy(currentUser = user, allEmployees = employees) }

            loadChats(user, employees)

            viewModelScope.launch {
                try {
                    val dipartimenti = employees.map { it.dipartimento }.distinct()
                    Log.d(TAG, "🏢 Initializing default chats for departments: $dipartimenti")
                    chatRepository.initializeDefaultChats(user.idAzienda, dipartimenti)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error initializing default chats: ${e.message}")
                }
            }
        }
    }

    private fun loadChats(user: User, employees: List<User>) {
        Log.d(TAG, "📝 Loading chats for user: ${user.uid}")

        chatListJob?.cancel()
        chatListJob = viewModelScope.launch {
            try {
                chatRepository.getChatsForUser(user, employees)
                    .collect { chats ->
                        Log.d(TAG, "📬 Received ${chats.size} chats")

                        // Calcola messaggi non letti per ogni chat
                        val chatsWithUnread = chats.map { chat ->
                            val unreadCount = chatRepository.getUnreadCount(chat.id, user.uid)
                            chat.copy(messaggiNonLetti = unreadCount)
                        }

                        _uiState.update { it.copy(chats = chatsWithUnread, isLoading = false) }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading chats: ${e.message}")
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
                chatRepository.getMessagesForChat(chatId, userId)
                    .collect { messages ->
                        Log.d(TAG, "📨 Received ${messages.size} messages")
                        _messages.value = messages
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading messages: ${e.message}")
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
                chatRepository.sendMessage(
                    chatId = chat.id,
                    senderId = user.uid,
                    senderNome = "${user.nome} ${user.cognome}",
                    content = content,
                    tipo = tipo
                )
                Log.d(TAG, "✅ Message sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending message: ${e.message}")
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
                val chatId = chatRepository.createPrivateChat(user, otherUser)
                Log.d(TAG, "✅ Private chat created with ID: $chatId")

                // Ricarica le chat per mostrare la nuova chat
                loadChats(user, _uiState.value.allEmployees)

                Log.d(TAG, "🔄 Chats reloaded after creating private chat")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error creating private chat: ${e.message}", e)
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