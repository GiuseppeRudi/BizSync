package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.usecases.ClearObsoleteCacheUseCase
import com.bizsync.domain.usecases.GetAbsencesUseCase
import com.bizsync.domain.usecases.GetContrattiUseCase
import com.bizsync.domain.usecases.GetTurniUseCase
import com.bizsync.domain.usecases.GetUsersUseCase
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.mapper.toUi
import com.bizsync.ui.model.SplashState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val getContrattiUseCase: GetContrattiUseCase,
    private val getAbsencesUseCase: GetAbsencesUseCase,
    private val getTurniUseCase: GetTurniUseCase,
    private val clearObsoleteCacheUseCase: ClearObsoleteCacheUseCase
) : ViewModel() {



    private val _uiState = MutableStateFlow(SplashState())
    val uiState: StateFlow<SplashState> = _uiState

    fun clearObsoleteCacheIfNeeded(today: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            try {
                clearObsoleteCacheUseCase(today)
                Log.d("CACHE_CLEANER", "✅ Pulizia completata")
            } catch (e: Exception) {
                Log.e("CACHE_CLEANER", "❌ Errore nella pulizia: ${e.message}")
            }
        }
    }

    fun getAllTurniByIdAzienda(
        idAzienda: String,
        idEmployee: String? = null,
        forceRefresh: Boolean = false
    ) {
        if (idAzienda.isEmpty()) {
            setError("ID Azienda non valido")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, syncInProgress = true) }

            try {
                when (val result = getTurniUseCase(idAzienda, idEmployee, forceRefresh)) {
                    is Resource.Success -> {
                        val turniPerData = result.data.groupBy { it.data }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                syncInProgress = false,
                                turni = turniPerData,
                                errorMsg = null,
                                lastSyncTime = System.currentTimeMillis()
                            )
                        }

                        Log.d("TURNI_DEBUG", "✅ Caricati ${result.data.size} turni per azienda: $idAzienda")
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                syncInProgress = false,
                                errorMsg = result.message,
                                statusType = DialogStatusType.ERROR
                            )
                        }

                        Log.e("TURNI_DEBUG", "❌ Errore nel caricamento turni: ${result.message}")
                    }

                    is Resource.Empty -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                syncInProgress = false,
                                turni = emptyMap(),
                                errorMsg = null,
                                lastSyncTime = System.currentTimeMillis()
                            )
                        }

                        Log.d("TURNI_DEBUG", "📭 Nessun turno trovato per azienda: $idAzienda")
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        syncInProgress = false,
                        errorMsg = "Errore imprevisto: ${e.message}",
                        statusType = DialogStatusType.ERROR
                    )
                }

                Log.e("TURNI_DEBUG", "🚨 Eccezione in getAllTurniByIdAzienda: ${e.message}")
            }
        }
    }

    fun getAllAbsencesByIdAzienda(
        idAzienda: String,
        idEmployee: String? = null,
        forceRefresh: Boolean = false
    ) {
        if (idAzienda.isEmpty()) {
            setError("ID Azienda non valido")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, syncInProgress = true) }

            try {
                when (val result = getAbsencesUseCase(idAzienda, idEmployee, forceRefresh)) {
                    is Resource.Success -> {
                        val absenceUiList = result.data.map { it.toUi() }

                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                syncInProgress = false,
                                absence = absenceUiList,
                                errorMsg = null,
                                lastSyncTime = System.currentTimeMillis()
                            )
                        }

                        Log.d("ABSENCE_DEBUG", "✅ Caricate ${result.data.size} assenze per azienda: $idAzienda")
                        result.data.forEach { absence ->
                            Log.d("ABSENCE_DEBUG", "   - ${absence.submittedName} (${absence.type.name}) - ${absence.status.name}")
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                syncInProgress = false,
                                errorMsg = result.message,
                                statusType = DialogStatusType.ERROR
                            )
                        }

                        Log.e("ABSENCE_DEBUG", "❌ Errore nel caricamento assenze: ${result.message}")
                    }

                    is Resource.Empty -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                syncInProgress = false,
                                absence = emptyList(),
                                errorMsg = null,
                                lastSyncTime = System.currentTimeMillis()
                            )
                        }

                        Log.d("ABSENCE_DEBUG", "📭 Nessuna assenza trovata per azienda: $idAzienda")
                    }
                }

            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        syncInProgress = false,
                        errorMsg = "Errore imprevisto: ${e.message}",
                        statusType = DialogStatusType.ERROR
                    )
                }

                Log.e("ABSENCE_DEBUG", "🚨 Eccezione in getAllAbsencesByIdAzienda: ${e.message}")
            }
        }
    }

    fun getAllContrattiByIdAzienda(idAzienda: String, forceRefresh: Boolean = false) {
        if (idAzienda.isEmpty()) {
            setError("ID Azienda non valido")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, syncInProgress = true) }

            try {
                when (val result = getContrattiUseCase(idAzienda, forceRefresh)) {
                    is Resource.Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                syncInProgress = false,
                                contratti = result.data,
                                errorMsg = null,
                                lastSyncTime = System.currentTimeMillis()
                            )
                        }

                        Log.d("CONTRATTI_DEBUG", "✅ Caricati ${result.data.size} contratti per azienda: $idAzienda")
                        result.data.forEach { contratto ->
                            Log.d("CONTRATTI_DEBUG", "   - ${contratto.id} - ${contratto.posizioneLavorativa} - ${contratto.tipoContratto}")
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                syncInProgress = false,
                                errorMsg = result.message,
                                statusType = DialogStatusType.ERROR
                            )
                        }

                        Log.d("CONTRATTI_DEBUG", "❌ Errore nel caricamento contratti: ${result.message}")
                    }

                    is Resource.Empty -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                syncInProgress = false,
                                contratti = emptyList(),
                                errorMsg = null,
                                lastSyncTime = System.currentTimeMillis()
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        syncInProgress = false,
                        errorMsg = "Errore imprevisto: ${e.message}",
                        statusType = DialogStatusType.ERROR
                    )
                }

                Log.e("CONTRATTI_DEBUG", "🚨 Eccezione in getAllContrattiByIdAzienda: ${e.message}")
            }
        }
    }

    fun getAllUserByIdAgency(
        idAzienda: String,
        idUser: String,
        forceRefresh: Boolean = false
    ) {
        if (idAzienda.isEmpty()) {
            setError("ID Azienda non valido")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, syncInProgress = true) }

            try {
                when (val result = getUsersUseCase(idAzienda, idUser, forceRefresh)) {
                    is Resource.Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                syncInProgress = false,
                                users = result.data,
                                errorMsg = null,
                                lastSyncTime = System.currentTimeMillis()
                            )
                        }

                        Log.d("DIPENDENTI_DEBUG", "✅ Caricati ${result.data.size} dipendenti per azienda: $idAzienda")
                        result.data.forEach { user ->
                            Log.d("DIPENDENTI_DEBUG", "   - ${user.nome} (${user.posizioneLavorativa})")
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                syncInProgress = false,
                                errorMsg = result.message,
                                statusType = DialogStatusType.ERROR
                            )
                        }

                        Log.d("DIPENDENTI_DEBUG", "❌ Errore nel caricamento dipendenti: ${result.message}")
                    }

                    is Resource.Empty -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                syncInProgress = false,
                                users = emptyList(),
                                errorMsg = null,
                                lastSyncTime = System.currentTimeMillis()
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        syncInProgress = false,
                        errorMsg = "Errore imprevisto: ${e.message}",
                        statusType = DialogStatusType.ERROR
                    )
                }

                Log.e("DIPENDENTI_DEBUG", "🚨 Eccezione in getAllUserByIdAgency: ${e.message}")
            }
        }
    }

    /**
     * GESTIONE ERRORI
     */
    private fun setError(message: String) {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                syncInProgress = false,
                errorMsg = message,
                statusType = DialogStatusType.ERROR
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }
}