package com.bizsync.domain.model

import java.time.DayOfWeek


data class Azienda(
    val idAzienda: String,
    val nome: String,
    val areeLavoro: List<AreaLavoro>,
    val turniFrequenti: List<TurnoFrequente>,
    val numDipendentiRange : String,
    val sector : String,
    val giornoPubblicazioneTurni: DayOfWeek = DayOfWeek.FRIDAY
)
