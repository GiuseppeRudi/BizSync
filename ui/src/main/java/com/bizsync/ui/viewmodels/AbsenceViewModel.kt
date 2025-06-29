package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AbsenceRepository
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.ui.mapper.toUiData
import com.bizsync.ui.model.AbsenceTypeUi
import com.bizsync.ui.model.AbsenceUi
import com.bizsync.ui.state.AbsenceState
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.mapper.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AbsenceViewModel @Inject constructor(private val absenceRepository: AbsenceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AbsenceState())
    val uiState: StateFlow<AbsenceState> = _uiState



    fun saveAbsence(fullName : String,idAzienda: String, idUser: String) {
        viewModelScope.launch {
            // Aggiorna lo stato con la data di invio e status PENDING
            _uiState.update {
                it.copy(addAbsence = it.addAbsence.copy(
                    statusUi = AbsenceStatus.PENDING.toUiData(),
                    submittedDate = LocalDate.now(),
                    idAzienda = idAzienda,
                    idUser = idUser,
                    submittedName = fullName
                ))
            }

            val absence = _uiState.value.addAbsence

            // Chiamata repository e cattura risultato
            when (val result = absenceRepository.salvaAbsence(absence.toDomain())) {
                is Resource.Success -> {

                    val updatedAbsence = _uiState.value.addAbsence.copy(id = result.data)

                    // Aggiungi la nuova assenza alla lista, reset addAbsence e mostra messaggio successo
                    _uiState.update { currentState ->
                        currentState.copy(
                            absences = currentState.absences + updatedAbsence, // oppure aggiorna da repo se preferisci
                            addAbsence = AbsenceUi(), // reset form
                            resultMsg = "Richiesta di assenza inviata con successo",
                            statusMsg = DialogStatusType.SUCCESS
                        )
                    }
                }
                is Resource.Error -> {
                    // Mostra messaggio di errore
                    _uiState.update {
                        it.copy(
                            resultMsg = result.message ?: "Errore sconosciuto durante il salvataggio",
                            statusMsg = DialogStatusType.ERROR
                        )
                    }
                }
                is Resource.Empty -> {
                    // Gestisci caso vuoto se serve, altrimenti ignora o logga
                    _uiState.update {
                        it.copy(
                            resultMsg = "Nessun dato salvato",
                            statusMsg = DialogStatusType.ERROR
                        )
                    }
                }
            }
        }
    }

    fun fetchAllAbsences(idUser : String) {
        viewModelScope.launch {
            when (val result = absenceRepository.getAllAbsences(idUser)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            absences = result.data.map { absence -> absence.toUi() },
                            hasLoadedAbsences = true,
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            hasLoadedAbsences = true,
                            resultMsg = result.message ?: "Errore nel caricamento delle assenze",
                            statusMsg = DialogStatusType.ERROR
                        )
                    }
                }
                is Resource.Empty -> {
                    _uiState.update {
                        it.copy(
                            absences = emptyList(),
                            hasLoadedAbsences = true,
                        )
                    }
                }
            }
        }
    }


    // Funzione per mostrare/nascondere dialog
    fun setShowNewRequestDialog(show: Boolean) {
        _uiState.update { it.copy(showNewRequestDialog = show) }
    }

    // Funzione per cambiare tab selezionata
    fun setSelectedTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }


    fun updateAddAbsenceType(typeUi: AbsenceTypeUi) {
        _uiState.value = _uiState.value.copy(
            addAbsence = _uiState.value.addAbsence.copy(typeUi = typeUi)
        )
    }


    fun updateAddAbsenceStartDate(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(
            addAbsence = _uiState.value.addAbsence.copy(startDate = date)
        )
    }

    fun updateAddAbsenceEndDate(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(
            addAbsence = _uiState.value.addAbsence.copy(endDate = date)
        )
    }


    fun updateAddAbsenceStartTime(time: LocalTime?) {
        _uiState.value = _uiState.value.copy(
            addAbsence = _uiState.value.addAbsence.copy(startTime = time)
        )
    }

    fun updateAddAbsenceEndTime(time: LocalTime?) {
        _uiState.value = _uiState.value.copy(
            addAbsence = _uiState.value.addAbsence.copy(endTime = time)
        )
    }


    fun updateAddAbsenceReason(reason: String) {
        _uiState.value = _uiState.value.copy(
            addAbsence = _uiState.value.addAbsence.copy(reason = reason)
        )
    }

    fun updateAddAbsenceComments(comments: String) {
        _uiState.value = _uiState.value.copy(
            addAbsence = _uiState.value.addAbsence.copy(comments = comments)
        )
    }

    fun updateAddAbsenceTotalDays(totalDays: String) {
        _uiState.value = _uiState.value.copy(
            addAbsence = _uiState.value.addAbsence.copy(totalDays = totalDays)
        )
    }


    fun calculateTotalDays(
        startDate: LocalDate?,
        endDate: LocalDate?,
        isFullDay: Boolean,
        startTime: LocalTime? = null,
        endTime: LocalTime? = null
    ): String {
        return try {
            if (startDate == null || endDate == null) return "0 giorni"

            val days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
            if (isFullDay) {
                "$days giorni"
            } else if (startTime != null && endTime != null) {
                val duration = java.time.Duration.between(startTime, endTime).toHours()
                val totalHours = duration * days
                "$totalHours ore"
            } else {
                "$days giorni (parziale)"
            }
        } catch (e: Exception) {
            "0 giorni"
        }
    }


    fun setIsFullDay(value: Boolean) {
        _uiState.value = _uiState.value.copy(isFullDay = value)
    }

    fun setShowStartDatePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showStartDatePicker = show)
    }

    fun setShowEndDatePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showEndDatePicker = show)
    }


    fun resetAddAbsence() {
        _uiState.value = _uiState.value.copy(addAbsence = AbsenceUi())
    }

    fun clearResultMessage() {
        _uiState.value = _uiState.value.copy(resultMsg = null)
    }

    // Funzioni legacy mantenute
    fun addAbsence(newAbsence: AbsenceUi) {
        _uiState.value = _uiState.value.copy(
            absences = _uiState.value.absences + newAbsence
        )
    }

    fun updateAbsence(updated: AbsenceUi) {
        _uiState.value = _uiState.value.copy(
            absences = _uiState.value.absences.map {
                if (it.id == updated.id) updated else it
            }
        )
    }

    fun setError(message: String?) {
        _uiState.value = _uiState.value.copy(
            resultMsg = message,
            statusMsg = DialogStatusType.ERROR
        )
    }
}
