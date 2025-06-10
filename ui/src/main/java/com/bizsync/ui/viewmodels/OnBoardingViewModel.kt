package com.bizsync.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.OnBoardingPianificaRepository
import com.bizsync.model.domain.AreaLavoro
import com.bizsync.model.domain.Turno
import com.bizsync.model.domain.TurnoFrequente
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class OnBoardingPianificaViewModel @Inject constructor(private val OnBoardingPianificaRepository: OnBoardingPianificaRepository) : ViewModel() {

    private val _currentStep = MutableStateFlow(0)
    val currentStep : StateFlow<Int> = _currentStep

    private val _aree = MutableStateFlow<List<AreaLavoro>>(emptyList())
    val aree : StateFlow<List<AreaLavoro>> = _aree


    private val _nomeAzienda = MutableStateFlow<String>("")
    val nomeAzienda : StateFlow<String> = _nomeAzienda

    private val _areePronte = MutableStateFlow<Boolean>(false)
    val areePronte : StateFlow<Boolean> = _areePronte

    private val _turniPronti = MutableStateFlow<Boolean>(false)
    val turniPronti : StateFlow<Boolean> = _turniPronti

    fun setStep(step: Int) {
        _currentStep.value = step
    }

    fun generaAreeAi(name : String){
        _nomeAzienda.value = name

        viewModelScope.launch {
            val aree = OnBoardingPianificaRepository.setAreaAi(nomeAzienda.value)

            if(aree.isNotEmpty())
            {

                _aree.value = aree
                _areePronte.value = true
            }
        }

    }

    fun generaTurniAi(name : String){
        _nomeAzienda.value = name

        viewModelScope.launch {
            val turni = OnBoardingPianificaRepository.setTurniAi(nomeAzienda.value)

            if(turni.isNotEmpty())
            {

                _turni.value = turni
                _turniPronti.value = true
            }
        }

    }

    private val _turni = MutableStateFlow<List<TurnoFrequente>>(emptyList())
    val turni : StateFlow<List<TurnoFrequente>> = _turni


    private val _nuovoTurno = MutableStateFlow<TurnoFrequente>(TurnoFrequente())
    val nuovoTurno : StateFlow<TurnoFrequente> = _nuovoTurno

    private val _nuovaArea = MutableStateFlow<AreaLavoro>(AreaLavoro())
    val nuovaArea : StateFlow<AreaLavoro> = _nuovaArea



    fun onNuovaAreaChangeName(name : String)
    {
        _nuovaArea.value = _nuovaArea.value.copy(nomeArea = name, id = _nuovaArea.value.id)
    }


    fun aggiungiArea() {
        _aree.value = _aree.value + _nuovaArea.value
    }

    fun resetNuovaArea(){
        _nuovaArea.value = AreaLavoro()
    }


    fun onRimuoviAreaById(idDaRimuovere: String) {
        _aree.value = _aree.value.filter { it.id != idDaRimuovere }
    }


    fun onRimuoviTurnoById(idDaRimuovere: String) {
        _turni.value = _turni.value.filter { it.id != idDaRimuovere }
    }


    fun onNewTurnoChangeFinishDate(finishDate: String) {
        _nuovoTurno.value = _nuovoTurno.value.copy(oraFine = finishDate)
    }

    fun onNewTurnoChangeStartDate(startDate: String) {
        _nuovoTurno.value = _nuovoTurno.value.copy(oraInizio = startDate)
    }

    fun onNewTurnoChangeName(name: String) {
        _nuovoTurno.value = _nuovoTurno.value.copy(nome = name)
    }

    fun aggiungiTurno() {
        _turni.value = _turni.value + _nuovoTurno.value
        _nuovoTurno.value = TurnoFrequente()
    }
}