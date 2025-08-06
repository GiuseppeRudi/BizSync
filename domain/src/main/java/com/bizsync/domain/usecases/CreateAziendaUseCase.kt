package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.repository.AziendaRemoteRepository
import javax.inject.Inject

class CreateAziendaUseCase @Inject constructor(
    private val aziendaRepository: AziendaRemoteRepository
) {
    suspend operator fun invoke(azienda: Azienda): Resource<String> {
        return try {
            aziendaRepository.creaAzienda(azienda)
        } catch (e: Exception) {
            Resource.Error("Errore nella creazione dell'azienda: ${e.message}")
        }
    }
}
