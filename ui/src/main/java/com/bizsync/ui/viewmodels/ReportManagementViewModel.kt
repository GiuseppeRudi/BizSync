package com.bizsync.ui.viewmodels

import com.bizsync.backend.repository.AbsenceRepository
import com.bizsync.backend.repository.ContractRepository
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.cache.dao.AbsenceDao
import com.bizsync.cache.dao.ContrattoDao
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.dao.UserDao
import com.bizsync.ui.model.ReportData


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.enumClass.ReportFilter
import com.bizsync.ui.model.CacheStatus
import com.bizsync.ui.model.ReportsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject




@HiltViewModel
class ReportsManagementViewModel @Inject constructor(
    private val contrattoDao: ContrattoDao,
    private val userDao: UserDao,
    private val absenceDao: AbsenceDao,
    private val turnoDao: TurnoDao,
    private val contrattoRepository: ContractRepository,
    private val absenceRepository: AbsenceRepository,
    private val turnoRepository: TurnoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState = _uiState.asStateFlow()

    private val _cacheStatus = MutableStateFlow(CacheStatus())
    private val cacheStatus = _cacheStatus.asStateFlow()

    init {
        loadInitialData()
    }

    fun updateSelectedFilter(filter: ReportFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedFilter = filter) }

            // Controlla se servono dati aggiuntivi da Firebase
            when (filter) {
                ReportFilter.TODAY, ReportFilter.WEEK -> {
                    // Questi usano solo la cache corrente (2 settimane)
                    if (!cacheStatus.value.hasCurrentData) {
                        loadCurrentDataFromFirebase()
                    }
                }
                ReportFilter.MONTH -> {
                    if (!cacheStatus.value.hasMonthData) {
                        loadMonthDataFromFirebase()
                    }
                }
                ReportFilter.QUARTER -> {
                    if (!cacheStatus.value.hasQuarterData) {
                        loadQuarterDataFromFirebase()
                    }
                }
                ReportFilter.YEAR, ReportFilter.ALL_TIME -> {
                    if (!cacheStatus.value.hasYearData) {
                        loadYearDataFromFirebase()
                    }
                }
            }

            // Ricarica i dati dalla cache con il nuovo filtro
            loadDataFromCache()
        }
    }

    fun updateSelectedDepartment(department: String) {
        _uiState.update { it.copy(selectedDepartment = department) }
    }

    fun updateSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Carica sempre dalla cache per iniziare
                loadDataFromCache()

                // Verifica se abbiamo dati sufficienti per le 2 settimane di default
                val twoWeeksAgo = LocalDate.now().minusWeeks(2)
                val hasRecentData = checkIfCacheHasRecentData(twoWeeksAgo)

                if (!hasRecentData) {
                    // Carica dati recenti da Firebase se non li abbiamo
                    loadCurrentDataFromFirebase()
                } else {
                    _cacheStatus.update { it.copy(hasCurrentData = true) }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Errore nel caricamento dei dati: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadDataFromCache() {
        try {

            // Carica tutti i dati dalla cache locale
            val contratti = contrattoDao.getContratti()
            val users = userDao.getDipendenti()
            val absences = absenceDao.getAbsences()
            val turni = turnoDao.getTurni()

            val reportData = ReportData(
                contratti = contratti.toDomainList(),
                users = users.toDomainList(),
                absences = absences.toDomainList(),
                turni = turni.toDomainList()
            )

            val departments = listOf("Tutti") + users.map { it.dipartimento }.distinct().sorted()

            _uiState.update {
                it.copy(
                    reportData = reportData,
                    departments = departments,
                    isLoading = false,
                    error = null
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Errore nel caricamento dalla cache: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadCurrentDataFromFirebase() {
        _uiState.update { it.copy(isLoading = true) }

        try {
            val twoWeeksAgo = LocalDate.now().minusWeeks(2)
            val today = LocalDate.now()

            // Carica dati recenti da Firebase (ultimi 2 settimane + oggi)
            contrattoRepository.syncRecentContratti()
            absenceRepository.syncAbsencesInRange(twoWeeksAgo, today)
            turnoRepository.syncTurniInRange(twoWeeksAgo, today)

            _cacheStatus.update {
                it.copy(
                    hasCurrentData = true,
                    lastUpdateDate = LocalDate.now()
                )
            }

            // Ricarica dalla cache aggiornata
            loadDataFromCache()

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Errore nel caricamento da Firebase: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadMonthDataFromFirebase() {
        if (cacheStatus.value.hasMonthData) return

        _uiState.update { it.copy(isLoading = true) }

        try {
            val oneMonthAgo = LocalDate.now().minusMonths(1)
            val twoWeeksAgo = LocalDate.now().minusWeeks(2)

            // Carica solo i dati che non abbiamo già (da 1 mese fa a 2 settimane fa)
            absenceRepository.syncAbsencesInRange(oneMonthAgo, twoWeeksAgo)
            turnoRepository.syncTurniInRange(oneMonthAgo, twoWeeksAgo)

            _cacheStatus.update {
                it.copy(
                    hasMonthData = true,
                    lastUpdateDate = LocalDate.now()
                )
            }

            loadDataFromCache()

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Errore nel caricamento dati mensili: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadQuarterDataFromFirebase() {
        if (cacheStatus.value.hasQuarterData) return

        _uiState.update { it.copy(isLoading = true) }

        try {
            val threeMonthsAgo = LocalDate.now().minusMonths(3)
            val oneMonthAgo = LocalDate.now().minusMonths(1)

            // Carica dati da 3 mesi fa a 1 mese fa (escludendo quello che abbiamo già)
            absenceRepository.syncAbsencesInRange(threeMonthsAgo, oneMonthAgo)
            turnoRepository.syncTurniInRange(threeMonthsAgo, oneMonthAgo)

            _cacheStatus.update {
                it.copy(
                    hasQuarterData = true,
                    hasMonthData = true, // Includiamo anche il mese
                    lastUpdateDate = LocalDate.now()
                )
            }

            loadDataFromCache()

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Errore nel caricamento dati trimestrali: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadYearDataFromFirebase() {
        if (cacheStatus.value.hasYearData) return

        _uiState.update { it.copy(isLoading = true) }

        try {
            val oneYearAgo = LocalDate.now().minusYears(1)
            val threeMonthsAgo = LocalDate.now().minusMonths(3)

            // Carica dati da 1 anno fa a 3 mesi fa
            absenceRepository.syncAbsencesInRange(oneYearAgo, threeMonthsAgo)
            turnoRepository.syncTurniInRange(oneYearAgo, threeMonthsAgo)

            // Per ALL_TIME potremmo voler caricare tutto lo storico
            if (_uiState.value.selectedFilter == ReportFilter.ALL_TIME) {
                contrattoRepository.syncAllContratti()
                absenceRepository.syncAllAbsences()
                turnoRepository.syncAllTurni()
            }

            _cacheStatus.update {
                it.copy(
                    hasYearData = true,
                    hasQuarterData = true,
                    hasMonthData = true,
                    hasCurrentData = true,
                    lastUpdateDate = LocalDate.now()
                )
            }

            loadDataFromCache()

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Errore nel caricamento dati annuali: ${e.message}"
                )
            }
        }
    }

    private suspend fun checkIfCacheHasRecentData(fromDate: LocalDate): Boolean {
        return try {
            val turni = turnoDao.getTurniInRange(fromDate, LocalDate.now())
            val absences = absenceDao.getAbsencesInRange(fromDate, LocalDate.now()).first()

            turni.isNotEmpty() || absences.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            // Reset cache status per forzare il reload
            _cacheStatus.update { CacheStatus() }

            // Ricarica in base al filtro corrente
            when (_uiState.value.selectedFilter) {
                ReportFilter.TODAY, ReportFilter.WEEK -> loadCurrentDataFromFirebase()
                ReportFilter.MONTH -> {
                    loadCurrentDataFromFirebase()
                    loadMonthDataFromFirebase()
                }
                ReportFilter.QUARTER -> {
                    loadCurrentDataFromFirebase()
                    loadMonthDataFromFirebase()
                    loadQuarterDataFromFirebase()
                }
                ReportFilter.YEAR, ReportFilter.ALL_TIME -> {
                    loadCurrentDataFromFirebase()
                    loadMonthDataFromFirebase()
                    loadQuarterDataFromFirebase()
                    loadYearDataFromFirebase()
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}