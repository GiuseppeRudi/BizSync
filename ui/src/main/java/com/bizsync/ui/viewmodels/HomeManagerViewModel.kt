package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.WeeklyShiftRepository
import com.bizsync.cache.dao.TimbraturaDao
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.mapper.UserEntityMapper.toDomain
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject



// Data classes per il ViewModel
data class TodayStats(
    val presenti: Int = 0,
    val assenti: Int = 0,
    val turniAttivi: Int = 0
)

data class TimbratureWithUser(
    val timbrature: Timbratura,
    val user: User
)

data class TurnoWithUsers(
    val turno: Turno,
    val users: List<User>
)

enum class UrgencyLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class HomeScreenRoute {
    Timbrature, Shifts, Reports, Absences
}

data class ManagerHomeState(
    val todayStats: TodayStats = TodayStats(),
    val recentTimbrature: List<TimbratureWithUser> = emptyList(),
    val todayShifts: List<TurnoWithUsers> = emptyList(),
    val daysUntilShiftPublication: Int = 0,
    val shiftsPublishedThisWeek: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ManagerHomeViewModel @Inject constructor(
    private val turnoDao: TurnoDao,
    private val timbratureDao: TimbraturaDao,
    private val userDao: UserDao,
    private val  weeklyShiftRepository : WeeklyShiftRepository
//    private val shiftPublicationDao: ShiftPublicationDao // Nuovo DAO per tracciare le pubblicazioni
) : ViewModel() {

    private val _homeState = MutableStateFlow(ManagerHomeState())
    val homeState = _homeState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true) }

            try {
                // Carica tutti i dati in parallelo
                launch { loadTodayStats() }
                launch { loadRecentTimbrature() }
                launch { loadTodayShifts() }
                launch { loadShiftPublicationInfo() }

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

    private suspend fun loadTodayStats() {
        try {
            val today = LocalDate.now()


            // Prendi tutti i turni di oggi
            val todayShifts = turnoDao.getTurniByDate(today).first()

            // Prendi tutte le timbrature di oggi
            val todayTimbrature = timbratureDao.getTimbratureByDate(today).first()

            // Prendi tutti gli utenti attivi
            val activeUsers = userDao.getDipendentiFull()

            // Calcola statistiche
            val stats = calculateTodayStats(todayShifts.toDomainList(), todayTimbrature.toDomainList(), activeUsers.toDomainList())

            _homeState.update {
                it.copy(
                    todayStats = stats,
                    isLoading = false
                )
            }

        } catch (e: Exception) {
            _homeState.update {
                it.copy(
                    isLoading = false,
                    error = "Errore nel calcolo delle statistiche: ${e.message}"
                )
            }
        }
    }

    private fun calculateTodayStats(
        todayShifts: List<Turno>,
        todayTimbrature: List<Timbratura>,
        activeUsers: List<User>
    ): TodayStats {
        val now = LocalDateTime.now()
        val todayDate = LocalDate.now()

        // Utenti con turni oggi
        val usersWithShiftsToday = todayShifts
            .flatMap { it.idDipendenti }
            .distinct()

        // Utenti che hanno timbrato entrata oggi
        val usersWithEntranceToday = todayTimbrature
            .filter { it.tipoTimbratura == TipoTimbratura.ENTRATA }
            .map { it.idDipendente }
            .distinct()

        // Utenti che hanno timbrato uscita oggi
        val usersWithExitToday = todayTimbrature
            .filter { it.tipoTimbratura == TipoTimbratura.USCITA }
            .map { it.idDipendente }
            .distinct()


        // Presenti = hanno timbrato entrata ma non uscita (e non sono in pausa)
        val presenti = (usersWithEntranceToday - usersWithExitToday ).size

        // Assenti = utenti con turni oggi che non hanno timbrato entrata
        val assenti = (usersWithShiftsToday - usersWithEntranceToday).size

        // Turni attualmente attivi
        val turniAttivi = todayShifts.count { turno ->
            val startTime = todayDate.atTime(turno.orarioInizio)
            val endTime = todayDate.atTime(turno.orarioFine)
            now.isAfter(startTime) && now.isBefore(endTime)
        }

        return TodayStats(
            presenti = presenti,
            assenti = assenti,
            turniAttivi = turniAttivi
        )
    }

    private suspend fun loadRecentTimbrature() {
        try {
            val recentTimbrature = timbratureDao.getRecentTimbrature(10).first()
            val users = userDao.getDipendentiFull()

            val usersDomain = users.toDomainList()
            val timbratureWithUsers = recentTimbrature.toDomainList().mapNotNull { timbrature ->
                val user = usersDomain.find { it.uid == timbrature.idDipendente }
                if (user != null) {
                    TimbratureWithUser(timbrature, user)
                } else null
            }

            _homeState.update {
                it.copy(recentTimbrature = timbratureWithUsers)
            }

        } catch (e: Exception) {
            _homeState.update {
                it.copy(error = "Errore nel caricamento timbrature: ${e.message}")
            }
        }
    }

    private suspend fun loadTodayShifts() {
        try {
            val today = LocalDate.now()
            val todayShifts = turnoDao.getTurniByDate(today).first()
            val users = userDao.getDipendentiFull()

            val shiftsWithUsers = todayShifts.map { turno ->
                val turnoUsers = users.filter { user ->
                    turno.idDipendenti.contains(user.uid)
                }
                TurnoWithUsers(turno.toDomain(), turnoUsers.toDomainList())
            }.sortedBy { it.turno.orarioInizio }

            _homeState.update {
                it.copy(todayShifts = shiftsWithUsers)
            }

        } catch (e: Exception) {
            _homeState.update {
                it.copy(error = "Errore nel caricamento turni: ${e.message}")
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

            val idAzienda = "ZNTzPHOA2xyJMgymaFN7"
            val publicationRecord = weeklyShiftRepository.getWeeklyShift(idAzienda,currentWeekStart)


            when (publicationRecord)
            {
                is Resource.Success -> {

                    _homeState.update {
                        it.copy(
                            daysUntilShiftPublication = daysUntilPublication,
                            shiftsPublishedThisWeek = true
                        )
                    }
                }

                else -> {}
            }



        } catch (e: Exception) {
            _homeState.update {
                it.copy(error = "Errore nel caricamento info pubblicazione: ${e.message}")
            }
        }
    }

    fun markShiftsAsPublished() {
        viewModelScope.launch {
            try {

                _homeState.update {
                    it.copy(shiftsPublishedThisWeek = true)
                }

            } catch (e: Exception) {
                _homeState.update {
                    it.copy(error = "Errore nella marcatura pubblicazione: ${e.message}")
                }
            }
        }
    }

    fun refreshData() {
        loadHomeData()
    }

    fun clearError() {
        _homeState.update { it.copy(error = null) }
    }

    private fun getCurrentUserId(): String {
        // Implementare per ottenere l'ID dell'utente corrente
        // Probabilmente da un repository di autenticazione o SharedPreferences
        return "current_manager_id" // Placeholder
    }
}


