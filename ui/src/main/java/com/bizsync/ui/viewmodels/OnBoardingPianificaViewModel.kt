package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.backend.repository.OnBoardingPianificaRepository
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente
import com.bizsync.ui.model.OnBoardingPianificaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject


@HiltViewModel
class OnBoardingPianificaViewModel @Inject constructor(private val aziendaRepository : AziendaRepository, private val OnBoardingPianificaRepository: OnBoardingPianificaRepository) : ViewModel() {


    private val _uiState = MutableStateFlow(OnBoardingPianificaState())
    val uiState: StateFlow<OnBoardingPianificaState> = _uiState

    fun setStep(step: Int) {
        _uiState.update { it.copy(currentStep = step) }
    }

    fun onNuovaAreaChangeName(name: String) {
        _uiState.update { it.copy(nuovaArea = it.nuovaArea.copy(nomeArea = name)) }
    }

    fun aggiungiArea() {
        _uiState.update { it.copy(aree = it.aree + it.nuovaArea) }
    }

    fun resetNuovaArea() {
        _uiState.update { it.copy(nuovaArea = AreaLavoro()) }
    }

    fun reset(){
        _uiState.update { OnBoardingPianificaState() }
    }


    fun generaAreeAi(nomeAzienda: String) {

        viewModelScope.launch {
            val aree = OnBoardingPianificaRepository.setAreaAi(nomeAzienda)
            _uiState.update { it.copy(aree = aree, areePronte = true) }
        }

    }

    fun generaTurniAi(nomeAzienda: String) {

        viewModelScope.launch {
            val turni = OnBoardingPianificaRepository.setTurniAi(nomeAzienda)

            _uiState.update { it.copy(turni = turni, turniPronti = true) }
        }

    }


    fun onRimuoviAreaById(idDaRimuovere: String) {
        _uiState.update { state ->
            state.copy(
                aree = state.aree.filter { it.id != idDaRimuovere }
            )
        }
    }

    fun onRimuoviTurnoById(idDaRimuovere: String) {
        _uiState.update { state ->
            state.copy(
                turni = state.turni.filter { it.id != idDaRimuovere }
            )
        }
    }

    fun onNewTurnoChangeFinishDate(finishDate: String) {
        _uiState.update { state ->
            state.copy(
                nuovoTurno = state.nuovoTurno.copy(oraFine = finishDate)
            )
        }
    }

    fun onNewTurnoChangeStartDate(startDate: String) {
        _uiState.update { state ->
            state.copy(
                nuovoTurno = state.nuovoTurno.copy(oraInizio = startDate)
            )
        }
    }

    fun onNewTurnoChangeName(name: String) {
        _uiState.update { state ->
            state.copy(
                nuovoTurno = state.nuovoTurno.copy(nome = name)
            )
        }
    }

    fun aggiungiTurno() {
        _uiState.update { state ->
            state.copy(
                turni = state.turni + state.nuovoTurno,
                nuovoTurno = TurnoFrequente() // resetta il form
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }

    fun onComplete(idAzienda: String) {
        viewModelScope.launch {
            val result = aziendaRepository.addPianificaSetup(
                idAzienda,
                _uiState.value.aree,
                _uiState.value.turni,
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(onDone = true) }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(errorMsg = result.message) }
                }

                is Resource.Empty -> {
                    _uiState.update { it.copy(errorMsg = "Nessun dato da salvare") }
                }
            }
        }
    }


    // ========== NUOVE FUNZIONI PER ORARI SETTIMANALI ==========

    fun onAreaSelectionChanged(areaId: String, isSelected: Boolean) {
        _uiState.update { state ->
            val newSelectedAree = if (isSelected) {
                state.selectedAree + areaId
            } else {
                state.selectedAree - areaId
            }

            // Carica gli orari della prima area selezionata come template
            val newOrariTemp = if (newSelectedAree.isNotEmpty()) {
                val primaAreaSelezionata = state.aree.find { it.id == newSelectedAree.first() }
                primaAreaSelezionata?.orariSettimanali?.ifEmpty {
                    mapOf(
                        DayOfWeek.MONDAY to ("08:00" to "18:00"),
                        DayOfWeek.TUESDAY to ("08:00" to "18:00"),
                        DayOfWeek.WEDNESDAY to ("08:00" to "18:00"),
                        DayOfWeek.THURSDAY to ("08:00" to "18:00"),
                        DayOfWeek.FRIDAY to ("08:00" to "18:00")
                    )
                } ?: mapOf(
                    DayOfWeek.MONDAY to ("08:00" to "18:00"),
                    DayOfWeek.TUESDAY to ("08:00" to "18:00"),
                    DayOfWeek.WEDNESDAY to ("08:00" to "18:00"),
                    DayOfWeek.THURSDAY to ("08:00" to "18:00"),
                    DayOfWeek.FRIDAY to ("08:00" to "18:00")
                )
            } else {
                // Se non ci sono aree selezionate, resetta gli orari temporanei
                mapOf()
            }

            state.copy(
                selectedAree = newSelectedAree,
                orariTemp = newOrariTemp
            )
        }
    }

    fun selectAllAree() {
        _uiState.update { state ->
            val allAreaIds = state.aree.map { it.id }

            // Carica gli orari della prima area come template, o orari di default se vuoti
            val newOrariTemp = if (state.aree.isNotEmpty()) {
                val primaArea = state.aree.first()
                if (primaArea.orariSettimanali.isNotEmpty()) {
                    primaArea.orariSettimanali
                } else {
                    mapOf(
                        DayOfWeek.MONDAY to ("08:00" to "18:00"),
                        DayOfWeek.TUESDAY to ("08:00" to "18:00"),
                        DayOfWeek.WEDNESDAY to ("08:00" to "18:00"),
                        DayOfWeek.THURSDAY to ("08:00" to "18:00"),
                        DayOfWeek.FRIDAY to ("08:00" to "18:00")
                    )
                }
            } else {
                mapOf()
            }

            state.copy(
                selectedAree = allAreaIds,
                orariTemp = newOrariTemp
            )
        }
    }

    fun deselectAllAree() {
        _uiState.update { state ->
            state.copy(
                selectedAree = emptyList(),
                orariTemp = mapOf()
            )
        }
    }

    fun onGiornoLavoroChanged(giorno: DayOfWeek, isLavorativo: Boolean) {
        _uiState.update { state ->
            val newOrariTemp = if (isLavorativo) {
                // Aggiungi il giorno con orari di default se non esiste già
                if (state.orariTemp.containsKey(giorno)) {
                    state.orariTemp
                } else {
                    state.orariTemp + (giorno to ("08:00" to "18:00"))
                }
            } else {
                // Rimuovi il giorno
                state.orariTemp - giorno
            }

            state.copy(orariTemp = newOrariTemp)
        }
    }

    fun onOrarioInizioChanged(giorno: DayOfWeek, orarioInizio: String) {
        _uiState.update { state ->
            val orarioCorrente = state.orariTemp[giorno]
            if (orarioCorrente != null) {
                val newOrariTemp = state.orariTemp + (giorno to (orarioInizio to orarioCorrente.second))
                state.copy(orariTemp = newOrariTemp)
            } else {
                state
            }
        }
    }

    fun onOrarioFineChanged(giorno: DayOfWeek, orarioFine: String) {
        _uiState.update { state ->
            val orarioCorrente = state.orariTemp[giorno]
            if (orarioCorrente != null) {
                val newOrariTemp = state.orariTemp + (giorno to (orarioCorrente.first to orarioFine))
                state.copy(orariTemp = newOrariTemp)
            } else {
                state
            }
        }
    }

    fun marcaAreeConfigurateOrari(areeIds: List<String>) {
        _uiState.update { currentState ->
            currentState.copy(
                areeOrariConfigurati = currentState.areeOrariConfigurati + areeIds.toSet()
            )
        }
    }

    // 3. Funzione per selezionare solo le aree non configurate
    fun selectAllAreeNonConfigurate(areeNonConfigurateIds: List<String>) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedAree = areeNonConfigurateIds
            )
        }
    }


    fun salvaOrariSettimanali() {
        _uiState.update { state ->
            // Applica gli orari temporanei a tutte le aree selezionate
            val areeAggiornate = state.aree.map { area ->
                if (state.selectedAree.contains(area.id)) {
                    area.copy(orariSettimanali = state.orariTemp)
                } else {
                    area
                }
            }

            // Aggiungi le aree selezionate a quelle configurate
            val nuoveAreeConfigurate = state.areeConOrariConfigurati + state.selectedAree

            state.copy(
                aree = areeAggiornate,
                areeConOrariConfigurati = nuoveAreeConfigurate,
                selectedAree = emptyList(), // Reset selezione dopo salvataggio
                orariTemp = mapOf() // Reset orari temporanei
            )
        }
    }

    fun selectAllAreeNonConfigurate() {
        _uiState.update { state ->
            val areeNonConfigurate = state.aree
                .filter { !state.areeConOrariConfigurati.contains(it.id) }
                .map { it.id }

            // Carica gli orari della prima area non configurata come template
            val newOrariTemp = if (areeNonConfigurate.isNotEmpty()) {
                val primaAreaNonConfigurata = state.aree.find { it.id == areeNonConfigurate.first() }
                primaAreaNonConfigurata?.orariSettimanali?.ifEmpty {
                    mapOf(
                        DayOfWeek.MONDAY to ("08:00" to "18:00"),
                        DayOfWeek.TUESDAY to ("08:00" to "18:00"),
                        DayOfWeek.WEDNESDAY to ("08:00" to "18:00"),
                        DayOfWeek.THURSDAY to ("08:00" to "18:00"),
                        DayOfWeek.FRIDAY to ("08:00" to "18:00")
                    )
                } ?: mapOf(
                    DayOfWeek.MONDAY to ("08:00" to "18:00"),
                    DayOfWeek.TUESDAY to ("08:00" to "18:00"),
                    DayOfWeek.WEDNESDAY to ("08:00" to "18:00"),
                    DayOfWeek.THURSDAY to ("08:00" to "18:00"),
                    DayOfWeek.FRIDAY to ("08:00" to "18:00")
                )
            } else {
                mapOf()
            }

            state.copy(
                selectedAree = areeNonConfigurate,
                orariTemp = newOrariTemp
            )
        }
    }

}