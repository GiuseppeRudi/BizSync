package com.bizsync.domain.usecases

import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoLocalRepository
import javax.inject.Inject

class SaveTurniLocalUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository
) {
    suspend operator fun invoke(turni: List<Turno>) {
        turnoLocalRepository.insertAll(turni)
    }
}