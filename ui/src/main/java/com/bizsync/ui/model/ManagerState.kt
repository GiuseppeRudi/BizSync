package com.bizsync.ui.model


import com.bizsync.domain.model.Absence
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.model.DipendentiGiorno
import com.bizsync.domain.model.Pausa
import com.bizsync.domain.model.StatoSettimanaleDipendente
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import java.time.DayOfWeek


data class ManagerState(
    val turniSettimanali: Map<DayOfWeek, List<Turno>> = emptyMap(),
    val turniGiornalieri: Map<String, List<Turno>> = emptyMap(),
    val turniGiornalieriDip: List<Turno> = emptyList(),
    val showDialogCreateShift: Boolean = false,

    val turnoToDelete: Turno? = null,
    val showDeleteConfirmDialog: Boolean = false,

    val dipendenti : List<User> = emptyList(),

    val dipendentiSettimana: Map<DayOfWeek, DipendentiGiorno> = emptyMap(),
    val statoSettimanaleDipendenti: Map<String, StatoSettimanaleDipendente> = emptyMap(),
    val assenze : List<Absence> = emptyList(),
    val contratti : List<Contratto> = emptyList(),
    val disponibilitaMembriTurno : DipendentiGiorno = DipendentiGiorno(),

    val turnoInModifica: Turno = Turno(),
    val isLoadingTurni: Boolean = false,
    val hasChangeShift : Boolean = false,
    val loading: Boolean = true,

    val errorMessage: String? = null,
    val successMessage: String? = null,

    val isFormValid: Boolean = false,
    val validationErrors: List<String> = emptyList(),

    val pausaInModifica: Pausa? = null,
    val showPauseDialog: Boolean = false,
    val showAddEditPauseDialog: Boolean = false,


    val isGeneratingTurni: Boolean = false,
    val turniGeneratiAI: List<Turno> = emptyList(),
    val showAIResultDialog: Boolean = false,
    val aiGenerationMessage: String? = null
) {
    // Computed properties
    val hasError: Boolean get() = errorMessage != null
    val hasSuccess: Boolean get() = successMessage != null
    val isModificaTurno: Boolean get() = turnoInModifica.id.isNotEmpty()
    val isNuovoTurno: Boolean get() = turnoInModifica.id.isEmpty()


}