package com.bizsync.ui.model

import com.bizsync.domain.model.Invito
import com.bizsync.ui.components.DialogStatusType

data class InvitiState(
    val invites: List<Invito> = emptyList(),
    val isLoading: Boolean = true,
    val updateInvte : Boolean? = null,
    val statusMsg: DialogStatusType = DialogStatusType.ERROR,
    val resultMsg: String? = null
)
