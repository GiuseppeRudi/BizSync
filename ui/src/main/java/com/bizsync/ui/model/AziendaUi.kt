package com.bizsync.ui.model

import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente




data class AziendaUi(

    var idAzienda: String = "",
    var nome: String = "",
    var areeLavoro: List<AreaLavoro> = emptyList(),
    var turniFrequenti: List<TurnoFrequente> = emptyList(),



    // Informazioni derivate
    val numeroDipendenti: Int = 0,
    val descrizioneAzienda: String = ""
)