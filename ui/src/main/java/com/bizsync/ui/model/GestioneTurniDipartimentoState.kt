package com.bizsync.ui.model

import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Turno
import com.bizsync.ui.viewmodels.DipartimentoStatus
import com.bizsync.ui.viewmodels.SuggerimentoTurno
import java.time.LocalDate

data class GestioneTurniDipartimentoState(

    val dipartimento: AreaLavoro = AreaLavoro(),
    val giornoSelezionato: LocalDate = LocalDate.now(),
    val turniAssegnati: List<Turno> = emptyList(),
    val stato: DipartimentoStatus = DipartimentoStatus.INCOMPLETE,
    val showTurnoDialog: Boolean = false,
    val turnoInModifica: Turno? = null,
    val isCompletato: Boolean = false,


    val errorMsg: String? = null,
    val isLoading: Boolean = false,

    ) {
    val hasError: Boolean
        get() = errorMsg != null

    val isDialogMode: Boolean
        get() = showTurnoDialog

    val isEditing: Boolean
        get() = turnoInModifica != null
}
