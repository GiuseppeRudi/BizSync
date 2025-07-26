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
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.mapper.toUiStateList
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

    fun fetchInvites(email: String) = viewModelScope.launch {
        // FIX 1: Imposta loading = true all'inizio
        _uiState.update { it.copy(isLoading = true, resultMsg = null) }

        Log.d("INVITI_DEBUG", "Fetching invites for: $email")

        try {
            val result = invitoRepository.getInvitesByEmail(email)
            Log.d("INVITI_DEBUG", "Result: $result")

            when (result) {
                is Success -> {
                    Log.d("INVITI_DEBUG", "Success: ${result.data}")
                    val uiInvites = result.data.toUiStateList()
                    _uiState.update {
                        it.copy(
                            invites = uiInvites,
                            isLoading = false,
                            resultMsg = null
                        )
                    }
                }

                is Resource.Error -> {
                    Log.e("INVITI_DEBUG", "Error: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            invites = emptyList(),
                            resultMsg = result.message,
                            statusMsg = DialogStatusType.ERROR
                        )
                    }
                }

                is Resource.Empty -> {
                    Log.d("INVITI_DEBUG", "Empty result")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            invites = emptyList(),
                            resultMsg = null
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("INVITI_DEBUG", "Exception in fetchInvites", e)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    invites = emptyList(),
                    resultMsg = "Errore imprevisto: ${e.message}",
                    statusMsg = DialogStatusType.ERROR
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update {
            it.copy(
                resultMsg = null,
                statusMsg = DialogStatusType.SUCCESS
            )
        }
    }

    fun acceptInvite(invite: InvitoUi, idUtente: String) = viewModelScope.launch {
        // FIX 2: Imposta loading durante l'operazione
        _uiState.update { it.copy(isLoading = true) }

        try {
            val oggi = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val newInvite = invite.copy(acceptedDate = oggi, stato = StatusInvite.ACCEPTED)

            val result = invitoRepository.updateInvito(newInvite.toDomain())

            Log.d("INVITI_DEBUG", "Update invite result: $result")

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

                    Log.d("INVITI_DEBUG", "Contract save result: $contrattoResult")

                    when (contrattoResult) {
                        is Success -> {
                            val contrattoConId = contratto.copy(id = contrattoResult.data)
                            _uiState.update {
                                it.copy(
                                    updateInvite = true,
                                    contratto = contrattoConId,
                                    isLoading = false,
                                    resultMsg = "Invito accettato con successo!",
                                    statusMsg = DialogStatusType.SUCCESS
                                )
                            }
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    resultMsg = "Invito accettato ma errore nel salvataggio contratto: ${contrattoResult.message}",
                                    statusMsg = DialogStatusType.ERROR
                                )
                            }
                        }

                        else -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    resultMsg = "Errore sconosciuto nel salvataggio contratto",
                                    statusMsg = DialogStatusType.ERROR
                                )
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            resultMsg = result.message,
                            statusMsg = DialogStatusType.ERROR
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            resultMsg = "Errore sconosciuto nell'accettazione invito",
                            statusMsg = DialogStatusType.ERROR
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("INVITI_DEBUG", "Exception in acceptInvite", e)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    resultMsg = "Errore imprevisto: ${e.message}",
                    statusMsg = DialogStatusType.ERROR
                )
            }
        }
    }

    fun declineInvite(invite: InvitoUi) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        try {
            val newInvite = invite.copy(stato = StatusInvite.REJECTED)
            val result = invitoRepository.updateInvito(newInvite.toDomain())

            when (result) {
                is Success -> {
                    // Rimuovi l'invito rifiutato dalla lista
                    val updatedInvites = _uiState.value.invites.filterNot { it.id == invite.id }
                    _uiState.update {
                        it.copy(
                            invites = updatedInvites,
                            isLoading = false,
                            resultMsg = "Invito rifiutato",
                            statusMsg = DialogStatusType.SUCCESS
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            resultMsg = result.message,
                            statusMsg = DialogStatusType.ERROR
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            resultMsg = "Errore nel rifiuto dell'invito",
                            statusMsg = DialogStatusType.ERROR
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("INVITI_DEBUG", "Exception in declineInvite", e)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    resultMsg = "Errore imprevisto: ${e.message}",
                    statusMsg = DialogStatusType.ERROR
                )
            }
        }
    }

    @Deprecated("Use internal state management")
    fun onInvitesLoaded(invites: List<InvitoUi>) {
        _uiState.update {
            it.copy(
                invites = invites,
                isLoading = false
            )
        }
    }

    @Deprecated("Use internal state management")
    fun onInviteMsg(status: DialogStatusType, message: String?) {
        _uiState.update {
            it.copy(
                statusMsg = status,
                resultMsg = message
            )
        }
    }
}