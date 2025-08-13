package com.bizsync.ui.viewmodels

import com.bizsync.ui.model.RequestState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.SickLeaveStatus
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.model.RequestDecisionResult
import com.bizsync.domain.usecases.FetchAllContractsUseCase
import com.bizsync.domain.usecases.FetchAllRequestsUseCase
import com.bizsync.domain.usecases.HandleRequestDecisionUseCase
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.mapper.toUi
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.model.AbsenceUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bizsync.domain.model.RequestsData
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import com.bizsync.domain.usecases.CheckEmployeeAvailabilityUseCase
import com.bizsync.domain.usecases.DeleteTurnoImmediateUseCase
import com.bizsync.domain.usecases.GetDipendentiFullUseCase
import com.bizsync.domain.usecases.GetTurniByDateRangeUseCase
import com.bizsync.domain.usecases.UpdateTurnoImmediateUseCase
import com.bizsync.ui.mapper.toUiData
import java.time.LocalDate

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val fetchAllRequestsUseCase: FetchAllRequestsUseCase,
    private val fetchAllContractsUseCase: FetchAllContractsUseCase,
    private val handleRequestDecisionUseCase: HandleRequestDecisionUseCase,
    private val getTurniByDateRangeUseCase: GetTurniByDateRangeUseCase,
    private val getDipendentiFullUseCase: GetDipendentiFullUseCase,
    private val updateTurnoUseCase: UpdateTurnoImmediateUseCase,
    private val deleteTurnoUseCase: DeleteTurnoImmediateUseCase,
    private val checkEmployeeAvailabilityUseCase: CheckEmployeeAvailabilityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestState())
    val uiState: StateFlow<RequestState> = _uiState
    fun handleSickLeaveRequest(
        request: AbsenceUi,
        employeeContract: Contratto?
    ) {
        viewModelScope.launch {
            try {
                val startDate = request.startDate
                val endDate = request.endDate

                if (startDate == null || endDate == null) {
                    _uiState.update {
                        it.copy(
                            resultMsg = "Date non valide per la richiesta di malattia",
                            statusMsg = DialogStatusType.ERROR
                        )
                    }
                    return@launch
                }

                // Cerca turni assegnati nel periodo di malattia
                val affectedShifts = getTurniByDateRangeUseCase(
                    idAzienda = request.idAzienda,
                    startDate = startDate,
                    endDate = endDate,
                    idDipendente = request.idUser
                )

                if (affectedShifts.isNotEmpty()) {
                    // Caso 1: Ci sono turni da gestire
                    val availableEmployeesMap = mutableMapOf<String, List<User>>()

                    for (turno in affectedShifts) {
                        val allEmployees = getDipendentiFullUseCase()
                        val availableForShift = mutableListOf<User>()

                        for (employee in allEmployees) {
                            if (employee.uid != request.idUser) {
                                val isAvailable = checkEmployeeAvailabilityUseCase(
                                    employeeId = employee.uid,
                                    date = turno.data,
                                    startTime = turno.orarioInizio,
                                    endTime = turno.orarioFine
                                )

                                if (isAvailable) {
                                    availableForShift.add(employee)
                                }
                            }
                        }
                        availableEmployeesMap[turno.id] = availableForShift
                    }

                    _uiState.update {
                        it.copy(
                            affectedShifts = mapOf(request.id to affectedShifts),
                            availableEmployees = availableEmployeesMap,
                            sickLeaveStatus = mapOf(request.id to SickLeaveStatus.REQUIRES_SHIFT_MANAGEMENT)
                        )
                    }
                } else {
                    // ✅ Caso 2: Nessun turno coinvolto - SEGNA COME VERIFICATA MA NON APPROVA
                    _uiState.update {
                        it.copy(
                            sickLeaveStatus = mapOf(request.id to SickLeaveStatus.VERIFIED_NO_SHIFTS),
                            affectedShifts = mapOf(request.id to emptyList())
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        resultMsg = "Errore nella gestione della malattia: ${e.message}",
                        statusMsg = DialogStatusType.ERROR
                    )
                }
            }
        }
    }

    fun uncoverShift(turno: Turno) {
        viewModelScope.launch {
            try {
                // Cancella il turno sia dalla cache che dal remoto
                deleteTurnoUseCase(turno.id)

                // Aggiorna la lista dei turni affetti
                _uiState.update { state ->
                    val updatedAffectedShifts = state.affectedShifts.mapValues { entry ->
                        entry.value.filter { it.id != turno.id }
                    }
                    state.copy(affectedShifts = updatedAffectedShifts)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        resultMsg = "Errore nella cancellazione del turno: ${e.message}",
                        statusMsg = DialogStatusType.ERROR
                    )
                }
            }
        }
    }

    fun replaceEmployeeInShift(
        turno: Turno,
        oldEmployeeId: String,
        newEmployeeId: String
    ) {
        viewModelScope.launch {
            try {
                // Aggiorna il turno con il nuovo dipendente
                val updatedIdDipendenti = turno.idDipendenti
                    .filter { it != oldEmployeeId } + newEmployeeId

                val updatedZoneLavorative = turno.zoneLavorative.toMutableMap().apply {
                    remove(oldEmployeeId)
                    put(newEmployeeId, turno.getZonaLavorativaDipendente(oldEmployeeId))
                }

                val updatedTurno = turno.copy(
                    idDipendenti = updatedIdDipendenti,
                    zoneLavorative = updatedZoneLavorative,
                    updatedAt = LocalDate.now()
                )

                updateTurnoUseCase(updatedTurno)

                // Aggiorna UI
                _uiState.update { state ->
                    val updatedAffectedShifts = state.affectedShifts.mapValues { entry ->
                        entry.value.map { shift ->
                            if (shift.id == turno.id) updatedTurno else shift
                        }
                    }
                    state.copy(affectedShifts = updatedAffectedShifts)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        resultMsg = "Errore nella sostituzione del dipendente: ${e.message}",
                        statusMsg = DialogStatusType.ERROR
                    )
                }
            }
        }
    }

    fun approveSickLeave(
        approver: String,
        request: AbsenceUi,
        employeeContract: Contratto?
    ) {
        // copia il request con status approvato
        val approvedRequest = request.copy(
            statusUi = AbsenceStatus.APPROVED.toUiData(),
            approver = approver,
            approvedDate = LocalDate.now(),
            comments = "Malattia approvata automaticamente dal sistema"
        )

        // semplicemente delega a handleRequestDecision
        handleRequestDecision(
            approver = approver,
            request = approvedRequest,
            isApproved = true,
            comment = "Approvazione automatica malattia",
            employeeContract = employeeContract
        )
    }


    fun fetchAllContract(idAzienda: String) {
        viewModelScope.launch {
            try {
                // ✅ Usa Use Case invece di orchestrator diretto
                when (val result = fetchAllContractsUseCase(idAzienda)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(contracts = result.data)
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                resultMsg = result.message ?: "Errore nel caricamento contratti",
                                statusMsg = DialogStatusType.ERROR
                            )
                        }
                    }

                    is Resource.Empty -> {
                        _uiState.update {
                            it.copy(
                                contracts = emptyList(),
                                resultMsg = "Nessun contratto trovato",
                                statusMsg = DialogStatusType.SUCCESS
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        resultMsg = "Errore imprevisto: ${e.message}",
                        statusMsg = DialogStatusType.ERROR
                    )
                }
            }
        }
    }

    fun fetchAllRequests(idAzienda: String) {
        viewModelScope.launch {
            try {
                // ✅ Usa Use Case invece del repository diretto
                when (val result = fetchAllRequestsUseCase(idAzienda)) {
                    is Resource.Success -> {
                        val data  : RequestsData = result.data

                        _uiState.update {
                            it.copy(
                                pendingRequests = data.pendingRequests.map { absence -> absence.toUi() },
                                historyRequests = data.historyRequests.map { absence -> absence.toUi() },
                                hasLoadedAbsences = true,
                                resultMsg = null,
                                statusMsg = DialogStatusType.SUCCESS
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                hasLoadedAbsences = true,
                                resultMsg = result.message,
                                statusMsg = DialogStatusType.ERROR
                            )
                        }
                    }

                    is Resource.Empty -> {
                        _uiState.update {
                            it.copy(
                                pendingRequests = emptyList(),
                                historyRequests = emptyList(),
                                hasLoadedAbsences = true,
                                resultMsg = "Nessuna richiesta trovata",
                                statusMsg = DialogStatusType.SUCCESS
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        hasLoadedAbsences = true,
                        resultMsg = "Errore imprevisto: ${e.message}",
                        statusMsg = DialogStatusType.ERROR
                    )
                }
            }
        }
    }

    fun handleRequestDecision(
        approver: String,
        request: AbsenceUi,
        isApproved: Boolean,
        comment: String,
        employeeContract: Contratto?
    ) {
        viewModelScope.launch {
            try {
                // ✅ Usa Use Case invece della logica complessa nel ViewModel
                when (val result = handleRequestDecisionUseCase(
                    approver = approver,
                    request = request.toDomain(),
                    isApproved = isApproved,
                    comment = comment,
                    employeeContract = employeeContract
                )) {
                    is Resource.Success -> {
                        val decisionResult = result.data

                        if (decisionResult.contractUpdateSuccess || decisionResult.updatedContract == null) {
                            // ✅ Successo completo o nessun aggiornamento contratto necessario
                            updateUIAfterSuccessfulDecision(decisionResult)
                        } else {
                            // ✅ Richiesta aggiornata ma errore contratto
                            updateUIAfterPartialSuccess(decisionResult)
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                resultMsg = result.message ?: "Errore durante l'aggiornamento",
                                statusMsg = DialogStatusType.ERROR
                            )
                        }
                    }

                    is Resource.Empty -> {
                        _uiState.update {
                            it.copy(
                                resultMsg = "Risposta vuota dal server",
                                statusMsg = DialogStatusType.ERROR
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        resultMsg = "Errore imprevisto: ${e.message}",
                        statusMsg = DialogStatusType.ERROR
                    )
                }
            }
        }
    }

    private fun updateUIAfterSuccessfulDecision(result: RequestDecisionResult) {
        _uiState.update { currentState ->
            // Aggiorna contratti se necessario
            val updatedContracts = result.updatedContract?.let { updated ->
                currentState.contracts.map { contract ->
                    if (contract.id == updated.id) updated else contract
                }
            } ?: currentState.contracts


            // Aggiorna liste richieste
            val updatedRequestUi = result.updatedRequest.toUi()
            val newPending = currentState.pendingRequests.filter { r -> r.id != updatedRequestUi.id }
            val newHistory = currentState.historyRequests + updatedRequestUi

            currentState.copy(
                contracts = updatedContracts ,
                pendingRequests = newPending,
                historyRequests = newHistory,
                resultMsg = if (result.isApproved) {
                    if (result.updatedContract != null) {
                        "Richiesta approvata e contratto aggiornato con successo!"
                    } else {
                        "Richiesta approvata con successo!"
                    }
                } else {
                    "Richiesta rifiutata"
                },
                statusMsg = DialogStatusType.SUCCESS
            )
        }
    }

    private fun updateUIAfterPartialSuccess(result: RequestDecisionResult) {
        _uiState.update { currentState ->
            val updatedRequestUi = result.updatedRequest.toUi()
            val newPending = currentState.pendingRequests.filter { r -> r.id != updatedRequestUi.id }
            val newHistory = currentState.historyRequests + updatedRequestUi

            currentState.copy(
                pendingRequests = newPending,
                historyRequests = newHistory,
                resultMsg = "Richiesta approvata ma errore aggiornamento contratto: ${result.contractError}",
                statusMsg = DialogStatusType.ERROR
            )
        }
    }
}

