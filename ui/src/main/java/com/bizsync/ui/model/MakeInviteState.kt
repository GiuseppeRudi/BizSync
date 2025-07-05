package com.bizsync.ui.model

import com.bizsync.domain.model.Ccnlnfo
import com.bizsync.ui.components.DialogStatusType


data class MakeInviteState(
    val invite : InvitoUi = InvitoUi(),
    val resultMessage: String? = null,
    val ccnlnfo: Ccnlnfo = Ccnlnfo(),
    val isLoadingCcnl: Boolean = false,
    val resultStatus: DialogStatusType = DialogStatusType.ERROR,
    var currentStep : Int = 1
)

