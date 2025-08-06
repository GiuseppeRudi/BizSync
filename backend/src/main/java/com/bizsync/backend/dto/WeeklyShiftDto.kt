package com.bizsync.backend.dto

import com.google.firebase.Timestamp


data class WeeklyShiftDto(
    val idAzienda: String = "",
    val weekStart: String = "",
    val createdBy: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val status: String = "NOT_PUBLISHED",
    val dipartimentiAttivi: List<AreaLavoroDto> = emptyList(),
    val dipendentiAttivi: List<DipendenteDto> = emptyList(),

    val weekEnd: String = "",
    val publishedAt: Timestamp? = null,
    val finalizedAt: Timestamp? = null,
    val notes: String = ""
)