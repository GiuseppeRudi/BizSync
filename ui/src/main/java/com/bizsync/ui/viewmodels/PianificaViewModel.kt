package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.orchestrator.TurnoOrchestrator
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.backend.repository.WeeklyShiftRepository
import com.bizsync.domain.constants.enumClass.PianificaScreenManager
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.domain.utils.AbsenceWindowCalculator
import com.bizsync.domain.utils.WeeklyPublicationCalculator
import com.bizsync.ui.model.PianificaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject


    @HiltViewModel
    class PianificaViewModel @Inject constructor(
        private val turnoRepository: TurnoRepository,
        private val weeklyShiftRepository: WeeklyShiftRepository,
        private val turnoOrchestrator: TurnoOrchestrator,
    ) : ViewModel() {


        private val _currentScreen = MutableStateFlow(PianificaScreenManager.MAIN)
        val currentScreen: StateFlow<PianificaScreenManager> = _currentScreen

        companion object {
            private const val TAG = "PianificaViewModel"
        }

        private val _uistate = MutableStateFlow(PianificaState())
        val uistate: StateFlow<PianificaState> = _uistate


        fun setWeeklyShiftIdentical(value: Boolean) {
            _uistate.update { it.copy(weeklyisIdentical = value) }
        }


        fun openCreateShift()
        {
            _currentScreen.update { PianificaScreenManager.CREATE_SHIFT }
        }
        fun openGestioneTurni(dip: AreaLavoro) {
            _uistate.update { it.copy(dipartimento = dip) }
            _currentScreen.value = PianificaScreenManager.GESTIONE_TURNI_DIPARTIMENTO
        }

        fun getWeeklyShiftCorrente(selectionDate: LocalDate) {
            viewModelScope.launch {
                val weekStart = AbsenceWindowCalculator.getWeekStartFromDate(selectionDate)
                Log.d(TAG, "📆 Recupero turni per settimana con start: $weekStart")


                try {
                    when (val result = weeklyShiftRepository.getWeeklyShiftCorrente(weekStart)) {
                        is Resource.Success -> {
                            Log.d(TAG, "✅ Weekly shift corrente trovato: ${result.data}")
                            _uistate.update {
                                it.copy(
                                    weeklyShiftAttuale = result.data
                                )
                            }
                        }

                        is Resource.Empty -> {
                            Log.d(TAG, "📭 Nessun weekly shift presente per la settimana selezionata")
                            _uistate.update {
                                it.copy(
                                    weeklyShiftAttuale = null
                                )
                            }
                        }

                        is Resource.Error -> {
                            Log.e(TAG, "❌ Errore nel recupero weekly shift corrente: ${result.message}")
                            _uistate.update {
                                it.copy(
                                    errorMsg = result.message,
                                    weeklyShiftAttuale = null
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Errore imprevisto nel recupero dei dati", e)
                    _uistate.update {
                        it.copy(
                            errorMsg = e.message ?: "Errore sconosciuto",
                            weeklyShiftAttuale = null
                        )
                    }
                }
            }
        }

        fun setDipartimentoScreen(dipartimento : AreaLavoro) {
         _uistate.update { it.copy(dipartimento = dipartimento) }
            _currentScreen.value = PianificaScreenManager.GESTIONE_TURNI_DIPARTIMENTO

        }

        fun backToMain() {
            _uistate.update { it.copy(dipartimento = null)}
            _currentScreen.value = PianificaScreenManager.MAIN
        }


        // ========== WEEKLY SHIFT FUNCTIONS ==========

        /**
         * Controlla se esiste già una pianificazione settimanale
         */
        fun checkWeeklyPlanningStatus(idAzienda: String) {
            viewModelScope.launch {
                Log.d(TAG, "🔍 Controllo pianificazione per azienda: $idAzienda")

                _uistate.update { it.copy(isLoading = true, errorMsg = null) }

                // Controlla se è possibile pubblicare oggi
                val (canPublish, publishableWeek) = canPublishToday()

                if (!canPublish) {
                    Log.w(TAG, "⚠️ Non è possibile pubblicare turni al momento")
                    _uistate.update {
                        it.copy(
                            isLoading = false,
                            weeklyPlanningExists = false,
                            canPublish = false,
                            errorMsg = "Non è possibile pubblicare turni al momento"
                        )
                    }
                    return@launch
                }

                // Controlla se esiste già una pianificazione
                when (val result = checkWeeklyPlanningExists(idAzienda)) {
                    is Resource.Success -> {
                        val exists = result.data != null
                        Log.d(TAG, "✅ Controllo completato - esiste pianificazione: $exists")

                        _uistate.update {
                            it.copy(
                                isLoading = false,
                                weeklyPlanningExists = exists,
                                weeklyShiftRiferimento = result.data,
                                canPublish = canPublish,
                                publishableWeek = publishableWeek,
                                onBoardingDone = exists // Se esiste, vai direttamente al core
                            )
                        }
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Errore controllo pianificazione: ${result.message}")
                        _uistate.update {
                            it.copy(
                                isLoading = false,
                                errorMsg = result.message,
                                weeklyPlanningExists = false
                            )
                        }
                    }
                    is Resource.Empty -> {
                        Log.d(TAG, "📭 Nessuna pianificazione trovata")
                        _uistate.update {
                            it.copy(
                                isLoading = false,
                                weeklyPlanningExists = false,
                                canPublish = canPublish,
                                publishableWeek = publishableWeek
                            )
                        }
                    }
                }
            }
        }

        /**
         * Crea una nuova pianificazione settimanale
         */
        fun createWeeklyPlanning(idAzienda: String, userId: String) {
            viewModelScope.launch {
                Log.d(TAG, "💾 Creazione nuova pianificazione per azienda: $idAzienda")

                _uistate.update { it.copy(isLoading = true, errorMsg = null) }

                when (val result = createWeeklyPlanningInternal(idAzienda, userId)) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Pianificazione creata con successo")
                        _uistate.update {
                            it.copy(
                                isLoading = false,
                                weeklyPlanningExists = true,
                                weeklyShiftRiferimento = result.data,
                                onBoardingDone = true // Vai al core
                            )
                        }
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Errore creazione pianificazione: ${result.message}")
                        _uistate.update {
                            it.copy(
                                isLoading = false,
                                errorMsg = result.message
                            )
                        }
                    }
                    is Resource.Empty -> {
                        Log.e(TAG, "📭 Risposta vuota durante creazione pianificazione")
                        _uistate.update {
                            it.copy(
                                isLoading = false,
                                errorMsg = "Errore imprevisto durante la creazione"
                            )
                        }
                    }
                }
            }
        }


        fun setHasUnsavedChanges(newValue: Boolean) {
            _uistate.update { it.copy(hasUnsavedChanges = newValue) }
        }

        fun changeStatoWeeklyAttuale(nuovoStato: WeeklyShiftStatus) {
            val weeklyShiftAttuale = _uistate.value.weeklyShiftAttuale ?: return

            viewModelScope.launch {

                _uistate.update { it.copy(isSyncing = true) }

                try {
                    // Aggiorna lo stato nel database
                    val weeklyShiftAggiornato = weeklyShiftAttuale.copy(status = nuovoStato)

                    when (val result = weeklyShiftRepository.updateWeeklyShiftStatus(weeklyShiftAggiornato)) {
                        is Resource.Success -> {
                            _uistate.update {
                                it.copy(
                                    isSyncing = false,
                                    weeklyShiftAttuale = weeklyShiftAggiornato,
                                )
                            }

                            // Se il nuovo stato è DRAFT o PUBLISHED, sincronizza automaticamente i turni
                            if (nuovoStato == WeeklyShiftStatus.DRAFT || nuovoStato == WeeklyShiftStatus.PUBLISHED) {
                                syncTurni(weeklyShiftAttuale.weekStart)
                            }

                            Log.d(TAG, "Stato settimana cambiato a: $nuovoStato")
                        }

                        is Resource.Error -> {
                            _uistate.update {
                                it.copy(
                                    isSyncing = false,
                                    errorMsg = result.message
                                )
                            }
                        }

                        else -> {
                            _uistate.update {
                                it.copy(
                                    isSyncing = false,
                                    errorMsg = "Errore imprevisto durante il cambio di stato"
                                )
                            }
                        }
                    }
                } catch (e: Exception) {

                    _uistate.update {
                        it.copy(
                            isSyncing = false,
                            errorMsg = "Errore durante il cambio di stato: ${e.message}"
                        )
                    }
                }
            }
        }


        // Funzione per sincronizzare i turni
        fun syncTurni(weekStart: LocalDate?) {
            if (weekStart == null) return

        viewModelScope.launch {

            try {
                when (val result = turnoOrchestrator.syncTurniToFirebase(weekStart)) {
                    is Resource.Success -> {
                        Log.d(TAG, "Turni sincronizzati con successo")
                        _uistate.update { it.copy(isSyncing = false, hasUnsavedChanges = false) }
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Errore sincronizzazione: ${result.message}")
                        _uistate.update {it.copy(errorMsg = result.message) }
                    }
                    is Resource.Empty -> {
                        Log.d(TAG, "Nessuna modifica da sincronizzare")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Eccezione durante sincronizzazione: ${e.message}")
                _uistate.update {
                    it.copy(
                        errorMsg = "Errore durante la sincronizzazione"
                    )
                }
            }

        }
        }


        /**
         * Elimina la pianificazione corrente
         */
        fun deleteWeeklyPlanning(idAzienda: String) {
            viewModelScope.launch {
                val currentShift = _uistate.value.weeklyShiftRiferimento
                if (currentShift == null) {
                    Log.w(TAG, "⚠️ Tentativo di eliminare senza pianificazione corrente")
                    return@launch
                }

                Log.d(TAG, "🗑️ Eliminazione pianificazione settimana: ${currentShift.weekStart}")
                _uistate.update { it.copy(isLoading = true) }

                when (val result = weeklyShiftRepository.deleteWeeklyShift(idAzienda, currentShift.weekStart)) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Pianificazione eliminata con successo")
                        _uistate.update {
                            it.copy(
                                isLoading = false,
                                weeklyPlanningExists = false,
                                weeklyShiftRiferimento = null,
                                onBoardingDone = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Errore eliminazione: ${result.message}")
                        _uistate.update {
                            it.copy(
                                isLoading = false,
                                errorMsg = result.message
                            )
                        }
                    }
                    else -> {
                        _uistate.update {
                            it.copy(
                                isLoading = false,
                                errorMsg = "Errore imprevisto durante l'eliminazione"
                            )
                        }
                    }
                }
            }
        }

        // ========== PRIVATE HELPER FUNCTIONS (ex-UseCase) ==========

        /**
         * Controlla se esiste già una pianificazione per la settimana pubblicabile
         */
        private suspend fun checkWeeklyPlanningExists(idAzienda: String): Resource<WeeklyShift?> {
            return try {
                val publishableWeek = WeeklyPublicationCalculator.getPublishableWeekStart()
                    ?: return Resource.Error("Errore calcolo settimana pubblicabile")

                weeklyShiftRepository.getWeeklyShift(idAzienda, publishableWeek)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Eccezione controllo pianificazione: ${e.message}")
                Resource.Error(e.message ?: "Errore controllo pianificazione")
            }
        }

        /**
         * Crea una nuova pianificazione settimanale (logica interna)
         */
        private suspend fun createWeeklyPlanningInternal(idAzienda: String, userId: String): Resource<WeeklyShift> {
            return try {
                val publishableWeek = WeeklyPublicationCalculator.getPublishableWeekStart()
                    ?: return Resource.Error("Errore calcolo settimana pubblicabile")

                val weeklyShift = WeeklyShift(
                    idAzienda = idAzienda,
                    weekStart = publishableWeek,
                    createdBy = userId,
                    createdAt = LocalDateTime.now(),
                    status = WeeklyShiftStatus.NOT_PUBLISHED
                )

                when (val result = weeklyShiftRepository.createWeeklyShift(weeklyShift)) {
                    is Resource.Success -> Resource.Success(weeklyShift)
                    is Resource.Error -> Resource.Error(result.message)
                    else -> Resource.Error("Errore creazione pianificazione")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Eccezione creazione pianificazione: ${e.message}")
                Resource.Error(e.message ?: "Errore creazione pianificazione")
            }
        }

        /**
         * Verifica se è possibile pubblicare per la settimana corrente
         */
        private fun canPublishToday(): Pair<Boolean, LocalDate?> {
            val publishableWeek = WeeklyPublicationCalculator.getPublishableWeekStart()
            return Pair(publishableWeek != null, publishableWeek)
        }



        fun setOnBoardingDone(value: Boolean) {
            _uistate.update { it.copy(onBoardingDone = value) }
        }

        fun addTurno(turno: Turno) {
            _uistate.update { it.copy(itemsList = _uistate.value.itemsList + turno) }
        }


        fun onSelectionDataChanged(newValue: LocalDate) {
            _uistate.update { it.copy(selectionData = newValue) }
        }




    }



