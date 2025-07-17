package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.dto.ChatDto
import com.bizsync.backend.dto.MessageDto
import com.bizsync.domain.constants.enumClass.ChatType
import com.bizsync.domain.constants.enumClass.MessageType
import com.bizsync.domain.model.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val chatsCollection = firestore.collection("chats")

    // Ottieni tutte le chat visibili all'utente
    fun getChatsForUser(user: User, allEmployees: List<User>): Flow<List<Chat>> = callbackFlow {
        val listener = chatsCollection
            .orderBy("ultimoMessaggioTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents?.mapNotNull { doc ->
                    val chatDto = doc.toObject(ChatDto::class.java) ?: return@mapNotNull null

                    // Filtra le chat in base al ruolo dell'utente
                    when {
                        // Manager vede tutto
                        user.manager -> chatDto.toDomainModel(user.uid)

                        // Chat generale - tutti la vedono
                        chatDto.tipo == "generale" -> chatDto.toDomainModel(user.uid)

                        // Chat dipartimento - solo se appartiene al dipartimento
                        chatDto.tipo == "dipartimento" &&
                                chatDto.dipartimentoId == user.dipartimento -> chatDto.toDomainModel(user.uid)

                        // Chat privata - solo se è partecipante
                        chatDto.tipo == "privata" &&
                                user.uid in chatDto.partecipanti -> chatDto.toDomainModel(user.uid)

                        else -> null
                    }
                } ?: emptyList()

                trySend(chats)
            }

        awaitClose { listener.remove() }
    }

    // Ascolta i messaggi di una chat specifica
    fun getMessagesForChat(chatId: String, userId: String): Flow<List<Message>> = callbackFlow {
        val listener = chatsCollection
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MessageDto::class.java)?.toDomainModel(userId)
                } ?: emptyList()

                // Segna come letti i messaggi non propri
                snapshot?.documents?.forEach { doc ->
                    val message = doc.toObject(MessageDto::class.java)
                    if (message != null && message.senderId != userId && userId !in message.lettoDa) {
                        doc.reference.update("lettoDa", message.lettoDa + userId)
                    }
                }

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    // Invia un messaggio
    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        senderNome: String,
        content: String,
        tipo: MessageType = MessageType.TEXT,
        categoria: String? = null
    ) {
        val messageDto = MessageDto(
            senderId = senderId,
            senderNome = senderNome,
            content = content,
            tipo = tipo.name.lowercase(),
            categoria = categoria,
            lettoDa = listOf(senderId)
        )

        // Aggiungi il messaggio
        chatsCollection
            .document(chatId)
            .collection("messages")
            .add(messageDto)
            .await()

        // Aggiorna l'ultimo messaggio nella chat
        chatsCollection
            .document(chatId)
            .update(mapOf(
                "ultimoMessaggio" to content,
                "ultimoMessaggioTimestamp" to Timestamp.now(),
                "ultimoMessaggioSenderId" to senderId
            ))
            .await()
    }

    // Crea una nuova chat privata
    suspend fun createPrivateChat(user1: User, user2: User): String {
        // Controlla se esiste già
        val existingChat = chatsCollection
            .whereEqualTo("tipo", "privata")
            .whereArrayContains("partecipanti", user1.uid)
            .get()
            .await()
            .documents
            .firstOrNull { doc ->
                val partecipanti = doc.get("partecipanti") as? List<*>
                partecipanti?.contains(user2.uid) == true
            }

        if (existingChat != null) {
            return existingChat.id
        }

        // Crea nuova chat
        val chatDto = ChatDto(
            tipo = "privata",
            nome = "${user2.nome} ${user2.cognome}",
            partecipanti = listOf(user1.uid, user2.uid)
        )

        val docRef = chatsCollection.add(chatDto).await()
        return docRef.id
    }

    // Ottieni il numero di messaggi non letti
    suspend fun getUnreadCount(chatId: String, userId: String): Int {
        return chatsCollection
            .document(chatId)
            .collection("messages")
            .whereNotEqualTo("senderId", userId)
            .get()
            .await()
            .documents
            .count { doc ->
                val message = doc.toObject(MessageDto::class.java)
                message != null && userId !in message.lettoDa
            }
    }

    // Inizializza le chat di default (generale e dipartimenti)
    suspend fun initializeDefaultChats(aziendaId: String, dipartimenti: List<String>) {
        // Chat generale
        Log.d("ChatRepository", "Inizializzazione chat " + aziendaId)

        val generalChat = chatsCollection
            .whereEqualTo("tipo", "generale")
            .whereEqualTo("nome", "Chat Generale $aziendaId")
            .get()
            .await()

        if (generalChat.isEmpty) {
            chatsCollection.add(
                ChatDto(
                    tipo = "generale",
                    nome = "Chat Generale $aziendaId",
                    partecipanti = emptyList() // Tutti possono accedere
                )
            ).await()
        }

        // Chat per dipartimento
        dipartimenti.forEach { dip ->
            val dipChat = chatsCollection
                .whereEqualTo("tipo", "dipartimento")
                .whereEqualTo("dipartimentoId", dip)
                .get()
                .await()

            if (dipChat.isEmpty) {
                chatsCollection.add(
                    ChatDto(
                        tipo = "dipartimento",
                        nome = "Chat $dip",
                        dipartimentoId = dip,
                        partecipanti = emptyList()
                    )
                ).await()
            }
        }
    }
}

// Extension functions per conversione
private fun ChatDto.toDomainModel(currentUserId: String): Chat {
    return Chat(
        id = id,
        tipo = when (tipo) {
            "generale" -> ChatType.GENERALE
            "dipartimento" -> ChatType.DIPARTIMENTO
            "privata" -> ChatType.PRIVATA
            else -> ChatType.GENERALE
        },
        nome = nome,
        dipartimentoId = dipartimentoId,
        partecipanti = partecipanti,
        ultimoMessaggio = ultimoMessaggio,
        ultimoMessaggioTimestamp = ultimoMessaggioTimestamp?.toDate(),
        ultimoMessaggioSenderNome = ultimoMessaggioSenderId,
        messaggiNonLetti = 0 // Calcolato separatamente
    )
}

private fun MessageDto.toDomainModel(currentUserId: String): Message {
    return Message(
        id = id,
        senderId = senderId,
        senderNome = senderNome,
        content = content,
        timestamp = timestamp?.toDate() ?: Date(),
        tipo = when (tipo) {
            "announcement" -> MessageType.ANNOUNCEMENT
            "system" -> MessageType.SYSTEM
            else -> MessageType.TEXT
        },
        isLetto = currentUserId in lettoDa,
        categoria = categoria
    )
}