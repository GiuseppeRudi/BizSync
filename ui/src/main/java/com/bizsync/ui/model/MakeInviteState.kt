package com.bizsync.ui.model

import com.bizsync.ui.components.DialogStatusType


data class MakeInviteState(
    val invite : InvitoUi = InvitoUi(),
    val resultMessage: String? = null,
    val resultStatus: DialogStatusType? = null
)

