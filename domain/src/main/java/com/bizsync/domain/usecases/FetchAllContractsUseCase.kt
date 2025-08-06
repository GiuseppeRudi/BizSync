package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.repository.ContractSyncRepository
import javax.inject.Inject

class FetchAllContractsUseCase @Inject constructor(
    private val contractSyncRepository: ContractSyncRepository
) {
    suspend operator fun invoke(idAzienda: String): Resource<List<Contratto>> {
        return try {
            contractSyncRepository.getContratti(idAzienda)
        } catch (e: Exception) {
            Resource.Error("Errore nel caricamento contratti: ${e.message}")
        }
    }
}
