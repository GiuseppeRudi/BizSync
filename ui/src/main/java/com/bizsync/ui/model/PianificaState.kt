package com.bizsync.ui.model

import com.bizsync.domain.model.Turno
import java.time.LocalDate

data class PianificaState(
    val itemsList: List<Turno> = emptyList(),
    val onBoardingDone: Boolean? = null,
    val selectionData: LocalDate? = null,
    val showDialogShift: Boolean = false,
    val errorMsg : String? = null,
    val successMsg : String? = null,
)

