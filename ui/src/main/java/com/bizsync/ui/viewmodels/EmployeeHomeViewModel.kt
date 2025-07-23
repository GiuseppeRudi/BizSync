package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.orchestrator.BadgeOrchestrator
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
import com.bizsync.domain.utils.WeeklyPublicationCalculator
import com.bizsync.domain.utils.WeeklyWindowCalculator
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.model.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class EmployeeHomeState(
    val badge: BadgeVirtuale? = null,
    val prossimoTurno: ProssimoTurno? = null,
    val todayTurno: TurnoWithDetails? = null,
    val timbratureOggi: List<Timbratura> = emptyList(),
    val canTimbra: Boolean = false,
    val daysUntilShiftPublication: Int? = null,
    val shiftsPublishedThisWeek: Boolean = false,
    val isLoading: Boolean = false,
    val isGettingLocation: Boolean = false,
    val error: String? = null,
    val showSuccess: Boolean = false,
    val successMessage: String = "",
    val user: User = User(),
    val azienda: Azienda = Azienda()
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

private const val TAG = "EmployeeHomeVM"

@HiltViewModel
class EmployeeHomeViewModel @Inject constructor(
    private val turnoDao: TurnoDao,
    private val timbratureDao: TimbraturaDao,
    private val badgeOrchestrator: BadgeOrchestrator,
    private val weeklyShiftRepository: WeeklyShiftRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(EmployeeHomeState())
    val homeState = _homeState.asStateFlow()

    private val _timerState = MutableStateFlow<ProssimoTurno?>(null)
    val timerState: StateFlow<ProssimoTurno?> = _timerState.asStateFlow()

    // Job per gestire il timer
    private var timerJob: Job? = null

    fun initializeEmployee(userState: UserState) {
        Log.d(TAG, "initializeEmployee: received userState=$userState")
        _homeState.update {
            it.copy(
                user = userState.user.toDomain(),
                azienda = userState.azienda.toDomain()
            )
        }
        loadEmployeeHomeData()
    }

    private fun loadEmployeeHomeData() {
        Log.d(TAG, "loadEmployeeHomeData: start")
        viewModelScope.launch(Dispatchers.IO) {
            // 🎯 AGGIORNA LOADING SU MAIN THREAD
            withContext(Dispatchers.Main) {
                _homeState.update { it.copy(isLoading = true) }
            }

            try {
                val user = _homeState.value.user
                if (user.uid.isNotEmpty()) {
                    Log.d(TAG, "Loading data for user=${user.uid}")

                    // 🚀 ESEGUI TUTTE LE OPERAZIONI IN PARALLELO
                    val loadTodayTurnoDeferred = async { loadTodayTurnoSuspend(user.uid) }
                    val loadTimbratureOggiDeferred = async { loadTimbratureOggiSuspend(user.uid) }
                    val loadShiftPublicationDeferred = async { loadShiftPublicationInfoSuspend() }
                    val loadTimbratureFunzionanteDeferred = async { loadTimbratureFunzionanteSuspend() }

                    // 🔄 ASPETTA CHE TUTTE LE OPERAZIONI FINISCANO
                    loadTodayTurnoDeferred.await()
                    loadTimbratureOggiDeferred.await()
                    loadShiftPublicationDeferred.await()
                    loadTimbratureFunzionanteDeferred.await()

                    // 🎯 AVVIA IL TIMER SOLO DOPO CHE I DATI SONO CARICATI
                    startTurnoTimer()

                    withContext(Dispatchers.Main) {
                        _homeState.update { it.copy(isLoading = false) }
                    }
                } else {
                    Log.d(TAG, "No currentUser set, skipping data load.")
                    withContext(Dispatchers.Main) {
                        _homeState.update { it.copy(isLoading = false) }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "loadEmployeeHomeData: exception=${e.message}", e)
                withContext(Dispatchers.Main) {
                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            error = "Errore nel caricamento dei dati: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    // 🔧 VERSIONE SUSPEND DI loadTodayTurno
    private suspend fun loadTodayTurnoSuspend(userId: String) {
        Log.d(TAG, "loadTodayTurnoSuspend: start for userId=$userId")
        try {
            val today = LocalDate.now()

            // 1) Prendo i turni di oggi - ESEGUITO SU IO DISPATCHER
            val turniOggiEntities = turnoDao
                .getTurniByDateAndUser(today)
                .first()
            Log.d(TAG, "loadTodayTurnoSuspend: DAO returned ${turniOggiEntities.size} turniOggi")

            if (turniOggiEntities.isNotEmpty()) {
                val turnoEntity = turniOggiEntities.first()
                Log.d(TAG, "loadTodayTurnoSuspend: using turnoEntity=$turnoEntity")

                val startOfDay = today.atStartOfDay().toString()         // "2025-07-23T00:00"
                val endOfDay = today.atTime(23, 59, 59).toString()       // "2025-07-23T23:59:59"

                // 2) Prendo le timbrature di oggi per l'utente - ESEGUITO SU IO DISPATCHER
                val timbratureEntities = timbratureDao
                    .getTimbratureByDateAndUser(startOfDay, endOfDay, userId)

                Log.d(TAG, "loadTodayTurnoSuspend: DAO returned ${timbratureEntities.size} timbratureOggi")

                // 3) Mappo in domain
                val turnoDomain = turnoEntity.toDomain()
                val timbratureDomain = timbratureEntities.toDomainList()
                Log.d(TAG, "loadTodayTurnoSuspend: mapped to domain models")

                // 4) Creo dettagli e aggiorno lo stato
                val turnoWithDetails = createTurnoWithDetails(turnoDomain, timbratureDomain)

                // 🎯 AGGIORNA LO STATO SUL MAIN THREAD
                withContext(Dispatchers.Main) {
                    _homeState.update { prev ->
                        val newState = prev.copy(todayTurno = turnoWithDetails)
                        Log.d(TAG, "loadTodayTurnoSuspend: updating state with todayTurno")
                        newState
                    }
                }
            } else {
                Log.d(TAG, "loadTodayTurnoSuspend: no turniOggi, skipping detail load")
            }

        } catch (e: Exception) {
            Log.e(TAG, "loadTodayTurnoSuspend: exception=${e.message}", e)
            withContext(Dispatchers.Main) {
                _homeState.update { prev ->
                    prev.copy(error = "Errore caricamento turno di oggi: ${e.message}")
                }
            }
        }
    }

    // 🔧 VERSIONE SUSPEND DI loadTimbratureOggi
    private suspend fun loadTimbratureOggiSuspend(userId: String) {
        Log.d(TAG, "loadTimbratureOggiSuspend: start for userId=$userId")
        try {
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay().toString()         // "2025-07-23T00:00"
            val endOfDay = today.atTime(23, 59, 59).toString()       // "2025-07-23T23:59:59"
            // ✅ OPERAZIONE DATABASE SU IO DISPATCHER
            val timbratureEntities = timbratureDao
                .getTimbratureByDateAndUser(startOfDay, endOfDay, userId)

            Log.d(TAG, "loadTimbratureOggiSuspend: DAO returned ${timbratureEntities.size} entities")

            val timbratureDomain = timbratureEntities.toDomainList()
            Log.d(TAG, "loadTimbratureOggiSuspend: Mapped to domain models")

            // 🎯 AGGIORNA LO STATO SUL MAIN THREAD
            withContext(Dispatchers.Main) {
                _homeState.update { prev ->
                    val newState = prev.copy(timbratureOggi = timbratureDomain)
                    Log.d(TAG, "loadTimbratureOggiSuspend: Updated state")
                    newState
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "loadTimbratureOggiSuspend: exception=${e.message}", e)
            withContext(Dispatchers.Main) {
                _homeState.update { prev ->
                    prev.copy(error = "Errore caricamento timbrature: ${e.message}")
                }
            }
        }
    }

    // 🔧 VERSIONE SUSPEND DI loadShiftPublicationInfo
    private suspend fun loadShiftPublicationInfoSuspend() {
        Log.d(TAG, "loadShiftPublicationInfoSuspend: start")
        try {
            val weekStartRiferimento = WeeklyPublicationCalculator.getReferenceWeekStart(LocalDate.now())
            val idAzienda = _homeState.value.azienda.idAzienda.ifEmpty { "ZNTzPHOA2xyJMgymaFN7" }

            val publicationRecord = weeklyShiftRepository.getThisWeekPublishedShift(idAzienda, weekStartRiferimento)
            Log.d(TAG, "loadShiftPublicationInfoSuspend: publicationRecord=$publicationRecord")

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
                    Log.d(TAG, "loadShiftPublicationInfoSuspend: updated publication flags")
                }
                is Resource.Error -> {
                    Log.e(TAG, "loadShiftPublicationInfoSuspend: error=${publicationRecord.message}")
                    withContext(Dispatchers.Main) {
                        _homeState.update {
                            it.copy(error = "Errore caricamento info pubblicazione: ${publicationRecord.message}")
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "loadShiftPublicationInfoSuspend: result not success, skipping")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "loadShiftPublicationInfoSuspend: exception=${e.message}", e)
            withContext(Dispatchers.Main) {
                _homeState.update { it.copy(error = "Errore caricamento info pubblicazione: ${e.message}") }
            }
        }
    }

    // 🔧 VERSIONE SUSPEND DI loadTimbratureFunzionante
    private suspend fun loadTimbratureFunzionanteSuspend() {
        Log.d(TAG, "loadTimbratureFunzionanteSuspend: start")
        try {
            when (val result = badgeOrchestrator.getTimbratureGiornaliere(
                _homeState.value.user.uid,
                LocalDate.now()
            )) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        _homeState.update {
                            it.copy(timbratureOggi = result.data)
                        }
                    }
                    Log.d(TAG, "loadTimbratureFunzionanteSuspend: Success - loaded ${result.data.size} timbrature")
                }
                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        _homeState.update {
                            it.copy(error = result.message)
                        }
                    }
                    Log.e(TAG, "loadTimbratureFunzionanteSuspend: Error - ${result.message}")
                }
                else -> {
                    Log.d(TAG, "loadTimbratureFunzionanteSuspend: Empty or Loading result")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadTimbratureFunzionanteSuspend: exception=${e.message}", e)
            withContext(Dispatchers.Main) {
                _homeState.update {
                    it.copy(error = "Errore caricamento timbrature funzionanti: ${e.message}")
                }
            }
        }
    }

    // 🔧 TIMER SISTEMATO CON DISPATCHER CORRETTO
    private fun startTurnoTimer() {
        // Cancella il timer precedente se esiste
        timerJob?.cancel()

        timerJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "startTurnoTimer: started")
            while (isActive) {
                try {
                    when (val result = badgeOrchestrator.getProssimoTurno(_homeState.value.user.uid)) {
                        is Resource.Success -> {
                            // 🎯 AGGIORNA GLI STATI SUL MAIN THREAD
                            withContext(Dispatchers.Main) {
                                _timerState.value = result.data
                                _homeState.update {
                                    it.copy(
                                        prossimoTurno = result.data,
                                        canTimbra = result.data.isTimbraturaPossibile()
                                    )
                                }
                            }
                            Log.d(TAG, "startTurnoTimer: Updated timer state")
                        }
                        is Resource.Error -> {
                            withContext(Dispatchers.Main) {
                                _homeState.update {
                                    it.copy(error = result.message)
                                }
                            }
                            Log.e(TAG, "startTurnoTimer: Error - ${result.message}")
                        }
                        else -> {
                            Log.d(TAG, "startTurnoTimer: Empty or Loading result")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "startTurnoTimer: exception=${e.message}", e)
                    withContext(Dispatchers.Main) {
                        _homeState.update {
                            it.copy(error = "Errore timer turno: ${e.message}")
                        }
                    }
                }

                delay(30000) // Aggiorna ogni 30 secondi
            }
        }
    }

    // 🔧 onTimbra SISTEMATO
    fun onTimbra(
        tipoTimbratura: TipoTimbratura,
        latitudine: Double? = null,
        longitudine: Double? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("VIEWMODEL_DEBUG", "=== VIEWMODEL onTimbra chiamato ===")
            Log.d("VIEWMODEL_DEBUG", "Tipo timbratura: $tipoTimbratura")
            Log.d("VIEWMODEL_DEBUG", "Latitudine ricevuta: $latitudine")
            Log.d("VIEWMODEL_DEBUG", "Longitudine ricevuta: $longitudine")

            val turno = _homeState.value.prossimoTurno?.turno
            if (turno == null) {
                Log.e("VIEWMODEL_DEBUG", "❌ Turno è null - impossibile procedere")
                withContext(Dispatchers.Main) {
                    _homeState.update {
                        it.copy(error = "Nessun turno disponibile per la timbratura")
                    }
                }
                return@launch
            }

            Log.d("VIEWMODEL_DEBUG", "Turno trovato: ${turno.id} - ${turno.titolo}")

            // 🎯 AGGIORNA LOADING SU MAIN THREAD
            withContext(Dispatchers.Main) {
                _homeState.update { it.copy(isLoading = true) }
            }

            try {
                Log.d("VIEWMODEL_DEBUG", "Chiamata badgeOrchestrator.creaTimbratura")

                when (val result = badgeOrchestrator.creaTimbratura(
                    turno = turno,
                    dipendente = _homeState.value.user,
                    azienda = _homeState.value.azienda,
                    tipoTimbratura = tipoTimbratura,
                    latitudine = latitudine,
                    longitudine = longitudine
                )) {
                    is Resource.Success -> {
                        Log.d("VIEWMODEL_DEBUG", "✅ Timbratura creata con successo")
                        Log.d("VIEWMODEL_DEBUG", "Timbratura risultante: ID=${result.data.idFirebase}")

                        withContext(Dispatchers.Main) {
                            _homeState.update {
                                it.copy(
                                    isLoading = false,
                                    showSuccess = true,
                                    successMessage = "Timbratura effettuata con successo"
                                )
                            }
                        }

                        // 🔄 RICARICA LE TIMBRATURE
                        loadTimbratureOggiSuspend(_homeState.value.user.uid)
                        loadTimbratureFunzionanteSuspend()
                    }
                    is Resource.Error -> {
                        Log.e("VIEWMODEL_DEBUG", "❌ Errore nella creazione timbratura: ${result.message}")
                        withContext(Dispatchers.Main) {
                            _homeState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.message
                                )
                            }
                        }
                    }
                    else -> {
                        Log.w("VIEWMODEL_DEBUG", "⚠️ Stato loading ricevuto")
                        withContext(Dispatchers.Main) {
                            _homeState.update { it.copy(isLoading = false) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("VIEWMODEL_DEBUG", "🚨 Eccezione in onTimbra: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            error = "Errore imprevisto: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    // 🔧 UTILITY FUNCTIONS
    fun daysUntilNextFriday(date: LocalDate): Int {
        val todayValue = date.dayOfWeek.value        // lun=1 … dom=7
        val fridayValue = DayOfWeek.FRIDAY.value     // 5
        return (fridayValue - todayValue + 7) % 7
    }

    private fun createTurnoWithDetails(turno: Turno, timbrature: List<Timbratura>): TurnoWithDetails {
        Log.d(TAG, "createTurnoWithDetails: turno=$turno, timbrature=${timbrature.size} items")

        val entrataTimbratura = timbrature.find { it.tipoTimbratura == TipoTimbratura.ENTRATA && it.idTurno == turno.id}
        val uscitaTimbratura = timbrature.find { it.tipoTimbratura == TipoTimbratura.USCITA  && it.idTurno == turno.id}

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

        val result = TurnoWithDetails(
            turno = turno,
            haTimbratoEntrata = haEntrata,
            haTimbratoUscita = haUscita,
            orarioEntrataEffettivo = entrataTimbratura?.dataOraTimbratura?.toLocalTime(),
            orarioUscitaEffettivo = uscitaTimbratura?.dataOraTimbratura?.toLocalTime(),
            minutiRitardo = minutiRitardo,
            minutiAnticipo = minutiAnticipo,
            statoTurno = stato
        )

        Log.d(TAG, "createTurnoWithDetails: result=$result")
        return result
    }

    // 🔧 PUBLIC FUNCTIONS
    fun setIsGettingLocation(isGetting: Boolean) {
        Log.d(TAG, "setIsGettingLocation: $isGetting")
        _homeState.update { it.copy(isGettingLocation = isGetting) }
    }

    fun dismissError() {
        Log.d(TAG, "dismissError")
        _homeState.update { it.copy(error = null) }
    }

    fun dismissSuccess() {
        Log.d(TAG, "dismissSuccess")
        _homeState.update { it.copy(showSuccess = false) }
    }

    fun refreshData() {
        Log.d(TAG, "refreshData: refreshing data")
        // 🚀 FERMA IL TIMER PRECEDENTE E RIAVVIA IL CARICAMENTO
        timerJob?.cancel()
        loadEmployeeHomeData()
    }

    // 🔧 CLEANUP
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: cancelling timer job")
        timerJob?.cancel()
    }
}