package com.bizsync.domain.usecases


import com.bizsync.domain.repository.TurnoRemoteRepository
import java.time.LocalDate
import javax.inject.Inject

class SyncTurniForUserInRangeUseCase @Inject constructor(
    private val turnoRemoteRepository: TurnoRemoteRepository
) {
    suspend operator fun invoke(userId: String, idAzienda: String, startDate: LocalDate, endDate: LocalDate) {
        turnoRemoteRepository.syncTurniForUserInRange(userId, idAzienda, startDate, endDate)
    }
}
