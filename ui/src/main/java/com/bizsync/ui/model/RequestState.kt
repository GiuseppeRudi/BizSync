package com.bizsync.ui.model

import com.bizsync.domain.model.Contratto
import com.bizsync.ui.components.DialogStatusType



data class RequestState(
    val pendingRequests : List<AbsenceUi> = emptyList(),
    val historyRequests:  List<AbsenceUi> = emptyList(),
    val hasLoadedAbsences: Boolean = false,
    val resultMsg: String? = null,
    val contracts : List<Contratto> = emptyList(),
    val statusMsg: DialogStatusType = DialogStatusType.ERROR,
)
