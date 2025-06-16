package com.bizsync.ui.model

import com.bizsync.ui.components.DialogStatusType


data class UserState(
    val user : UserUi = UserUi(),
    val azienda : AziendaUi = AziendaUi(),
    val hasLoadedUser: Boolean = false,
    val hasLoadedAgency: Boolean = false,
    val resultMsg : String? = null,
    val statusMsg : DialogStatusType = DialogStatusType.ERROR,
    val checkUser : Boolean? = null,
    val checkAcceptInvite : Boolean = false
)