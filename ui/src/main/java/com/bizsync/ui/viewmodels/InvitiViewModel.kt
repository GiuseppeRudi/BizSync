package com.bizsync.ui.viewmodels

import com.bizsync.domain.model.Contratto
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.ContractRepository
import com.bizsync.backend.repository.InvitoRepository
import com.bizsync.domain.constants.enumClass.StatusInvite
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.constants.sealedClass.Resource.Success
import com.bizsync.domain.utils.toDomain
import com.bizsync.domain.utils.toUiStateList
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.model.InvitiState
import com.bizsync.ui.model.InvitoUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@HiltViewModel
class InvitiViewModel @Inject constructor(
    private val invitoRepository: InvitoRepository,
    private val contractRepository: ContractRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(InvitiState())
    val uiState: StateFlow<InvitiState> = _uiState



    fun fetchInvites(email : String) = viewModelScope.launch {

        val result =  invitoRepository.getInvitesByEmail(email)

        when(result){
            is Success -> { onInvitesLoaded(result.data.toUiStateList()) }
            is Resource.Error -> { onInviteMsg(DialogStatusType.ERROR, result.message) }
            is Resource.Empty -> { onInviteMsg(DialogStatusType.ERROR, "Nessun invito trovato") }
        }

    }


    fun clearMessage() {
        _uiState.update { it.copy(resultMsg = null, statusMsg = DialogStatusType.ERROR) }
    }

    fun acceptInvite(invite: InvitoUi, idUtente : String) = viewModelScope.launch {
        val oggi = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val newInvite = invite.copy(acceptedDate = oggi, stato = StatusInvite.ACCEPTED)

        val result = invitoRepository.updateInvito(newInvite.toDomain())

        Log.d("Contratto ", result.toString())
        when (result) {
            is Success -> {
                // Crea oggetto contratto
                val contratto = Contratto(
                    idDipendente = idUtente,
                    idAzienda = newInvite.idAzienda,
                    emailDipendente = newInvite.email,
                    posizioneLavorativa = newInvite.posizioneLavorativa,
                    dipartimento = newInvite.dipartimento,
                    tipoContratto = newInvite.tipoContratto,
                    oreSettimanali = newInvite.oreSettimanali,
                    settoreAziendale = newInvite.settoreAziendale,
                    dataInizio = oggi,
                    ccnlInfo = newInvite.ccnlInfo
                )

                // Salva contratto
                val contrattoResult = contractRepository.saveContract(contratto)

                Log.d("Contratto ", contrattoResult.toString())

                when (contrattoResult) {
                    is Success -> {
                        val contrattoConId = contratto.copy(id = contrattoResult.data)
                        _uiState.update { it.copy(updateInvite = true, contratto = contrattoConId) }
                    }
                    is Resource.Error -> {
                        onInviteMsg(DialogStatusType.ERROR, "Invito accettato ma errore nel salvataggio contratto: ${contrattoResult.message}")
                    }
                    else -> {
                        onInviteMsg(DialogStatusType.ERROR, "Errore sconosciuto nel salvataggio contratto")
                    }
                }
            }

            is Resource.Error -> {
                onInviteMsg(DialogStatusType.ERROR, result.message)
            }

            else -> {
                onInviteMsg(DialogStatusType.ERROR, "Errore Sconosciuto")
            }
        }
    }


    fun declineInvite(invite: InvitoUi) = viewModelScope.launch {
        // repo.declineInvite(...)
//        fetchInvites(// da implemenrtare)
    }

    fun showDetails(invite: InvitoUi) {
        // potresti navigare o mostrare dialog
    }



    fun onInvitesLoaded(invites: List<InvitoUi>) {
        _uiState.value = _uiState.value.copy(invites = invites, isLoading = false)
    }

    fun onInviteMsg(status: DialogStatusType, message: String?) {
        _uiState.value = _uiState.value.copy(statusMsg = status, resultMsg = message)
    }



}
