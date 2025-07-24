package com.bizsync.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.TimbraturaRepository
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.cache.dao.TimbraturaDao
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.enumClass.*
import com.bizsync.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class ShiftsManagementState(
    val selectedFilter: ShiftTimeFilter = ShiftTimeFilter.DEFAULT_WINDOW,
    val turniWithTimbrature: List<TurnoWithTimbratureDetails> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasMoreData: Boolean = true
)

data class TurnoWithTimbratureDetails(
    val turno: Turno,
    val timbratureEntrata: Timbratura? = null,
    val timbratureUscita: Timbratura? = null,
    val statoTurno: StatoTurnoDettagliato,
    val minutiRitardoEntrata: Int = 0,
    val minutiRitardoUscita: Int = 0,
    val minutiLavoratiEffettivi: Int = 0,
    val completezza: CompletenessaTurno
)

enum class ShiftTimeFilter(val displayName: String) {
    DEFAULT_WINDOW("Ultimo Mese"),
    LAST_3_MONTHS("Ultimi 3 Mesi"),
    LAST_6_MONTHS("Ultimi 6 Mesi"),
    LAST_YEAR("Ultimo Anno"),
    ALL_TIME("Tutti i Turni")
}

enum class StatoTurnoDettagliato {
    NON_INIZIATO,           // Non ancora iniziato
    COMPLETATO_REGOLARE,    // Entrata e uscita in orario
    COMPLETATO_RITARDO,     // Completato ma con ritardi
    COMPLETATO_ANTICIPO,    // Completato con uscita anticipata
    PARZIALE_SOLO_ENTRATA, // Solo entrata, nessuna uscita
    ASSENTE,               // Nessuna timbratura
    TURNO_FUTURO           // Turno programmato futuro
}

enum class CompletenessaTurno {
    COMPLETO,      // Entrata + Uscita
    PARZIALE,      // Solo entrata O solo uscita
    ASSENTE,       // Nessuna timbratura
    NON_RICHIESTO  // Turno futuro
}

@HiltViewModel
class ShiftsManagementViewModel @Inject constructor(
    private val turnoDao: TurnoDao,
    private val timbraturaDao: TimbraturaDao,
    private val turnoRepository: TurnoRepository,
    private val timbraturaRepository: TimbraturaRepository
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
                val turni = turnoDao.getTurniInRangeForUser(startDate, endDate).first()

                // Carica timbrature associate
                val timbrature = timbraturaDao.getTimbratureInRangeForUser(startDate, endDate, user.uid).first()

                // Combina turni con timbrature
                val turniWithDetails = createTurniWithTimbratureDetails(turni.toDomainList(), timbrature.toDomainList())

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
                    turnoRepository.syncTurniForUserInRange(user.uid, azienda.idAzienda, startDate, endDate)
                    timbraturaRepository.syncTimbratureForUserInRange(user.uid, azienda.idAzienda, startDate, endDate)

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
    ): CompletenessaTurno {
        return when {
            turno.data.isAfter(LocalDate.now()) -> CompletenessaTurno.NON_RICHIESTO
            entrata != null && uscita != null -> CompletenessaTurno.COMPLETO
            entrata != null || uscita != null -> CompletenessaTurno.PARZIALE
            else -> CompletenessaTurno.ASSENTE
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




