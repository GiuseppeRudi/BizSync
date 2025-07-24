package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.cache.dao.ContrattoDao
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.constants.enumClass.EmployeeSection
import com.bizsync.domain.constants.sealedClass.Resource
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
    private val userDao: UserDao,
    private val contractDao: ContrattoDao,
    private val turniDao: TurnoDao,
    private val turnoRepository: TurnoRepository
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

                val result = userDao.getDipendenti()
                val resultDomain = result.toDomainList()
                _uiState.update { it.copy(employees = resultDomain.toUiList()) }

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

                val contract = contractDao.getContratto(employeeId)

                if (contract != null)
                    _uiState.update { it.copy(contract = contract.toDomain()) }

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
        endDate: LocalDate = LocalDate.now(),
        idAzienda : String
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                Log.d("ShiftsViewModel", " Caricamento turni passati per $employeeId dal $startDate al $endDate")

                // 1. Prova prima dalla cache
                val tuttiTurni = turniDao.getPastShifts(idAzienda, startDate, endDate)
                val turniDelDip = tuttiTurni.filter { it.idDipendenti.contains(employeeId) }
                val turniDelDipendente = turniDelDip.toDomainList()

                Log.d("ShiftsViewModel", " Trovati ${turniDelDipendente.size} turni in cache")

                // 2. Se la cache è vuota o insufficiente per il range richiesto, carica da Firebase
                if (shouldLoadFromFirebase(turniDelDipendente, startDate, endDate)) {
                    Log.d("ShiftsViewModel", " Caricamento da Firebase necessario")



                    when (val result = turnoRepository.getTurniRangeByAzienda(
                        idAzienda = idAzienda,
                        startRange = startDate,
                        endRange = endDate,
                        idEmployee = employeeId
                    )) {
                        is Resource.Success -> {
                            Log.d("ShiftsViewModel", " Caricati ${result.data.size} turni da Firebase")

                            // Salva in cache i nuovi turni
                            val turniEntity = result.data.toEntityList()
                            turniDao.insertAll(turniEntity)

                            // Aggiorna lo stato con i turni da Firebase
                            val turniPassati = result.data.filter {
                                it.data <= LocalDate.now()
                            }
                            _uiState.update { it.copy(shifts = turniPassati) }
                        }
                        is Resource.Error -> {
                            Log.e("ShiftsViewModel", " Errore Firebase: ${result.message}")
                            _uiState.update {
                                it.copy(
                                    shifts = turniDelDipendente,
                                    errorMessage = "Alcuni dati potrebbero non essere aggiornati: ${result.message}"
                                )
                            }
                        }
                        is Resource.Empty -> {
                            Log.d("ShiftsViewModel", "Nessun turno trovato su Firebase")
                            _uiState.update { it.copy(shifts = turniDelDipendente) }
                        }
                    }
                } else {
                    // Usa i dati dalla cache
                    Log.d("ShiftsViewModel", " Utilizzando dati dalla cache")
                    _uiState.update { it.copy(shifts = turniDelDipendente) }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Errore nei turni passati: ${e.message}")
                }
                Log.e("EmployeeManagementViewModel", "Error loading past shifts", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Carica i turni futuri di un dipendente (principalmente dalla cache)
     * @param employeeId ID del dipendente
     */
    fun loadEmployeeFutureShifts(employeeId: String, idAzienda : String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                Log.d("ShiftsViewModel", " Caricamento turni futuri per $employeeId")

                val tuttiTurni = turniDao.getFutureShiftsFromToday(idAzienda, LocalDate.now())
                val turniDelD = tuttiTurni.filter { it.idDipendenti.contains(employeeId) }
                val turniDelDipendente = turniDelD.toDomainList()

                Log.d("ShiftsViewModel", " Trovati ${turniDelDipendente.size} turni futuri in cache")

                _uiState.update { it.copy(shifts = turniDelDipendente) }

                // Opzionale: verifica se servono aggiornamenti da Firebase
                if (turniDelDipendente.isEmpty()) {
                    Log.d("ShiftsViewModel", " Nessun turno futuro in cache, provo Firebase")



                    when (val result = turnoRepository.getTurniRangeByAzienda(
                        idAzienda = idAzienda,
                        startRange = LocalDate.now(),
                        endRange = LocalDate.now().plusMonths(2),
                        idEmployee = employeeId
                    )) {
                        is Resource.Success -> {
                            val turniEntity = result.data.toEntityList()
                            turniDao.insertAll(turniEntity)

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

    /**
     * Determina se è necessario caricare i dati da Firebase
     * basandosi sulla completezza dei dati in cache per il range richiesto
     */
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