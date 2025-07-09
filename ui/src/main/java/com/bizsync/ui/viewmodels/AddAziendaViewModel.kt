package com.bizsync.ui.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.model.AddAziendaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlinx.coroutines.launch
import java.time.DayOfWeek

@HiltViewModel
class AddAziendaViewModel @Inject constructor(private val aziendaRepository: AziendaRepository): ViewModel() {

    private val _uiState = MutableStateFlow(AddAziendaState())
    val uiState: StateFlow<AddAziendaState> = _uiState

    fun aggiungiAzienda(idUtente: String) {
        viewModelScope.launch(Dispatchers.IO){
            val azienda = _uiState.value.azienda
            val result = aziendaRepository.creaAzienda(azienda.toDomain())

            when (result){
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            azienda = it.azienda.copy(idAzienda = result.data),
                            isAgencyAdded = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(resultMsg = result.message) }
                }
                else -> {
                    _uiState.update { it.copy(resultMsg = "Errore nella creazione dell'azienda") }
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(resultMsg = null)
    }

    fun onSectorChanged(newValue: String) {
        _uiState.value = _uiState.value.copy(
            azienda = _uiState.value.azienda.copy(sector = newValue)
        )
    }

    fun onNumDipendentiRangeChanged(newValue: String) {
        _uiState.value = _uiState.value.copy(
            azienda = _uiState.value.azienda.copy(numDipendentiRange = newValue)
        )
    }

    fun onNomeAziendaChanged(newValue: String) {
        _uiState.value = _uiState.value.copy(
            azienda = _uiState.value.azienda.copy(nome = newValue)
        )
    }

    fun onGiornoPubblicazioneChanged(newValue: DayOfWeek) {
        _uiState.value = _uiState.value.copy(
            azienda = _uiState.value.azienda.copy(giornoPubblicazioneTurni = newValue)
        )
    }

    fun onCurrentStepDown() {
        _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep - 1)
    }

    fun onCurrentStepUp() {
        _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
    }
}