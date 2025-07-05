package com.bizsync.domain.model

data class Invito (
    val id: String,
    val aziendaNome: String,
    val email: String,
    val idAzienda: String,
    val manager : Boolean,
    val nomeRuolo : String,
    val stato : String,
    val ccnlInfo: Ccnlnfo
)
