package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoLocalRepository
import com.bizsync.domain.repository.TurnoSyncRepository
import javax.inject.Inject

class GetLocalTurniUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository
) {
    suspend operator fun invoke(

    ): List<Turno> {
        return turnoLocalRepository.getTurni()
    }
}