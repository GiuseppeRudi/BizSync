package com.bizsync.domain.usecases

import com.bizsync.domain.repository.TurnoRemoteRepository
import java.time.LocalDate
import javax.inject.Inject

class SyncTurniInRangeUseCase @Inject constructor(
    private val turnoRemoteRepository: TurnoRemoteRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate) {
        turnoRemoteRepository.syncTurniInRange(startDate, endDate)
    }
}