package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.WeeklyShiftRepository
import com.bizsync.cache.dao.TimbraturaDao
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.*
import com.bizsync.domain.utils.WeeklyPublicationCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlin.text.ifEmpty


data class TodayStats(
    val turniTotaliAssegnati: Int = 0,
    val dipartimentiAperti: Int = 0,
    val utentiAttiviOggi: Int = 0,
    val turniCompletati: Int = 0,
    val turniIniziati: Int = 0,
    val turniDaIniziare: Int = 0,
    val dipartimentiDetails: List<DipartimentoInfo> = emptyList()
)

data class DipartimentoInfo(
    val nome: String,
    val orarioApertura: String,
    val orarioChiusura: String,
    val numeroTurni: Int
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


data class ManagerHomeState(
    val azienda : Azienda = Azienda(),
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


    fun loadHomeManagerData(azienda : Azienda) {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, azienda = azienda) }

            try {
                launch { loadTodayStats(azienda) }
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
    // 🔧 FUNZIONE CARICAMENTO AGGIORNATA
    private suspend fun loadTodayStats(azienda : Azienda) {
        try {
            val today = LocalDate.now()

            // Prendi tutti i turni di oggi
            val todayShifts = turnoDao.getTurniByDate(today).first()

            val startOfDay = today.atStartOfDay().toString()         // "2025-07-23T00:00"
            val endOfDay = today.atTime(23, 59, 59).toString()
            // Prendi tutte le timbrature di oggi
            val todayTimbrature = timbratureDao.getTimbratureByDate(startOfDay,endOfDay).first()

            // Calcola statistiche
            val stats = calculateSimplifiedTodayStats(
                todayShifts.toDomainList(),
                todayTimbrature.toDomainList(),
                azienda
            )

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

    private fun calculateSimplifiedTodayStats(
        todayShifts: List<Turno>,
        todayTimbrature: List<Timbratura>,
        azienda: Azienda
    ): TodayStats {
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek

        Log.d("TodayStats", "📅 Calcolo statistiche per il giorno: $today ($dayOfWeek)")

        // 1️⃣ TURNI TOTALI ASSEGNATI OGGI
        val turniTotaliAssegnati = todayShifts.size
        Log.d("TodayStats", "✅ Turni totali assegnati oggi: $turniTotaliAssegnati")

        // 2️⃣ UTENTI ATTIVI OGGI (con turni assegnati)
        val utentiAttiviOggi = todayShifts
            .flatMap { it.idDipendenti }
            .distinct()
            .size
        Log.d("TodayStats", "👥 Utenti attivi oggi (con turni): $utentiAttiviOggi")

        // 3️⃣ Dipartimenti con turni e orari (usando orari dell'azienda)
        val dipartimentiConTurni = todayShifts
            .groupBy { it.dipartimento }
            .mapValues { (dipartimento, turni) ->

                Log.d("DipartimentoCheck", "🔍 Analizzo dipartimento: $dipartimento con ${turni.size} turni")

                val areaLavoro = azienda.areeLavoro.find { it.nomeArea == dipartimento }

                if (areaLavoro == null) {
                    Log.w("DipartimentoCheck", "⚠️ Nessuna areaLavoro trovata per: $dipartimento")
                } else {
                    Log.d("DipartimentoCheck", "✅ Area trovata: ${areaLavoro.nomeArea}")
                }

                val orari = areaLavoro?.orariSettimanali?.get(dayOfWeek)
                if (orari == null) {
                    Log.w("OrariCheck", "⚠️ Nessun orario per $dayOfWeek in area: ${areaLavoro?.nomeArea}")
                } else {
                    Log.d("OrariCheck", "⏰ Orari per $dayOfWeek: apertura=${orari.first}, chiusura=${orari.second}")
                }

                DipartimentoInfo(
                    nome = areaLavoro?.nomeArea ?: dipartimento,
                    orarioApertura = orari?.first?.toString() ?: "N/A",
                    orarioChiusura = orari?.second?.toString() ?: "N/A",
                    numeroTurni = turni.size
                )
            }

        val dipartimentiAperti = dipartimentiConTurni.size
        val dipartimentiDetails = dipartimentiConTurni.values.toList()
        Log.d("TodayStats", "🏢 Dipartimenti aperti oggi: $dipartimentiAperti")

        // 4️⃣ ANALISI STATO TURNI
        val turniConStato = todayShifts.map { turno ->
            val timbratureTurno = todayTimbrature.filter { it.idTurno == turno.id }
            val haEntrata = timbratureTurno.any { it.tipoTimbratura == TipoTimbratura.ENTRATA }
            val haUscita = timbratureTurno.any { it.tipoTimbratura == TipoTimbratura.USCITA }

            val stato = when {
                haEntrata && haUscita -> "COMPLETATO"
                haEntrata && !haUscita -> "INIZIATO"
                else -> "DA_INIZIARE"
            }

            Log.d("TurniStato", "📊 Turno ID=${turno.id}: $stato")
            stato
        }

        val turniCompletati = turniConStato.count { it == "COMPLETATO" }
        val turniIniziati = turniConStato.count { it == "INIZIATO" }
        val turniDaIniziare = turniConStato.count { it == "DA_INIZIARE" }

        Log.d("TodayStats", "✅ Turni completati: $turniCompletati")
        Log.d("TodayStats", "⏳ Turni iniziati: $turniIniziati")
        Log.d("TodayStats", "🕒 Turni da iniziare: $turniDaIniziare")

        return TodayStats(
            turniTotaliAssegnati = turniTotaliAssegnati,
            dipartimentiAperti = dipartimentiAperti,
            utentiAttiviOggi = utentiAttiviOggi,
            turniCompletati = turniCompletati,
            turniIniziati = turniIniziati,
            turniDaIniziare = turniDaIniziare,
            dipartimentiDetails = dipartimentiDetails
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
            val weekStartRiferimento = WeeklyPublicationCalculator.getReferenceWeekStart(LocalDate.now())
            val idAzienda = _homeState.value.azienda.idAzienda

            val publicationRecord = weeklyShiftRepository.getThisWeekPublishedShift(idAzienda, weekStartRiferimento)
            Log.d("TAG", "loadShiftPublicationInfoSuspend: publicationRecord=$publicationRecord")

            when (publicationRecord) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        _homeState.update {
                            it.copy(
                                daysUntilShiftPublication = daysUntilNextFriday(LocalDate.now()),
                                shiftsPublishedThisWeek = publicationRecord.data != null
                            )
                        }
                    }
                    Log.d("TAG", "loadShiftPublicationInfoSuspend: updated publication flags")
                }
                is Resource.Error -> {
                    Log.e("TAG", "loadShiftPublicationInfoSuspend: error=${publicationRecord.message}")
                    withContext(Dispatchers.Main) {
                        _homeState.update {
                            it.copy(error = "Errore caricamento info pubblicazione: ${publicationRecord.message}")
                        }
                    }
                }
                else -> {
                    Log.d("TAG", "loadShiftPublicationInfoSuspend: result not success, skipping")
                }
            }

        } catch (e: Exception) {
            Log.e("TAG", "loadShiftPublicationInfoSuspend: exception=${e.message}", e)
            withContext(Dispatchers.Main) {
                _homeState.update { it.copy(error = "Errore caricamento info pubblicazione: ${e.message}") }
            }
        }
    }
    fun daysUntilNextFriday(date: LocalDate): Int {
        val todayValue = date.dayOfWeek.value        // lun=1 … dom=7
        val fridayValue = DayOfWeek.FRIDAY.value     // 5
        return (fridayValue - todayValue + 7) % 7
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



    fun clearError() {
        _homeState.update { it.copy(error = null) }
    }


}


