package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.enumClass.PianificaScreenManager
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.domain.usecases.CreateWeeklyShiftUseCase
import com.bizsync.domain.usecases.GetDipendentiFullUseCase
import com.bizsync.domain.usecases.GetTurniInRangeNonSyncUseCase
import com.bizsync.domain.usecases.GetWeeklyShiftCorrenteUseCase
import com.bizsync.domain.usecases.GetWeeklyShiftUseCase
import com.bizsync.domain.usecases.SyncTurniToFirebaseUseCase
import com.bizsync.domain.usecases.UpdateWeeklyShiftStatusUseCase
import com.bizsync.domain.utils.WeeklyPublicationCalculator
import com.bizsync.domain.utils.WeeklyWindowCalculator
import com.bizsync.ui.model.PianificaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class PianificaViewModel @Inject constructor(
    private val getWeeklyShiftCorrenteUseCase: GetWeeklyShiftCorrenteUseCase,
    private val getWeeklyShiftUseCase: GetWeeklyShiftUseCase,
    private val createWeeklyShiftUseCase: CreateWeeklyShiftUseCase,
    private val updateWeeklyShiftStatusUseCase: UpdateWeeklyShiftStatusUseCase,
    private val syncTurniToFirebaseUseCase: SyncTurniToFirebaseUseCase,
    private val getTurniInRangeNonSyncUseCase: GetTurniInRangeNonSyncUseCase,
    private val getDipendentiFullUseCase: GetDipendentiFullUseCase
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

    fun openCreateShift() {
        _currentScreen.update { PianificaScreenManager.CREATE_SHIFT }
    }

    fun openGestioneTurni(dip: AreaLavoro) {
        _uistate.update { it.copy(dipartimento = dip) }
        _currentScreen.value = PianificaScreenManager.GESTIONE_TURNI_DIPARTIMENTO
    }

    fun getWeeklyShiftCorrente(selectionDate: LocalDate) {
        val currentWeekStart = WeeklyWindowCalculator.getWeekStartFromDate(selectionDate)
        val weeklyShiftAttuale = _uistate.value.weeklyShiftAttuale

        if (weeklyShiftAttuale?.weekStart != currentWeekStart) {

            viewModelScope.launch {
                _uistate.update { it.copy(loadingWeekly = true) }
                val weekStart = WeeklyWindowCalculator.getWeekStartFromDate(selectionDate)
                Log.d(TAG, "Recupero turni per settimana con start: $weekStart")

                try {
                    when (val result = getWeeklyShiftCorrenteUseCase(weekStart)) {
                        is Resource.Success -> {
                            Log.d(TAG, "Weekly shift corrente trovato: ${result.data}")
                            _uistate.update {
                                it.copy(
                                    weeklyShiftAttuale = result.data,
                                    loadingWeekly = false
                                )
                            }
                        }

                        is Resource.Empty -> {
                            Log.d(TAG, "Nessun weekly shift presente per la settimana selezionata")
                            _uistate.update {
                                it.copy(
                                    weeklyShiftAttuale = null,
                                    loadingWeekly = false

                                )
                            }
                        }

                        is Resource.Error -> {
                            Log.e(TAG, "Errore nel recupero weekly shift corrente: ${result.message}")
                            _uistate.update {
                                it.copy(
                                    errorMsg = result.message,
                                    weeklyShiftAttuale = null,
                                    loadingWeekly = false
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Errore imprevisto nel recupero dei dati", e)
                    _uistate.update {
                        it.copy(
                            errorMsg = e.message ?: "Errore sconosciuto",
                            weeklyShiftAttuale = null,
                            loadingWeekly = false
                        )
                    }
                }
            }
        }
    }

    fun setDipartimentoScreen(dipartimento: AreaLavoro) {
        _uistate.update { it.copy(dipartimento = dipartimento) }
        _currentScreen.value = PianificaScreenManager.GESTIONE_TURNI_DIPARTIMENTO
    }

    fun backToMain() {
        _uistate.update { it.copy(dipartimento = null) }
        _currentScreen.value = PianificaScreenManager.MAIN
    }

    fun checkWeeklyPlanningStatus(idAzienda: String) {
        viewModelScope.launch {
            Log.d(TAG, "Controllo pianificazione per azienda: $idAzienda")

            _uistate.update { it.copy(isLoading = true, errorMsg = null) }

            // Controlla se è possibile pubblicare oggi
            val (canPublish, publishableWeek) = canPublishToday()

            if (!canPublish) {
                Log.w(TAG, "Non è possibile pubblicare turni al momento")
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
                    Log.d(TAG, "Controllo completato - esiste pianificazione: $exists")

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

    fun createWeeklyPlanning(azienda: Azienda, userId: String) {
        viewModelScope.launch {
            Log.d(TAG, "💾 Creazione nuova pianificazione per azienda: ${azienda.idAzienda}")

            _uistate.update { it.copy(isLoading = true, errorMsg = null) }

            when (val result = createWeeklyPlanningInternal(azienda, userId)) {
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
        val weeklyShiftRiferimento = _uistate.value.weeklyShiftRiferimento ?: return

        viewModelScope.launch {
            _uistate.update { it.copy(isSyncing = true) }

            try {
                // Aggiorna lo stato nel database
                val weeklyShiftRifAggiornato = weeklyShiftRiferimento.copy(status = nuovoStato)

                when (val result = updateWeeklyShiftStatusUseCase(weeklyShiftRifAggiornato)) {
                    is Resource.Success -> {
                        _uistate.update {
                            it.copy(
                                isSyncing = false,
                                weeklyShiftRiferimento = weeklyShiftRifAggiornato,
                            )
                        }

                        // Se il nuovo stato è DRAFT o PUBLISHED, sincronizza automaticamente i turni
                        if (nuovoStato == WeeklyShiftStatus.DRAFT || nuovoStato == WeeklyShiftStatus.PUBLISHED) {
                            syncTurni(weeklyShiftRifAggiornato.weekStart)
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

    fun syncTurniAvvio(weekStart: LocalDate) {
        viewModelScope.launch {
            try {
                // Calcolo range settimanale
                val weekEnd = weekStart.plusDays(6)

                // Ottieni i turni locali non sincronizzati nel range
                val turniNonSync = getTurniInRangeNonSyncUseCase(weekStart, weekEnd)

                Log.d(TAG, "Turni non sincronizzati trovati: ${turniNonSync.size}")

                if (turniNonSync.isNotEmpty()) {
                    _uistate.update { it.copy(hasUnsavedChanges = true) }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Errore durante syncTurni: ${e.message}")
                _uistate.update {
                    it.copy(errorMsg = "Errore nel controllo modifiche locali")
                }
            }
        }
    }

    fun syncTurni(weekStart: LocalDate?) {
        if (weekStart == null) return

        viewModelScope.launch {
            try {
                when (val result = syncTurniToFirebaseUseCase(weekStart)) {
                    is Resource.Success -> {
                        Log.d(TAG, "Turni sincronizzati con successo")
                        _uistate.update { it.copy(isSyncing = false, hasUnsavedChanges = false) }
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Errore sincronizzazione: ${result.message}")
                        _uistate.update { it.copy(errorMsg = result.message) }
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

    private suspend fun checkWeeklyPlanningExists(idAzienda: String): Resource<WeeklyShift?> {
        return try {
            val publishableWeek = WeeklyPublicationCalculator.getPublishableWeekStart()
                ?: return Resource.Error("Errore calcolo settimana pubblicabile")

            getWeeklyShiftUseCase(idAzienda, publishableWeek)
        } catch (e: Exception) {
            Log.e(TAG, "Eccezione controllo pianificazione: ${e.message}")
            Resource.Error(e.message ?: "Errore controllo pianificazione")
        }
    }

    fun setDipendentiAzienda() {
        viewModelScope.launch {
            try {
                val dipendenti = getDipendentiFullUseCase()
                Log.d(TAG, "Dipendenti caricati: ${dipendenti.size}")
                _uistate.update { it.copy(dipendenti = dipendenti) }
            } catch (e: Exception) {
                Log.e(TAG, "Errore durante il recupero dei dipendenti: ${e.message}")
                _uistate.update { it.copy(errorMsg = "Errore durante il caricamento dei dipendenti") }
            }
        }
    }

    private suspend fun createWeeklyPlanningInternal(azienda: Azienda, userId: String): Resource<WeeklyShift> {
        return try {
            val publishableWeek = WeeklyPublicationCalculator.getPublishableWeekStart()
                ?: return Resource.Error("Errore calcolo settimana pubblicabile")

            val generatedId = generateWeeklyShiftId(azienda, publishableWeek)

            val weeklyShift = WeeklyShift(
                id = generatedId,
                idAzienda = azienda.idAzienda,
                dipartimentiAttivi = azienda.areeLavoro,
                dipendentiAttivi = _uistate.value.dipendenti,
                weekStart = publishableWeek,
                createdBy = userId,
                createdAt = LocalDateTime.now(),
                status = WeeklyShiftStatus.NOT_PUBLISHED
            )

            when (val result = createWeeklyShiftUseCase(weeklyShift)) {
                is Resource.Success -> Resource.Success(weeklyShift)
                is Resource.Error -> Resource.Error(result.message)
                else -> Resource.Error("Errore creazione pianificazione")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Eccezione creazione pianificazione: ${e.message}")
            Resource.Error(e.message ?: "Errore creazione pianificazione")
        }
    }

    private fun generateWeeklyShiftId(azienda: Azienda, weekStart: LocalDate): String {
        val weekKey = weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return "${azienda.idAzienda}_$weekKey"
    }


    private fun canPublishToday(): Pair<Boolean, LocalDate?> {
        val publishableWeek = WeeklyPublicationCalculator.getPublishableWeekStart()
        return Pair(publishableWeek != null, publishableWeek)
    }

    fun setOnBoardingDone(value: Boolean) {
        _uistate.update { it.copy(onBoardingDone = value) }
    }

    fun onSelectionDataChanged(newValue: LocalDate) {
        _uistate.update { it.copy(selectionData = newValue) }
    }
}