package com.bizsync.domain.usecases

import com.bizsync.domain.repository.AbsenceRemoteRepository
import java.time.LocalDate
import javax.inject.Inject

class SyncAbsencesInRangeUseCase @Inject constructor(
    private val absenceRemoteRepository: AbsenceRemoteRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate) {
        absenceRemoteRepository.syncAbsencesInRange(startDate, endDate)
    }
}