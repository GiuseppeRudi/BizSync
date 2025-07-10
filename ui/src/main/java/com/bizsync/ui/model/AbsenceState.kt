package com.bizsync.ui.model

import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.model.AbsenceUi

// Aggiungi questo al tuo AbsenceState
data class AbsenceState(
    val absences: List<AbsenceUi> = emptyList(),
    val hasLoadedAbsences: Boolean = false,
    val resultMsg: String? = null,
    val statusMsg: DialogStatusType = DialogStatusType.ERROR,
    val addAbsence: AbsenceUi = AbsenceUi(),

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
// Enum per i tipi di selezione temporale
enum class AbsenceTimeType {
    FULL_DAYS_ONLY,      // Solo giorni interi (VACATION, SICK_LEAVE, STRIKE)
    HOURLY_SINGLE_DAY,   // Solo fascia oraria su singolo giorno (ROL)
    FLEXIBLE             // Scelta tra giorni interi o fascia oraria (PERSONAL_LEAVE, UNPAID_LEAVE)
}