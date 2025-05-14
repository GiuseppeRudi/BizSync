package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.model.domain.Turno
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.channels.Selector
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DialogAddShiftViewModel @Inject constructor(private val turnoRepository: TurnoRepository) : ViewModel() {


    private val _itemsList = MutableStateFlow<List<Turno>>(emptyList())
    val itemsList : StateFlow<List<Turno>> = _itemsList

    private val _text = MutableStateFlow("")
    val text : StateFlow<String> = _text

    fun onTextChanged(newValue : String)
    {
        _text.value = newValue
    }

    fun caricaturni(giornoSelezionato: LocalDate){
        viewModelScope.launch {
            Log.d("TURNI_DEBUG", "SONO nel viewmodel")
            Log.d("VERIFICA_GIORNO", "SONO nel viewmodel"  + giornoSelezionato.toString())
            val turniCaricati = turnoRepository.caricaTurni(giornoSelezionato)
            _itemsList.value = turniCaricati

        }
    }

    fun aggiungiturno(turno: Turno){
        viewModelScope.launch {
            turnoRepository.aggiungiTurno(turno)
            _itemsList.value = _itemsList.value + turno
        }
    }

}