package com.bizsync.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TurnoFrequente(
    val id: String = UUID.randomUUID().toString(),
    val nome: String = "",
    val oraInizio: String = "",
    val oraFine: String = "",
)

{
    constructor() : this(UUID.randomUUID().toString(),"","","")
}