package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.backend.repository.OnBoardingPianificaRepository
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnBoardingPianificaViewModel @Inject constructor(private val aziendaRepository : AziendaRepository, private val OnBoardingPianificaRepository: OnBoardingPianificaRepository) : ViewModel() {

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

    private val _onDone = MutableStateFlow<Boolean>(false)
    val onDone : StateFlow<Boolean> = _onDone

    fun onComplete(idAzienda : String)
    {
        viewModelScope.launch {
            val check = aziendaRepository.addPianificaSetup(idAzienda, aree.value,turni.value)

            if (check)
            {
                _onDone.value = true
            }

            else
            {
                // GESTISCO L'ERRORE
            }
        }

    }
}