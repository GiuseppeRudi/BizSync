package com.bizsync.backend.dto


import com.bizsync.backend.repository.WeeklyShiftRepository
import com.bizsync.domain.model.Turno
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TurnoFirestore(
    val nome: String = "",
    val orarioInizio: String = "",
    val orarioFine: String = "",
    val dipendente: String = "",
    val dipartimentoId: String = "",
    val data: String = "", // ISO format "2025-07-15"
    val note: String = "",
    val isConfermato: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

