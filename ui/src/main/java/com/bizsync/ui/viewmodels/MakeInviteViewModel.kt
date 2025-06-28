package com.bizsync.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.InvitoRepository
import com.bizsync.domain.constants.enumClass.StatusInvite
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.model.AziendaUi
import com.bizsync.ui.model.MakeInviteState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import toDomain
import javax.inject.Inject


@HiltViewModel
class MakeInviteViewModel @Inject constructor(private val invitoRepository: InvitoRepository) : ViewModel() {


    private val _uiState = MutableStateFlow(MakeInviteState())
    val uiState: StateFlow<MakeInviteState> = _uiState


    fun onEmailChanged(newValue: String) {
        _uiState.update { it.copy(invite = it.invite.copy(email = newValue)) }
    }


    fun onManagerChanged(newValue: Boolean) {
        _uiState.update { it.copy(invite = it.invite.copy(manager = newValue)) }
    }

    fun onRuoloChanged(newValue: String) {
        _uiState.update { it.copy(invite = it.invite.copy(nomeRuolo = newValue)) }
    }


    fun inviaInvito(azienda : AziendaUi) {
        viewModelScope.launch {

                if(azienda.idAzienda.isNotEmpty() && azienda.nome.isNotEmpty())
                {
                    _uiState.update { it.copy(invite = _uiState.value.invite.copy(aziendaNome = azienda.nome,idAzienda = azienda.idAzienda, stato = StatusInvite.INPENDING)) }
                    val result = invitoRepository.caricaInvito(_uiState.value.invite.toDomain())

                    when (result)
                    {
                        is Resource.Success -> { _uiState.update { it.copy(resultStatus = DialogStatusType.SUCCESS) }}
                        is Resource.Error -> { _uiState.update { it.copy(resultStatus = DialogStatusType.ERROR, resultMessage = result.message) } }
                        else -> {_uiState.update { it.copy( resultMessage = " Unknown Error") }}
                    }
                }
                else {
                    _uiState.update { it.copy(resultStatus = DialogStatusType.ERROR, resultMessage = "Azienda non trovata") }
                }
        }
    }

    fun clearResult() {
        _uiState.update { MakeInviteState() }
    }

}
