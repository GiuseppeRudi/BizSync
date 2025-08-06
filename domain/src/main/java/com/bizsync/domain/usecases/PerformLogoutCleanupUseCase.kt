package com.bizsync.domain.usecases

import com.bizsync.domain.constants.enumClass.CleanupStep
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.LogoutCleanupProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PerformLogoutCleanupUseCase @Inject constructor(
    private val clearDatabaseUseCase: ClearDatabaseUseCase,
    private val clearHashStorageUseCase: ClearHashStorageUseCase
) {
    suspend operator fun invoke(): Flow<LogoutCleanupProgress> = flow {
        try {
            // ✅ Step 1: Starting
            emit(LogoutCleanupProgress(CleanupStep.STARTING, isLoading = true))

            // ✅ Step 2: Database cleanup
            emit(LogoutCleanupProgress(CleanupStep.CLEARING_CACHE, isLoading = true))

            val databaseResult = clearDatabaseUseCase()
            if (databaseResult is Resource.Error) {
                emit(LogoutCleanupProgress(
                    CleanupStep.ERROR,
                    isLoading = false,
                    errorMessage = databaseResult.message
                ))
                return@flow
            }

            // ✅ Step 3: Hash storage cleanup
            emit(LogoutCleanupProgress(CleanupStep.CLEARING_PREFERENCES, isLoading = true))

            val hashResult = clearHashStorageUseCase()
            if (hashResult is Resource.Error) {
                emit(LogoutCleanupProgress(
                    CleanupStep.ERROR,
                    isLoading = false,
                    errorMessage = hashResult.message
                ))
                return@flow
            }

            // ✅ Step 4: Completed
            emit(
                LogoutCleanupProgress(
                    CleanupStep.COMPLETED,
                    isLoading = false,
                    databaseResult = (databaseResult as? Resource.Success)?.data
                )
            )

        } catch (e: Exception) {
            emit(LogoutCleanupProgress(
                CleanupStep.ERROR,
                isLoading = false,
                errorMessage = e.message ?: "Errore durante la pulizia"
            ))
        }
    }
}