package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.repository.TurnoSyncRepository
import java.time.LocalDate
import javax.inject.Inject

class SyncTurniToFirebaseUseCase @Inject constructor(
    private val turnoSyncRepository: TurnoSyncRepository
) {
    suspend operator fun invoke(weekStart: LocalDate): Resource<String> {
        return turnoSyncRepository.syncTurniToFirebase(weekStart)
    }
}
