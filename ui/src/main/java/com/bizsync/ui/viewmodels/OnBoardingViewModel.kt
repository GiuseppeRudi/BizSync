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

    private val _listaPronta = MutableStateFlow<Boolean>(false)
    val listaPronta : StateFlow<Boolean> = _listaPronta

    fun setStep(step: Int) {
        _currentStep.value = step
    }

    fun generaTurniAi(name : String){
        _nomeAzienda.value = name

        viewModelScope.launch {
            val aree = OnBoardingPianificaRepository.setTurniAi(nomeAzienda.value)

            if(aree.isNotEmpty())
            {

                _aree.value = aree
                _listaPronta.value = true
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

    val areeDefault = listOf(
        "Reception",
        "Cucina",
        "Sala",
        "Bar",
        "Magazzino",
        "Pulizie"
    )

    val turniDefault = listOf(
        TurnoFrequente("", "Mattina", "08:00", "14:00", "Turno mattutino"),
        TurnoFrequente("", "Pomeriggio", "14:00", "20:00", "Turno pomeridiano"),
        TurnoFrequente("", "Sera", "20:00", "02:00", "Turno serale"),
        TurnoFrequente("", "Notte", "22:00", "06:00", "Turno notturno")
    )

}