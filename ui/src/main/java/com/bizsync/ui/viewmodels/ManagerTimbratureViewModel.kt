package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.TimbraturaRepository
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Timbratura
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ManagerTimbratureViewModel @Inject constructor(
    private val timbraturaRepository: TimbraturaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerTimbratureUiState())
    val uiState: StateFlow<ManagerTimbratureUiState> = _uiState.asStateFlow()

    fun loadTimbrature(idAzienda: String, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = timbraturaRepository.getTimbratureByAzienda(
                idAzienda, startDate, endDate
            )) {
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
            when (val result = timbraturaRepository.verificaTimbratura(idTimbratura)) {
                is Resource.Success -> {
                    // Ricarica le timbrature
                    _uiState.value.selectedDate?.let { date ->
                        loadTimbrature(
                            _uiState.value.idAzienda,
                            date,
                            date
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        showSuccess = true,
                        successMessage = "Timbratura verificata"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun selectTimbratura(timbratura: Timbratura) {
        _uiState.value = _uiState.value.copy(selectedTimbratura = timbratura)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun dismissSuccess() {
        _uiState.value = _uiState.value.copy(showSuccess = false)
    }
}

data class ManagerTimbratureUiState(
    val idAzienda: String = "",
    val timbrature: List<Timbratura> = emptyList(),
    val timbratureAnomale: List<Timbratura> = emptyList(),
    val timbratureDaVerificare: List<Timbratura> = emptyList(),
    val selectedTimbratura: Timbratura? = null,
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showSuccess: Boolean = false,
    val successMessage: String = ""
)