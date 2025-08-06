package com.bizsync.domain.usecases

import com.bizsync.domain.repository.AbsenceSyncRepository
import com.bizsync.domain.repository.TurnoSyncRepository
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class ClearObsoleteCacheUseCase @Inject constructor(
    private val turnoRepository: TurnoSyncRepository,
    private val absenceRepository: AbsenceSyncRepository
) {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()) {
        if (today.dayOfWeek == DayOfWeek.MONDAY) {
            turnoRepository.deleteOldCachedData(today)
            absenceRepository.deleteOldCachedData(today)
        }
    }
}