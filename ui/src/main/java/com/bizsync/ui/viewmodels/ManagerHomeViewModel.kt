package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.*
import com.bizsync.ui.model.ManagerHomeState
import com.bizsync.domain.model.TodayStats
import com.bizsync.domain.usecases.LoadRecentTimbratureUseCase
import com.bizsync.domain.usecases.LoadShiftPublicationInfoUseCase
import com.bizsync.domain.usecases.LoadTodayShiftsUseCase
import com.bizsync.domain.usecases.LoadTodayStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class ManagerHomeViewModel @Inject constructor(
    private val loadTodayStatsUseCase: LoadTodayStatsUseCase,
    private val loadRecentTimbratureUseCase: LoadRecentTimbratureUseCase,
    private val loadTodayShiftsUseCase: LoadTodayShiftsUseCase,
    private val loadShiftPublicationInfoUseCase: LoadShiftPublicationInfoUseCase,
) : ViewModel() {

    private val _homeState = MutableStateFlow(ManagerHomeState())
    val homeState = _homeState.asStateFlow()

    fun loadHomeManagerData(azienda: Azienda) {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, azienda = azienda) }

            try {
                // ✅ OPZIONE 1: Use Cases separati (come prima)
                launch { loadTodayStatsAsync(azienda) }
                launch { loadRecentTimbratureAsync() }
                launch { loadTodayShiftsAsync() }
                launch { loadShiftPublicationInfoAsync(azienda) }

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


    private suspend fun loadTodayStatsAsync(azienda: Azienda) {
        try {
            // ✅ Usa Use Case invece di DAO diretti
            when (val result = loadTodayStatsUseCase(azienda)) {
                is Resource.Success -> {
                    _homeState.update {
                        it.copy(
                            todayStats = result.data,
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Empty -> {
                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            todayStats = TodayStats()
                        )
                    }
                }
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

    private suspend fun loadRecentTimbratureAsync() {
        try {
            // ✅ Usa Use Case invece di DAO diretti
            when (val result = loadRecentTimbratureUseCase(10)) {
                is Resource.Success -> {
                    _homeState.update {
                        it.copy(recentTimbrature = result.data)
                    }
                }
                is Resource.Error -> {
                    _homeState.update {
                        it.copy(error = result.message)
                    }
                }
                is Resource.Empty -> {
                    _homeState.update {
                        it.copy(recentTimbrature = emptyList())
                    }
                }
            }
        } catch (e: Exception) {
            _homeState.update {
                it.copy(error = "Errore nel caricamento timbrature: ${e.message}")
            }
        }
    }

    private suspend fun loadTodayShiftsAsync() {
        try {
            // ✅ Usa Use Case invece di DAO diretti
            when (val result = loadTodayShiftsUseCase()) {
                is Resource.Success -> {
                    _homeState.update {
                        it.copy(todayShifts = result.data)
                    }
                }
                is Resource.Error -> {
                    _homeState.update {
                        it.copy(error = result.message)
                    }
                }
                is Resource.Empty -> {
                    _homeState.update {
                        it.copy(todayShifts = emptyList())
                    }
                }
            }
        } catch (e: Exception) {
            _homeState.update {
                it.copy(error = "Errore nel caricamento turni: ${e.message}")
            }
        }
    }

    private suspend fun loadShiftPublicationInfoAsync(azienda: Azienda) {
        try {
            // ✅ Usa Use Case invece del repository diretto
            when (val result = loadShiftPublicationInfoUseCase(azienda)) {
                is Resource.Success -> {
                    _homeState.update {
                        it.copy(
                            daysUntilShiftPublication = result.data.daysUntilShiftPublication,
                            shiftsPublishedThisWeek = result.data.shiftsPublishedThisWeek
                        )
                    }
                }
                is Resource.Error -> {
                    _homeState.update {
                        it.copy(error = result.message)
                    }
                }
                is Resource.Empty -> {
                    _homeState.update {
                        it.copy(
                            daysUntilShiftPublication = daysUntilNextFriday(LocalDate.now()),
                            shiftsPublishedThisWeek = false
                        )
                    }
                }
            }
        } catch (e: Exception) {
            _homeState.update {
                it.copy(error = "Errore caricamento info pubblicazione: ${e.message}")
            }
        }
    }

    fun daysUntilNextFriday(date: LocalDate): Int {
        val todayValue = date.dayOfWeek.value        // lun=1 … dom=7
        val fridayValue = DayOfWeek.FRIDAY.value     // 5
        return (fridayValue - todayValue + 7) % 7
    }

    fun clearError() {
        _homeState.update { it.copy(error = null) }
    }
}

