package com.bizsync.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.hash.HashStorage
import com.bizsync.cache.dao.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val absenceDao: AbsenceDao,
    private val timbraturaDao: TimbraturaDao,
    private val turnoDao: TurnoDao,
    private val contrattoDao: ContrattoDao,
    private val userDao: UserDao,
    private val hashStorage: HashStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogoutCleanupUiState())
    val uiState: StateFlow<LogoutCleanupUiState> = _uiState.asStateFlow()

    fun startCleanup() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    currentStep = CleanupStep.STARTING
                )

                // Step 1: Pulizia cache locale
                _uiState.value = _uiState.value.copy(currentStep = CleanupStep.CLEARING_CACHE)
                clearAllDatabaseTables()

                // Step 2: Pulizia SharedPreferences
                _uiState.value = _uiState.value.copy(currentStep = CleanupStep.CLEARING_PREFERENCES)
                hashStorage.clearAllHashes()

                // Step 3: Completamento
                _uiState.value = _uiState.value.copy(
                    currentStep = CleanupStep.COMPLETED,
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentStep = CleanupStep.ERROR,
                    errorMessage = e.message ?: "Errore durante la pulizia"
                )
            }
        }
    }

    private suspend fun clearAllDatabaseTables() {
        // Pulisci tutte le tabelle del database
        try {
            // Assenze
            absenceDao.clearAll()

            // Timbrature - Aggiungi questa query al TimbraturaDao
            timbraturaDao.clearAll()

            // Turni - Aggiungi questa query al TurnoDao
            turnoDao.clearAll()

            // Contratti
            contrattoDao.deleteAll()

            // Utenti
            userDao.deleteAll()

        } catch (e: Exception) {
            throw Exception("Errore durante la pulizia del database: ${e.message}")
        }
    }
}

data class LogoutCleanupUiState(
    val isLoading: Boolean = false,
    val currentStep: CleanupStep = CleanupStep.STARTING,
    val errorMessage: String? = null
)

enum class CleanupStep(val message: String) {
    STARTING("Inizializzazione..."),
    CLEARING_CACHE("Pulizia cache locale..."),
    CLEARING_PREFERENCES("Rimozione preferenze..."),
    COMPLETED("Pulizia completata!"),
    ERROR("Errore durante la pulizia")
}