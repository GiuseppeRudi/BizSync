package com.bizsync.ui.model

import com.bizsync.domain.constants.enumClass.CleanupStep
import com.bizsync.domain.model.DatabaseCleanupResult

data class LogoutCleanupUiState(
    val isLoading: Boolean = false,
    val currentStep: CleanupStep = CleanupStep.STARTING,
    val errorMessage: String? = null,
    val databaseCleanupResult: DatabaseCleanupResult? = null
)