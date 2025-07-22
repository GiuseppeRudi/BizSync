package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.orchestrator.BadgeOrchestrator
import com.bizsync.domain.constants.enumClass.HomeScreenRoute
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.*
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.model.AziendaUi
import com.bizsync.ui.model.HomeState
import com.bizsync.ui.model.UserState
import com.bizsync.ui.model.UserUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val badgeOrchestrator: BadgeOrchestrator
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private val _timerState = MutableStateFlow<ProssimoTurno?>(null)
    val timerState: StateFlow<ProssimoTurno?> = _timerState.asStateFlow()

    fun changeCurrentScreen(newScreen: HomeScreenRoute) {
        _uiState.value = _uiState.value.copy(currentScreen = newScreen)
    }

  

    fun setUserAndAgency(user: UserUi, agency: AziendaUi) {
        _uiState.value = _uiState.value.copy(
            user = user,
            azienda = agency
        )
    }

    fun initializeEmployee(userState: UserState) {
        viewModelScope.launch {
            val badge = badgeOrchestrator.createBadgeVirtuale(userState.user.toDomain(), userState.azienda.toDomain())
            _uiState.value = _uiState.value.copy(
                badge = badge,
                user = userState.user,
                azienda = userState.azienda
            )

            // Avvia il timer per il prossimo turno
            startTurnoTimer()

            // Carica timbrature di oggi
            loadTimbratureOggi()
        }
    }

    private fun startTurnoTimer() {
        viewModelScope.launch {
            while (isActive) {
                when (val result = badgeOrchestrator.getProssimoTurno(_uiState.value.user.uid)) {
                    is Resource.Success -> {
                        _timerState.value = result.data
                        _uiState.value = _uiState.value.copy(
                            prossimoTurno = result.data,
                            canTimbra = result.data.isTimbraturaPossibile()
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message
                        )
                    }
                    else -> {}
                }

                delay(30000) // Aggiorna ogni 30 secondi
            }
        }
    }

    fun onTimbra(
        tipoTimbratura: TipoTimbratura,
        latitudine: Double? = null,
        longitudine: Double? = null
    ) {
        viewModelScope.launch {
            Log.d("VIEWMODEL_DEBUG", "=== VIEWMODEL onTimbra chiamato ===")
            Log.d("VIEWMODEL_DEBUG", "Tipo timbratura: $tipoTimbratura")
            Log.d("VIEWMODEL_DEBUG", "Latitudine ricevuta: $latitudine")
            Log.d("VIEWMODEL_DEBUG", "Longitudine ricevuta: $longitudine")

            val turno = _uiState.value.prossimoTurno?.turno
            if (turno == null) {
                Log.e("VIEWMODEL_DEBUG", "❌ Turno è null - impossibile procedere")
                return@launch
            }

            Log.d("VIEWMODEL_DEBUG", "Turno trovato: ${turno.id} - ${turno.titolo}")

            _uiState.value = _uiState.value.copy(isLoading = true)

            Log.d("VIEWMODEL_DEBUG", "Chiamata badgeOrchestrator.creaTimbratura con:")
            Log.d("VIEWMODEL_DEBUG", "  turno: ${turno.id}")
            Log.d("VIEWMODEL_DEBUG", "  dipendente: ${_uiState.value.user.uid}")
            Log.d("VIEWMODEL_DEBUG", "  azienda: ${_uiState.value.azienda.idAzienda}")
            Log.d("VIEWMODEL_DEBUG", "  tipoTimbratura: $tipoTimbratura")
            Log.d("VIEWMODEL_DEBUG", "  latitudine: $latitudine")
            Log.d("VIEWMODEL_DEBUG", "  longitudine: $longitudine")

            when (val result = badgeOrchestrator.creaTimbratura(
                turno = turno,
                dipendente = _uiState.value.user.toDomain(),
                azienda = _uiState.value.azienda.toDomain(),
                tipoTimbratura = tipoTimbratura,
                latitudine = latitudine,
                longitudine = longitudine
            )) {
                is Resource.Success -> {
                    Log.d("VIEWMODEL_DEBUG", "✅ Timbratura creata con successo")
                    Log.d("VIEWMODEL_DEBUG", "Timbratura risultante:")
                    Log.d("VIEWMODEL_DEBUG", "  ID: ${result.data.idFirebase}")
                    Log.d("VIEWMODEL_DEBUG", "  Posizione verificata: ${result.data.posizioneVerificata}")
                    Log.d("VIEWMODEL_DEBUG", "  Distanza dall'azienda: ${result.data.distanzaDallAzienda}")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastTimbratura = result.data,
                        showSuccess = true,
                        successMessage = "Timbratura effettuata con successo"
                    )
                    loadTimbratureOggi()
                }
                is Resource.Error -> {
                    Log.e("VIEWMODEL_DEBUG", "❌ Errore nella creazione timbratura: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {
                    Log.w("VIEWMODEL_DEBUG", "⚠️ Stato loading ricevuto")
                }
            }
        }
    }

    private fun loadTimbratureOggi() {
        viewModelScope.launch {
            when (val result = badgeOrchestrator.getTimbratureGiornaliere(
                _uiState.value.user.uid,
                LocalDate.now()
            )) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        timbratureOggi = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun dismissSuccess() {
        _uiState.value = _uiState.value.copy(showSuccess = false)
    }
}