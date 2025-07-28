package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.dto.ChatDto
import com.bizsync.backend.dto.MessageDto
import com.bizsync.backend.mapper.toDomain
import com.bizsync.backend.remote.ChatFirestore
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
    private val chatsCollection = firestore.collection(ChatFirestore.COLLECTION)

    fun getChatsForUser(user: User, allEmployees: List<User>): Flow<List<Chat>> = callbackFlow {
        val listener = chatsCollection
            .whereEqualTo(ChatFirestore.Fields.ID_AZIENDA, user.idAzienda)
            .orderBy(ChatFirestore.Fields.ULTIMO_MESSAGGIO_TIMESTAMP, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents?.mapNotNull { doc ->
                    val chatDto = doc.toObject(ChatDto::class.java) ?: return@mapNotNull null

                    // Filtra le chat in base al ruolo dell'utente
                    when {
                        // Manager vede tutto della sua azienda
                        user.isManager -> chatDto.toDomain(user.uid)

                        // Chat generale - tutti la vedono (della propria azienda)
                        chatDto.tipo == ChatFirestore.TipoChat.GENERALE -> chatDto.toDomain(user.uid)

                        // Chat dipartimento - solo se appartiene al dipartimento E alla stessa azienda
                        chatDto.tipo == ChatFirestore.TipoChat.DIPARTIMENTO &&
                                chatDto.dipartimento == user.dipartimento &&
                                chatDto.idAzienda == user.idAzienda -> chatDto.toDomain(user.uid)

                        // Chat privata - solo se è partecipante E della stessa azienda
                        chatDto.tipo == ChatFirestore.TipoChat.PRIVATA &&
                                user.uid in chatDto.partecipanti &&
                                chatDto.idAzienda == user.idAzienda -> chatDto.toDomain(user.uid)

                        else -> null
                    }
                } ?: emptyList()

                trySend(chats)
            }

        awaitClose { listener.remove() }
    }

    fun getMessagesForChat(chatId: String, userId: String): Flow<List<Message>> = callbackFlow {
        val listener = chatsCollection
            .document(chatId)
            .collection(ChatFirestore.MESSAGES_SUBCOLLECTION)
            .orderBy(ChatFirestore.MessageFields.TIMESTAMP, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MessageDto::class.java)?.toDomain(userId)
                } ?: emptyList()

                // Segna come letti i messaggi non propri
                snapshot?.documents?.forEach { doc ->
                    val message = doc.toObject(MessageDto::class.java)
                    if (message != null &&
                        message.senderId != userId &&
                        userId !in message.lettoDa) {
                        doc.reference.update(
                            ChatFirestore.MessageFields.LETTO_DA,
                            message.lettoDa + userId
                        )
                    }
                }

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

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
            tipo = when (tipo) {
                MessageType.TEXT -> ChatFirestore.MessageTypes.TEXT
                MessageType.ANNOUNCEMENT -> ChatFirestore.MessageTypes.ANNOUNCEMENT
                MessageType.SYSTEM -> ChatFirestore.MessageTypes.SYSTEM
                else -> ChatFirestore.MessageTypes.TEXT
            },
            categoria = categoria,
            lettoDa = listOf(senderId)
        )

        // Aggiungi il messaggio
        chatsCollection
            .document(chatId)
            .collection(ChatFirestore.MESSAGES_SUBCOLLECTION)
            .add(messageDto)
            .await()

        // Aggiorna l'ultimo messaggio nella chat
        chatsCollection
            .document(chatId)
            .update(mapOf(
                ChatFirestore.Fields.ULTIMO_MESSAGGIO to content,
                ChatFirestore.Fields.ULTIMO_MESSAGGIO_TIMESTAMP to Timestamp.now(),
                ChatFirestore.Fields.ULTIMO_MESSAGGIO_SENDER_ID to senderId
            ))
            .await()
    }

    // Crea una nuova chat privata
    suspend fun createPrivateChat(user1: User, user2: User): String {
        Log.d("ChatRepo", "Cerco chat privata tra ${user1.uid} e ${user2.uid} nella stessa azienda ${user1.idAzienda}")

        // Controlla se esiste già nella stessa azienda
        val existingChat = chatsCollection
            .whereEqualTo(ChatFirestore.Fields.TIPO, ChatFirestore.TipoChat.PRIVATA)
            .whereEqualTo(ChatFirestore.Fields.ID_AZIENDA, user1.idAzienda)
            .whereArrayContains(ChatFirestore.Fields.PARTECIPANTI, user1.uid)
            .get()
            .await()
            .documents
            .firstOrNull { doc ->
                val partecipanti = doc.get(ChatFirestore.Fields.PARTECIPANTI) as? List<*>
                Log.d("ChatRepo", "Controllo partecipanti della chat ${doc.id}: $partecipanti")
                partecipanti?.contains(user2.uid) == true
            }

        if (existingChat != null) {
            Log.d("ChatRepo", "Chat già esistente trovata con ID: ${existingChat.id}")
            return existingChat.id
        }

        // Crea nuova chat con idAzienda
        val chatDto = ChatDto(
            tipo = ChatFirestore.TipoChat.PRIVATA,
            nome = "${user2.nome} ${user2.cognome}",
            idAzienda = user1.idAzienda,
            partecipanti = listOf(user1.uid, user2.uid)
        )

        Log.d("ChatRepo", "Nessuna chat trovata. Creo nuova chat con: $chatDto")

        val docRef = chatsCollection.add(chatDto).await()

        Log.d("ChatRepo", "Nuova chat creata con ID: ${docRef.id}")

        return docRef.id
    }

    suspend fun getUnreadCount(chatId: String, userId: String): Int {
        return chatsCollection
            .document(chatId)
            .collection(ChatFirestore.MESSAGES_SUBCOLLECTION)
            .whereNotEqualTo(ChatFirestore.MessageFields.SENDER_ID, userId)
            .get()
            .await()
            .documents
            .count { doc ->
                val message = doc.toObject(MessageDto::class.java)
                message != null && userId !in message.lettoDa
            }
    }

    // Inizializza le chat di default (generale e dipartimenti) con idAzienda
    suspend fun initializeDefaultChats(aziendaId: String, dipartimenti: List<String>) {
        Log.d("ChatRepository", "Inizializzazione chat per azienda: $aziendaId")

        // Chat generale con idAzienda
        val generalChat = chatsCollection
            .whereEqualTo(ChatFirestore.Fields.TIPO, ChatFirestore.TipoChat.GENERALE)
            .whereEqualTo(ChatFirestore.Fields.ID_AZIENDA, aziendaId)
            .get()
            .await()

        if (generalChat.isEmpty) {
            chatsCollection.add(
                ChatDto(
                    tipo = ChatFirestore.TipoChat.GENERALE,
                    nome = "Chat Generale",
                    idAzienda = aziendaId,
                    partecipanti = emptyList() // Tutti possono accedere
                )
            ).await()
            Log.d("ChatRepository", "Creata chat generale per azienda: $aziendaId")
        }

        // Chat per dipartimento con idAzienda
        dipartimenti.forEach { dip ->
            val dipChat = chatsCollection
                .whereEqualTo(ChatFirestore.Fields.TIPO, ChatFirestore.TipoChat.DIPARTIMENTO)
                .whereEqualTo(ChatFirestore.Fields.ID_AZIENDA, aziendaId)
                .whereEqualTo(ChatFirestore.Fields.DIPARTIMENTO, dip)
                .get()
                .await()

            if (dipChat.isEmpty) {
                chatsCollection.add(
                    ChatDto(
                        tipo = ChatFirestore.TipoChat.DIPARTIMENTO,
                        nome = "Chat $dip",
                        idAzienda = aziendaId,
                        dipartimento = dip,
                        partecipanti = emptyList()
                    )
                ).await()
                Log.d("ChatRepository", "Creata chat dipartimento '$dip' per azienda: $aziendaId")
            }
        }
    }
}