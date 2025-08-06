package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.repository.AziendaRemoteRepository
import javax.inject.Inject

class GetAziendaByIdUseCase @Inject constructor(
    private val aziendaRemoteRepository: AziendaRemoteRepository
) {
    suspend operator fun invoke(idAzienda: String): Resource<Azienda> {
        return aziendaRemoteRepository.getAziendaById(idAzienda)
    }
}
