package com.bizsync.ui.model

import com.bizsync.ui.components.DialogStatusType



data class RequestState(
    val pendingRequests : List<AbsenceUi> = emptyList(),
    val historyRequests:  List<AbsenceUi> = emptyList(),
    val hasLoadedAbsences: Boolean = false,
    val resultMsg: String? = null,
    val statusMsg: DialogStatusType = DialogStatusType.ERROR,
)
