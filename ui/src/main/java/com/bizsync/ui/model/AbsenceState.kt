package com.bizsync.ui.state

import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.model.AbsenceUi

data class AbsenceState(
    val absences: List<AbsenceUi> = emptyList(),
    val hasLoadedAbsences: Boolean = false,
    val resultMsg: String? = null,
    val statusMsg: DialogStatusType = DialogStatusType.ERROR,
    val addAbsence: AbsenceUi = AbsenceUi(),

    // Nuove proprietà per la UI
    val isFullDay: Boolean = true,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false
)
