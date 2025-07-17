package com.bizsync.backend.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class ChatDto(
    @DocumentId
    val id: String = "",
    val tipo: String = "", // "generale", "dipartimento", "privata"
    val nome: String = "",
    val dipartimentoId: String? = null,
    val partecipanti: List<String> = emptyList(),
    @ServerTimestamp
    val creatoIl: Timestamp? = null,
    val ultimoMessaggio: String? = null,
    val ultimoMessaggioTimestamp: Timestamp? = null,
    val ultimoMessaggioSenderId: String? = null
)
