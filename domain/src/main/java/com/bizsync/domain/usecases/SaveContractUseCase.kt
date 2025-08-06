package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.repository.ContractRemoteRepository
import javax.inject.Inject

class SaveContractUseCase @Inject constructor(
    private val contractRemoteRepository: ContractRemoteRepository
) {
    suspend operator fun invoke(contratto: Contratto): Resource<String> {
        return contractRemoteRepository.saveContract(contratto)
    }
}