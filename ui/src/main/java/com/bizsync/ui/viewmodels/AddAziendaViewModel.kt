package com.bizsync.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.model.Azienda
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAziendaViewModel  @Inject constructor(private val aziendaRepository: AziendaRepository): ViewModel() {


    var currentStep = mutableStateOf(1)
    var nomeAzienda = mutableStateOf(" Ciccio Industry")
    var numDipendentiRange = mutableStateOf("")
    val settore = mutableStateOf("")
    val customSettore = mutableStateOf("")

    fun aggiungiAzienda()
    {
        viewModelScope.launch {
            aziendaRepository.creaAzienda(Azienda("",nomeAzienda.value))
        }
    }
}
