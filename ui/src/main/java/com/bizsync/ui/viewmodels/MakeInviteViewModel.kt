package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.ContractRepository
import com.bizsync.backend.repository.InvitoRepository
import com.bizsync.domain.constants.enumClass.StatusInvite
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Ccnlnfo
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
class MakeInviteViewModel @Inject constructor(
    private val invitoRepository: InvitoRepository,
    private val contractRepository: ContractRepository
    ) : ViewModel() {

    private val _uiState = MutableStateFlow(MakeInviteState())
    val uiState: StateFlow<MakeInviteState> = _uiState

    fun setCurrentStep(step: Int) {
        _uiState.update { state ->
            state.copy(currentStep = step)
        }
    }

    fun isCurrentStepValid(): Boolean {
        val invite = _uiState.value.invite
        return invite.email.isNotBlank()
                && invite.posizioneLavorativa.isNotBlank()
                && invite.dipartimento.isNotBlank()
                && invite.tipoContratto.isNotBlank()
                && invite.oreSettimanali.isNotBlank()
    }

    fun onEmailChanged(newValue: String) {
        _uiState.update { it.copy(invite = it.invite.copy(email = newValue)) }
    }

    fun onManagerChanged(newValue: Boolean) {
        _uiState.update { it.copy(invite = it.invite.copy(manager = newValue)) }
    }

    fun setPosizioneLavorativa(value: String) {
        _uiState.update { state ->
            state.copy(invite = state.invite.copy(posizioneLavorativa = value))
        }
    }

    fun setDipartimento(value: String) {
        _uiState.update { state ->
            state.copy(invite = state.invite.copy(dipartimento = value))
        }
    }

    fun setSettoreAziendale(value: String) {
        _uiState.update { state ->
            state.copy(invite = state.invite.copy(settoreAziendale = value))
        }
    }

    fun setTipoContratto(value: String) {
        _uiState.update { state ->
            state.copy(invite = state.invite.copy(tipoContratto = value))
        }
    }

    fun setOreSettimanali(value: String) {
        _uiState.update { state ->
            state.copy(invite = state.invite.copy(oreSettimanali = value))
        }
    }

    fun generateContractInfo() {
        viewModelScope.launch {
            val currentInvite = _uiState.value.invite


            _uiState.update { it.copy(isLoadingCcnl = true, resultMessage = null) }

            val result = contractRepository.generateCcnlInfo(
                posizioneLavorativa = currentInvite.posizioneLavorativa,
                dipartimento = currentInvite.dipartimento,
                settoreAziendale = currentInvite.settoreAziendale,
                tipoContratto = currentInvite.tipoContratto,
                oreSettimanali = currentInvite.oreSettimanali
            )

            _uiState.update {
                it.copy(
                    ccnlnfo = result,
                    isLoadingCcnl = false,
                    resultMessage = null
                )
            }
        }
    }




    // Functions to update CCNL info manually
    fun updateCcnlSettore(value: String) {
        _uiState.update { it.copy(ccnlnfo = it.ccnlnfo.copy(settore = value)) }
    }

    fun updateCcnlRuolo(value: String) {
        _uiState.update { it.copy(ccnlnfo = it.ccnlnfo.copy(ruolo = value)) }
    }

    fun updateCcnlFerieAnnue(value: String) {
        val intValue = value.toIntOrNull() ?: 0
        _uiState.update { it.copy(ccnlnfo = it.ccnlnfo.copy(ferieAnnue = intValue)) }
    }

    fun updateCcnlRolAnnui(value: String) {
        val intValue = value.toIntOrNull() ?: 0
        _uiState.update { it.copy(ccnlnfo = it.ccnlnfo.copy(rolAnnui = intValue)) }
    }

    fun updateCcnlStipendio(value: String) {
        val intValue = value.toIntOrNull() ?: 0
        _uiState.update { it.copy(ccnlnfo = it.ccnlnfo.copy(stipendioAnnualeLordo = intValue)) }
    }

    fun updateCcnlMalattia(value: String) {
        val intValue = value.toIntOrNull() ?: 0
        _uiState.update { it.copy(ccnlnfo = it.ccnlnfo.copy(malattiaRetribuita = intValue)) }
    }

    fun inviaInvito(azienda: AziendaUi) {
        viewModelScope.launch {
            if (azienda.idAzienda.isNotEmpty() && azienda.nome.isNotEmpty()) {

                val ccnlInfo = _uiState.value.ccnlnfo

                if (ccnlInfo == Ccnlnfo()) {
                    _uiState.update {
                        it.copy(
                            resultStatus = DialogStatusType.ERROR,
                            resultMessage = "Informazioni CCNL mancanti. Generale prima dell'invio."
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        invite = _uiState.value.invite.copy(
                            aziendaNome = azienda.nome,
                            idAzienda = azienda.idAzienda,
                            stato = StatusInvite.INPENDING,
                            ccnlInfo = ccnlInfo  // 👈 Inseriamo l'oggetto CCNL
                        )
                    )
                }

                val result = invitoRepository.caricaInvito(_uiState.value.invite.toDomain())

                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(resultStatus = DialogStatusType.SUCCESS) }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                resultStatus = DialogStatusType.ERROR,
                                resultMessage = result.message
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(resultMessage = "Unknown Error") }
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        resultStatus = DialogStatusType.ERROR,
                        resultMessage = "Azienda non trovata"
                    )
                }
            }
        }
    }


    fun clearResult() {
        _uiState.update { MakeInviteState() }
    }
}