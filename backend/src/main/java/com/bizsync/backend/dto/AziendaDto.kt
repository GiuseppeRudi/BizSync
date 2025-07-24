package com.bizsync.backend.dto



import com.bizsync.domain.model.TurnoFrequente
import java.time.DayOfWeek

data class AziendaDto(
    val id: String,
    val nome: String,
    val areeLavoro: List<AreaLavoroDto>,
    val turniFrequenti: List<TurnoFrequente>,
    val numDipendentiRange: String,
    val sector: String,
    val giornoPubblicazioneTurni: String,

    val latitudine: Double,
    val longitudine: Double,
    val tolleranzaMetri: Int
) {
    constructor() : this(
        id = "",
        nome = "",
        areeLavoro = emptyList(),
        turniFrequenti = emptyList(),
        numDipendentiRange = "",
        sector = "",
        giornoPubblicazioneTurni = DayOfWeek.FRIDAY.toString(),
        latitudine = 0.0,
        longitudine = 0.0,
        tolleranzaMetri = 100
    )
}