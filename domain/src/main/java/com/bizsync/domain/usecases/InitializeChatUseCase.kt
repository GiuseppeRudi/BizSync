package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.repository.ChatRepository
import javax.inject.Inject

class InitializeChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(idAzienda: String, dipartimenti: List<String>): Resource<Unit> {
        return try {
            chatRepository.initializeDefaultChats(idAzienda, dipartimenti)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Errore nell'inizializzazione chat: ${e.message}")
        }
    }
}