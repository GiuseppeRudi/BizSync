package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.backend.repository.InvitoRepository
import com.bizsync.backend.repository.UserRepository
import com.bizsync.model.Invito
import com.bizsync.ui.components.DialogStatusType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class InvitiViewModel @Inject constructor(private val invitoRepository: InvitoRepository, private val userRepository: UserRepository) : ViewModel() {



    private val _invites = MutableStateFlow<List<Invito>>(emptyList())
    val invites: StateFlow<List<Invito>> = _invites

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _email = MutableStateFlow("")
    val email : StateFlow<String> = _email

    private val _errorStatusInvite = MutableStateFlow<DialogStatusType>(DialogStatusType.ERROR)
    val errorStatusInvite : StateFlow<DialogStatusType> = _errorStatusInvite

    private val _errorMessageStatusInvite = MutableStateFlow<String?>(null)
    val errorMessageStatusInvite : StateFlow<String?> = _errorMessageStatusInvite


    fun fetchInvites(email : String) = viewModelScope.launch {
        _isLoading.value = true
         _email.value = email
        //qui la chiamata a Firebase, poi:
        val list =  invitoRepository.loadInvito(email)
        Log.d("INVITI_DEBUG" , "LISTA" + list.toString())
        _invites.value = list
        _isLoading.value = false
    }



    fun acceptInvite(invite: Invito, userViewMdel : UserViewModel) = viewModelScope.launch {
        // cambiare il valore di invito lo stato
        val errore1 = invitoRepository.updateInvito(invite)

        // aggiornare il valroe di user della sua azienda
        val errore2 = userRepository.updateAcceptInvite(invite,userViewMdel.uid.value)

        if (errore1 && errore2 == false )
        {
            userViewMdel.onAcceptInvite(invite)
            _errorStatusInvite.value = DialogStatusType.SUCCESS
            _errorMessageStatusInvite.value = "Invito accettato con successo. Complimenti"
        }
        else
        {
            _errorMessageStatusInvite.value = " Non è stato possbile accettare l'invito, riprovare"
        }

    }
    fun declineInvite(invite: Invito) = viewModelScope.launch {
        // repo.declineInvite(...)
        fetchInvites(_email.value)
    }
    fun showDetails(invite: Invito) {
        // potresti navigare o mostrare dialog
    }

    fun clearErrorMessage(){
        _errorMessageStatusInvite.value = null
        _errorStatusInvite.value = DialogStatusType.ERROR
    }
}
