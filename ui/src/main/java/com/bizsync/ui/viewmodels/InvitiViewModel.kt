package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.backend.repository.InvitoRepository
import com.bizsync.model.Invito
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class InvitiViewModel @Inject constructor(private val invitoRepository: InvitoRepository) : ViewModel() {



    private val _invites = MutableStateFlow<List<Invito>>(emptyList())
    val invites: StateFlow<List<Invito>> = _invites

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading


    private val _uid = MutableStateFlow("")
    val uid : StateFlow<String> = _uid

    fun caricaUid(idUtente : String)
    {
        _uid.value = idUtente
    }



     fun fetchInvites() = viewModelScope.launch {
        _isLoading.value = true
        //qui la chiamata a Firebase, poi:
        val list =  invitoRepository.loadInvito(_uid.value)
        Log.d("INVITI_DEBUG" , "LISTA" + list.toString())
        _invites.value = list
        _isLoading.value = false
    }

    private fun generateSampleInvites() {
        // Simuliamo una lista di inviti
//        _invites.value = listOf(
//            Invito(id = "1", companyName = "Azienda A", message = "Offerta di lavoro", date = "2025-05-01", position = "Sviluppatore"),
//            Invito(id = "2", companyName = "Azienda B", message = "Posizione aperta", date = "2025-06-15", position = "Designer"),
//            Invito(id = "3", companyName = "Azienda C", message = "Un'opportunità per te!", date = "2025-07-01", position = "Manager")
//        )
        // Impostiamo il loading a false dopo aver generato i dati
        _isLoading.value = false
    }

    fun acceptInvite(invite: Invito) = viewModelScope.launch {
        // repo.acceptInvite(...)
        fetchInvites()  // ricarica
    }
    fun declineInvite(invite: Invito) = viewModelScope.launch {
        // repo.declineInvite(...)
        fetchInvites()
    }
    fun showDetails(invite: Invito) {
        // potresti navigare o mostrare dialog
    }
}
