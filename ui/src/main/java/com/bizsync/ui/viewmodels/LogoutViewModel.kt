package com.bizsync.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.enumClass.CleanupStep
import com.bizsync.domain.usecases.PerformLogoutCleanupUseCase
import com.bizsync.ui.model.LogoutCleanupUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val performLogoutCleanupUseCase: PerformLogoutCleanupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogoutCleanupUiState())
    val uiState: StateFlow<LogoutCleanupUiState> = _uiState.asStateFlow()

    fun startCleanup() {
        viewModelScope.launch {
            try {
                performLogoutCleanupUseCase()
                    .collect { progress ->
                        _uiState.value = LogoutCleanupUiState(
                            isLoading = progress.isLoading,
                            currentStep = progress.currentStep,
                            errorMessage = progress.errorMessage,
                            databaseCleanupResult = progress.databaseResult
                        )
                    }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentStep = CleanupStep.ERROR,
                    errorMessage = e.message ?: "Errore durante la pulizia"
                )
            }
        }
    }


    fun resetState() {
        _uiState.value = LogoutCleanupUiState()
    }
}