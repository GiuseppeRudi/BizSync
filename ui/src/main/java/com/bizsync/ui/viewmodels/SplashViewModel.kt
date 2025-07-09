package com.bizsync.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.traceEventEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.orchestrator.UserOrchestrator
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import com.bizsync.ui.components.DialogStatusType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userOrchestrator: UserOrchestrator 
) : ViewModel() {

    data class SplashState(
        val isLoading: Boolean = false,
        val users: List<User> = emptyList(),
        val errorMsg: String? = null,
        val statusType: DialogStatusType = DialogStatusType.ERROR,
        val syncInProgress: Boolean = false,
        val lastSyncTime: Long? = null
    )

    private val _uiState = MutableStateFlow(SplashState())
    val uiState: StateFlow<SplashState> = _uiState

    fun getAllUserByIdAgency(idAzienda: String, forceRefresh: Boolean = false) {
        if (idAzienda.isEmpty()) {
            setError("ID Azienda non valido")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, syncInProgress = true) }

            try {
                // 🔄 STEP 1: Chiama l'orchestrator per sincronizzazione intelligente
                when (val result = userOrchestrator.getDipendenti(idAzienda, forceRefresh)) {
                    is Resource.Success -> {
                        // ✅ STEP 2: Aggiorna UI con i dati sincronizzati
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                syncInProgress = false,
                                users = result.data,
                                errorMsg = null,
                                lastSyncTime = System.currentTimeMillis()
                            )
                        }

                        // 📊 Log per debug
                        println("✅ Caricati ${result.data.size} dipendenti per azienda: $idAzienda")
                        result.data.forEach { user ->
                            println("   - ${user.nome} (${user.posizioneLavorativa})")
                        }
                    }

                    is Resource.Error -> {
                        // ❌ STEP 3: Gestisci errori
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                syncInProgress = false,
                                errorMsg = result.message,
                                statusType = DialogStatusType.ERROR
                            )
                        }

                        println("❌ Errore nel caricamento dipendenti: ${result.message}")
                    }

                    is Resource.Empty -> {
                        // 📭 STEP 4: Nessun dipendente trovato
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                syncInProgress = false,
                                users = emptyList(),
                                errorMsg = null,
                                lastSyncTime = System.currentTimeMillis()
                            )
                        }

                        println("📭 Nessun dipendente trovato per azienda: $idAzienda")
                    }
                }

            } catch (e: Exception) {
                // 🚨 STEP 5: Gestisci eccezioni impreviste
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        syncInProgress = false,
                        errorMsg = "Errore imprevisto: ${e.message}",
                        statusType = DialogStatusType.ERROR
                    )
                }

                println("🚨 Eccezione in getAllUserByIdAgency: ${e.message}")
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