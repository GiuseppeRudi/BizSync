package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.enumClass.HomeScreenRoute
import com.bizsync.domain.usecases.CreateBadgeUseCase
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.model.HomeState
import com.bizsync.ui.model.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BadgeViewModel @Inject constructor(
    private val createBadgeUseCase: CreateBadgeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    fun changeCurrentScreen(newScreen: HomeScreenRoute) {
        _uiState.value = _uiState.value.copy(currentScreen = newScreen)
    }

    fun setBadge(userState: UserState) {
        viewModelScope.launch {
            try {
                val badge = createBadgeUseCase(
                    user = userState.user.toDomain(),
                    azienda = userState.azienda.toDomain()
                )
                _uiState.value = _uiState.value.copy(
                    badge = badge,
                    user = userState.user,
                    azienda = userState.azienda
                )
            } catch (e: Exception) {
                // Gestione degli errori
                _uiState.value = _uiState.value.copy(
                    // Aggiungi campo error in HomeState se necessario
                )
            }
        }
    }
}