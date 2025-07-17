package com.bizsync.backend.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class MessageDto(
    @DocumentId
    val id: String = "",
    val senderId: String = "",
    val senderNome: String = "",
    val content: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val tipo: String = "text", // "text", "announcement", "system"
    val lettoDa: List<String> = emptyList(),
    val categoria: String? = null // "cambio_turno", etc.
)