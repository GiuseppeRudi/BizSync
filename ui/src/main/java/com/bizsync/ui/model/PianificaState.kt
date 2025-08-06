package com.bizsync.ui.model


import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.WeeklyShift
import java.time.LocalDate
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.User

data class PianificaState(

    // Stati di caricamento e errori
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val loadingWeekly : Boolean = true,

    val hasUnsavedChanges: Boolean = false,
    val isSyncing: Boolean = false,

    // Stati onboarding
    val onBoardingDone: Boolean? = null,
    val showTurniDipartimento : Boolean = false,
    val dipartimento : AreaLavoro? = null,

    // Stati WeeklyShift
    val weeklyPlanningExists: Boolean? = null,
    val weeklyShiftRiferimento: WeeklyShift? = null,
    val weeklyShiftAttuale : WeeklyShift? = null,
    val weeklyisIdentical : Boolean = false,
    val canPublish: Boolean = false,
    val publishableWeek: LocalDate? = null,

    val dipendenti: List<User> = emptyList(),

    // Stati turni
    val selectionData: LocalDate? = null,
    val itemsList: List<Turno> = emptyList(),
    val showDialogShift: Boolean = false
)