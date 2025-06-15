package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.Turno
import com.bizsync.ui.model.AziendaUi
import com.bizsync.ui.model.PianificaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject



@HiltViewModel
class PianificaViewModel @Inject constructor(private val turnoRepository: TurnoRepository) : ViewModel() {


    private val _uistate = MutableStateFlow(PianificaState())
    val uistate : StateFlow<PianificaState> = _uistate


    fun checkOnBoardingStatus(azienda : AziendaUi)
    {
        if(azienda.areeLavoro.isNotEmpty() && azienda.turniFrequenti.isNotEmpty())
        {
            _uistate.update { it.copy(onBoardingDone = true) }
        }
        else
        {
            _uistate.update { it.copy(onBoardingDone = false) }
        }
    }

    fun setOnBoardingDone(value : Boolean)
    {
        _uistate.update { it.copy(onBoardingDone = value) }
    }


    fun addTurno(turno: Turno) {
        _uistate.update { it.copy(itemsList = _uistate.value.itemsList + turno) }
    }

    fun caricaturni(giornoSelezionato: LocalDate){
        viewModelScope.launch {

            val result = turnoRepository.caricaTurni(giornoSelezionato)

            when(result){
                is Resource.Success -> { _uistate.update { it.copy(itemsList = result.data) } }
                is Resource.Error -> { _uistate.update { it.copy(errorMsg = result.message) } }
                is Resource.Empty -> { _uistate.update { it.copy(errorMsg = "Nessun turno trovato") } }
                else -> { _uistate.update { it.copy(errorMsg = "Errore nella ricerca dei turni") }}
            }

        }
    }

    fun onSelectionDataChanged(newValue : LocalDate)
    {
        _uistate.update { it.copy(selectionData = newValue) }
    }

    fun onShowDialogShiftChanged(newValue : Boolean)
    {
        _uistate.update { it.copy(showDialogShift = newValue) }
    }
}