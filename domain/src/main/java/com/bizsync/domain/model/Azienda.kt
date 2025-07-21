package com.bizsync.domain.model

import java.time.DayOfWeek


data class Azienda(
    val idAzienda: String,
    val nome: String,
    val areeLavoro: List<AreaLavoro>,
    val turniFrequenti: List<TurnoFrequente>,

    val numDipendentiRange : String,
    val sector : String,
    val giornoPubblicazioneTurni: DayOfWeek = DayOfWeek.FRIDAY,

    // Nuovi campi per la geolocalizzazione
    val latitudine: Double = 0.0,
    val longitudine: Double = 0.0,
    val tolleranzaMetri: Int = 100, // tolleranza predefinita 100 metri


)
