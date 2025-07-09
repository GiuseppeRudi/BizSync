package com.bizsync.ui.model

import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente
import java.time.DayOfWeek


data class AziendaUi(

    val idAzienda: String = "",
    val nome: String = "Ciccio Industry",
    val areeLavoro: List<AreaLavoro> = emptyList(),
    val turniFrequenti: List<TurnoFrequente> = emptyList(),
    val numDipendentiRange : String = "",
    val sector : String = "",
    val giornoPubblicazioneTurni: DayOfWeek = DayOfWeek.FRIDAY,



    // Informazioni derivate
    val numeroDipendenti: Int = 0,
    val descrizioneAzienda: String = ""
)