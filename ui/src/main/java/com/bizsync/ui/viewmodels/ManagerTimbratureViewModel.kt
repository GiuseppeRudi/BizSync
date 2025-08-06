package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.usecases.GetTimbratureByAziendaUseCase
import com.bizsync.domain.usecases.VerificaTimbrnaturaUseCase
import com.bizsync.ui.model.ManagerTimbratureState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ManagerTimbratureViewModel @Inject constructor(
    private val getTimbratureByAziendaUseCase: GetTimbratureByAziendaUseCase,
    private val verificaTimbrnaturaUseCase: VerificaTimbrnaturaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerTimbratureState())
    val uiState: StateFlow<ManagerTimbratureState> = _uiState.asStateFlow()

    fun loadTimbrature(idAzienda: String, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                idAzienda = idAzienda
            )

            when (val result = getTimbratureByAziendaUseCase(idAzienda, startDate, endDate)) {
                is Resource.Success -> {
                    val timbrature = result.data
                    val anomale = timbrature.filter { it.isAnomala() }
                    val daVerificare = timbrature.filter { !it.verificataDaManager }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        timbrature = timbrature,
                        timbratureAnomale = anomale,
                        timbratureDaVerificare = daVerificare,
                        selectedDate = startDate
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun verificaTimbratura(idTimbratura: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = verificaTimbrnaturaUseCase(idTimbratura)) {
                is Resource.Success -> {
                    // Ricarica le timbrature per aggiornare lo stato
                    _uiState.value.selectedDate?.let { date ->
                        loadTimbrature(
                            _uiState.value.idAzienda,
                            date,
                            date
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        showSuccess = true,
                        successMessage = "Timbratura verificata con successo",
                        selectedTimbratura = null // Chiudi il dialog automaticamente
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun selectTimbratura(timbratura: Timbratura) {
        _uiState.value = _uiState.value.copy(selectedTimbratura = timbratura)
    }

    fun clearSelectedTimbratura() {
        _uiState.value = _uiState.value.copy(selectedTimbratura = null)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun dismissSuccess() {
        _uiState.value = _uiState.value.copy(showSuccess = false)
    }
}

