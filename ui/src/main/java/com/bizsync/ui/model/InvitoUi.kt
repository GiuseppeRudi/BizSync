package com.bizsync.ui.model

import com.bizsync.domain.constants.enumClass.StatusInvite


data class InvitoUi (
    val id: String = "",
    val aziendaNome: String = "",
    val email: String = "",
    val idAzienda: String = "",
    val manager : Boolean = false ,
    val nomeRuolo : String = "",
    val stato : StatusInvite = StatusInvite.INPENDING
)
