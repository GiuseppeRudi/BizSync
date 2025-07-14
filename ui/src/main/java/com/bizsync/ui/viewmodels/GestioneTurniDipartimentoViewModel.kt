package com.bizsync.ui.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.backend.repository.WeeklyShiftRepository
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Turno
import com.bizsync.ui.model.GestioneTurniDipartimentoState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class GestioneTurniDipartimentoViewModel @Inject constructor(
    private val turnoRepository: TurnoRepository,
    private val weeklyShiftRepository: WeeklyShiftRepository
) : ViewModel() {

    companion object {
        private const val TAG = "GestioneTurniDipViewModel"
    }

    private val _uiState = MutableStateFlow(GestioneTurniDipartimentoState())
    val uiState: StateFlow<GestioneTurniDipartimentoState> = _uiState.asStateFlow()




    fun showAddTurnoDialog() {
        Log.d(TAG, "➕ Apertura dialog aggiungi turno")
        _uiState.update {
            it.copy(
                showTurnoDialog = true,
                turnoInModifica = null
            )
        }
    }

    fun editTurno(turno: Turno) {
        Log.d(TAG, "✏️ Modifica turno: ${turno.nome}")
        _uiState.update {
            it.copy(
                showTurnoDialog = true,
                turnoInModifica = turno
            )
        }
    }


    fun deleteTurno(turno: Turno) {
        viewModelScope.launch {
            Log.d(TAG, "🗑️ Eliminazione turno: ${turno.nome}")

            _uiState.update { it.copy(isLoading = true) }

            when (val result = turnoRepository.deleteTurno(turno.id)) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ Turno eliminato con successo")

                    // Rimuovi dalla lista locale e ricalcola stato
                    val turniAggiornati = _uiState.value.turniAssegnati.filter { it.id != turno.id }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            turniAssegnati = turniAggiornati,
                            stato = calcolaStatoDipartimento(turniAggiornati),
                        )
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ Errore eliminazione turno: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMsg = result.message
                        )
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMsg = "Errore imprevisto durante l'eliminazione"
                        )
                    }
                }
            }
        }
    }


    fun saveTurno(turno: Turno) {
//        viewModelScope.launch {
//            Log.d(TAG, "💾 Salvataggio turno: ${turno.nome}")
//
//            _uiState.update { it.copy(isLoading = true) }
//
//            val isNewTurno = turno.id.isEmpty()
//            val turnoToSave = if (isNewTurno) {
//                turno.copy(
//                    dipartimentoId = _uiState.value.dipartimento.id,
//                    data = _uiState.value.giornoSelezionato
//                )
//            } else turno
//
//            val result = if (isNewTurno) {
//                turnoRepository.createTurno(turnoToSave)
//            } else {
//                turnoRepository.updateTurno(turnoToSave)
//            }
//
//            when (result) {
//                is Resource.Success -> {
//                    Log.d(TAG, "✅ Turno salvato con successo")
//
//                    // Ricarica i dati per avere lo stato aggiornato
//                    loadDipartimentoData(_uiState.value.dipartimento, _uiState.value.giornoSelezionato)
//
//                    // Chiudi il dialog
//                    hideTurnoDialog()
//                }
//                is Resource.Error -> {
//                    Log.e(TAG, "❌ Errore salvataggio turno: ${result.message}")
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            errorMsg = result.message
//                        )
//                    }
//                }
//                else -> {
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            errorMsg = "Errore imprevisto durante il salvataggio"
//                        )
//                    }
//                }
//            }
//        }
    }


    fun hideTurnoDialog() {
        Log.d(TAG, "❌ Chiusura dialog turno")
        _uiState.update {
            it.copy(
                showTurnoDialog = false,
                turnoInModifica = null
            )
        }
    }


    fun segnaDipartimentoCompletato() {
//        viewModelScope.launch {
//            Log.d(TAG, "✅ Segna dipartimento come completato")
//
//            _uiState.update { it.copy(isLoading = true) }
//
//            when (val result = weeklyShiftRepository.segnaDipartimentoCompletato(
//                _uiState.value.dipartimentoId,
//                _uiState.value.giornoSelezionato
//            )) {
//                is Resource.Success -> {
//                    Log.d(TAG, "✅ Dipartimento segnato come completato")
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            isCompletato = true
//                        )
//                    }
//                }
//                is Resource.Error -> {
//                    Log.e(TAG, "❌ Errore completamento dipartimento: ${result.message}")
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            errorMsg = result.message
//                        )
//                    }
//                }
//                else -> {
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            errorMsg = "Errore imprevisto durante il completamento"
//                        )
//                    }
//                }
//            }
//        }
    }

    /**
     * Pulisce gli errori
     */
    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }

    // ========== FUNZIONI PRIVATE DI CALCOLO ==========

    /**
     * Calcola lo stato del dipartimento basandosi sui turni assegnati
     */
    private fun calcolaStatoDipartimento(turni: List<Turno>): DipartimentoStatus {
        if (turni.isEmpty()) {
            return DipartimentoStatus.INCOMPLETE
        }

        // Calcola copertura oraria (logica semplificata)
        val oreTotaliRichieste = 8 // TODO: calcolare dalle ore di apertura del dipartimento
        val oreAssegnate = turni.sumOf { calcolaDurataTurno(it) }

        return when {
            oreAssegnate >= oreTotaliRichieste -> DipartimentoStatus.COMPLETE
            oreAssegnate > 0 -> DipartimentoStatus.PARTIAL
            else -> DipartimentoStatus.INCOMPLETE
        }
    }
    private fun calcolaDurataTurno(turno: Turno): Int {
        return try {
            val inizio = LocalTime.parse(turno.orarioInizio, DateTimeFormatter.ofPattern("HH:mm"))
            val fine = LocalTime.parse(turno.orarioFine, DateTimeFormatter.ofPattern("HH:mm"))

            val minuti = fine.toSecondOfDay() - inizio.toSecondOfDay()
            (minuti / 3600).toInt()
        } catch (e: Exception) {
            0
        }
    }

}

// ========== DATA CLASSES ==========


enum class DipartimentoStatus {
    COMPLETE,   // ✅ Completato
    PARTIAL,    // ⚠️ Parziale (turni mancanti)
    INCOMPLETE  // ❌ Da pianificare
}