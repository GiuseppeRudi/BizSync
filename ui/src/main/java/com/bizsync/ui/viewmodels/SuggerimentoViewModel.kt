//package com.bizsync.ui.viewmodels
//
//import com.bizsync.ui.model.SuggerimentoState
//
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.bizsync.backend.repository.TurnoRepository
//import com.bizsync.backend.repository.WeeklyShiftRepository
//import com.bizsync.domain.model.Turno
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import java.time.LocalDate
//import java.time.LocalTime
//import java.time.format.DateTimeFormatter
//import javax.inject.Inject
//
//@HiltViewModel
//class SuggerimentoViewModel @Inject constructor(
//    private val turnoRepository: TurnoRepository,
//    private val weeklyShiftRepository: WeeklyShiftRepository
//) : ViewModel() {
//
//    companion object {
//        private const val TAG = "GestioneTurniDipViewModel"
//    }
//
//    val suggerimenti: List<SuggerimentoTurno> = emptyList()
//
//    private val _uiState = MutableStateFlow(SuggerimentoState())
//    val uiState: StateFlow<SuggerimentoState> = _uiState.asStateFlow()
//
//
//
//
//    fun applySuggerimento(suggerimento: SuggerimentoTurno) {
//        viewModelScope.launch {
//            Log.d(TAG, "💡 Applicazione suggerimento: ${suggerimento.tipo}")
//
//            when (suggerimento.tipo) {
//                SuggerimentoTipo.CREA_TURNO_MANCANTE -> {
//                    val nuovoTurno = suggerimento.turnoSuggerito ?: return@launch
////                    saveTurno(nuovoTurno)
//                }
//                SuggerimentoTipo.DIVIDI_TURNO_LUNGO -> {
//                    // Implementa logica per dividere turno troppo lungo
//                    dividiTurnoLungo(suggerimento)
//                }
//                SuggerimentoTipo.UNISCI_TURNI_ADIACENTI -> {
//                    // Implementa logica per unire turni adiacenti
//                    unisciTurniAdiacenti(suggerimento)
//                }
//                SuggerimentoTipo.RIMUOVI_SOVRAPPOSIZIONE -> {
//                    // Implementa logica per rimuovere sovrapposizioni
//                    rimuoviSovrapposizione(suggerimento)
//                }
//            }
//        }
//    }
//
//    private fun generaSuggerimenti(turni: List<Turno>, giorno: LocalDate): List<SuggerimentoTurno> {
//        val suggerimenti = mutableListOf<SuggerimentoTurno>()
//
//        // Suggerimento: turni mancanti per copertura completa
//        if (turni.isEmpty()) {
//            suggerimenti.add(
//                SuggerimentoTurno(
//                    id = "turno_mancante_mattina",
//                    tipo = SuggerimentoTipo.CREA_TURNO_MANCANTE,
//                    titolo = "Crea turno mattina",
//                    descrizione = "Nessun turno assegnato. Crea un turno per la mattina (08:00-12:00)",
//                    turnoSuggerito = Turno(
//                        titolo = "Turno Mattina",
//                        orarioInizio = "08:00",
//                        orarioFine = "12:00",
//                        data = giorno
//                    )
//                )
//            )
//        }
//
//        // Suggerimento: turni troppo lunghi
//        turni.forEach { turno ->
//            val durata = calcolaDurataTurno(turno)
//            if (durata > 8) {
//                suggerimenti.add(
//                    SuggerimentoTurno(
//                        id = "dividi_${turno.id}",
//                        tipo = SuggerimentoTipo.DIVIDI_TURNO_LUNGO,
//                        titolo = "Dividi turno lungo",
//                        descrizione = "Il turno '${turno.titolo}' dura $durata ore. Considera di dividerlo in turni più corti",
//                        turnoOriginale = turno
//                    )
//                )
//            }
//        }
//
//        // Suggerimento: turni adiacenti da unire
//        detectTurniAdiacenti(turni).forEach { (turno1, turno2) ->
//            suggerimenti.add(
//                SuggerimentoTurno(
//                    id = "unisci_${turno1.id}_${turno2.id}",
//                    tipo = SuggerimentoTipo.UNISCI_TURNI_ADIACENTI,
//                    titolo = "Unisci turni adiacenti",
//                    descrizione = "I turni '${turno1.titolo}' e '${turno2.titolo}' sono adiacenti. Considera di unirli",
//                    turniCoinvolti = listOf(turno1, turno2)
//                )
//            )
//        }
//
//        // Suggerimento: sovrapposizioni da risolvere
//        detectSovrapposizioni(turni).forEach { (turno1, turno2) ->
//            suggerimenti.add(
//                SuggerimentoTurno(
//                    id = "rimuovi_sovrapposizione_${turno1.id}_${turno2.id}",
//                    tipo = SuggerimentoTipo.RIMUOVI_SOVRAPPOSIZIONE,
//                    titolo = "Risolvi sovrapposizione",
//                    descrizione = "I turni '${turno1.titolo}' e '${turno2.titolo}' si sovrappongono",
//                    turniCoinvolti = listOf(turno1, turno2)
//                )
//            )
//        }
//
//        return suggerimenti
//    }
//
//    /**
//     * Calcola la durata di un turno in ore
//     */
//
//    private fun calcolaDurataTurno(turno: Turno): Int {
//        return try {
//            val inizio = LocalTime.parse(turno.orarioInizio, DateTimeFormatter.ofPattern("HH:mm"))
//            val fine = LocalTime.parse(turno.orarioFine, DateTimeFormatter.ofPattern("HH:mm"))
//
//            val minuti = fine.toSecondOfDay() - inizio.toSecondOfDay()
//            (minuti / 3600).toInt()
//        } catch (e: Exception) {
//            0
//        }
//    }
//    /**
//     * Rileva turni adiacenti che potrebbero essere uniti
//     */
//    private fun detectTurniAdiacenti(turni: List<Turno>): List<Pair<Turno, Turno>> {
//        val adiacenti = mutableListOf<Pair<Turno, Turno>>()
//
//        for (i in turni.indices) {
//            for (j in i + 1 until turni.size) {
//                val turno1 = turni[i]
//                val turno2 = turni[j]
//
//                if (turno1.orarioFine == turno2.orarioInizio || turno2.orarioFine == turno1.orarioInizio) {
//                    adiacenti.add(turno1 to turno2)
//                }
//            }
//        }
//
//        return adiacenti
//    }
//
//    /**
//     * Rileva sovrapposizioni tra turni
//     */
//    private fun detectSovrapposizioni(turni: List<Turno>): List<Pair<Turno, Turno>> {
//        val sovrapposizioni = mutableListOf<Pair<Turno, Turno>>()
//
//        for (i in turni.indices) {
//            for (j in i + 1 until turni.size) {
//                val turno1 = turni[i]
//                val turno2 = turni[j]
//
//                if (turniSiSovrappongono(turno1, turno2)) {
//                    sovrapposizioni.add(turno1 to turno2)
//                }
//            }
//        }
//
//        return sovrapposizioni
//    }
//
//    /**
//     * Verifica se due turni si sovrappongono
//     */
//    private fun turniSiSovrappongono(turno1: Turno, turno2: Turno): Boolean {
//        return try {
//            val inizio1 = LocalTime.parse(turno1.orarioInizio)
//            val fine1 = LocalTime.parse(turno1.orarioFine)
//            val inizio2 = LocalTime.parse(turno2.orarioInizio)
//            val fine2 = LocalTime.parse(turno2.orarioFine)
//
//            !(fine1 <= inizio2 || fine2 <= inizio1)
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    // ========== IMPLEMENTAZIONE SUGGERIMENTI ==========
//
//    private suspend fun dividiTurnoLungo(suggerimento: SuggerimentoTurno) {
//        val turnoOriginale = suggerimento.turnoOriginale ?: return
//
//        // Elimina turno originale e crea due turni più corti
////        deleteTurno(turnoOriginale)
//
//        // Crea due turni di 4 ore ciascuno
//        val metaGiornata = LocalTime.parse(turnoOriginale.orarioInizio).plusHours(4)
//
//        val turno1 = turnoOriginale.copy(
//            id = "",
//            titolo = "${turnoOriginale.titolo} - Mattina",
//            orarioFine = metaGiornata.format(DateTimeFormatter.ofPattern("HH:mm"))
//        )
//
//        val turno2 = turnoOriginale.copy(
//            id = "",
//            titolo = "${turnoOriginale.titolo} - Pomeriggio",
//            orarioInizio = metaGiornata.format(DateTimeFormatter.ofPattern("HH:mm"))
//        )
//
////        saveTurno(turno1)
////        saveTurno(turno2)
//    }
//
//    private suspend fun unisciTurniAdiacenti(suggerimento: SuggerimentoTurno) {
//        val turni = suggerimento.turniCoinvolti
//        if (turni.size != 2) return
//
//        val turno1 = turni[0]
//        val turno2 = turni[1]
//
//        // Determina ordine cronologico
//        val (primo, secondo) = if (turno1.orarioInizio < turno2.orarioInizio) {
//            turno1 to turno2
//        } else {
//            turno2 to turno1
//        }
//
//        // Crea turno unificato
//        val turnoUnificato = primo.copy(
//            id = "",
//            titolo = "Turno Unificato",
//            orarioFine = secondo.orarioFine,
//            dipendente = if (primo.dipendente == secondo.dipendente) primo.dipendente else ""
//        )
//
////        // Elimina turni originali
////        deleteTurno(primo)
////        deleteTurno(secondo)
////
////        // Crea nuovo turno
////        saveTurno(turnoUnificato)
//    }
//
//    private suspend fun rimuoviSovrapposizione(suggerimento: SuggerimentoTurno) {
//        val turni = suggerimento.turniCoinvolti
//        if (turni.size != 2) return
//
//        val turno1 = turni[0]
//        val turno2 = turni[1]
//
//        // Strategia semplice: accorcia il secondo turno per evitare sovrapposizione
//        val turnoModificato = turno2.copy(
//            orarioInizio = turno1.orarioFine
//        )
//
////        saveTurno(turnoModificato)
//    }
//}
//
//// ========== DATA CLASSES ==========
//
//
//data class SuggerimentoTurno(
//    val id: String,
//    val tipo: SuggerimentoTipo,
//    val titolo: String,
//    val descrizione: String,
//    val turnoSuggerito: Turno? = null,
//    val turnoOriginale: Turno? = null,
//    val turniCoinvolti: List<Turno> = emptyList()
//)
//
//enum class SuggerimentoTipo {
//    CREA_TURNO_MANCANTE,
//    DIVIDI_TURNO_LUNGO,
//    UNISCI_TURNI_ADIACENTI,
//    RIMUOVI_SOVRAPPOSIZIONE
//}
//
