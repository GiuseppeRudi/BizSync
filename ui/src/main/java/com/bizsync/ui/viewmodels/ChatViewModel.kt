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
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userDao : UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _selectedChat = MutableStateFlow<Chat?>(null)
    val selectedChat: StateFlow<Chat?> = _selectedChat.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var currentUser: User? = null
    private var chatListJob: kotlinx.coroutines.Job? = null
    private var messagesJob: kotlinx.coroutines.Job? = null

    fun loadUsers(user : UserUi)
    {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val users = userDao.getDipendenti()
            _uiState.update { it.copy(allEmployees = users.toDomainList(), isLoading = false, currentUser = user.toDomain()) }
        }

    }

    fun initializeChat(user: User, employees: List<User>) {

        if(!_uiState.value.isLoading)
        {
            currentUser = user
            _uiState.update { it.copy(currentUser = user, allEmployees = employees) }
            loadChats(user, employees)

            // Inizializza chat di default
            viewModelScope.launch {
                val dipartimenti = employees.map { it.dipartimento }.distinct()
                chatRepository.initializeDefaultChats(user.idAzienda, dipartimenti)
            }
        }

    }

    private fun loadChats(user: User, employees: List<User>) {
        chatListJob?.cancel()
        chatListJob = viewModelScope.launch {
            chatRepository.getChatsForUser(user, employees)
                .collect { chats ->
                    // Calcola messaggi non letti per ogni chat
                    val chatsWithUnread = chats.map { chat ->
                        val unreadCount = chatRepository.getUnreadCount(chat.id, user.uid)
                        chat.copy(messaggiNonLetti = unreadCount)
                    }
                    _uiState.update { it.copy(chats = chatsWithUnread, isLoading = false) }
                }
        }
    }

    fun selectChat(chat: Chat) {
        _selectedChat.value = chat
        currentUser?.let { user ->
            loadMessages(chat.id, user.uid)
        }
    }

    fun deselectChat() {
        _selectedChat.value = null
        messagesJob?.cancel()
        _messages.value = emptyList()
    }

    private fun loadMessages(chatId: String, userId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatRepository.getMessagesForChat(chatId, userId)
                .collect { messages ->
                    _messages.value = messages
                }
        }
    }

    fun sendMessage(content: String, tipo: MessageType = MessageType.TEXT) {
        val chat = _selectedChat.value ?: return
        val user = currentUser ?: return

        viewModelScope.launch {
            chatRepository.sendMessage(
                chatId = chat.id,
                senderId = user.uid,
                senderNome = "${user.nome} ${user.cognome}",
                content = content,
                tipo = tipo
            )
        }
    }

    fun createPrivateChat(otherUser: User) {
        val user = currentUser ?: return

        viewModelScope.launch {
            val chatId = chatRepository.createPrivateChat(user, otherUser)
            // Ricarica le chat
            loadChats(user, _uiState.value.allEmployees)
        }
    }
}