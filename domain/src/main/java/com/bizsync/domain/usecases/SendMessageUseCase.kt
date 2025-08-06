package com.bizsync.domain.usecases

import com.bizsync.domain.constants.enumClass.MessageType
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        chatId: String,
        senderId: String,
        senderNome: String,
        content: String,
        tipo: MessageType = MessageType.TEXT
    ): Resource<Unit> {
        return try {
            chatRepository.sendMessage(chatId, senderId, senderNome, content, tipo)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Errore nell'invio messaggio: ${e.message}")
        }
    }
}