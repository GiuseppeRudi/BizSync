package com.bizsync.backend.dto

import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente
import com.google.firebase.firestore.Exclude

data class AziendaDto(

    @get:Exclude
    val id: String = "",
    val nome: String = "",
    val areeLavoro: List<AreaLavoro> = emptyList(),
    val turniFrequenti: List<TurnoFrequente> = emptyList(),
    val numDipendentiRange : String = "",
    val sector : String = ""
)
