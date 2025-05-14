package com.bizsync.ui.viewmodels


import android.util.Log
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.InvitoRepository
import com.bizsync.model.domain.Azienda
import com.bizsync.model.domain.Invito
import com.bizsync.ui.components.DialogStatusType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MakeInviteViewModel @Inject constructor(private val invitoRepository: InvitoRepository) : ViewModel() {

    private val _email = MutableStateFlow<String>("")
    val email : StateFlow<String> = _email

    fun onEmailChanged(newValue : String)
    {
        _email.value = newValue
    }

    private val _stato = MutableStateFlow<String>("in pending")
    val stato : StateFlow<String> = _stato

    fun onEsitoChanged(newValue : String)
    {
        _stato.value = newValue
    }

    private val _manager = MutableStateFlow<Boolean>(false)
    val manager : StateFlow<Boolean> = _manager

    fun onManagerChanged(newValue: Boolean)
    {
        _manager.value = newValue
    }

    private val _ruolo = MutableStateFlow<String>("")
    val ruolo : StateFlow<String> = _ruolo

    fun onRuoloChanged(newValue : String)
    {
        _ruolo.value = newValue
    }


    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage : StateFlow<String?> = _resultMessage

    private val _resultStatus = MutableStateFlow<DialogStatusType?>(null)
    val resultStatus : StateFlow<DialogStatusType?> = _resultStatus

    fun inviaInvito(azienda : Azienda?) {
        viewModelScope.launch {
            try {
                if(azienda!=null)
                {
                    invitoRepository.caricaInvito(Invito("",azienda.Nome,_email.value,azienda.idAzienda,_manager.value,_ruolo.value,_stato.value))
                    _resultMessage.value = "Invito inviato con successo!"
                }
                else
                {
                    _resultStatus.value = DialogStatusType.ERROR

                }

            } catch (e: Exception) {
                _resultMessage.value = "Errore: ${e.message}"
                _resultStatus.value = DialogStatusType.ERROR
            }
        }
    }

    fun clearResult() {
        _resultMessage.value = null
        _resultStatus.value = null
    }



}
