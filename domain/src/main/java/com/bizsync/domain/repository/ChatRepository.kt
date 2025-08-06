package com.bizsync.domain.repository

import com.bizsync.domain.constants.enumClass.MessageType
import com.bizsync.domain.model.Chat
import com.bizsync.domain.model.Message
import com.bizsync.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun initializeDefaultChats(idAzienda: String, dipartimenti: List<String>)
    fun getChatsForUser(user: User, employees: List<User>): Flow<List<Chat>>
    suspend fun getUnreadCount(chatId: String, userId: String): Int
    fun getMessagesForChat(chatId: String, userId: String): Flow<List<Message>>

    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        senderNome: String,
        content: String,
        tipo: MessageType
    )
    suspend fun createPrivateChat(user: User, otherUser: User): String
}