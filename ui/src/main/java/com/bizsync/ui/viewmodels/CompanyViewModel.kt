package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.enumClass.CompanyOperation
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.User
import com.bizsync.domain.usecases.LoadDipendentiAziendaUseCase
import com.bizsync.domain.usecases.SaveCompanyChangesUseCase
import com.bizsync.domain.usecases.UpdateDipartimentoDipendentiUseCase
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.model.AziendaUi
import com.bizsync.ui.model.CompanyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject
@HiltViewModel
class CompanyViewModel @Inject constructor(
    private val loadDipendentiAziendaUseCase: LoadDipendentiAziendaUseCase,
    private val saveCompanyChangesUseCase: SaveCompanyChangesUseCase,
    private val updateDipartimentoDipendentiUseCase: UpdateDipartimentoDipendentiUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyState())
    val uiState: StateFlow<CompanyState> = _uiState.asStateFlow()

    fun setOrariSettimanaliModificati(orari: Map<String, Map<DayOfWeek, Pair<LocalTime, LocalTime>>>) {
        _uiState.update { it.copy(orariSettimanaliModificati = orari) }
    }

    fun loadDipendentiAzienda() {
        viewModelScope.launch {
            try {
                // ✅ Usa Use Case invece di DAO diretto
                when (val result = loadDipendentiAziendaUseCase()) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(dipendenti = result.data) }
                    }
                    is Resource.Error -> {
                        Log.e("CompanyViewModel", "Errore nel caricamento dipendenti: ${result.message}")
                        setStatusMessage(result.message, DialogStatusType.ERROR)
                    }
                    is Resource.Empty -> {
                        _uiState.update { it.copy(dipendenti = emptyList()) }
                    }
                }
            } catch (e: Exception) {
                Log.e("CompanyViewModel", "Errore nel caricamento dipendenti", e)
                setStatusMessage("Errore imprevisto: ${e.message}", DialogStatusType.ERROR)
            }
        }
    }

    fun setSelectedDipendente(dipendente: User?) {
        _uiState.update { it.copy(selectedDipendente = dipendente) }
    }

    fun setShowDipartimentoDialog(show: Boolean) {
        _uiState.update { it.copy(showDipartimentoDialog = show) }
    }

    fun updateDipartimentoDipendente(dipendenteId: String, nuovoDipartimento: String) {
        val dipendente = _uiState.value.dipendenti.find { it.uid == dipendenteId }
        if (dipendente != null) {
            val dipendenteModificato = dipendente.copy(dipartimento = nuovoDipartimento)
            val modificati = _uiState.value.dipendentiModificati.toMutableMap()
            modificati[dipendenteId] = dipendenteModificato

            _uiState.update {
                it.copy(dipendentiModificati = modificati)
            }
        }
    }

    fun salvaDipendentiModificati(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val dipendentiDaAggiornare = _uiState.value.dipendentiModificati.values.toList()

                // ✅ Usa Use Case invece del repository diretto
                when (val result = updateDipartimentoDipendentiUseCase(dipendentiDaAggiornare)) {
                    is Resource.Success -> {
                        // Ricarica i dipendenti aggiornati
                        loadDipendentiAzienda()

                        // Reset delle modifiche
                        _uiState.update {
                            it.copy(
                                dipendentiModificati = emptyMap(),
                                isLoading = false
                            )
                        }

                        onComplete()
                    }

                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        setStatusMessage(result.message ?: "Errore nel salvataggio dipendenti", DialogStatusType.ERROR)
                    }

                    is Resource.Empty -> {
                        _uiState.update { it.copy(isLoading = false) }
                        setStatusMessage("Nessun dipendente aggiornato", DialogStatusType.ERROR)
                    }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                Log.e("CompanyViewModel", "Errore nel salvataggio dipendenti", e)
                setStatusMessage("Errore imprevisto: ${e.message}", DialogStatusType.ERROR)
            }
        }
    }

    fun onSaveChanges(idAzienda: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Salva le modifiche ai dipartimenti
                val nuoviDipartimenti = _uiState.value.areeModificate

                // Aggiorna Firebase
                saveCompanyChangesUseCase(idAzienda, nuoviDipartimenti)

                // Salva i nuovi dipartimenti temporaneamente per la verifica
                _uiState.update {
                    it.copy(
                        nuoviDipartimenti = nuoviDipartimenti,
                        isLoading = false,
                        hasChanges = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                Log.e("CompanyViewModel", "Errore nel salvataggio dipartimenti", e)
            }
        }
    }



    // ✅ RESTO DELLE FUNZIONI IDENTICHE
    fun openOrariDialog(areaId: String) {
        val orariEsistenti = _uiState.value.orariSettimanaliModificati[areaId] ?: emptyMap()
        _uiState.update {
            it.copy(
                editingOrariArea = areaId,
                showOrariDialog = true,
                orariTemp = orariEsistenti
            )
        }
    }

    fun onGiornoLavoroChanged(giorno: DayOfWeek, isChecked: Boolean) {
        _uiState.update { current ->
            val nuoviOrari = current.orariTemp.toMutableMap()
            if (isChecked) {
                nuoviOrari[giorno] = LocalTime.of(8, 0) to LocalTime.of(17, 0)
            } else {
                nuoviOrari.remove(giorno)
            }
            current.copy(orariTemp = nuoviOrari)
        }
    }

    fun onOrarioInizioChanged(giorno: DayOfWeek, orario: LocalTime) {
        _uiState.update { current ->
            val nuoviOrari = current.orariTemp.toMutableMap()
            val orarioCorrente = nuoviOrari[giorno] ?: (LocalTime.of(8, 0) to LocalTime.of(18, 0))
            nuoviOrari[giorno] = orarioCorrente.copy(first = orario)
            current.copy(orariTemp = nuoviOrari)
        }
    }

    fun onOrarioFineChanged(giorno: DayOfWeek, orario: LocalTime) {
        _uiState.update { current ->
            val nuoviOrari = current.orariTemp.toMutableMap()
            val orarioCorrente = nuoviOrari[giorno] ?: (LocalTime.of(8, 0) to LocalTime.of(18, 0))
            nuoviOrari[giorno] = orarioCorrente.copy(second = orario)
            current.copy(orariTemp = nuoviOrari)
        }
    }

    fun onChangedOrariTemp(orari: Map<DayOfWeek, Pair<LocalTime, LocalTime>>?) {
        _uiState.update { current ->
            current.copy(orariTemp = orari ?: emptyMap())
        }
    }

    fun salvaOrariArea() {
        val areaId = _uiState.value.editingOrariArea ?: return
        _uiState.update { current ->
            val nuoviOrariModificati = current.orariSettimanaliModificati.toMutableMap()
            nuoviOrariModificati[areaId] = current.orariTemp
            current.copy(
                orariSettimanaliModificati = nuoviOrariModificati,
                showOrariDialog = false,
                editingOrariArea = null,
                orariTemp = emptyMap(),
                hasChanges = true
            )
        }
    }

    fun closeOrariDialog() {
        _uiState.update {
            it.copy(
                showOrariDialog = false,
                editingOrariArea = null,
                orariTemp = emptyMap()
            )
        }
    }

    fun removeAreaModificata(area: AreaLavoro) {
        _uiState.update { current ->
            val nuoveAree = current.areeModificate.filterNot { it.nomeArea == area.nomeArea }
            val nuoviOrari = current.orariSettimanaliModificati.filterKeys { it != area.nomeArea }
            current.copy(
                areeModificate = nuoveAree,
                orariSettimanaliModificati = nuoviOrari,
                hasChanges = true
            )
        }
    }

    fun setSelectedOperation(operation: CompanyOperation?) {
        _uiState.update { it.copy(selectedOperation = operation) }
    }

    fun checkOnBoardingStatus(azienda: AziendaUi) {
        if (azienda.areeLavoro.isNotEmpty() && azienda.turniFrequenti.isNotEmpty()) {
            _uiState.update { it.copy(onBoardingDone = true) }
        } else {
            _uiState.update { it.copy(onBoardingDone = false) }
        }
    }

    fun setOnBoardingDone(done: Boolean) {
        _uiState.update { it.copy(onBoardingDone = done) }
    }

    fun setEditingArea(area: AreaLavoro?) {
        _uiState.update { it.copy(editingArea = area) }
    }

    fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    fun setStatusMessage(message: String?, type: DialogStatusType) {
        _uiState.update { it.copy(resultMsg = message, statusMsg = type) }
    }

    fun addAreaModificata(area: AreaLavoro) {
        _uiState.update { current ->
            val nuoveAree = current.areeModificate.toMutableList().apply { add(area) }
            current.copy(areeModificate = nuoveAree, hasChanges = true)
        }
    }

    fun updateAreaModificata(id: String, nuovoNome: String) {
        _uiState.update { current ->
            val nuoveAree = current.areeModificate.map {
                if (it.nomeArea == id) it.copy(nomeArea = nuovoNome) else it
            }
            current.copy(areeModificate = nuoveAree, hasChanges = true)
        }
    }

    fun setHasChanges(value: Boolean) {
        _uiState.update { it.copy(hasChanges = value) }
    }

    fun setShowAddDialog(show: Boolean) {
        _uiState.update { it.copy(showAddDialog = show) }
    }

    fun setAreeModificate(aree: List<AreaLavoro>) {
        _uiState.update { it.copy(areeModificate = aree, hasChanges = true) }
    }
}