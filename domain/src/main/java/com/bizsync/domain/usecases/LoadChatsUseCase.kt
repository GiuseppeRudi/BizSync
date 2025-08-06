package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Chat
import com.bizsync.domain.model.User
import com.bizsync.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoadChatsUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(user: User, employees: List<User>): Flow<Resource<List<Chat>>> {
        return chatRepository.getChatsForUser(user, employees)
            .map { chats ->
                try {
                    // Calcola messaggi non letti per ogni chat
                    val chatsWithUnread = chats.map { chat ->
                        val unreadCount = chatRepository.getUnreadCount(chat.id, user.uid)
                        chat.copy(messaggiNonLetti = unreadCount)
                    }
                    Resource.Success(chatsWithUnread)
                } catch (e: Exception) {
                    Resource.Error("Errore nel caricamento chat: ${e.message}")
                }
            }
            .catch { e ->
                emit(Resource.Error("Errore nel caricamento chat: ${e.message}"))
            }
    }
}
