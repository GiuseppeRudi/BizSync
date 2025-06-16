package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.InvitoRepository
import com.bizsync.backend.repository.UserRepository
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.constants.sealedClass.Resource.Success
import com.bizsync.domain.model.Invito
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.model.InvitiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class InvitiViewModel @Inject constructor(private val invitoRepository: InvitoRepository) : ViewModel() {


    private val _uiState = MutableStateFlow(InvitiState())
    val uiState: StateFlow<InvitiState> = _uiState



    fun fetchInvites(email : String) = viewModelScope.launch {

        val result =  invitoRepository.loadInvito(email)

        when(result){
            is Success -> { onInvitesLoaded(result.data) }
            is Resource.Error -> { onInviteMsg(DialogStatusType.ERROR, result.message) }
            is Resource.Empty -> { onInviteMsg(DialogStatusType.ERROR, "Nessun invito trovato") }
        }

    }


    fun clearMessage() {
        _uiState.update { it.copy(resultMsg = null, statusMsg = DialogStatusType.ERROR) }
    }


    fun acceptInvite(invite: Invito) = viewModelScope.launch {
        val result  = invitoRepository.updateInvito(invite)

        when(result){
            is Success -> {  _uiState.update { it.copy(updateInvte = true) }}
            is Resource.Error -> { onInviteMsg(DialogStatusType.ERROR, result.message) }
            else -> {onInviteMsg(DialogStatusType.ERROR, "Errore Sconosciuto")}
        }

    }

    fun declineInvite(invite: Invito) = viewModelScope.launch {
        // repo.declineInvite(...)
//        fetchInvites(// da implemenrtare)
    }

    fun showDetails(invite: Invito) {
        // potresti navigare o mostrare dialog
    }



    fun onInvitesLoaded(invites: List<Invito>) {
        _uiState.value = _uiState.value.copy(invites = invites, isLoading = false)
    }

    fun onInviteMsg(status: DialogStatusType, message: String?) {
        _uiState.value = _uiState.value.copy(statusMsg = status, resultMsg = message)
    }



}
