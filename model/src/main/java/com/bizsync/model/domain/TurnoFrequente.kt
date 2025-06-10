package com.bizsync.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class TurnoFrequente(
    val id: String = "",
    val nome: String = "",
    val oraInizio: String = "",
    val oraFine: String = "",
    val descrizione: String = ""
)
{
    constructor() : this("","","","","")
}