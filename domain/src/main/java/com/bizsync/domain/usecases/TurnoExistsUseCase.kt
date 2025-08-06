package com.bizsync.domain.usecases

import com.bizsync.domain.repository.TurnoLocalRepository
import javax.inject.Inject

class TurnoExistsUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository
) {
    suspend operator fun invoke(turnoId: String): Boolean {
        return turnoLocalRepository.exists(turnoId)
    }
}
