package com.bizsync.backend.dto


import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude


data class TurnoDto(
    @get:Exclude
    val idFirebase: String = "",

    val id: String = "",
    val titolo: String = "",
    val idAzienda: String = "",
    val idDipendenti: List<String> = emptyList(),
    val dipartimento: String = "",

    val data: Timestamp = Timestamp.now(),
    val orarioInizio: Timestamp = Timestamp.now(),
    val orarioFine: Timestamp = Timestamp.now(),

    // Nuova proprietà: salviamo come Map<String, String> per Firebase
    val zoneLavorative: Map<String, String> = emptyMap(),

    val note: List<NotaDto> = emptyList(),
    val pause: List<PausaDto> = emptyList(),

    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)