package com.bizsync.domain.model

import java.time.LocalDate

data class Invito (
    val id: String,
    val aziendaNome: String,
    val email: String,
    val idAzienda: String,
    val manager : Boolean,
    val nomeRuolo : String,
    val settoreAziendale : String,
    val stato : String,
    val ccnlInfo: Ccnlnfo,
    val sentDate: LocalDate,
    val acceptedDate: LocalDate?,
    val dipartimento: String = "",
    val tipoContratto: String = "",
    val oreSettimanali: String = ""
)
