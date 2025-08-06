package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Message
import com.bizsync.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoadMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(chatId: String, userId: String): Flow<Resource<List<Message>>> {
        return chatRepository.getMessagesForChat(chatId, userId)
            .map<List<Message>, Resource<List<Message>>> { messages ->
                Resource.Success(messages)
            }
            .catch { e ->
                emit(Resource.Error("Errore nel caricamento messaggi: ${e.message}"))
            }
    }
}