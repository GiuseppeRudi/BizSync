package com.bizsync.backend.dto



import com.bizsync.domain.model.TurnoFrequente

data class AziendaDto(
    val id: String,
    val nome: String,
    val areeLavoro: List<AreaLavoroDto>,
    val turniFrequenti: List<TurnoFrequente>,
    val numDipendentiRange: String,
    val sector: String,

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
        latitudine = 0.0,
        longitudine = 0.0,
        tolleranzaMetri = 100
    )
}