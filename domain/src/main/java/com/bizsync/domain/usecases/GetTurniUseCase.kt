package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoSyncRepository
import javax.inject.Inject


class GetTurniUseCase @Inject constructor(
    private val turnoRepository: TurnoSyncRepository
) {
    suspend operator fun invoke(
        idAzienda: String,
        idEmployee: String? = null,
        forceRefresh: Boolean = false
    ): Resource<List<Turno>> {
        return turnoRepository.getTurni(idAzienda, idEmployee, forceRefresh)
    }
}