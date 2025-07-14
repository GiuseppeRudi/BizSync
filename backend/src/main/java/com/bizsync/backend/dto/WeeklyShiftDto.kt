package com.bizsync.backend.dto

import com.google.firebase.Timestamp


data class WeeklyShiftDto(
    val idAzienda: String = "",
    val weekStart: String = "", // ISO format "2025-07-15"
    val createdBy: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val status: String = "IN_PROGRESS",

    // Metadati aggiuntivi
    val weekEnd: String = "", // ISO format "2025-07-21" (domenica)
    val publishedAt: Timestamp? = null,
    val finalizedAt: Timestamp? = null,
    val notes: String = ""
)