package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.enumClass.InviteView
import com.bizsync.domain.constants.enumClass.StatusInvite
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Ccnlnfo
import com.bizsync.domain.usecases.GenerateContractInfoUseCase
import com.bizsync.domain.usecases.LoadInvitesUseCase
import com.bizsync.domain.usecases.SendInviteUseCase
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.mapper.toUiStateList
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.model.AziendaUi
import com.bizsync.ui.model.InvitoUi
import com.bizsync.ui.model.ManageInviteState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
@HiltViewModel
class ManageInviteViewModel @Inject constructor(
    private val loadInvitesUseCase: LoadInvitesUseCase,
    private val generateContractInfoUseCase: GenerateContractInfoUseCase,
    private val sendInviteUseCase: SendInviteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageInviteState())
    val uiState: StateFlow<ManageInviteState> = _uiState

    fun setCurrentStep(step: Int) {
        _uiState.update { state ->
            state.copy(currentStep = step)
        }
    }

    fun loadInvites(idAzienda: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                when (val result = loadInvitesUseCase(idAzienda)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                invites = result.data.toUiStateList(),
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Empty -> {
                        _uiState.update {
                            it.copy(
                                invites = emptyList(),
                                isLoading = false,
                                resultMessage = "Nessun invito trovato per l'azienda.",
                                resultStatus = DialogStatusType.SUCCESS
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                resultMessage = result.message ?: "Errore sconosciuto",
                                resultStatus = DialogStatusType.ERROR
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resultMessage = "Errore imprevisto: ${e.message}",
                        resultStatus = DialogStatusType.ERROR
                    )
                }
            }
        }
    }

    fun setCurrentView(view: InviteView) {
        _uiState.update { currentState ->
            when (view) {
                InviteView.CREATE_INVITE -> {
                    currentState.copy(
                        currentView = view,
                        invite = InvitoUi(),
                        ccnlnfo = Ccnlnfo(),
                        currentStep = 1,
                        resultMessage = null
                    )
                }
                InviteView.VIEW_INVITES -> {
                    currentState.copy(currentView = view)
                }
            }
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
            try {
                val currentInvite = _uiState.value.invite

                _uiState.update { it.copy(isLoading = true, resultMessage = null) }

                // ✅ Usa Use Case invece del repository diretto
                when (val result = generateContractInfoUseCase(
                    posizioneLavorativa = currentInvite.posizioneLavorativa,
                    dipartimento = currentInvite.dipartimento,
                    settoreAziendale = currentInvite.settoreAziendale,
                    tipoContratto = currentInvite.tipoContratto,
                    oreSettimanali = currentInvite.oreSettimanali
                )) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                ccnlnfo = result.data,
                                isLoading = false,
                                resultMessage = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                resultMessage = result.message ?: "Errore nella generazione CCNL",
                                resultStatus = DialogStatusType.ERROR
                            )
                        }
                    }

                    is Resource.Empty -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                resultMessage = "Nessuna informazione CCNL generata",
                                resultStatus = DialogStatusType.ERROR
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resultMessage = "Errore imprevisto: ${e.message}",
                        resultStatus = DialogStatusType.ERROR
                    )
                }
            }
        }
    }

    // Functions to update CCNL info manually (identiche)
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
            try {
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
                                stato = StatusInvite.PENDING,
                                ccnlInfo = ccnlInfo,
                                settoreAziendale = azienda.sector,
                                sentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            )
                        )
                    }

                    // ✅ Usa Use Case invece del repository diretto
                    when (val result = sendInviteUseCase(_uiState.value.invite.toDomain())) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    resultStatus = DialogStatusType.SUCCESS,
                                    resultMessage = "Invito inviato con successo!"
                                )
                            }
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    resultStatus = DialogStatusType.ERROR,
                                    resultMessage = result.message
                                )
                            }
                        }

                        is Resource.Empty -> {
                            _uiState.update {
                                it.copy(
                                    resultStatus = DialogStatusType.ERROR,
                                    resultMessage = "Errore: risposta vuota dal server"
                                )
                            }
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
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        resultStatus = DialogStatusType.ERROR,
                        resultMessage = "Errore imprevisto: ${e.message}"
                    )
                }
            }
        }
    }


    fun clearResult() {
        _uiState.update { ManageInviteState() }
    }
}