package com.bizsync.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.TimbraturaRepository
import com.bizsync.backend.repository.WeeklyShiftRepository
import com.bizsync.cache.dao.TimbraturaDao
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.enumClass.*
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.*
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.model.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class EmployeeHomeState(
    val badge: BadgeVirtuale? = null,
    val prossimoTurno: ProssimoTurno? = null,
    val todayTurno: TurnoWithDetails? = null,
    val timbratureOggi: List<Timbratura> = emptyList(),
    val canTimbra: Boolean = false,
    val daysUntilShiftPublication: Int = 0,
    val shiftsPublishedThisWeek: Boolean = false,
    val isLoading: Boolean = false,
    val isGettingLocation: Boolean = false,
    val error: String? = null,
    val showSuccess: Boolean = false,
    val successMessage: String = ""
)

data class TurnoWithDetails(
    val turno: Turno,
    val haTimbratoEntrata: Boolean = false,
    val haTimbratoUscita: Boolean = false,
    val orarioEntrataEffettivo: LocalTime? = null,
    val orarioUscitaEffettivo: LocalTime? = null,
    val minutiRitardo: Int = 0,
    val minutiAnticipo: Int = 0,
    val statoTurno: StatoTurno = StatoTurno.NON_INIZIATO
)

enum class StatoTurno {
    NON_INIZIATO,
    IN_CORSO,
    IN_PAUSA,
    COMPLETATO,
    IN_RITARDO,
    ASSENTE
}

@HiltViewModel
class EmployeeHomeViewModel @Inject constructor(
    private val turnoDao: TurnoDao,
    private val timbratureDao: TimbraturaDao,
    private val userDao: UserDao,
    private val timbratureRepository: TimbraturaRepository,
    private val weeklyShiftRepository: WeeklyShiftRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(EmployeeHomeState())
    val homeState = _homeState.asStateFlow()

    private var currentUser: User? = null

    fun initializeEmployee(userState: UserState) {
        currentUser = userState.user.toDomain()
        loadEmployeeHomeData()
    }

    private fun loadEmployeeHomeData() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true) }

            try {
                currentUser?.let { user ->
                    // Carica tutti i dati in parallelo
                    launch { loadTodayTurno(user.uid) }
                    launch { loadTimbratureOggi(user.uid) }
                    launch { loadShiftPublicationInfo() }
                }

            } catch (e: Exception) {
                _homeState.update {
                    it.copy(
                        isLoading = false,
                        error = "Errore nel caricamento dei dati: ${e.message}"
                    )
                }
            }
        }
    }




    private suspend fun loadTodayTurno(userId: String) {
        try {
            val today = LocalDate.now()
            val turniOggi = turnoDao.getTurniByDateAndUser(today).first()

            if (turniOggi.isNotEmpty()) {
                val turno = turniOggi.first() // Assumiamo un turno per giorno
                val timbratureOggi = timbratureDao.getTimbratureByDateAndUser(today, userId).first()

                val turnoWithDetails = createTurnoWithDetails(turno.toDomain(), timbratureOggi.toDomainList())
                _homeState.update { it.copy(todayTurno = turnoWithDetails) }
            }

        } catch (e: Exception) {
            _homeState.update { it.copy(error = "Errore caricamento turno di oggi: ${e.message}") }
        }
    }

    private suspend fun loadTimbratureOggi(userId: String) {
        try {
            val oggi = LocalDate.now()
            val timbrature = timbratureDao.getTimbratureByDateAndUser(oggi, userId).first()

            _homeState.update {
                it.copy(
                    timbratureOggi = timbrature.toDomainList().sortedByDescending { t -> t.dataOraPrevista},
                    isLoading = false
                )
            }

        } catch (e: Exception) {
            _homeState.update {
                it.copy(
                    isLoading = false,
                    error = "Errore caricamento timbrature: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadShiftPublicationInfo() {
        try {
            val today = LocalDate.now()
            val nextFriday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
            val daysUntilPublication = today.until(nextFriday).days

            // Controlla se i turni sono già stati pubblicati questa settimana
            val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val currentWeekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

            val idAzienda = "ZNTzPHOA2xyJMgymaFN7"
            val publicationRecord = weeklyShiftRepository.getWeeklyShift(idAzienda,currentWeekStart)

            when (publicationRecord )
            {
                is Resource.Success -> {
                    _homeState.update {
                        it.copy(
                            daysUntilShiftPublication = daysUntilPublication,
                            shiftsPublishedThisWeek = publicationRecord.data != null
                        )
                    }
                }

                else -> {}
            }

        } catch (e: Exception) {
            _homeState.update {
                it.copy(error = "Errore caricamento info pubblicazione: ${e.message}")
            }
        }
    }

    private fun createTurnoWithDetails(turno: Turno, timbrature: List<Timbratura>): TurnoWithDetails {
        val entrataTimbratura = timbrature.find { it.tipoTimbratura == TipoTimbratura.ENTRATA }
        val uscitaTimbratura = timbrature.find { it.tipoTimbratura == TipoTimbratura.USCITA }

        val haEntrata = entrataTimbratura != null
        val haUscita = uscitaTimbratura != null

        // Calcola ritardi e anticipi
        val minutiRitardo = if (haEntrata) {
            val orarioEntrata = entrataTimbratura!!.dataOraTimbratura.toLocalTime()
            val orarioPrevisto = turno.orarioInizio
            if (orarioEntrata.isAfter(orarioPrevisto)) {
                orarioPrevisto.until(orarioEntrata, java.time.temporal.ChronoUnit.MINUTES).toInt()
            } else 0
        } else 0

        val minutiAnticipo = if (haUscita) {
            val orarioUscita = uscitaTimbratura!!.dataOraTimbratura.toLocalTime()
            val orarioFinePrevisto = turno.orarioFine
            if (orarioUscita.isBefore(orarioFinePrevisto)) {
                orarioUscita.until(orarioFinePrevisto, java.time.temporal.ChronoUnit.MINUTES).toInt()
            } else 0
        } else 0

        // Determina stato turno
        val now = LocalDateTime.now()
        val turnoStart = turno.data.atTime(turno.orarioInizio)
        val turnoEnd = turno.data.atTime(turno.orarioFine)

        val stato = when {
            haEntrata && haUscita -> StatoTurno.COMPLETATO
            haEntrata && now.isAfter(turnoEnd) && !haUscita -> StatoTurno.ASSENTE
            haEntrata && now.isBefore(turnoEnd) -> StatoTurno.IN_CORSO
            !haEntrata && now.isAfter(turnoStart.plusMinutes(15)) -> StatoTurno.IN_RITARDO
            !haEntrata && now.isAfter(turnoEnd) -> StatoTurno.ASSENTE
            else -> StatoTurno.NON_INIZIATO
        }

        return TurnoWithDetails(
            turno = turno,
            haTimbratoEntrata = haEntrata,
            haTimbratoUscita = haUscita,
            orarioEntrataEffettivo = entrataTimbratura?.dataOraTimbratura?.toLocalTime(),
            orarioUscitaEffettivo = uscitaTimbratura?.dataOraTimbratura?.toLocalTime(),
            minutiRitardo = minutiRitardo,
            minutiAnticipo = minutiAnticipo,
            statoTurno = stato
        )
    }




    fun setIsGettingLocation(isGetting: Boolean) {
        _homeState.update { it.copy(isGettingLocation = isGetting) }
    }

    fun dismissError() {
        _homeState.update { it.copy(error = null) }
    }

    fun dismissSuccess() {
        _homeState.update { it.copy(showSuccess = false) }
    }

    fun refreshData() {
        loadEmployeeHomeData()
    }
}

