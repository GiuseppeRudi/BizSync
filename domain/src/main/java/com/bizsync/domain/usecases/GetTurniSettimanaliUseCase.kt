package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoSyncRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTurniSettimanaliUseCase @Inject constructor(
    private val turnoSyncRepository: TurnoSyncRepository
) {
    suspend operator fun invoke(
        idAzienda: String,
        idEmployee: String,
        startWeek : LocalDate
    ): Resource<List<Turno>> {
        return turnoSyncRepository.fetchTurniSettimana(startWeek, idAzienda, idEmployee)
    }
}