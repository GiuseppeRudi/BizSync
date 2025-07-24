package com.bizsync.ui.model

import com.bizsync.domain.constants.enumClass.StatusInvite
import com.bizsync.domain.model.Ccnlnfo

data class InvitoUi (
    val id: String = "",
    val aziendaNome: String = "",
    val email: String = "",
    val idAzienda: String = "",
    val manager: Boolean = false,

    val posizioneLavorativa: String = "",
    val dipartimento: String = "",
    val settoreAziendale: String = "",
    val tipoContratto: String = "",
    val oreSettimanali: String = "",

    val ccnlInfo : Ccnlnfo = Ccnlnfo(),

    val sentDate: String = "",
    val acceptedDate: String = "",

    val stato: StatusInvite = StatusInvite.PENDING
)
