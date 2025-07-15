package com.bizsync.backend.dto


import com.google.firebase.Timestamp

import com.google.firebase.firestore.Exclude

data class TurnoDto(
    @get:Exclude
    val id: String = "",

    val nome: String = "",
    val idAzienda: String = "",
    val idDipendenti: List<String> = emptyList(),
    val orarioInizio: Timestamp = Timestamp.now(),
    val orarioFine: Timestamp = Timestamp.now(),
    val dipendente: String = "",
    val dipartimentoId: String = "",
    val data: Timestamp = Timestamp.now(),
    val note: String = "",
    val isConfermato: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
