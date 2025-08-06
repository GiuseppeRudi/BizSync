package com.bizsync.domain.usecases


import com.bizsync.domain.repository.TurnoSyncRepository
import java.time.LocalDate
import javax.inject.Inject

class DeleteOldTurniCacheUseCase @Inject constructor(
    private val turnoSyncRepository: TurnoSyncRepository
) {
    suspend operator fun invoke(currentDate: LocalDate = LocalDate.now()) {
        turnoSyncRepository.deleteOldCachedData(currentDate)
    }
}