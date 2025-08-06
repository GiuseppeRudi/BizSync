package com.bizsync.domain.usecases

import com.bizsync.domain.model.Contratto
import com.bizsync.domain.repository.ContractLocalRepository
import javax.inject.Inject

class GetLocalContrattiUseCase @Inject constructor(
    private val contractLocalRepository: ContractLocalRepository
) {
    suspend operator fun invoke(
    ): List<Contratto> {
        return contractLocalRepository.getContratti()
    }
}