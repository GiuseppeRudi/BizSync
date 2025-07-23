package com.bizsync.domain.model


import com.bizsync.domain.constants.enumClass.ChatType
import java.util.Date

data class Chat(
    val id: String,
    val tipo: ChatType,
    val nome: String,
    val dipartimento: String? = null,
    val partecipanti: List<String>,
    val ultimoMessaggio: String? = null,
    val ultimoMessaggioTimestamp: Date? = null,
    val ultimoMessaggioSenderNome: String? = null,
    val messaggiNonLetti: Int = 0
)