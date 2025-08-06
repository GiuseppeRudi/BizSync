package com.bizsync.domain.usecases


import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.repository.ContractLocalRepository
import com.bizsync.domain.repository.ContractSyncRepository
import javax.inject.Inject

class GetContrattiUseCase @Inject constructor(
    private val contractLocalRepository: ContractSyncRepository
) {
    suspend operator fun invoke(
        idAzienda: String,
        forceRefresh: Boolean = false
    ): Resource<List<Contratto>> {
        return contractLocalRepository.getContratti(idAzienda)
    }
}