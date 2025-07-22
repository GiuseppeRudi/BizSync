package com.bizsync.domain.model
import kotlinx.serialization.Serializable

@Serializable
data class TurniGeneratiAI(
    val turniGenerati: List<TurnoGeneratoAI>,
    val coperturaTotale: Boolean,
    val motivoCoperturaParziale: String? = null
)

@Serializable
data class TurnoGeneratoAI(
    val titolo: String,
    val orarioInizio: String,
    val orarioFine: String,
    val idDipendenti: List<String>,
    val pause: List<PausaGenerataAI> = emptyList(),
    val note: String? = null
)

@Serializable
data class PausaGenerataAI(
    val tipo: String,
    val durataMinuti: Int,
    val retribuita: Boolean
)