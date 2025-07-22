package com.bizsync.ui.model

import com.bizsync.domain.model.Chat
import com.bizsync.domain.model.User

data class ChatUiState(
    val currentUser: User = User(),
    val allEmployees: List<User> = emptyList(),
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)