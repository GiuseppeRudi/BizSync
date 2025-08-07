package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoSyncRepository
import javax.inject.Inject

class UpdateTurnoImmediateUseCase @Inject constructor(
    private val turnoOrchestrator: TurnoSyncRepository
) {
    suspend operator fun invoke(turno: Turno): Boolean {
        return when (turnoOrchestrator.updateTurnoImmediate(turno)) {
            is Resource.Success -> true
            else -> false
        }
    }
}
