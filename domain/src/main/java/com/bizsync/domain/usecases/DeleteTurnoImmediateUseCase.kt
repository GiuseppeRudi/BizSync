package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.repository.TurnoSyncRepository
import javax.inject.Inject

class DeleteTurnoImmediateUseCase @Inject constructor(
    private val turnoOrchestrator: TurnoSyncRepository
) {
    suspend operator fun invoke(turnoId: String): Boolean {
        return when (turnoOrchestrator.deleteTurnoImmediate(turnoId)) {
            is Resource.Success -> true
            else -> false
        }
    }
}
