package com.bizsync.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TurnoGeneratoAI(
    val titolo: String,
    val orarioInizio: String,
    val orarioFine: String,
    val idDipendenti: List<String>,
    val pause: List<PausaGenerataAI> = emptyList(),
    val note: String? = null
)