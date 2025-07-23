package com.bizsync.backend.dto

import com.google.firebase.Timestamp



data class NotaDto(
    val id: String = "",
    val testo: String = "",
    val tipo: String = "GENERALE", // Come stringa, nome dell'enum
    val autore: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
