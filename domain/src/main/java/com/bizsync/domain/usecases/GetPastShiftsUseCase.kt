package com.bizsync.domain.usecases

import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoLocalRepository
import java.time.LocalDate
import javax.inject.Inject


class GetPastShiftsUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository
) {
    suspend operator fun invoke(idAzienda: String, startDate: LocalDate, endDate: LocalDate): List<Turno> {
        return turnoLocalRepository.getPastShifts(idAzienda, startDate, endDate)
    }
}
