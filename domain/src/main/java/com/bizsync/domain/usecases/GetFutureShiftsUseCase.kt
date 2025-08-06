package com.bizsync.domain.usecases

import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoLocalRepository
import java.time.LocalDate
import javax.inject.Inject

class GetFutureShiftsUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository
) {
    suspend operator fun invoke(idAzienda: String, fromDate: LocalDate): List<Turno> {
        return turnoLocalRepository.getFutureShiftsFromToday(idAzienda, fromDate)
    }
}