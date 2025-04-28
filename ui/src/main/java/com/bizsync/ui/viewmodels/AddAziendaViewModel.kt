package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.backend.repository.UserRepository
import com.bizsync.model.Azienda
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAziendaViewModel  @Inject constructor(private val aziendaRepository: AziendaRepository, private val userRepository: UserRepository): ViewModel() {


    var currentStep = mutableStateOf(1)
    var nomeAzienda = mutableStateOf(" Ciccio Industry")
    var numDipendentiRange = mutableStateOf("")
    val settore = mutableStateOf("")

    val customSettore = mutableStateOf("")
    val idAzienda = mutableStateOf<String?>("")

    fun aggiungiAzienda(idUtente: String) {
        viewModelScope.launch {
            try {
                val idAzienda = aziendaRepository.creaAzienda(Azienda("", nomeAzienda.value))
                Log.d("AZIENDA_DEBUG", "VEDO SE CE L'ID AZIENDA"  + idAzienda.toString())
                Log.d("AZIENDA_DEBUG", "VEDO SE CE L'ID UTENTE"  + idUtente)

                if (idAzienda != null) {
                    userRepository.aggiornaAzienda(idAzienda, idUtente)
                    Log.d("AZIENDA_DEBUG", "Campo aggiornato con successo")
                } else {
                    Log.e("AZIENDA_DEBUG", "ID azienda è nullo")
                }
            } catch (e: Exception) {
                Log.e("AZIENDA_DEBUG", "Errore durante creazione o aggiornamento", e)
            }
        }
    }


    fun ottieniAzienda(idUtente : String) {
        viewModelScope.launch {
            idAzienda.value = userRepository.ottieniIdAzienda(idUtente)
            Log.d("AZIENDA_DEBUG", "voglio ottenere l'azienda" + idAzienda.value.toString())
        }
    }


}
