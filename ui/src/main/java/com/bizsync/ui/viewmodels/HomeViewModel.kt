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

  

    fun setBadge(userState: UserState) {
        viewModelScope.launch {
            val badge = badgeOrchestrator.createBadgeVirtuale(userState.user.toDomain(), userState.azienda.toDomain())
            _uiState.value = _uiState.value.copy(
                badge = badge,
                user = userState.user,
                azienda = userState.azienda
            )
        }
    }


}