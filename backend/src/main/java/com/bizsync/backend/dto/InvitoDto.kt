package com.bizsync.backend.dto

import com.bizsync.domain.model.Ccnlnfo
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class InvitoDto(
    @get:Exclude
    val id: String = "",

    val aziendaNome: String = "",
    val email: String = "",
    val idAzienda: String = "",
    val manager: Boolean = false,
    val nomeRuolo: String = "",
    val stato: String = "PENDING",
    val ccnlInfo: Ccnlnfo = Ccnlnfo(),
    val sentDate: Timestamp = Timestamp(0, 0),
    val acceptedDate: Timestamp = Timestamp(0, 0),

    val settoreAziendale: String = "",
    val dipartimento: String = "",
    val tipoContratto: String = "",
    val oreSettimanali: String = ""
)
