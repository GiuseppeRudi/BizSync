package com.bizsync.domain.usecases

import com.bizsync.domain.model.Contratto
import com.bizsync.domain.repository.ContractLocalRepository
import javax.inject.Inject

class GetContrattoUseCase @Inject constructor(
    private val contrattoLocalRepository: ContractLocalRepository
) {
    suspend operator fun invoke(employeeId: String): Contratto? {
        return contrattoLocalRepository.getContratto(employeeId)
    }
}