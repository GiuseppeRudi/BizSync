package com.bizsync.domain.usecases

import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoLocalRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTurniInRangeForUserUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<Turno>> {
        return turnoLocalRepository.getTurniInRangeForUser(startDate, endDate)
    }
}