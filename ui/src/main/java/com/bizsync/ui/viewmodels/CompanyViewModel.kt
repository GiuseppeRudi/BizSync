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
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class CompanyViewModel @Inject constructor( private val aziendaRepository: AziendaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyState())
    val uiState: StateFlow<CompanyState> = _uiState.asStateFlow()

    fun setOrariSettimanaliModificati(orari: Map<String, Map<DayOfWeek, Pair<LocalTime, LocalTime>>>) {
        _uiState.update { it.copy(orariSettimanaliModificati = orari) }
    }

    fun setEditingOrariAreaId(areaId: String?) {
        _uiState.update { it.copy(editingOrariArea = areaId) }
    }

    fun setShowOrariDialog(show: Boolean) {
        _uiState.update { it.copy(showOrariDialog = show) }
    }

    fun setOrariTemp(orari: Map<DayOfWeek, Pair<LocalTime, LocalTime>>) {
        _uiState.update { it.copy(orariTemp = orari) }
    }

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
            // Fallback a coppia di LocalTime (esempio 08:00-18:00)
            val orarioCorrente = nuoviOrari[giorno] ?: (LocalTime.of(8, 0) to LocalTime.of(18, 0))
            // Aggiorna solo il "first" (orario inizio)
            nuoviOrari[giorno] = orarioCorrente.copy(first = orario)
            current.copy(orariTemp = nuoviOrari)
        }
    }



    fun setGiornoPublicazioneTemp(giorno: DayOfWeek) {
        _uiState.update {
            it.copy(
                giornoPublicazioneTemp = giorno,
                hasGiornoPublicazioneChanges = true
            )
        }
    }

    fun setGiornoPublicazioneTempNull() {
        _uiState.update {
            it.copy(
                giornoPublicazioneTemp = null,
            )
        }
    }


    fun setShowGiornoPublicazioneDialog(show: Boolean) {
        _uiState.update { it.copy(showGiornoPublicazioneDialog = show) }
    }

    fun openGiornoPublicazioneDialog(giornoAttuale: DayOfWeek) {
        _uiState.update {
            it.copy(
                giornoPublicazioneTemp = giornoAttuale,
                showGiornoPublicazioneDialog = true,
                hasGiornoPublicazioneChanges = false
            )
        }
    }

    fun closeGiornoPublicazioneDialog() {
        _uiState.update {
            it.copy(
                showGiornoPublicazioneDialog = false,
                giornoPublicazioneTemp = null,
                hasGiornoPublicazioneChanges = false
            )
        }
    }

    fun setHasGiornoPubblicazioneChanges(value: Boolean) {
        _uiState.update { it.copy(hasGiornoPubblicato = value) }
    }


    fun salvaGiornoPublicazione(idAzienda: String) {
        val nuovoGiorno = _uiState.value.giornoPublicazioneTemp ?: return

        viewModelScope.launch {
            setLoading(true)

            when (val result = aziendaRepository.updateGiornoPublicazioneTurni(idAzienda, nuovoGiorno)) {
                is Resource.Success -> {
                    setStatusMessage("Giorno di pubblicazione aggiornato con successo", DialogStatusType.SUCCESS)
                    _uiState.update {
                        it.copy(
                            showGiornoPublicazioneDialog = false,
                            hasGiornoPublicazioneChanges = false,
                            hasGiornoPubblicato = true

                        )
                    }
                }

                is Resource.Error -> {
                    setStatusMessage(result.message ?: "Errore nel salvataggio", DialogStatusType.ERROR)
                }

                is Resource.Empty -> {
                    setStatusMessage("Nessuna modifica da salvare", DialogStatusType.ERROR)
                }
            }

            setLoading(false)
        }
    }

    // AGGIORNA resetTempState
    fun resetTempState() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                editingArea = null,
                showOrariDialog = false,
                editingOrariArea = null,
                orariTemp = emptyMap(),
                showGiornoPublicazioneDialog = false,
                giornoPublicazioneTemp = null,
                hasGiornoPublicazioneChanges = false,
                resultMsg = null,
                hasChanges = false
            )
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

    // AGGIORNA la funzione di salvataggio per includere gli orari
    fun onSaveChanges(idAzienda: String) {
        viewModelScope.launch {
            setLoading(true)

            val areeDaSalvare = _uiState.value.areeModificate
            val orariDaSalvare = _uiState.value.orariSettimanaliModificati

            val areeAggiornate = areeDaSalvare.map { area ->
                val nuoviOrari = orariDaSalvare[area.nomeArea] ?: area.orariSettimanali
                area.copy(orariSettimanali = nuoviOrari)
            }

            when (val result = aziendaRepository.updateAreeLavoro(idAzienda, areeAggiornate)) {
                is Resource.Success -> {
                    setStatusMessage("Modifiche salvate con successo", DialogStatusType.SUCCESS)
                    setHasChanges(false)
                }

                is Resource.Empty -> {
                    setStatusMessage("Nessuna modifica da salvare", DialogStatusType.ERROR)
                }

                is Resource.Error -> {
                    setStatusMessage(result.message ?: "Errore generico", DialogStatusType.ERROR)
                }
            }

            setLoading(false)
        }
    }



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
                if (it.nomeArea == id) it.copy(nomeArea = nuovoNome) else it
            }
            current.copy(areeModificate = nuoveAree, hasChanges = true)
        }
    }

//    /** Rimuove un'area dall'elenco */
//    fun removeAreaModificata(area: AreaLavoro) {
//        _uiState.update { current ->
//            val nuoveAree = current.areeModificate.filterNot { it.id == area.id }
//            current.copy(areeModificate = nuoveAree, hasChanges = true)
//        }
//    }

    /** Imposta se ci sono modifiche da salvare */
    fun setHasChanges(value: Boolean) {
        _uiState.update { it.copy(hasChanges = value) }
    }

    /** Mostra o nasconde il dialog di aggiunta */
    fun setShowAddDialog(show: Boolean) {
        _uiState.update { it.copy(showAddDialog = show) }
    }

//    fun onSaveChanges(idAzienda: String) {
//        viewModelScope.launch {
//            setLoading(true)
//
//            val areeDaSalvare = _uiState.value.areeModificate
//
//            when (val result = aziendaRepository.updateAreeLavoro(idAzienda, areeDaSalvare)) {
//                is Resource.Success -> {
//                    setStatusMessage("Modifiche salvate con successo", DialogStatusType.SUCCESS)
//                    setHasChanges(false)
//                }
//
//                is Resource.Empty -> {
//                    setStatusMessage("Nessuna area da salvare", DialogStatusType.ERROR)
//                }
//
//                is Resource.Error -> {
//                    setStatusMessage(result.message ?: "Errore generico", DialogStatusType.ERROR)
//                }
//            }
//
//            setLoading(false)
//        }
//    }



    /** Imposta le aree modificate (ad esempio dopo editing) */
    fun setAreeModificate(aree: List<AreaLavoro>) {
        _uiState.update { it.copy(areeModificate = aree, hasChanges = true) }
    }

    /** Reset dello stato temporaneo, utile dopo conferma salvataggi */
//    fun resetTempState() {
//        _uiState.update {
//            it.copy(
//                showAddDialog = false,
//                editingArea = null,
//                resultMsg = null,
//                hasChanges = false
//            )
//        }
//    }
}
