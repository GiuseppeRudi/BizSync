package com.bizsync.ui.model

import com.bizsync.domain.constants.enumClass.StatusInvite
import com.bizsync.domain.model.Ccnlnfo

data class InvitoUi (
    val id: String = "",
    val aziendaNome: String = "",
    val email: String = "",
    val idAzienda: String = "",
    val manager: Boolean = false,

    // Campi modificati/aggiunti
    val posizioneLavorativa: String = "", // era nomeRuolo
    val dipartimento: String = "", // nuovo - selezionato dalle AreaLavoro
    val settoreAziendale: String = "", // nuovo - preso dall'oggetto Azienda
    val tipoContratto: String = "", // nuovo - Full Time, Part Time, ecc.
    val oreSettimanali: String = "", // nuovo - ore settimanali di lavoro

    val ccnlInfo : Ccnlnfo = Ccnlnfo(),

    val stato: StatusInvite = StatusInvite.INPENDING
)
