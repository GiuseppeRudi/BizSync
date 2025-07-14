package com.bizsync.ui.model


import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.WeeklyShift
import java.time.LocalDate
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import com.bizsync.domain.model.AreaLavoro

data class PianificaState(
    // Stati di caricamento e errori
    val isLoading: Boolean = false,
    val errorMsg: String? = null,

    // Stati onboarding (esistenti)
    val onBoardingDone: Boolean? = null,
    val showTurniDipartimento : Boolean = false,
    val dipartimento : AreaLavoro? = null,

    // Stati WeeklyShift (nuovi)
    val weeklyPlanningExists: Boolean? = null,
    val currentWeeklyShift: WeeklyShift? = null,
    val canPublish: Boolean = false,
    val publishableWeek: LocalDate? = null,

    // Stati turni (esistenti)
    val selectionData: LocalDate? = null,
    val itemsList: List<Turno> = emptyList(),
    val showDialogShift: Boolean = false
) {
    // Computed properties per facilitare l'uso nell'UI

    /**
     * Indica se si può procedere con la pianificazione
     */
    val canProceedWithPlanning: Boolean
        get() = onBoardingDone == true && weeklyPlanningExists == true

    /**
     * Indica se bisogna mostrare la schermata di setup
     */
    val shouldShowSetup: Boolean
        get() = onBoardingDone == false

    /**
     * Indica se bisogna mostrare la schermata "Inizia Pubblicazione"
     */
    val shouldShowStartPublication: Boolean
        get() = onBoardingDone != false && weeklyPlanningExists == false && canPublish

    /**
     * Indica se bisogna mostrare PianificaCore
     */
    val shouldShowCore: Boolean
        get() = canProceedWithPlanning

    /**
     * Stato corrente della pianificazione come stringa
     */
    val currentPlanningStatusText: String
        get() = when (currentWeeklyShift?.status) {
            WeeklyShiftStatus.IN_PROGRESS -> "In preparazione"
            WeeklyShiftStatus.PUBLISHED -> "Pubblicata"
            WeeklyShiftStatus.FINALIZED -> "Finalizzata"
            null -> "Nessuna pianificazione"
        }

    /**
     * Indica se la pianificazione corrente può essere modificata
     */
    val canModifyCurrentPlanning: Boolean
        get() = currentWeeklyShift?.status == WeeklyShiftStatus.IN_PROGRESS

    /**
     * Indica se la pianificazione corrente può essere pubblicata
     */
    val canPublishCurrentPlanning: Boolean
        get() = currentWeeklyShift?.status == WeeklyShiftStatus.IN_PROGRESS

    /**
     * Indica se la pianificazione corrente può essere finalizzata
     */
    val canFinalizeCurrentPlanning: Boolean
        get() = currentWeeklyShift?.status == WeeklyShiftStatus.PUBLISHED

    /**
     * Indica se la pianificazione corrente può essere eliminata
     */
    val canDeleteCurrentPlanning: Boolean
        get() = currentWeeklyShift?.status == WeeklyShiftStatus.IN_PROGRESS

    /**
     * Settimana corrente formattata come stringa
     */
    val currentWeekFormatted: String?
        get() = currentWeeklyShift?.let { weeklyShift ->
            val start = weeklyShift.weekStart
            val end = start.plusDays(6)
            "${start.dayOfMonth}/${start.monthValue} - ${end.dayOfMonth}/${end.monthValue}/${end.year}"
        }

    /**
     * Indica se ci sono errori da mostrare
     */
    val hasError: Boolean
        get() = errorMsg != null
}