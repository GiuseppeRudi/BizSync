package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.domain.model.Turno
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TurnoViewModel @Inject constructor(private val turnoRepository: TurnoRepository) : ViewModel() {



    private val _text = MutableStateFlow("")
    val text : StateFlow<String> = _text

    fun onTextChanged(newValue : String)
    {
        _text.value = newValue
    }


    fun aggiungiturno(pianificaVM : PianificaViewModel, turno: Turno){
        viewModelScope.launch {
            val esito = turnoRepository.aggiungiTurno(turno)

            if(esito)
            {
                pianificaVM.addTurno(turno)
            }

            else
            {
                //GESTIRE L'ERRORE
            }
        }
    }


}