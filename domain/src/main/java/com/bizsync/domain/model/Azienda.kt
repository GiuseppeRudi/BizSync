package com.bizsync.domain.model


data class Azienda(
    val idAzienda: String,
    val nome: String,
    val areeLavoro: List<AreaLavoro>,
    val turniFrequenti: List<TurnoFrequente>
)
