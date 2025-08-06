package com.bizsync.domain.usecases

import com.bizsync.domain.repository.TurnoRemoteRepository
import javax.inject.Inject

class SyncAllTurniUseCase @Inject constructor(
    private val turnoRemoteRepository: TurnoRemoteRepository
) {
    suspend operator fun invoke() {
        turnoRemoteRepository.syncAllTurni()
    }
}
