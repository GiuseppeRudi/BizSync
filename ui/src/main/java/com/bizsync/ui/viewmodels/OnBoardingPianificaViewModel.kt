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
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class OnBoardingPianificaViewModel @Inject constructor(
    private val aziendaRepository: AziendaRepository,
    private val OnBoardingPianificaRepository: OnBoardingPianificaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnBoardingPianificaState())
    val uiState: StateFlow<OnBoardingPianificaState> = _uiState

    // ========== FUNZIONI BASE ==========

    fun setStep(step: Int) {
        _uiState.update { it.copy(currentStep = step) }
    }

    fun reset() {
        _uiState.update { OnBoardingPianificaState() }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }

    // ========== GESTIONE AREE ==========

    fun onNuovaAreaChangeName(name: String) {
        _uiState.update { it.copy(nuovaArea = it.nuovaArea.copy(nomeArea = name)) }
    }

    fun aggiungiArea() {
        _uiState.update { it.copy(aree = it.aree + it.nuovaArea) }
    }

    fun resetNuovaArea() {
        _uiState.update { it.copy(nuovaArea = AreaLavoro()) }
    }

    fun onRimuoviAreaById(idDaRimuovere: String) {
        _uiState.update { state ->
            state.copy(
                aree = state.aree.filter { it.nomeArea != idDaRimuovere }
            )
        }
    }

    fun generaAreeAi(nomeAzienda: String) {
        viewModelScope.launch {
            val aree = OnBoardingPianificaRepository.setAreaAi(nomeAzienda)
            _uiState.update { it.copy(aree = aree, areePronte = true) }
        }
    }

    // ========== GESTIONE ORARI CON LOCALTIME ==========

    fun onAreaSelectionChanged(areaId: String, isSelected: Boolean) {
        _uiState.update { state ->
            val newSelectedAree = if (isSelected) {
                state.selectedAree + areaId
            } else {
                state.selectedAree - areaId
            }

            // Carica gli orari della prima area selezionata come template
            val newOrariTemp = if (newSelectedAree.isNotEmpty()) {
                val primaAreaSelezionata = state.aree.find { it.nomeArea == newSelectedAree.first() }
                primaAreaSelezionata?.orariSettimanali?.ifEmpty {
                    // Orari di default se l'area non ha orari configurati
                    mapOf(
                        DayOfWeek.MONDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
                        DayOfWeek.TUESDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
                        DayOfWeek.WEDNESDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
                        DayOfWeek.THURSDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
                        DayOfWeek.FRIDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0))
                    )
                } ?: mapOf(
                    // Orari di default se l'area è null
                    DayOfWeek.MONDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
                    DayOfWeek.TUESDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
                    DayOfWeek.WEDNESDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
                    DayOfWeek.THURSDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
                    DayOfWeek.FRIDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0))
                )
            } else {
                emptyMap()
            }

            state.copy(
                selectedAree = newSelectedAree,
                orariTemp = newOrariTemp
            )
        }
    }

    fun deselectAllAree() {
        _uiState.update { state ->
            state.copy(
                selectedAree = emptyList(),
                orariTemp = emptyMap()
            )
        }
    }

    fun selectAllAreeNonConfigurate(areeNonConfigurateIds: List<String>) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedAree = areeNonConfigurateIds
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
                    state.orariTemp + (giorno to (LocalTime.of(8, 0) to LocalTime.of(18, 0)))
                }
            } else {
                // Rimuovi il giorno
                state.orariTemp - giorno
            }

            state.copy(orariTemp = newOrariTemp)
        }
    }

    fun onOrarioInizioChangedLocalTime(giorno: DayOfWeek, orarioInizio: LocalTime) {
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

    fun onOrarioFineChangedLocalTime(giorno: DayOfWeek, orarioFine: LocalTime) {
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

    fun impostaOrariStandard() {
        val orariStandard = mapOf(
            DayOfWeek.MONDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
            DayOfWeek.TUESDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
            DayOfWeek.WEDNESDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
            DayOfWeek.THURSDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0)),
            DayOfWeek.FRIDAY to (LocalTime.of(8, 0) to LocalTime.of(18, 0))
        )

        _uiState.update { state ->
            state.copy(orariTemp = orariStandard)
        }
    }

    fun resetOrariTemp() {
        _uiState.update { state ->
            state.copy(orariTemp = emptyMap())
        }
    }

    fun marcaAreeConfigurateOrari(areeIds: List<String>) {
        _uiState.update { currentState ->
            currentState.copy(
                areeOrariConfigurati = currentState.areeOrariConfigurati + areeIds.toSet()
            )
        }
    }

    fun salvaOrariSettimanali() {
        _uiState.update { state ->
            // Applica gli orari temporanei a tutte le aree selezionate
            val areeAggiornate = state.aree.map { area ->
                if (state.selectedAree.contains(area.nomeArea)) {
                    area.copy(orariSettimanali = state.orariTemp)
                } else {
                    area
                }
            }

            state.copy(
                aree = areeAggiornate,
                selectedAree = emptyList(), // Reset selezione dopo salvataggio
                orariTemp = emptyMap() // Reset orari temporanei
            )
        }
    }

    // ========== FUNZIONI LEGACY PER RETROCOMPATIBILITÀ ==========

    fun onOrarioInizioChanged(giorno: DayOfWeek, orarioInizio: String) {
        try {
            // Gestisci diversi formati di input
            val time = when {
                orarioInizio.matches(Regex("^\\d{1,2}:\\d{2}$")) -> {
                    // Aggiungi zero iniziale se necessario (es: "8:00" -> "08:00")
                    val parts = orarioInizio.split(":")
                    val hour = parts[0].padStart(2, '0')
                    val minute = parts[1]
                    LocalTime.parse("$hour:$minute")
                }
                orarioInizio.matches(Regex("^\\d{2}:\\d{2}$")) -> {
                    // Formato già corretto
                    LocalTime.parse(orarioInizio)
                }
                else -> {
                    // Fallback per formato non riconosciuto
                    LocalTime.of(8, 0)
                }
            }

            onOrarioInizioChangedLocalTime(giorno, time)
        } catch (e: Exception) {
            // In caso di errore, usa un orario di default
            onOrarioInizioChangedLocalTime(giorno, LocalTime.of(8, 0))
        }
    }

    fun onOrarioFineChanged(giorno: DayOfWeek, orarioFine: String) {
        try {
            // Gestisci diversi formati di input
            val time = when {
                orarioFine.matches(Regex("^\\d{1,2}:\\d{2}$")) -> {
                    // Aggiungi zero iniziale se necessario (es: "8:00" -> "08:00")
                    val parts = orarioFine.split(":")
                    val hour = parts[0].padStart(2, '0')
                    val minute = parts[1]
                    LocalTime.parse("$hour:$minute")
                }
                orarioFine.matches(Regex("^\\d{2}:\\d{2}$")) -> {
                    // Formato già corretto
                    LocalTime.parse(orarioFine)
                }
                else -> {
                    // Fallback per formato non riconosciuto
                    LocalTime.of(18, 0)
                }
            }

            onOrarioFineChangedLocalTime(giorno, time)
        } catch (e: Exception) {
            // In caso di errore, usa un orario di default
            onOrarioFineChangedLocalTime(giorno, LocalTime.of(18, 0))
        }
    }

    // ========== GESTIONE TURNI ==========

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

    fun generaTurniAi(nomeAzienda: String) {
        viewModelScope.launch {
            val turni = OnBoardingPianificaRepository.setTurniAi(nomeAzienda)
            _uiState.update { it.copy(turni = turni, turniPronti = true) }
        }
    }

    // ========== COMPLETAMENTO SETUP ==========

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
}