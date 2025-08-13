package com.bizsync.ui.model

import com.bizsync.domain.constants.enumClass.SickLeaveStatus
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import com.bizsync.ui.components.DialogStatusType



data class RequestState(
    val pendingRequests : List<AbsenceUi> = emptyList(),
    val historyRequests:  List<AbsenceUi> = emptyList(),
    val hasLoadedAbsences: Boolean = false,
    val resultMsg: String? = null,
    val contracts : List<Contratto> = emptyList(),
    val statusMsg: DialogStatusType = DialogStatusType.ERROR,

    val affectedShifts: Map<String, List<Turno>> = emptyMap(),
    val availableEmployees: Map<String, List<User>> = emptyMap(),
    val sickLeaveStatus: Map<String, SickLeaveStatus> = emptyMap(),

    )
