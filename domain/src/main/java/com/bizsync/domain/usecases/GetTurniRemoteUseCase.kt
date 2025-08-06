package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoRemoteRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTurniRemoteUseCase @Inject constructor(
    private val turnoRemoteRepository: TurnoRemoteRepository
) {
    suspend operator fun invoke(
        idAzienda: String,
        startRange: LocalDate,
        endRange: LocalDate,
        idEmployee: String
    ): Resource<List<Turno>> {
        return turnoRemoteRepository.getTurniRangeByAzienda(idAzienda, startRange, endRange, idEmployee)
    }
}