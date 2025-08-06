package com.bizsync.domain.usecases

import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoLocalRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTurniInRangeNonSyncUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository
) {
    suspend operator fun invoke(weekStart: LocalDate, weekEnd: LocalDate): List<Turno> {
        return turnoLocalRepository.getTurniInRangeNonSync(weekStart, weekEnd)
    }
}