package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.enumClass.EmployeeSection
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.usecases.GetContrattoUseCase
import com.bizsync.domain.usecases.GetDipendentiUseCase
import com.bizsync.domain.usecases.GetFutureShiftsUseCase
import com.bizsync.domain.usecases.GetPastShiftsUseCase
import com.bizsync.domain.usecases.GetTurniRemoteUseCase
import com.bizsync.domain.usecases.SaveTurniLocalUseCase
import com.bizsync.ui.mapper.toUiList
import com.bizsync.ui.model.EmployeeManagementState
import com.bizsync.ui.model.UserUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class EmployeeManagementViewModel @Inject constructor(
    private val getDipendentiUseCase: GetDipendentiUseCase,
    private val getContrattoUseCase: GetContrattoUseCase,
    private val getPastShiftsUseCase: GetPastShiftsUseCase,
    private val getFutureShiftsUseCase: GetFutureShiftsUseCase,
    private val getTurniRemoteUseCase: GetTurniRemoteUseCase,
    private val saveTurniLocalUseCase: SaveTurniLocalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeManagementState())
    val uiState: StateFlow<EmployeeManagementState> = _uiState

    fun setCurrentSection(section: EmployeeSection) {
        _uiState.update { it.copy(currentSection = section) }
    }

    fun loadEmployees() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val result = getDipendentiUseCase()
                _uiState.update { it.copy(employees = result.toUiList()) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Errore nel caricamento dei dipendenti: ${e.message}")
                }
                Log.e("EmployeeManagementViewModel", "Error loading employees", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadEmployeeContract(employeeId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val contract = getContrattoUseCase(employeeId)

                if (contract != null)
                    _uiState.update { it.copy(contract = contract) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Errore nel caricamento del contratto: ${e.message}")
                }
                Log.e("EmployeeManagementViewModel", "Error loading contract", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadEmployeePastShifts(
        employeeId: String,
        startDate: LocalDate = LocalDate.now().minusMonths(3),
        endDate: LocalDate = LocalDate.now().minusDays(1),
        idAzienda: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                Log.d("ShiftsViewModel", "Caricamento turni passati per $employeeId dal $startDate al $endDate")

                // Funzione helper per filtrare i turni passati
                fun filterPastShifts(turni: List<Turno>): List<Turno> {
                    return turni
                        .filter { it.idDipendenti.contains(employeeId) }
                        .filter { it.data <= endDate }
                        .sortedByDescending { it.data }
                }

                // 1. Prova prima dalla cache
                val tuttiTurni = getPastShiftsUseCase(idAzienda, startDate, endDate)
                val turniDelDipendente = tuttiTurni.filter { it.idDipendenti.contains(employeeId) }

                Log.d("ShiftsViewModel", "Trovati ${turniDelDipendente.size} turni in cache")

                // 2. Se la cache è vuota o insufficiente per il range richiesto, carica da Firebase
                if (shouldLoadFromFirebase(turniDelDipendente, startDate, endDate)) {
                    Log.d("ShiftsViewModel", "Caricamento da Firebase necessario")

                    when (val result = getTurniRemoteUseCase(idAzienda, startDate, endDate, employeeId)) {
                        is Resource.Success -> {
                            Log.d("ShiftsViewModel", "Caricati ${result.data.size} turni da Firebase")

                            // GESTIONE DUPLICATI: confronta con cache esistente
                            val turniDaFirebase = result.data
                            val turniEsistenti = getPastShiftsUseCase(idAzienda, startDate, endDate) // Ricarica cache completa

                            // Identifica turni veramente nuovi (non già in cache)
                            val turniNuovi = turniDaFirebase.filter { turnoDaFirebase ->
                                turniEsistenti.none { turnoEsistente ->
                                    // Confronta per ID o combinazione univoca
                                    turnoEsistente.id == turnoDaFirebase.id ||
                                            (turnoEsistente.data == turnoDaFirebase.data &&
                                                    turnoEsistente.orarioInizio == turnoDaFirebase.orarioInizio &&
                                                    turnoEsistente.orarioFine == turnoDaFirebase.orarioFine &&
                                                    turnoEsistente.idDipendenti == turnoDaFirebase.idDipendenti)
                                }
                            }

                            Log.d("ShiftsViewModel", "Identificati ${turniNuovi.size} turni nuovi da salvare")

                            // Salva SOLO i turni nuovi in cache
                            if (turniNuovi.isNotEmpty()) {
                                saveTurniLocalUseCase(turniNuovi)
                                Log.d("ShiftsViewModel", "Salvati ${turniNuovi.size} turni nuovi in cache")
                            }

                            // Combina cache aggiornata + Firebase per risultato finale
                            val tuttiTurniAggiornati = (turniEsistenti + turniNuovi).distinctBy {
                                // Rimuovi eventuali duplicati residui
                                "${it.id}_${it.data}_${it.orarioInizio}"
                            }

                            val turniPassati = filterPastShifts(tuttiTurniAggiornati)

                            Log.d("ShiftsViewModel", "Trovati ${turniPassati.size} turni passati dopo filtro")

                            _uiState.update { it.copy(shifts = turniPassati) }
                            Log.d("ShiftsViewModel", "UI aggiornata con ${turniPassati.size} turni finali")
                        }

                        is Resource.Error -> {
                            Log.e("ShiftsViewModel", "Errore Firebase: ${result.message}")
                            val turniPassati = filterPastShifts(turniDelDipendente)
                            _uiState.update {
                                it.copy(
                                    shifts = turniPassati,
                                    errorMessage = "Alcuni dati potrebbero non essere aggiornati: ${result.message}"
                                )
                            }
                            Log.d("ShiftsViewModel", "Fallback cache: ${turniPassati.size} turni passati")
                        }

                        is Resource.Empty -> {
                            Log.d("ShiftsViewModel", "Nessun turno trovato su Firebase")
                            val turniPassati = filterPastShifts(turniDelDipendente)
                            _uiState.update { it.copy(shifts = turniPassati) }
                            Log.d("ShiftsViewModel", "Cache vuota Firebase: ${turniPassati.size} turni passati")
                        }
                    }
                } else {
                    // Usa i dati dalla cache
                    Log.d("ShiftsViewModel", "Utilizzando dati dalla cache")
                    val turniPassati = filterPastShifts(turniDelDipendente)
                    _uiState.update { it.copy(shifts = turniPassati) }
                    Log.d("ShiftsViewModel", "Solo cache: ${turniPassati.size} turni passati")
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Errore nei turni passati: ${e.message}")
                }
                Log.e("ShiftsViewModel", "Error loading past shifts", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadEmployeeFutureShifts(employeeId: String, idAzienda: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                Log.d("ShiftsViewModel", "Caricamento turni futuri per $employeeId")

                val tuttiTurni = getFutureShiftsUseCase(idAzienda, LocalDate.now())
                val turniDelDipendente = tuttiTurni.filter { it.idDipendenti.contains(employeeId) }

                Log.d("ShiftsViewModel", "Trovati ${turniDelDipendente.size} turni futuri in cache")

                _uiState.update { it.copy(shifts = turniDelDipendente) }

                // Opzionale: verifica se servono aggiornamenti da Firebase
                if (turniDelDipendente.isEmpty()) {
                    Log.d("ShiftsViewModel", "Nessun turno futuro in cache, provo Firebase")

                    when (val result = getTurniRemoteUseCase(
                        idAzienda,
                        LocalDate.now(),
                        LocalDate.now().plusMonths(2),
                        employeeId
                    )) {
                        is Resource.Success -> {
                            saveTurniLocalUseCase(result.data)

                            val turniFuturi = result.data.filter {
                                it.data >= LocalDate.now()
                            }
                            _uiState.update { it.copy(shifts = turniFuturi) }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(errorMessage = "Errore caricamento turni futuri: ${result.message}")
                            }
                        }
                        is Resource.Empty -> {
                            // Nessun turno futuro trovato
                            _uiState.update { it.copy(shifts = emptyList()) }
                        }
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Errore nei turni futuri: ${e.message}")
                }
                Log.e("EmployeeManagementViewModel", "Error loading future shifts", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun shouldLoadFromFirebase(
        cachedShifts: List<com.bizsync.domain.model.Turno>,
        startDate: LocalDate,
        endDate: LocalDate
    ): Boolean {
        // Se non ci sono turni in cache, carica da Firebase
        if (cachedShifts.isEmpty()) return true

        // Calcola i giorni totali nel range richiesto
        val totalDaysInRange = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt()

        // Se il range richiesto è più di 2 settimane (limite cache), carica da Firebase
        if (totalDaysInRange > 14) return true

        // Controlla se abbiamo dati per tutto il range richiesto
        val daysWithShifts = cachedShifts.map { it.data }.distinct().size

        // Se abbiamo meno di 1/3 dei giorni con dati, carica da Firebase
        return daysWithShifts < (totalDaysInRange / 3)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateSelectedDepartment(department: String) {
        _uiState.update { it.copy(selectedDepartment = department) }
    }

    fun updateSelectedEmployee(employee: UserUi?) {
        _uiState.update { it.copy(selectedEmployee = employee) }
    }
}