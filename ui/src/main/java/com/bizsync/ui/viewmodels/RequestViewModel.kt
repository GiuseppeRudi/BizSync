package com.bizsync.ui.viewmodels

import android.util.Log
import com.bizsync.backend.repository.AbsenceRepository
import com.bizsync.ui.model.RequestState


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.orchestrator.ContrattoOrchestrator
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Contratto
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.mapper.toUi

import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.mapper.toUiData
import com.bizsync.ui.model.AbsenceUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val absenceRepository: AbsenceRepository,
    private val contractOrchestrator: ContrattoOrchestrator
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestState())
    val uiState: StateFlow<RequestState> = _uiState


    fun fetchAllContract(idAzienda: String)
    {
        viewModelScope.launch {
           when ( val result = contractOrchestrator.getContratti(idAzienda)){
               is Resource.Success -> {
                   _uiState.update {
                       it.copy(
                           contracts = result.data
                       )
                   }
               }

               Resource.Empty -> TODO()
               is Resource.Error -> TODO()
           }
        }

    }
    fun fetchAllRequests(idAzienda: String) {
        viewModelScope.launch {
            when (val result = absenceRepository.getAllAbsencesByAzienda(idAzienda)) {
                is Resource.Success -> {
                    val allAbsences = result.data.map { it.toUi() }
                    val pending = allAbsences.filter {
                        it.statusUi == AbsenceStatus.PENDING.toUiData()
                    }
                    val history = allAbsences.filter {
                        it.statusUi != AbsenceStatus.PENDING.toUiData()
                    }
                    _uiState.update {
                        it.copy(
                            pendingRequests = pending,
                            historyRequests = history,
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
                            resultMsg = result.message ?: "Errore nel caricamento delle richieste",
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
                            statusMsg = DialogStatusType.ERROR
                        )
                    }
                }
            }
        }
    }



    // Funzione helper per aggiornare l'UI dopo la decisione
    private fun updateUIAfterDecision(updatedRequest: AbsenceUi) {
        _uiState.update { currentState ->
            val newPending = currentState.pendingRequests.filter { r -> r.id != updatedRequest.id }
            val newHistory = currentState.historyRequests + updatedRequest

            currentState.copy(
                pendingRequests = newPending,
                historyRequests = newHistory,
                resultMsg = if (updatedRequest.statusUi.status == AbsenceStatus.APPROVED) {
                    "Richiesta approvata con successo"
                } else {
                    "Richiesta rifiutata"
                },
                statusMsg = DialogStatusType.SUCCESS
            )
        }
    }

    // Nel RequestViewModel - ANCORA PIÙ SEMPLICE
    fun handleRequestDecision(
        approver: String,
        request: AbsenceUi,
        isApproved: Boolean,
        comment: String,
        employeeContract: Contratto?
    ) {
        viewModelScope.launch {
            val updatedRequest = request.copy(
                statusUi = if (isApproved) AbsenceStatus.APPROVED.toUiData() else AbsenceStatus.REJECTED.toUiData(),
                comments = comment,
                approver = approver,
                approvedDate = LocalDate.now()
            )

            try {
                // 1. Aggiorna la richiesta di assenza
                when (val result = absenceRepository.updateAbsence(updatedRequest.toDomain())) {
                    is Resource.Success -> {
                        // 2. Se approvata e ha contratto, aggiorna il contratto
                        if (isApproved && employeeContract != null) {
                            val updatedContract = updateContractForApprovedAbsence(employeeContract, updatedRequest)

                            if (updatedContract != employeeContract) {
                                // 3. SEMPLICISSIMO: Update Firebase + Force Sync
                                when (val contractResult = contractOrchestrator.updateContratto(updatedContract)) {
                                    is Resource.Success -> {
                                        Log.d("REQUEST_DECISION", "✅ Contratto aggiornato e sincronizzato")

                                        // 4. Aggiorna UI con i nuovi dati
                                        updateUIAfterSuccessfulApproval(updatedRequest, updatedContract)
                                    }
                                    is Resource.Error -> {
                                        Log.e("REQUEST_DECISION", "❌ Errore aggiornamento contratto: ${contractResult.message}")

                                        // Anche se il contratto fallisce, la richiesta è stata approvata
                                        _uiState.update {
                                            it.copy(
                                                resultMsg = "Richiesta approvata ma errore aggiornamento contratto: ${contractResult.message}",
                                                statusMsg = DialogStatusType.ERROR
                                            )
                                        }
                                    }

                                    Resource.Empty -> TODO()
                                }
                            } else {
                                // Nessun aggiornamento contratto necessario (es. PERSONAL_LEAVE)
                                updateUIAfterDecision(updatedRequest)
                            }
                        } else {
                            // Richiesta rifiutata o senza contratto
                            updateUIAfterDecision(updatedRequest)
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

                    Resource.Empty -> TODO()
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

    // Funzione helper per aggiornare UI dopo successo completo
    private fun updateUIAfterSuccessfulApproval(updatedRequest: AbsenceUi, updatedContract: Contratto) {
        _uiState.update { currentState ->
            // Aggiorna la lista dei contratti con quello nuovo
            val updatedContracts = currentState.contracts.map { contract ->
                if (contract.id == updatedContract.id) updatedContract else contract
            }

            val newPending = currentState.pendingRequests.filter { r -> r.id != updatedRequest.id }
            val newHistory = currentState.historyRequests + updatedRequest

            currentState.copy(
                contracts = updatedContracts,
                pendingRequests = newPending,
                historyRequests = newHistory,
                resultMsg = "Richiesta approvata e contratto aggiornato con successo!",
                statusMsg = DialogStatusType.SUCCESS
            )
        }
    }
    // Funzione helper per aggiornare il contratto quando si approva
    private fun updateContractForApprovedAbsence(
        contract: Contratto,
        approvedAbsence: AbsenceUi
    ): Contratto {
        return when (approvedAbsence.typeUi.type) {
            AbsenceType.VACATION -> {
                val daysToAdd = approvedAbsence.totalDays ?: 0
                contract.copy(ferieUsate = contract.ferieUsate + daysToAdd)
            }

            AbsenceType.ROL -> {
                val hoursToAdd = approvedAbsence.totalHours ?: 0
                contract.copy(rolUsate = contract.rolUsate + hoursToAdd)
            }

            AbsenceType.SICK_LEAVE -> {
                val daysToAdd = approvedAbsence.totalDays ?: 0
                contract.copy(malattiaUsata = contract.malattiaUsata + daysToAdd)
            }

            AbsenceType.PERSONAL_LEAVE,
            AbsenceType.UNPAID_LEAVE,
            AbsenceType.STRIKE -> {
                // Questi tipi non aggiornano i limiti del contratto
                contract
            }
        }
    }


    fun handleRequestDecision(approver : String , request: AbsenceUi, isApproved: Boolean, comment: String) {
        viewModelScope.launch {

            val updatedRequest = request.copy(
                statusUi = if (isApproved) AbsenceStatus.APPROVED.toUiData() else AbsenceStatus.REJECTED.toUiData(),
                comments = comment,
                approver = approver,
                approvedDate = LocalDate.now()
            )

            when (val result = absenceRepository.updateAbsence(updatedRequest.toDomain())) {
                is Resource.Success -> {
                    _uiState.update {
                        val newPending = it.pendingRequests.filter { r -> r.id != updatedRequest.id }
                        val newHistory = it.historyRequests + updatedRequest
                        it.copy(
                            pendingRequests = newPending,
                            historyRequests = newHistory,
                            resultMsg = "Richiesta aggiornata con successo",
                            statusMsg = DialogStatusType.SUCCESS
                        )
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
                else -> {
                    // Puoi gestire anche Resource.Empty se serve
                }
            }
        }
    }



}
