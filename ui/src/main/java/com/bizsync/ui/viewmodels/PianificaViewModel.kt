package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.model.domain.Azienda
import com.bizsync.model.domain.Turno
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class PianificaViewModel @Inject constructor(private val turnoRepository: TurnoRepository) : ViewModel() {

    private val _itemsList = MutableStateFlow<List<Turno>>(emptyList())
    val itemsList : StateFlow<List<Turno>> = _itemsList

    private val _onBoardingDone = MutableStateFlow<Boolean?>(null)
    val onBoardingDone : StateFlow<Boolean?> = _onBoardingDone

    private val _azienda = MutableStateFlow<Azienda>(Azienda())
    val azienda : StateFlow<Azienda> = _azienda

    fun checkOnBoardingStatus(azienda : Azienda)
    {
        if(azienda.areeLavoro.isNotEmpty() && azienda.turniFrequenti.isNotEmpty())
        {
            _onBoardingDone.value = true
        }
        else
        {
            _onBoardingDone.value = false
        }
    }

    fun setOnBoardingDone(value : Boolean)
    {
        _onBoardingDone.value = value
    }


    fun addTurno(turno: Turno) {
        _itemsList.update { currentList ->
            currentList + turno
        }
    }

    fun caricaturni(giornoSelezionato: LocalDate){
        viewModelScope.launch {
            Log.d("TURNI_DEBUG", "SONO nel viewmodel")
            Log.d("VERIFICA_GIORNO", "SONO nel viewmodel"  + giornoSelezionato.toString())
            val turniCaricati = turnoRepository.caricaTurni(giornoSelezionato)
            Log.d("TURNI_DEBUG", "TURNI CARICATIl"  + turniCaricati)

            _itemsList.value = turniCaricati

        }
    }


    private val _selectionData = MutableStateFlow<LocalDate?>(null)
    val selectionData : StateFlow<LocalDate?> = _selectionData

    private val _showDialogShift = MutableStateFlow(false)
    val showDialogShift : StateFlow<Boolean> = _showDialogShift

    fun onSelectionDataChanged(newValue : LocalDate)
    {
        _selectionData.value = newValue
    }

    fun onShowDialogShiftChanged(newValue : Boolean)
    {
        _showDialogShift.value = newValue
    }
}