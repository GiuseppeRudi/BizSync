package com.bizsync.backend.dto

import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente
import com.google.firebase.firestore.Exclude

data class AziendaDto(

    @get:Exclude
    val id: String? = null,

    val nome: String? = null,
    val aree_lavoro: List<AreaLavoro>? = null,
    val turni_frequenti: List<TurnoFrequente>? = null
)
