package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import com.bizsync.domain.repository.ChatRepository
import javax.inject.Inject

class CreatePrivateChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(user: User, otherUser: User): Resource<String> {
        return try {
            val chatId = chatRepository.createPrivateChat(user, otherUser)
            Resource.Success(chatId)
        } catch (e: Exception) {
            Resource.Error("Errore nella creazione chat privata: ${e.message}")
        }
    }
}