package com.bizsync.domain.usecases


import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.repository.TurnoSyncRepository
import javax.inject.Inject

class DeleteTurnoUseCase @Inject constructor(
    private val turnoSyncRepository: TurnoSyncRepository
) {


    suspend operator fun invoke(turnoId: String): Resource<String> {
        return turnoSyncRepository.deleteTurno(turnoId)
    }
}