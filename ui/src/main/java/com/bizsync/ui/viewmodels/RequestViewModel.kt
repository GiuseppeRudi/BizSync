package com.bizsync.ui.viewmodels

import com.bizsync.backend.repository.AbsenceRepository
import com.bizsync.ui.model.RequestState


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.sealedClass.Resource
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
    private val absenceRepository: AbsenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestState())
    val uiState: StateFlow<RequestState> = _uiState


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


    fun handleRequestDecision(approver : String , request: AbsenceUi, isApproved: Boolean, comment: String) {
        viewModelScope.launch {
            // Aggiorna la richiesta con lo stato deciso e commento
            val updatedRequest = request.copy(
                statusUi = if (isApproved) AbsenceStatus.APPROVED.toUiData()
                else AbsenceStatus.REJECTED.toUiData(),
                comments = comment,
                approver = approver,
                approvedDate = LocalDate.now()
            )

            // Chiama il repository per aggiornare su Firestore
            when (val result = absenceRepository.updateAbsence(updatedRequest.toDomain())) {
                is Resource.Success -> {
                    // Aggiorna UI: rimuovi da pending, aggiungi a history
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
