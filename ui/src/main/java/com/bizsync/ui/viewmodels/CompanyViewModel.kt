package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.domain.constants.enumClass.CompanyOperation
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.model.AziendaUi
import com.bizsync.ui.model.CompanyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyViewModel @Inject constructor( private val aziendaRepository: AziendaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyState())
    val uiState: StateFlow<CompanyState> = _uiState.asStateFlow()

    /** Seleziona l'operazione attiva nella schermata principale */
    fun setSelectedOperation(operation: CompanyOperation?) {
        _uiState.update { it.copy(selectedOperation = operation) }
    }

    fun checkOnBoardingStatus(azienda : AziendaUi)
    {
        if(azienda.areeLavoro.isNotEmpty() && azienda.turniFrequenti.isNotEmpty())
        {
            _uiState.update { it.copy(onBoardingDone = true) }
        }
        else
        {
            _uiState.update { it.copy(onBoardingDone = false) }
        }
    }
    /** Segna l'onboarding come completato */
    fun setOnBoardingDone(done: Boolean) {
        _uiState.update { it.copy(onBoardingDone = done) }
    }

    /** Mostra/Nasconde dialog per aggiunta area */
    fun toggleAddDialog(show: Boolean) {
        _uiState.update { it.copy(showAddDialog = show) }
    }

    /** Imposta area in fase di modifica */
    fun setEditingArea(area: AreaLavoro?) {
        _uiState.update { it.copy(editingArea = area) }
    }

    /** Imposta lo stato di caricamento */
    fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    /** Imposta messaggio e tipo stato per i dialog */
    fun setStatusMessage(message: String?, type: DialogStatusType) {
        _uiState.update { it.copy(resultMsg = message, statusMsg = type) }
    }


    /** Aggiunge una nuova area modificata */
    fun addAreaModificata(area: AreaLavoro) {
        _uiState.update { current ->
            val nuoveAree = current.areeModificate.toMutableList().apply { add(area) }
            current.copy(areeModificate = nuoveAree, hasChanges = true)
        }
    }

    /** Modifica un'area esistente nell'elenco */
    fun updateAreaModificata(id: String, nuovoNome: String) {
        _uiState.update { current ->
            val nuoveAree = current.areeModificate.map {
                if (it.id == id) it.copy(nomeArea = nuovoNome) else it
            }
            current.copy(areeModificate = nuoveAree, hasChanges = true)
        }
    }

    /** Rimuove un'area dall'elenco */
    fun removeAreaModificata(area: AreaLavoro) {
        _uiState.update { current ->
            val nuoveAree = current.areeModificate.filterNot { it.id == area.id }
            current.copy(areeModificate = nuoveAree, hasChanges = true)
        }
    }

    /** Imposta se ci sono modifiche da salvare */
    fun setHasChanges(value: Boolean) {
        _uiState.update { it.copy(hasChanges = value) }
    }

    /** Mostra o nasconde il dialog di aggiunta */
    fun setShowAddDialog(show: Boolean) {
        _uiState.update { it.copy(showAddDialog = show) }
    }

    fun onSaveChanges(idAzienda: String) {
        viewModelScope.launch {
            setLoading(true)

            val areeDaSalvare = _uiState.value.areeModificate

            when (val result = aziendaRepository.updateAreeLavoro(idAzienda, areeDaSalvare)) {
                is Resource.Success -> {
                    setStatusMessage("Modifiche salvate con successo", DialogStatusType.SUCCESS)
                    setHasChanges(false)
                }

                is Resource.Empty -> {
                    setStatusMessage("Nessuna area da salvare", DialogStatusType.ERROR)
                }

                is Resource.Error -> {
                    setStatusMessage(result.message ?: "Errore generico", DialogStatusType.ERROR)
                }
            }

            setLoading(false)
        }
    }



    /** Imposta le aree modificate (ad esempio dopo editing) */
    fun setAreeModificate(aree: List<AreaLavoro>) {
        _uiState.update { it.copy(areeModificate = aree, hasChanges = true) }
    }

    /** Reset dello stato temporaneo, utile dopo conferma salvataggi */
    fun resetTempState() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                editingArea = null,
                resultMsg = null,
                hasChanges = false
            )
        }
    }
}
