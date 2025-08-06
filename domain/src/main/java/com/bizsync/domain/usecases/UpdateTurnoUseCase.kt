package com.bizsync.domain.usecases

import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoLocalRepository
import javax.inject.Inject

class UpdateTurnoUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository
) {
    suspend operator fun invoke(turno: Turno) {
        turnoLocalRepository.update(turno)
    }
}
