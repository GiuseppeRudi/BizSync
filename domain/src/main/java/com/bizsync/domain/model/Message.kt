package com.bizsync.domain.model

import com.bizsync.domain.constants.enumClass.MessageType
import java.util.Date

data class Message(
    val id: String,
    val senderId: String,
    val senderNome: String,
    val content: String,
    val timestamp: Date,
    val tipo: MessageType,
    val isLetto: Boolean = false,
    val categoria: String? = null
)
