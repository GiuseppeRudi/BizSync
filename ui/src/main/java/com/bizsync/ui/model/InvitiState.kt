package com.bizsync.ui.model

import com.bizsync.ui.components.DialogStatusType

data class InvitiState(
    val invites: List<InvitoUi> = emptyList(),
    val isLoading: Boolean = true,
    val updateInvite : Boolean? = null,
    val statusMsg: DialogStatusType = DialogStatusType.ERROR,
    val resultMsg: String? = null
)
