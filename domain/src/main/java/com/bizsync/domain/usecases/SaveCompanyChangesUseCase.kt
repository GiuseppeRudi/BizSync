package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.User
import com.bizsync.domain.repository.AziendaRemoteRepository
import javax.inject.Inject

class SaveCompanyChangesUseCase @Inject constructor(
    private val aziendaRemoteRepository: AziendaRemoteRepository
) {
    suspend operator fun invoke(
        idAzienda: String,
        nuoviDipartimenti: List<AreaLavoro>,
    ): Resource<Unit> {
        return try {
            aziendaRemoteRepository.updateAreeLavoro(idAzienda, nuoviDipartimenti)
        } catch (e: Exception) {
            Resource.Error("Errore nel salvataggio modifiche: ${e.message}")
        }
    }
}