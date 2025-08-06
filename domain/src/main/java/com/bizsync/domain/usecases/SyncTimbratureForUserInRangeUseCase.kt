package com.bizsync.domain.usecases

import com.bizsync.domain.repository.TimbraturaRemoteRepository
import java.time.LocalDate
import javax.inject.Inject

class SyncTimbratureForUserInRangeUseCase @Inject constructor(
    private val timbraturaRemoteRepository: TimbraturaRemoteRepository
) {
    suspend operator fun invoke(userId: String, idAzienda: String, startDate: LocalDate, endDate: LocalDate) {
        timbraturaRemoteRepository.syncTimbratureForUserInRange(userId, idAzienda, startDate, endDate)
    }
}