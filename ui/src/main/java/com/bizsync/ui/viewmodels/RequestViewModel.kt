package com.bizsync.ui.viewmodels

import com.bizsync.ui.model.RequestState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
@HiltViewModel
class RequestViewModel @Inject constructor(
    private val fetchAllRequestsUseCase: FetchAllRequestsUseCase,
    private val fetchAllContractsUseCase: FetchAllContractsUseCase,
    private val handleRequestDecisionUseCase: HandleRequestDecisionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestState())
    val uiState: StateFlow<RequestState> = _uiState

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

