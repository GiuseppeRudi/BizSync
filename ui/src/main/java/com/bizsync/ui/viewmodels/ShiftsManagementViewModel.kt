package com.bizsync.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.enumClass.*
import com.bizsync.domain.model.*
import com.bizsync.domain.usecases.GetTimbratureInRangeForUserUseCase
import com.bizsync.domain.usecases.GetTurniInRangeForUserUseCase
import com.bizsync.domain.usecases.SyncTimbratureForUserInRangeUseCase
import com.bizsync.domain.usecases.SyncTurniForUserInRangeUseCase
import com.bizsync.ui.model.ShiftsManagementState
import com.bizsync.ui.model.TurnoWithTimbratureDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject



@HiltViewModel
class ShiftsManagementViewModel @Inject constructor(
    private val getTurniInRangeForUserUseCase: GetTurniInRangeForUserUseCase,
    private val getTimbratureInRangeForUserUseCase: GetTimbratureInRangeForUserUseCase,
    private val syncTurniForUserInRangeUseCase: SyncTurniForUserInRangeUseCase,
    private val syncTimbratureForUserInRangeUseCase: SyncTimbratureForUserInRangeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShiftsManagementState())
    val uiState = _uiState.asStateFlow()

    private var currentUser: User? = null
    private var currentAzienda: Azienda? = null

    fun initializeScreen(user: User, azienda: Azienda) {
        currentUser = user
        currentAzienda = azienda
        loadDefaultWindow()
    }

    fun changeFilter(filter: ShiftTimeFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedFilter = filter, isLoading = true) }

            when (filter) {
                ShiftTimeFilter.DEFAULT_WINDOW -> loadDefaultWindow()
                ShiftTimeFilter.LAST_3_MONTHS -> loadDataWithFirebase(LocalDate.now().minusMonths(3))
                ShiftTimeFilter.LAST_6_MONTHS -> loadDataWithFirebase(LocalDate.now().minusMonths(6))
                ShiftTimeFilter.LAST_YEAR -> loadDataWithFirebase(LocalDate.now().minusYears(1))
                ShiftTimeFilter.ALL_TIME -> loadDataWithFirebase(null) // Tutti i dati
            }
        }
    }

    private fun loadDefaultWindow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val today = LocalDate.now()
                val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val (startDate, endDate) = calculateWindowForEmployee(weekStart)

                loadDataFromCache(startDate, endDate)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Errore nel caricamento turni: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadDataFromCache(startDate: LocalDate, endDate: LocalDate) {
        try {
            currentUser?.let { user ->
                // Carica turni dalla cache per il dipendente
                val turni = getTurniInRangeForUserUseCase(startDate, endDate).first()

                // Carica timbrature associate
                val timbrature = getTimbratureInRangeForUserUseCase(startDate, endDate, user.uid).first()

                // Combina turni con timbrature
                val turniWithDetails = createTurniWithTimbratureDetails(turni, timbrature)

                _uiState.update {
                    it.copy(
                        turniWithTimbrature = turniWithDetails.sortedByDescending { turno -> turno.turno.data },
                        isLoading = false,
                        error = null,
                        hasMoreData = true
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Errore caricamento dalla cache: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadDataWithFirebase(fromDate: LocalDate?) {
        try {
            currentUser?.let { user ->
                currentAzienda?.let { azienda ->
                    _uiState.update { it.copy(isLoading = true) }

                    val startDate = fromDate ?: LocalDate.of(2020, 1, 1)
                    val endDate = LocalDate.now().plusDays(30)

                    // Sincronizza da Firebase
                    syncTurniForUserInRangeUseCase(user.uid, azienda.idAzienda, startDate, endDate)
                    syncTimbratureForUserInRangeUseCase(user.uid, azienda.idAzienda, startDate, endDate)

                    // Ricarica dalla cache aggiornata
                    loadDataFromCache(startDate, endDate)
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Errore sincronizzazione Firebase: ${e.message}"
                )
            }
        }
    }

    private fun createTurniWithTimbratureDetails(
        turni: List<Turno>,
        timbrature: List<Timbratura>
    ): List<TurnoWithTimbratureDetails> {
        return turni.map { turno ->
            val timbratureTurno = timbrature.filter { it.idTurno == turno.id }

            val entrata = timbratureTurno.find { it.tipoTimbratura == TipoTimbratura.ENTRATA }
            val uscita = timbratureTurno.find { it.tipoTimbratura == TipoTimbratura.USCITA }

            val stato = determineStatoTurno(turno, entrata, uscita)
            val completezza = determineCompleteness(turno, entrata, uscita)

            val minutiRitardoEntrata = calculateRitardoEntrata(turno, entrata)
            val minutiRitardoUscita = calculateRitardoUscita(turno, uscita)
            val minutiEffettivi = calculateMinutiLavorati(entrata, uscita)

            TurnoWithTimbratureDetails(
                turno = turno,
                timbratureEntrata = entrata,
                timbratureUscita = uscita,
                statoTurno = stato,
                minutiRitardoEntrata = minutiRitardoEntrata,
                minutiRitardoUscita = minutiRitardoUscita,
                minutiLavoratiEffettivi = minutiEffettivi,
                completezza = completezza
            )
        }
    }

    private fun determineStatoTurno(
        turno: Turno,
        entrata: Timbratura?,
        uscita: Timbratura?
    ): StatoTurnoDettagliato {
        val now = LocalDate.now()
        val turnoDate = turno.data

        return when {
            turnoDate.isAfter(now) -> StatoTurnoDettagliato.TURNO_FUTURO
            entrata == null && uscita == null -> StatoTurnoDettagliato.ASSENTE
            entrata != null && uscita == null -> StatoTurnoDettagliato.PARZIALE_SOLO_ENTRATA
            entrata != null && uscita != null -> {
                val hasRitardoEntrata = entrata.statoTimbratura == StatoTimbratura.RITARDO_LIEVE ||
                        entrata.statoTimbratura == StatoTimbratura.RITARDO_GRAVE
                val hasAnticipoUscita = uscita.statoTimbratura == StatoTimbratura.ANTICIPO

                when {
                    hasAnticipoUscita -> StatoTurnoDettagliato.COMPLETATO_ANTICIPO
                    hasRitardoEntrata -> StatoTurnoDettagliato.COMPLETATO_RITARDO
                    else -> StatoTurnoDettagliato.COMPLETATO_REGOLARE
                }
            }
            else -> StatoTurnoDettagliato.NON_INIZIATO
        }
    }

    private fun determineCompleteness(
        turno: Turno,
        entrata: Timbratura?,
        uscita: Timbratura?
    ): CompletenezzaTurno {
        return when {
            turno.data.isAfter(LocalDate.now()) -> CompletenezzaTurno.NON_RICHIESTO
            entrata != null && uscita != null -> CompletenezzaTurno.COMPLETO
            entrata != null || uscita != null -> CompletenezzaTurno.PARZIALE
            else -> CompletenezzaTurno.ASSENTE
        }
    }

    private fun calculateRitardoEntrata(turno: Turno, entrata: Timbratura?): Int {
        if (entrata == null) return 0

        val orarioPrevisto = turno.data.atTime(turno.orarioInizio)
        val orarioEffettivo = entrata.dataOraTimbratura

        return if (orarioEffettivo.isAfter(orarioPrevisto)) {
            orarioPrevisto.until(orarioEffettivo, java.time.temporal.ChronoUnit.MINUTES).toInt()
        } else 0
    }

    private fun calculateRitardoUscita(turno: Turno, uscita: Timbratura?): Int {
        if (uscita == null) return 0

        val orarioPrevisto = turno.data.atTime(turno.orarioFine)
        val orarioEffettivo = uscita.dataOraTimbratura

        return if (orarioEffettivo.isBefore(orarioPrevisto)) {
            orarioEffettivo.until(orarioPrevisto, java.time.temporal.ChronoUnit.MINUTES).toInt()
        } else 0
    }

    private fun calculateMinutiLavorati(entrata: Timbratura?, uscita: Timbratura?): Int {
        if (entrata == null || uscita == null) return 0

        return entrata.dataOraTimbratura
            .until(uscita.dataOraTimbratura, java.time.temporal.ChronoUnit.MINUTES)
            .toInt()
    }

    fun calculateWindowForEmployee(weekStart: LocalDate): Pair<LocalDate, LocalDate> {
        // Finestra: 4 settimane indietro + 1 settimana avanti
        val startDate = weekStart.minusWeeks(4)
        val endDate = weekStart.plusWeeks(1).with(DayOfWeek.SUNDAY)
        return Pair(startDate, endDate)
    }

    fun refreshData() {
        when (_uiState.value.selectedFilter) {
            ShiftTimeFilter.DEFAULT_WINDOW -> loadDefaultWindow()
            else -> changeFilter(_uiState.value.selectedFilter)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}