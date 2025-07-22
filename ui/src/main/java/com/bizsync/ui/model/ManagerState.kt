package com.bizsync.ui.model


import com.bizsync.domain.model.Absence
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.model.DipendentiGiorno
import com.bizsync.domain.model.Pausa
import com.bizsync.domain.model.StatoSettimanaleDipendente
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import com.bizsync.ui.components.DialogStatusType
import java.time.DayOfWeek
import java.time.LocalDate


// Aggiungi queste proprietà al ManagerState esistente
data class ManagerState(
    // Proprietà esistenti
    val turniSettimanali: Map<DayOfWeek, List<Turno>> = emptyMap(),
    val turniGiornalieri: Map<String, List<Turno>> = emptyMap(),
    val turniGiornalieriDip: List<Turno> = emptyList(),
    val showDialogCreateShift: Boolean = false,


    // dipendentiT
    val dipendenti : List<User> = emptyList(),

    val dipendentiSettimana: Map<DayOfWeek, DipendentiGiorno> = emptyMap(),
    val statoSettimanaleDipendenti: Map<String, StatoSettimanaleDipendente> = emptyMap(),
    val assenze : List<Absence> = emptyList(),
    val contratti : List<Contratto> = emptyList(),
    val disponibilitaMembriTurno : DipendentiGiorno = DipendentiGiorno(),

    val turnoInModifica: Turno = Turno(),
    val hasChangeShift : Boolean = false,
    val loading: Boolean = true,

    // Nuove proprietà per gestione messaggi
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Nuove proprietà per form validation
    val isFormValid: Boolean = false,
    val validationErrors: List<String> = emptyList(),

    // NUOVE PROPRIETÀ PER GESTIONE PAUSE
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