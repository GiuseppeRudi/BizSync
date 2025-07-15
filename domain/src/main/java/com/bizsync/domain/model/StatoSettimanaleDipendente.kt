package com.bizsync.domain.model

data class StatoSettimanaleDipendente(
    val oreContrattoSettimana: Int,      // es. 40
    val oreAssegnateSettimana: Int = 0,  // aggiornato man mano che assegni
    val turniAssegnati: List<Turno> = emptyList()
)
