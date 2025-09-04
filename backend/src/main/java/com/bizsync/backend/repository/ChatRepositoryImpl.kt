package com.bizsync.backend.repository

import android.util.Log
import com.bizsync.backend.dto.ChatDto
import com.bizsync.backend.dto.MessageDto
import com.bizsync.backend.mapper.toDomain
import com.bizsync.backend.remote.ChatFirestore
import com.bizsync.domain.constants.enumClass.MessageType
import com.bizsync.domain.model.*
import com.bizsync.domain.repository.ChatRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class  ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {
    private val chatsCollection = firestore.collection(ChatFirestore.COLLECTION)

    override fun getChatsForUser(user: User, allEmployees: List<User>): Flow<List<Chat>> = callbackFlow {
        val generalChatFlow = callbackFlow<List<Chat>> {
            val listener = chatsCollection
                .whereEqualTo(ChatFirestore.Fields.ID_AZIENDA, user.idAzienda)
                .whereEqualTo(ChatFirestore.Fields.TIPO, ChatFirestore.TipoChat.GENERALE)
                .orderBy(ChatFirestore.Fields.ULTIMO_MESSAGGIO_TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val chats = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(ChatDto::class.java)?.toDomain(user.uid)
                    } ?: emptyList()

                    trySend(chats)
                }
            awaitClose { listener.remove() }
        }

        val privateChatFlow = callbackFlow<List<Chat>> {
            val listener = chatsCollection
                .whereEqualTo(ChatFirestore.Fields.ID_AZIENDA, user.idAzienda)
                .whereEqualTo(ChatFirestore.Fields.TIPO, ChatFirestore.TipoChat.PRIVATA)
                .whereArrayContains(ChatFirestore.Fields.PARTECIPANTI, user.uid)
                .orderBy(ChatFirestore.Fields.ULTIMO_MESSAGGIO_TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val chats = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(ChatDto::class.java)?.toDomain(user.uid)
                    } ?: emptyList()

                    trySend(chats)
                }
            awaitClose { listener.remove() }
        }

        val departmentChatFlow = if (user.isManager) {
            // Manager vede tutte le chat dipartimento
            callbackFlow<List<Chat>> {
                val listener = chatsCollection
                    .whereEqualTo(ChatFirestore.Fields.ID_AZIENDA, user.idAzienda)
                    .whereEqualTo(ChatFirestore.Fields.TIPO, ChatFirestore.TipoChat.DIPARTIMENTO)
                    .orderBy(ChatFirestore.Fields.ULTIMO_MESSAGGIO_TIMESTAMP, Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        val chats = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(ChatDto::class.java)?.toDomain(user.uid)
                        } ?: emptyList()

                        trySend(chats)
                    }
                awaitClose { listener.remove() }
            }
        } else if (user.dipartimento.isNotEmpty()) {
            // Dipendente vede solo il suo dipartimento
            callbackFlow<List<Chat>> {
                val listener = chatsCollection
                    .whereEqualTo(ChatFirestore.Fields.ID_AZIENDA, user.idAzienda)
                    .whereEqualTo(ChatFirestore.Fields.TIPO, ChatFirestore.TipoChat.DIPARTIMENTO)
                    .whereEqualTo(ChatFirestore.Fields.DIPARTIMENTO, user.dipartimento)
                    .orderBy(ChatFirestore.Fields.ULTIMO_MESSAGGIO_TIMESTAMP, Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        val chats = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(ChatDto::class.java)?.toDomain(user.uid)
                        } ?: emptyList()

                        trySend(chats)
                    }
                awaitClose { listener.remove() }
            }
        } else {
            // Nessun dipartimento, emetti lista vuota
            flowOf(emptyList<Chat>())
        }

        // Combina i tre flow
        combine(generalChatFlow, privateChatFlow, departmentChatFlow) { general, private, department ->
            (general + private + department)
                .distinctBy { it.id }
                .sortedByDescending { it.ultimoMessaggioTimestamp }
        }.collect {
            trySend(it)
        }

        awaitClose { }
    }

    override fun getMessagesForChat(chatId: String, userId: String): Flow<List<Message>> = callbackFlow {
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

    override suspend fun sendMessage(
        chatId: String,
        senderId: String,
        senderNome: String,
        content: String,
        tipo: MessageType,
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
    override suspend fun createPrivateChat(user1: User, user2: User): String {
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

    override suspend fun getUnreadCount(chatId: String, userId: String): Int {
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
    override suspend fun initializeDefaultChats(idAzienda: String, dipartimenti: List<String>) {
        Log.d("ChatRepositoryImpl", "DIPARTIMENTI azienda: $dipartimenti")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("ChatRepositoryImpl", "❌ Utente non autenticato - FirebaseAuth.currentUser è null")
        } else {
            Log.d("ChatRepositoryImpl", "✅ Utente autenticato: ${currentUser.uid}")
        }

        Log.d("ChatRepositoryImpl", "➡️ Controllo esistenza chat GENERALE per azienda: $idAzienda")
        val generalChat = chatsCollection
            .whereEqualTo(ChatFirestore.Fields.TIPO, ChatFirestore.TipoChat.GENERALE)
            .whereEqualTo(ChatFirestore.Fields.ID_AZIENDA, idAzienda)
            .get()
            .await()
        Log.d("ChatRepositoryImpl", "📦 Risultato query GENERALE: ${generalChat.size()} documenti trovati")

        if (generalChat.isEmpty) {
            chatsCollection.add(
                ChatDto(
                    tipo = ChatFirestore.TipoChat.GENERALE,
                    nome = "Chat Generale",
                    idAzienda = idAzienda,
                    partecipanti = emptyList() // Tutti possono accedere
                )
            ).await()
            Log.d("ChatRepositoryImpl", "Creata chat generale per azienda: $idAzienda")
        }

        // Chat per dipartimento con idAzienda
        dipartimenti.forEach { dip ->
            val dipChat = chatsCollection
                .whereEqualTo(ChatFirestore.Fields.TIPO, ChatFirestore.TipoChat.DIPARTIMENTO)
                .whereEqualTo(ChatFirestore.Fields.ID_AZIENDA, idAzienda)
                .whereEqualTo(ChatFirestore.Fields.DIPARTIMENTO, dip)
                .get()
                .await()

            Log.d("ChatRepositoryImpl", "DIPARTIMENTO CHAT : $dip")

            if (dipChat.isEmpty) {
                chatsCollection.add(
                    ChatDto(
                        tipo = ChatFirestore.TipoChat.DIPARTIMENTO,
                        nome = "Chat $dip",
                        idAzienda = idAzienda,
                        dipartimento = dip,
                        partecipanti = emptyList()
                    )
                ).await()
                Log.d("ChatRepositoryImpl", "Creata chat dipartimento '$dip' per azienda: $idAzienda")
            }
        }
    }
}