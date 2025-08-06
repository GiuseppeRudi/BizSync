package com.bizsync.domain.usecases

import com.bizsync.domain.repository.AbsenceRemoteRepository
import javax.inject.Inject

class SyncAllAbsencesUseCase @Inject constructor(
    private val absenceRemoteRepository: AbsenceRemoteRepository
) {
    suspend operator fun invoke() {
        absenceRemoteRepository.syncAllAbsences()
    }
}