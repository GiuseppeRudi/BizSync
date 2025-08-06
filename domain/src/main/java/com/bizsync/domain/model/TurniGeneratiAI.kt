package com.bizsync.domain.model
import kotlinx.serialization.Serializable

@Serializable
data class TurniGeneratiAI(
    val turniGenerati: List<TurnoGeneratoAI>,
    val coperturaTotale: Boolean,
    val motivoCoperturaParziale: String? = null
)


