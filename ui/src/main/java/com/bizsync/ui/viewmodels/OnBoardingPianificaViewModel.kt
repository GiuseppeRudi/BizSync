package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.backend.repository.OnBoardingPianificaRepository
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente
import com.bizsync.ui.model.OnBoardingPianificaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnBoardingPianificaViewModel @Inject constructor(private val aziendaRepository : AziendaRepository, private val OnBoardingPianificaRepository: OnBoardingPianificaRepository) : ViewModel() {


    private val _uiState = MutableStateFlow(OnBoardingPianificaState())
    val uiState: StateFlow<OnBoardingPianificaState> = _uiState

    fun setStep(step: Int) {
        _uiState.update { it.copy(currentStep = step) }
    }

    fun onNuovaAreaChangeName(name: String) {
        _uiState.update { it.copy(nuovaArea = it.nuovaArea.copy(nomeArea = name)) }
    }

    fun aggiungiArea() {
        _uiState.update { it.copy(aree = it.aree + it.nuovaArea) }
    }

    fun resetNuovaArea() {
        _uiState.update { it.copy(nuovaArea = AreaLavoro()) }
    }

    fun reset(){
        _uiState.update { OnBoardingPianificaState() }
    }


    fun generaAreeAi(nomeAzienda: String) {

        viewModelScope.launch {
            val aree = OnBoardingPianificaRepository.setAreaAi(nomeAzienda)
            _uiState.update { it.copy(aree = aree, areePronte = true) }
        }

    }

    fun generaTurniAi(nomeAzienda: String) {

        viewModelScope.launch {
            val turni = OnBoardingPianificaRepository.setTurniAi(nomeAzienda)

            _uiState.update { it.copy(turni = turni, turniPronti = true) }
        }

    }


    fun onRimuoviAreaById(idDaRimuovere: String) {
        _uiState.update { state ->
            state.copy(
                aree = state.aree.filter { it.id != idDaRimuovere }
            )
        }
    }

    fun onRimuoviTurnoById(idDaRimuovere: String) {
        _uiState.update { state ->
            state.copy(
                turni = state.turni.filter { it.id != idDaRimuovere }
            )
        }
    }

    fun onNewTurnoChangeFinishDate(finishDate: String) {
        _uiState.update { state ->
            state.copy(
                nuovoTurno = state.nuovoTurno.copy(oraFine = finishDate)
            )
        }
    }

    fun onNewTurnoChangeStartDate(startDate: String) {
        _uiState.update { state ->
            state.copy(
                nuovoTurno = state.nuovoTurno.copy(oraInizio = startDate)
            )
        }
    }

    fun onNewTurnoChangeName(name: String) {
        _uiState.update { state ->
            state.copy(
                nuovoTurno = state.nuovoTurno.copy(nome = name)
            )
        }
    }

    fun aggiungiTurno() {
        _uiState.update { state ->
            state.copy(
                turni = state.turni + state.nuovoTurno,
                nuovoTurno = TurnoFrequente() // resetta il form
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }

    fun onComplete(idAzienda: String) {
        viewModelScope.launch {
            val result = aziendaRepository.addPianificaSetup(
                idAzienda,
                _uiState.value.aree,
                _uiState.value.turni,
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(onDone = true) }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(errorMsg = result.message) }
                }

                is Resource.Empty -> {
                    _uiState.update { it.copy(errorMsg = "Nessun dato da salvare") }
                }
            }
        }
    }
}