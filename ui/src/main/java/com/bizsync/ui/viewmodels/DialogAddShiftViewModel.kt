package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.model.Turno
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.nio.channels.Selector
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DialogAddShiftViewModel @Inject constructor(private val turnoRepository: TurnoRepository) : ViewModel() {

    var text = mutableStateOf("")
    var itemsList = mutableStateListOf<Turno>()


    fun caricaturni(giornoSelezionato: LocalDate){
        viewModelScope.launch {
            Log.d("TURNI_DEBUG", "SONO nel viewmodel")
            Log.d("VERIFICA_GIORNO", "SONO nel viewmodel"  + giornoSelezionato.toString())
            itemsList.clear()
            itemsList.addAll(turnoRepository.caricaTurni(giornoSelezionato))

        }
    }


}