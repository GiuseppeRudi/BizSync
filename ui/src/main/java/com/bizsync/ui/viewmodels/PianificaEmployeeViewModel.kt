package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.orchestrator.TurnoOrchestrator
import com.bizsync.cache.dao.AbsenceDao
import com.bizsync.cache.dao.ContrattoDao
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.*
import com.bizsync.ui.model.EmployeeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class PianificaEmployeeViewModel @Inject constructor(
    private val turnoOrchestrator: TurnoOrchestrator,
    private val userDao: UserDao,
    private val turnoDao: TurnoDao,
    private val absenceDao: AbsenceDao,
    private val contrattiDao: ContrattoDao,
) : ViewModel() {

    companion object {
        private const val TAG = "PianificaEmployeeVM"
    }

    private val _uiState = MutableStateFlow(EmployeeState())
    val uiState: StateFlow<EmployeeState> = _uiState

    // ========== GESTIONE LOADING ==========
    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(loading = loading) }
    }

    // ========== GESTIONE TURNI SETTIMANALI ==========
    fun setTurniSettimanaliDipendente(startWeek: LocalDate, idAzienda: String, idUser: String) {
        viewModelScope.launch {
            setLoading(true)
            try {
                when (val result = turnoOrchestrator.fetchTurniSettimana(startWeek, idAzienda, idUser)) {
                    is Resource.Success -> {
                        val turni = result.data
                        val grouped = turni.groupBy { it.data.dayOfWeek }
                        val allDays = DayOfWeek.entries.associateWith { grouped[it] ?: emptyList() }

                        _uiState.update { current ->
                            current.copy(
                                turniSettimanali = allDays,
                                turniEmployee = turni
                            )
                        }

                        // Calcola statistiche settimanali
                        calcolaStatisticheSettimanali(startWeek, turni)

                        Log.d(TAG, "✅ Turni settimanali caricati: ${turni.size}")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Errore caricamento turni: ${result.message}")
                        val allDays = DayOfWeek.entries.associateWith { emptyList<Turno>() }
                        _uiState.update { current ->
                            current.copy(
                                turniSettimanali = allDays,
                                errorMessage = result.message
                            )
                        }
                    }
                    is Resource.Empty -> {
                        val allDays = DayOfWeek.entries.associateWith { emptyList<Turno>() }
                        _uiState.update { current -> current.copy(turniSettimanali = allDays) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Errore imprevisto: ${e.message}")
                _uiState.update { it.copy(errorMessage = "Errore imprevisto: ${e.message}") }
            } finally {
                setLoading(false)
            }
        }
    }

    // ========== GESTIONE TURNI GIORNALIERI ==========
    fun setTurniGiornalieri(dataSelezionata: LocalDate) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🗓️ Caricamento turni per data: $dataSelezionata")

                // Prendi i turni del giorno selezionato
                val turniGiorno = _uiState.value.turniEmployee.filter { it.data == dataSelezionata }

                // Calcola dettagli giornalieri
                val dettagliGiornalieri = calcolaDettagliGiornalieri(dataSelezionata, turniGiorno)

                _uiState.update { current ->
                    current.copy(
                        turniGiornalieri = turniGiorno,
                        dataSelezionata = dataSelezionata,
                        dettagliGiornalieri = dettagliGiornalieri
                    )
                }

                Log.d(TAG, "✅ Turni giornalieri impostati: ${turniGiorno.size} turni")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Errore caricamento turni giornalieri: ${e.message}")
                _uiState.update { it.copy(errorMessage = "Errore caricamento turni giornalieri: ${e.message}") }
            }
        }
    }

    // ========== CALCOLO DETTAGLI GIORNALIERI ==========
    private fun calcolaDettagliGiornalieri(data: LocalDate, turni: List<Turno>): DettagliGiornalieri {
        val oreTotaliAssegnate = turni.sumOf { it.calcolaDurata() }
        val pauseTotali = turni.flatMap { it.pause }.sumOf { it.durata.toMinutes() }
        val oreEffettive = oreTotaliAssegnate - (pauseTotali / 60.0)

        val orarioInizio = turni.minByOrNull { it.orarioInizio }?.orarioInizio
        val orarioFine = turni.maxByOrNull { it.orarioFine }?.orarioFine

        // Calcola colleghi della giornata
        val colleghiGiornata = turni.flatMap { turno ->
            turno.idDipendenti.mapNotNull { idDipendente ->
                _uiState.value.colleghiTurno.find { it.uid == idDipendente }
            }
        }.distinctBy { it.uid }

        return DettagliGiornalieri(
            data = data,
            oreTotaliAssegnate = oreTotaliAssegnate,
            oreEffettive = oreEffettive,
            orarioInizio = orarioInizio,
            orarioFine = orarioFine,
            numeroTurni = turni.size,
            colleghi = colleghiGiornata,
            pause = turni.flatMap { it.pause },
            note = turni.flatMap { it.note },
            nomeDipartimento = _uiState.value.dipartimentoEmployee?.nomeArea,
            orarioAperturaDipartimento = _uiState.value.dipartimentoEmployee?.orariSettimanali?.get(data.dayOfWeek)?.first,
            orarioChiusuraDipartimento = _uiState.value.dipartimentoEmployee?.orariSettimanali?.get(data.dayOfWeek)?.second
        )
    }

    // ========== CALCOLO STATISTICHE SETTIMANALI ==========
    private fun calcolaStatisticheSettimanali(startWeek: LocalDate, turni: List<Turno>) {
        val contratto = _uiState.value.contrattoEmployee
        val oreContrattuali = contratto?.oreSettimanali?.toIntOrNull() ?: 0

        val oreAssegnateSettimana = turni.sumOf { it.calcolaDurata() }
        val pauseSettimana = turni.flatMap { it.pause }.sumOf { it.durata.toMinutes() }
        val oreEffettiveSettimana = oreAssegnateSettimana - (pauseSettimana / 60.0)

        val giorniLavorativi = turni.map { it.data }.distinct().size
        val turniTotali = turni.size

        val statistiche = StatisticheSettimanali(
            weekStart = startWeek,
            oreContrattuali = oreContrattuali,
            oreAssegnate = oreAssegnateSettimana,
            oreEffettive = oreEffettiveSettimana,
            giorniLavorativi = giorniLavorativi,
            turniTotali = turniTotali,
            differenzaOre = oreAssegnateSettimana - oreContrattuali
        )

        _uiState.update { it.copy(statisticheSettimanali = statistiche) }
    }

    // ========== INIZIALIZZAZIONE DATI ==========
    fun inizializzaDatiEmployee(userId: String, idAzienda: String, dipartimento : AreaLavoro) {
        viewModelScope.launch {
            try {
                setLoading(true)

                val colleghiDeferred = async { userDao.getDipendenti(idAzienda) }
                val assenzeDeferred = async { absenceDao.getAbsencesByUser(userId) }

                val colleghiEntity = colleghiDeferred.await()
                val assenzeEntity = assenzeDeferred.await()

                val colleghi = colleghiEntity.toDomainList()
                val assenze = assenzeEntity.toDomainList()

                _uiState.update {
                    it.copy(
                        colleghiTurno = colleghi,
                        assenzeEmployee = assenze,
                        dipartimentoEmployee = dipartimento
                    )
                }

                Log.d(TAG, "✅ Dati employee inizializzati")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Errore inizializzazione dati employee: ${e.message}")
                _uiState.update { it.copy(errorMessage = "Errore caricamento dati: ${e.message}") }
            } finally {
                setLoading(false)
            }
        }
    }

    // ========== GESTIONE DIALOG ==========
    fun setShowDialogDettagliTurno(show: Boolean, turno: Turno? = null) {
        _uiState.update {
            it.copy(
                showDialogDettagliTurno = show,
                turnoSelezionato = turno
            )
        }
    }

    // ========== GESTIONE MESSAGGI ==========
    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun  inizializzaDati(userCurrent : User, contratti: Contratto) {
        _uiState.update {
            it.copy(
                currentUser = userCurrent,
                contrattoEmployee = contratti
            )
        }
    }

    // ========== UTILITY ==========
    fun calcolaOreLavorate(data: LocalDate): String {
        val turniGiorno = _uiState.value.turniEmployee.filter { it.data == data }
        val oreTotali = turniGiorno.sumOf { it.calcolaDurata() }
        val pauseTotali = turniGiorno.flatMap { it.pause }.sumOf { it.durata.toMinutes() }
        val oreEffettive = oreTotali - (pauseTotali / 60.0)

        return if (oreEffettive > 0) {
            "${oreEffettive.toInt()}h ${((oreEffettive % 1) * 60).toInt()}m"
        } else {
            "0h 0m"
        }
    }

    fun calcolaOreSettimanali(startWeek: LocalDate): String {
        val endWeek = startWeek.plusDays(6)
        val turniSettimana = _uiState.value.turniEmployee.filter {
            it.data >= startWeek && it.data <= endWeek
        }

        val oreTotali = turniSettimana.sumOf { it.calcolaDurata() }
        val pauseTotali = turniSettimana.flatMap { it.pause }.sumOf { it.durata.toMinutes() }
        val oreEffettive = oreTotali - (pauseTotali / 60.0)

        return "${oreEffettive.toInt()}h ${((oreEffettive % 1) * 60).toInt()}m"
    }

    fun hasTurnoOggi(): Boolean {
        val oggi = LocalDate.now()
        return _uiState.value.turniEmployee.any { it.data == oggi }
    }

    fun getTurnoById(turnoId: String): Turno? {
        return _uiState.value.turniEmployee.find { it.id == turnoId }
    }

    fun getColleghiByTurno(turnoId: String): List<User> {
        val turno = getTurnoById(turnoId)
        return turno?.idDipendenti?.mapNotNull { idDipendente ->
            _uiState.value.colleghiTurno.find { it.uid == idDipendente }
        } ?: emptyList()
    }
}



// ========== MODELLI DATI AGGIUNTIVI ==========
data class DettagliGiornalieri(
    val data: LocalDate,
    val oreTotaliAssegnate: Int,
    val oreEffettive: Double,
    val orarioInizio: LocalTime?,
    val orarioFine: LocalTime?,
    val numeroTurni: Int,
    val colleghi: List<User>,
    val pause: List<Pausa>,
    val note: List<Nota>,
    // Informazioni dipartimento
    val nomeDipartimento: String? = null,
    val orarioAperturaDipartimento: LocalTime? = null,
    val orarioChiusuraDipartimento: LocalTime? = null
)

data class StatisticheSettimanali(
    val weekStart: LocalDate,
    val oreContrattuali: Int,
    val oreAssegnate: Int,
    val oreEffettive: Double,
    val giorniLavorativi: Int,
    val turniTotali: Int,
    val differenzaOre: Int
)