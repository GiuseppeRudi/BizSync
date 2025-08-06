package com.bizsync.domain.usecases

import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoLocalRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTurniInRangeUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): List<Turno> {
        return turnoLocalRepository.getTurniInRange(startDate, endDate)
    }
}