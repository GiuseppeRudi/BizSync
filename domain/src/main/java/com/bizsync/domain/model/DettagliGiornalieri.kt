package com.bizsync.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class DettagliGiornalieri(
    val data: LocalDate,
    val oreTotaliAssegnate: Int,
    val oreEffettive: Double,
    val orarioInizio: LocalTime?,
    val orarioFine: LocalTime?,
    val numeroTurni: Int,
    val colleghi: List<User>,
    val pause: List<Pausa>,
    val note: List<Nota>,
    // Informazioni dipartimento
    val nomeDipartimento: String? = null,
    val orarioAperturaDipartimento: LocalTime? = null,
    val orarioChiusuraDipartimento: LocalTime? = null
)