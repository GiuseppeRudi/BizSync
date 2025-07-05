package com.bizsync.ui.model

import com.bizsync.domain.constants.enumClass.InviteView
import com.bizsync.domain.model.Ccnlnfo
import com.bizsync.ui.components.DialogStatusType


data class ManageInviteState(
    val currentView: InviteView = InviteView.SELECTION,
    val invites: List<InvitoUi> = emptyList(),

    val invite : InvitoUi = InvitoUi(),
    val resultMessage: String? = null,
    val ccnlnfo: Ccnlnfo = Ccnlnfo(),
    val isLoading: Boolean = false,
    val resultStatus: DialogStatusType = DialogStatusType.ERROR,
    var currentStep : Int = 1
)

