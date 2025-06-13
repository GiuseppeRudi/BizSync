package com.bizsync.ui.model

import com.bizsync.domain.model.Azienda

data class UserState(
    val user : UserUi,
    val azienda : AziendaUi,
    val check : Boolean? = null
)