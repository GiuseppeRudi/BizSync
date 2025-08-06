package com.bizsync.domain.model

import com.bizsync.domain.constants.enumClass.CleanupStep

data class LogoutCleanupProgress(
    val currentStep: CleanupStep,
    val isLoading: Boolean,
    val errorMessage: String? = null,
    val databaseResult: DatabaseCleanupResult? = null
)