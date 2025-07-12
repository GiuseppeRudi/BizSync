package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AbsenceRepository
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.AbsenceTimeType
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Contratto
import com.bizsync.ui.mapper.toUiData
import com.bizsync.ui.model.AbsenceTypeUi
import com.bizsync.ui.model.AbsenceUi
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.mapper.toUi
import com.bizsync.ui.model.AbsenceState
import com.bizsync.ui.model.PendingStats
import com.bizsync.ui.model.getTimeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class AbsenceViewModel @Inject constructor(private val absenceRepository: AbsenceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AbsenceState())
    val uiState: StateFlow<AbsenceState> = _uiState

    fun updateAddAbsenceTotalHours(totalHours: Int?) {
        _uiState.update { currentState ->
            currentState.copy(
                addAbsence = currentState.addAbsence.copy(
                    totalHours = totalHours
                )
            )
        }
    }

    fun setFlexibleModeFullDay(isFullDay: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(isFlexibleModeFullDay = isFullDay)
        }
    }

    fun updateSelectedTimeType(timeType: AbsenceTimeType) {
        _uiState.update { currentState ->
            currentState.copy(selectedTimeType = timeType)
        }
    }

    fun updateAddAbsenceType(typeUi: AbsenceTypeUi) {
        val timeType = typeUi.type.getTimeType()

        _uiState.update { currentState ->
            currentState.copy(
                addAbsence = currentState.addAbsence.copy(typeUi = typeUi),
                selectedTimeType = timeType,
                isFlexibleModeFullDay = true
            )
        }

        resetDateTimeFields()
    }

    private fun resetDateTimeFields() {
        updateAddAbsenceStartDate(null)
        updateAddAbsenceEndDate(null)
        updateAddAbsenceStartTime(null)
        updateAddAbsenceEndTime(null)
        updateAddAbsenceTotalDays(null)
        updateAddAbsenceTotalHours(null)
    }

    fun saveAbsence(fullName : String,idAzienda: String, idUser: String, contratto : Contratto ) {
        viewModelScope.launch {

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

            when (val result = absenceRepository.salvaAbsence(absence.toDomain())) {
                is Resource.Success -> {

                    val updatedAbsence = _uiState.value.addAbsence.copy(id = result.data)

                    // Aggiorna il contratto in base al tipo di assenza
                    val updatedContract = when (updatedAbsence.typeUi.type) {
                        AbsenceType.VACATION -> {
                            // Ferie: aggiorna ferieUsate con i giorni
                            val daysToAdd = updatedAbsence.totalDays ?: 0
                            contratto.copy(ferieUsate = contratto.ferieUsate + daysToAdd)
                        }

                        AbsenceType.ROL -> {
                            // ROL: aggiorna rolUsate con le ore
                            val hoursToAdd = updatedAbsence.totalHours ?: 0
                            contratto.copy(rolUsate = contratto.rolUsate + hoursToAdd)
                        }

                        AbsenceType.SICK_LEAVE -> {
                            // Malattia: aggiorna malattiaUsata con i giorni
                            val daysToAdd = updatedAbsence.totalDays ?: 0
                            contratto.copy(malattiaUsata = contratto.malattiaUsata + daysToAdd)
                        }

                        else -> contratto
                    }

                        _uiState.update { currentState ->
                            currentState.copy(
                                absences = currentState.absences + updatedAbsence,
                                contract = updatedContract,
                                addAbsence = AbsenceUi(),
                                resultMsg = "Richiesta di assenza inviata con successo",
                                statusMsg = DialogStatusType.SUCCESS
                            )
                        }

                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            resultMsg = result.message ?: "Errore sconosciuto durante il salvataggio",
                            statusMsg = DialogStatusType.ERROR
                        )
                    }
                }
                is Resource.Empty -> {
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

    fun changePendingStatus() {
        _uiState.update { it.copy( pendingStats = calculatePendingStats(it.absences) ) }

    }

    // Funzione per calcolare le statistiche pending
    private fun calculatePendingStats(absences: List<AbsenceUi>): PendingStats {
        val pendingAbsences = absences.filter { it.statusUi.status == AbsenceStatus.PENDING }

        var pendingVacationDays = 0
        var pendingRolHours = 0
        var pendingSickDays = 0

        pendingAbsences.forEach { absence ->
            when (absence.typeUi.type) {
                AbsenceType.VACATION -> {
                    pendingVacationDays += absence.totalDays ?: 0
                }
                AbsenceType.ROL -> {
                    pendingRolHours += absence.totalHours ?: 0
                }
                AbsenceType.SICK_LEAVE -> {
                    pendingSickDays += absence.totalDays ?: 0
                }
                else -> {
                    // PERSONAL_LEAVE, UNPAID_LEAVE, STRIKE non contano per i limiti
                }
            }
        }

        return PendingStats(
            pendingVacationDays = pendingVacationDays,
            pendingRolHours = pendingRolHours,
            pendingSickDays = pendingSickDays
        )
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

    fun updateAddAbsenceTotalDays(totalDays: Int?) {
        _uiState.update { currentState ->
            currentState.copy(
                addAbsence = currentState.addAbsence.copy(
                    totalDays = totalDays
                )
            )
        }
    }

    // Crea un metodo helper per calcolare i giorni
    fun calculateTotalDaysInt(startDate: LocalDate, endDate: LocalDate): Int {
        return ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
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
