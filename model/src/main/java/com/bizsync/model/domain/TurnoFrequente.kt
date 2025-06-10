package com.bizsync.model.domain

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