package com.bizsync.ui.model

import com.bizsync.domain.constants.enumClass.AbsenceTimeType
import com.bizsync.domain.model.Contratto
import com.bizsync.ui.components.DialogStatusType

// Aggiungi questo al tuo AbsenceState
data class AbsenceState(
    val absences: List<AbsenceUi> = emptyList(),
    val hasLoadedAbsences: Boolean = false,
    val resultMsg: String? = null,
    val statusMsg: DialogStatusType = DialogStatusType.ERROR,
    val addAbsence: AbsenceUi = AbsenceUi(),

    val contract : Contratto? = null,

    val pendingStats: PendingStats = PendingStats(),

    // Proprietà per la UI
    val isFullDay: Boolean = true,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val showNewRequestDialog: Boolean = false,
    val selectedTab: Int = 0,

    // NUOVE PROPRIETÀ PER LA LOGICA
    val selectedTimeType: AbsenceTimeType = AbsenceTimeType.FULL_DAYS_ONLY,
    val isFlexibleModeFullDay: Boolean = true // Per PERSONAL_LEAVE e UNPAID_LEAVE
)

// Data class per le statistiche pending
data class PendingStats(
    val pendingVacationDays: Int = 0,
    val pendingRolHours: Int = 0,
    val pendingSickDays: Int = 0
)

