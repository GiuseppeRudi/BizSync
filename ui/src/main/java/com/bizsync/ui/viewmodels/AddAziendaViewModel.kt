package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.backend.repository.UserRepository
import com.bizsync.model.Azienda
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@HiltViewModel
class AddAziendaViewModel  @Inject constructor(private val aziendaRepository: AziendaRepository, private val userRepository: UserRepository): ViewModel() {


    var currentStep = mutableStateOf(1)
    var nomeAzienda = mutableStateOf(" Ciccio Industry")
    var numDipendentiRange = mutableStateOf("")
    val settore = mutableStateOf("")

    val customSettore = mutableStateOf("")
    val idAzienda = mutableStateOf<String?>("")

    fun aggiungiAzienda(idUtente: String, userViewModel: UserViewModel) {
        CoroutineScope(Dispatchers.IO).launch{
            idAzienda.value = aziendaRepository.creaAzienda(Azienda("", nomeAzienda.value))

            var azienda = idAzienda.value
            Log.d("AZIENDA_DEBUG", "VEDO SE CE L'ID AZIENDA"  + idAzienda.toString())
            Log.d("AZIENDA_DEBUG", "VEDO SE CE L'ID UTENTE"  + idUtente)

            if (azienda != null) {
                userRepository.aggiornaAzienda(azienda, idUtente)
                userViewModel.user.value?.idAzienda = azienda
                Log.d("AZIENDA_DEBUG", "Campo aggiornato con successo")
            } else {
                Log.e("AZIENDA_DEBUG", "ID azienda è nullo")
            }

        }
    }



}
