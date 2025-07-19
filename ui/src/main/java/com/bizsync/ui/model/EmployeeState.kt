package com.bizsync.ui.model

import com.bizsync.ui.viewmodels.DettagliGiornalieri
import com.bizsync.ui.viewmodels.StatisticheSettimanali


import com.bizsync.domain.model.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class EmployeeState(
    // Dati utente corrente
    val currentUser: User? = null,
    val contrattoEmployee: Contratto? = null,
    val dipartimentoEmployee: AreaLavoro? = null,

    // Turni employee
    val turniEmployee: List<Turno> = emptyList(),
    val turniSettimanali: Map<DayOfWeek, List<Turno>> = emptyMap(),
    val turniGiornalieri: List<Turno> = emptyList(),
    val turnoSelezionato: Turno? = null,

    // Data selezionata e dettagli
    val dataSelezionata: LocalDate? = null,
    val dettagliGiornalieri: DettagliGiornalieri? = null,
    val statisticheSettimanali: StatisticheSettimanali? = null,

    // Colleghi
    val colleghiTurno: List<User> = emptyList(),

    // Assenze employee
    val assenzeEmployee: List<Absence> = emptyList(),

    // Dialog states
    val showDialogDettagliTurno: Boolean = false,

    // Loading e messaggi
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Validazione
    val isFormValid: Boolean = false,
    val validationErrors: List<String> = emptyList()
) {
    // Computed properties
    val hasError: Boolean get() = errorMessage != null
    val hasSuccess: Boolean get() = successMessage != null
    val hasTurnoOggi: Boolean get() = turniEmployee.any { it.data == LocalDate.now() }
    val hasTurniGiornalieri: Boolean get() = turniGiornalieri.isNotEmpty()
}