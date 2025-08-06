package com.bizsync.domain.model

import java.time.LocalDate

data class StatisticheSettimanali(
    val weekStart: LocalDate,
    val oreContrattuali: Int,
    val oreAssegnate: Int,
    val oreEffettive: Double,
    val giorniLavorativi: Int,
    val turniTotali: Int,
    val differenzaOre: Int
)