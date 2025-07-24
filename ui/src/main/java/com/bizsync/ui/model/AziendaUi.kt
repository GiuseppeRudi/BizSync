package com.bizsync.ui.model

import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente
import java.time.DayOfWeek

data class AziendaUi(
    val idAzienda: String = "",
    val nome: String = "",
    val areeLavoro: List<AreaLavoro> = emptyList(),
    val turniFrequenti: List<TurnoFrequente> = emptyList(),
    val numDipendentiRange: String = "",
    val sector: String = "",
    val giornoPubblicazioneTurni: DayOfWeek = DayOfWeek.FRIDAY,

    val latitudine: Double = 0.0,
    val longitudine: Double = 0.0,
    val tolleranzaMetri: Int = 100
)
